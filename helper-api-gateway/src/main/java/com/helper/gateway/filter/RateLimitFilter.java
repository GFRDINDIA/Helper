package com.helper.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for dev/MVP.
 * Production should use Spring Cloud Gateway + Redis RequestRateLimiter.
 *
 * Rate limits:
 *   - Authenticated users: 100 requests/minute (identified by X-User-Id)
 *   - Anonymous (open endpoints): 30 requests/minute (identified by IP)
 *
 * In production, replace with:
 *   - name: RequestRateLimiter
 *     args:
 *       redis-rate-limiter.replenishRate: 100
 *       redis-rate-limiter.burstCapacity: 150
 *       key-resolver: "#{@userKeyResolver}"
 */
@Component
@Slf4j
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int AUTH_LIMIT = 100;  // per minute for authenticated users
    private static final int ANON_LIMIT = 30;   // per minute for anonymous
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, RateBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return -150; // After logging, before JWT
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getHostString() : "unknown";

        String key;
        int limit;
        if (userId != null && !userId.isBlank()) {
            key = "user:" + userId;
            limit = AUTH_LIMIT;
        } else {
            key = "ip:" + clientIp;
            limit = ANON_LIMIT;
        }

        RateBucket bucket = buckets.computeIfAbsent(key, k -> new RateBucket());

        if (!bucket.tryConsume(limit)) {
            log.warn("Rate limit exceeded for {}: {} requests/min", key, limit);
            return tooManyRequests(exchange);
        }

        // Add rate limit headers
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, limit - bucket.getCount())));

        return chain.filter(exchange);
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        String body = "{\"success\":false,\"message\":\"Rate limit exceeded. Try again in 60 seconds.\",\"error\":\"RATE_LIMIT_EXCEEDED\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Simple sliding window counter.
     */
    private static class RateBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean tryConsume(int limit) {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                // Reset window
                synchronized (this) {
                    if (now - windowStart > WINDOW_MS) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            return count.incrementAndGet() <= limit;
        }

        int getCount() {
            return count.get();
        }
    }
}
