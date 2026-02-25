package com.helper.auth.service;

import com.helper.auth.dto.request.*;
import com.helper.auth.dto.response.AuthResponse;
import com.helper.auth.entity.RefreshToken;
import com.helper.auth.entity.User;
import com.helper.auth.enums.Role;
import com.helper.auth.exception.AuthExceptions;
import com.helper.auth.repository.RefreshTokenRepository;
import com.helper.auth.repository.UserRepository;
import com.helper.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    /**
     * Register a new user and send email verification OTP
     */
    @Transactional
    public String register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new AuthExceptions.DuplicateResourceException(
                    "An account with this email already exists");
        }

        // Check for duplicate phone
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new AuthExceptions.DuplicateResourceException(
                    "An account with this phone number already exists");
        }

        // Prevent creating ADMIN accounts via public registration
        if (request.getRole() == Role.ADMIN) {
            throw new AuthExceptions.BadRequestException(
                    "Admin accounts cannot be created via public registration");
        }

        // Create user
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Send verification OTP
        otpService.generateAndSendOtp(user.getEmail(), "REGISTRATION");

        log.info("New user registered: {} role: {}", maskEmail(user.getEmail()), user.getRole());

        return "Registration successful. Please check your email for the verification code.";
    }

    /**
     * Verify email with OTP and activate account
     */
    @Transactional
    public AuthResponse verifyEmailOtp(VerifyOtpRequest request) {
        // Verify the OTP
        otpService.verifyOtp(request.getEmail().toLowerCase(), request.getOtpCode(), request.getPurpose());

        if ("REGISTRATION".equalsIgnoreCase(request.getPurpose())) {
            // Mark email as verified
            User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                    .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

            user.setEmailVerified(true);
            userRepository.save(user);

            // Auto-login after verification
            return generateAuthResponse(user, null);
        }

        // For other purposes (like PASSWORD_RESET), just return success
        return AuthResponse.builder()
                .tokenType("OTP_VERIFIED")
                .build();
    }

    /**
     * Login with email and password
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if account is active
        if (!user.isActive()) {
            throw new AuthExceptions.UnauthorizedException("Your account has been deactivated. Please contact support.");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            // Resend OTP for convenience
            otpService.generateAndSendOtp(user.getEmail(), "REGISTRATION");
            throw new AuthExceptions.AccountNotVerifiedException(
                    "Email not verified. A new verification code has been sent to your email.");
        }

        // Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {} role: {}", maskEmail(user.getEmail()), user.getRole());

        return generateAuthResponse(user, request.getDeviceInfo());
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthExceptions.UnauthorizedException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            // If token is compromised (used after revocation), revoke all tokens for this user
            refreshTokenRepository.revokeAllByUser(refreshToken.getUser());
            throw new AuthExceptions.UnauthorizedException(
                    "Refresh token expired or revoked. Please login again.");
        }

        User user = refreshToken.getUser();

        // Revoke old refresh token (rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", maskEmail(user.getEmail()));

        return generateAuthResponse(user, refreshToken.getDeviceInfo());
    }

    /**
     * Forgot password - send reset OTP
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElse(null);

        // Always return success message to prevent email enumeration
        if (user != null && user.isActive()) {
            otpService.generateAndSendOtp(user.getEmail(), "PASSWORD_RESET");
        }

        return "If an account exists with this email, a password reset code has been sent.";
    }

    /**
     * Reset password after OTP verification
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        // Verify OTP first
        otpService.verifyOtp(request.getEmail().toLowerCase(), request.getOtpCode(), "PASSWORD_RESET");

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens (force re-login on all devices)
        refreshTokenRepository.revokeAllByUser(user);

        log.info("Password reset for user: {}", maskEmail(user.getEmail()));

        return "Password reset successful. Please login with your new password.";
    }

    /**
     * Change password (authenticated user)
     */
    @Transactional
    public String changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthExceptions.BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUser(user);

        log.info("Password changed for user: {}", maskEmail(user.getEmail()));

        return "Password changed successfully. Please login again on all devices.";
    }

    /**
     * Resend OTP
     */
    @Transactional
    public String resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

        otpService.generateAndSendOtp(user.getEmail(), request.getPurpose());

        return "A new verification code has been sent to your email.";
    }

    /**
     * Logout - revoke refresh token
     */
    @Transactional
    public String logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
        return "Logged out successfully.";
    }

    /**
     * Logout from all devices
     */
    @Transactional
    public String logoutAll(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
        return "Logged out from all devices successfully.";
    }

    /**
     * Get current user profile
     */
    public AuthResponse.UserInfo getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));
        return mapToUserInfo(user);
    }

    // ===== Private Helpers =====

    private AuthResponse generateAuthResponse(User user, String deviceInfo) {
        // Generate access token
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(), user.getEmail(), user.getRole());

        // Generate and save refresh token
        String refreshTokenStr = jwtTokenProvider.generateRefreshTokenString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(mapToUserInfo(user))
                .build();
    }

    private AuthResponse.UserInfo mapToUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .verificationStatus(user.getVerificationStatus())
                .emailVerified(user.isEmailVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) return name + "@" + parts[1];
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + parts[1];
    }

    /**
     * Cleanup expired/revoked refresh tokens every 6 hours
     */
    @Scheduled(fixedRate = 21600000)
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.debug("Cleaned up expired/revoked refresh tokens");
    }
}
