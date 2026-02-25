package com.helper.notification.controller;

import com.helper.notification.dto.request.RegisterDeviceRequest;
import com.helper.notification.dto.request.UpdatePreferencesRequest;
import com.helper.notification.dto.response.ApiResponse;
import com.helper.notification.entity.DeviceToken;
import com.helper.notification.entity.UserNotificationPreference;
import com.helper.notification.security.AuthenticatedUser;
import com.helper.notification.service.DeviceTokenService;
import com.helper.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Tokens & Preferences", description = "Register FCM tokens, manage notification preferences")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceTokenService deviceService;
    private final NotificationService notifService;

    @PostMapping("/register")
    @Operation(summary = "Register FCM device token",
            description = "Called by Flutter app on login/startup. Stores the Firebase Cloud Messaging token for push notifications.")
    public ResponseEntity<ApiResponse<DeviceToken>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device registered",
                        deviceService.registerDevice(user.getUserId(), request)));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate device token", description = "Called on logout")
    public ResponseEntity<ApiResponse<Void>> deactivateDevice(
            @RequestParam String token) {
        deviceService.deactivateToken(token);
        return ResponseEntity.ok(ApiResponse.success("Device deactivated"));
    }

    @GetMapping("/my-devices")
    @Operation(summary = "My registered devices")
    public ResponseEntity<ApiResponse<List<DeviceToken>>> getMyDevices(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Your devices",
                deviceService.getUserDevices(user.getUserId())));
    }

    // ===== NOTIFICATION PREFERENCES =====
    @GetMapping("/preferences")
    @Operation(summary = "My notification preferences",
            description = "Channel toggles (push, SMS, email, in-app), quiet hours, promotional opt-out")
    public ResponseEntity<ApiResponse<UserNotificationPreference>> getPreferences(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Your preferences",
                notifService.getPreferences(user.getUserId())));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<ApiResponse<UserNotificationPreference>> updatePreferences(
            @RequestBody UpdatePreferencesRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Preferences updated",
                notifService.updatePreferences(user.getUserId(), request)));
    }
}
