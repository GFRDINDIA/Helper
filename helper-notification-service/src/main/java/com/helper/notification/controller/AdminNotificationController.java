package com.helper.notification.controller;

import com.helper.notification.dto.request.SendNotificationRequest;
import com.helper.notification.dto.response.ApiResponse;
import com.helper.notification.dto.response.NotificationResponse;
import com.helper.notification.dto.response.NotificationStatsResponse;
import com.helper.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin - Notifications", description = "Send notifications, broadcast, view stats (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notifService;

    @PostMapping("/send")
    @Operation(summary = "Send notification to specific users",
            description = "Specify user IDs, event type, title, body. System auto-routes to correct channels per PRD matrix.")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notifications sent",
                        notifService.sendNotification(request)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Notification statistics",
            description = "Total, pending, sent, failed, active device tokens, breakdown by status")
    public ResponseEntity<ApiResponse<NotificationStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Notification stats",
                notifService.getStats()));
    }
}
