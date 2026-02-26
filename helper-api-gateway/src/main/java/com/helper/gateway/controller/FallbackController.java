package com.helper.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Circuit breaker fallback endpoints.
 * When a downstream service is unavailable, the gateway returns
 * a graceful degraded response instead of a raw 502/503.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return fallbackResponse("Auth Service", "Authentication service is temporarily unavailable. Please try again shortly.");
    }

    @GetMapping("/task")
    public ResponseEntity<Map<String, Object>> taskFallback() {
        return fallbackResponse("Task Service", "Task service is temporarily unavailable. Your tasks are safe — please retry in a moment.");
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        return fallbackResponse("User Service", "User profile service is temporarily unavailable. Please try again shortly.");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return fallbackResponse("Payment Service", "Payment service is temporarily unavailable. No payments have been processed. Please retry.");
    }

    @GetMapping("/rating")
    public ResponseEntity<Map<String, Object>> ratingFallback() {
        return fallbackResponse("Rating Service", "Rating service is temporarily unavailable. Please try again shortly.");
    }

    @GetMapping("/notification")
    public ResponseEntity<Map<String, Object>> notificationFallback() {
        return fallbackResponse("Notification Service", "Notification service is temporarily unavailable. You won't miss any notifications — they'll be delivered when service resumes.");
    }

    private ResponseEntity<Map<String, Object>> fallbackResponse(String service, String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "message", message,
                "error", "SERVICE_UNAVAILABLE",
                "service", service,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
