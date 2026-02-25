package com.helper.auth.controller;

import com.helper.auth.dto.request.*;
import com.helper.auth.dto.response.ApiResponse;
import com.helper.auth.dto.response.AuthResponse;
import com.helper.auth.security.CustomUserDetails;
import com.helper.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, OTP verification, and token management")
public class AuthController {

    private final AuthService authService;

    // ==================== PUBLIC ENDPOINTS ====================

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a new CUSTOMER or WORKER account and sends email verification OTP")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password",
            description = "Authenticates user and returns JWT access token + refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code",
            description = "Verifies email OTP for registration or password reset. Auto-logs in after registration verification.")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyEmailOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Verification successful", response));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP code",
            description = "Sends a new OTP to the registered email. Rate limited to 1 per 60 seconds.")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        String message = authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
            description = "Uses refresh token to get a new access token. Old refresh token is revoked (rotation).")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset",
            description = "Sends password reset OTP to email. Returns success even if email doesn't exist (security).")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with OTP",
            description = "Resets password after verifying the password reset OTP. Logs out all devices.")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ==================== AUTHENTICATED ENDPOINTS ====================

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
            description = "Returns the authenticated user's profile information")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AuthResponse.UserInfo userInfo = authService.getCurrentUser(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", userInfo));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password",
            description = "Changes password for authenticated user. Requires current password. Logs out all devices.")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        String message = authService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session",
            description = "Revokes the provided refresh token")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody RefreshTokenRequest request) {
        String message = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices",
            description = "Revokes all refresh tokens for the authenticated user")
    public ResponseEntity<ApiResponse<String>> logoutAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String message = authService.logoutAll(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
