package com.helper.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for the API Gateway.
 *
 * Allows:
 * - Flutter mobile app (no Origin header — handled by device)
 * - React/Next.js web app (localhost:3000 in dev, helper.app in prod)
 * - Web admin panel (admin.helper.app)
 *
 * Note: CORS is also configured in application.yml via spring.cloud.gateway.globalcors.
 * This bean provides additional programmatic control.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*")); // Dev: allow all. Prod: restrict via yml
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of(
                "Authorization",
                "X-Request-Id",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining"
        ));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
