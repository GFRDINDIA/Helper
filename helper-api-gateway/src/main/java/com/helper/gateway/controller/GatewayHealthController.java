package com.helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gateway health check that also checks downstream service availability.
 * Useful for Flutter app startup health probes and load balancer checks.
 */
@RestController
@RequestMapping("/gateway")
public class GatewayHealthController {

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authUrl;

    @Value("${TASK_SERVICE_URL:http://localhost:8082}")
    private String taskUrl;

    @Value("${USER_SERVICE_URL:http://localhost:8083}")
    private String userUrl;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8084}")
    private String paymentUrl;

    @Value("${RATING_SERVICE_URL:http://localhost:8085}")
    private String ratingUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8086}")
    private String notificationUrl;

    private final WebClient webClient = WebClient.builder()
            .codecs(config -> config.defaultCodecs().maxInMemorySize(1024))
            .build();

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> gatewayHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "helper-api-gateway");
        health.put("port", 8080);
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/services")
    public Mono<ResponseEntity<Map<String, Object>>> serviceStatus() {
        Map<String, Mono<String>> checks = new LinkedHashMap<>();
        checks.put("auth-service (8081)", checkHealth(authUrl));
        checks.put("task-service (8082)", checkHealth(taskUrl));
        checks.put("user-service (8083)", checkHealth(userUrl));
        checks.put("payment-service (8084)", checkHealth(paymentUrl));
        checks.put("rating-service (8085)", checkHealth(ratingUrl));
        checks.put("notification-service (8086)", checkHealth(notificationUrl));

        return Mono.zip(
                checks.values().stream().toList(),
                results -> {
                    Map<String, Object> status = new LinkedHashMap<>();
                    status.put("gateway", "UP");
                    status.put("timestamp", LocalDateTime.now().toString());

                    Map<String, String> services = new LinkedHashMap<>();
                    int i = 0;
                    for (String key : checks.keySet()) {
                        services.put(key, (String) results[i++]);
                    }
                    status.put("services", services);

                    long upCount = services.values().stream().filter("UP"::equals).count();
                    status.put("healthy", upCount + "/" + services.size());
                    return ResponseEntity.ok(status);
                }
        );
    }

    private Mono<String> checkHealth(String baseUrl) {
        return webClient.get()
                .uri(baseUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(3))
                .map(body -> "UP")
                .onErrorReturn("DOWN");
    }
}
