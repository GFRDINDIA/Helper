package com.helper.auth.controller;

import com.helper.auth.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Service health check")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = Map.of(
                "service", "helper-auth-service",
                "status", "UP",
                "version", "1.0.0"
        );
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", health));
    }
}
