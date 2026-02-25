package com.helper.notification.controller;

import com.helper.notification.dto.response.ApiResponse;
import com.helper.notification.dto.response.NotificationResponse;
import com.helper.notification.security.AuthenticatedUser;
import com.helper.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification inbox: view, read, delete")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notifService;

    @GetMapping
    @Operation(summary = "My notifications", description = "Paginated, newest first")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Notifications",
                notifService.getUserNotifications(user.getUserId(), pageable)));
    }

    @GetMapping("/unread")
    @Operation(summary = "Unread notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnread(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Unread notifications",
                notifService.getUnreadNotifications(user.getUserId(), pageable)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Unread count", description = "Returns the badge count number")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Unread count",
                notifService.getUnreadCount(user.getUserId())));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Marked as read",
                notifService.markAsRead(id, user)));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Integer>> markAllRead(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("All marked as read",
                notifService.markAllRead(user.getUserId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        notifService.deleteNotification(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }
}
