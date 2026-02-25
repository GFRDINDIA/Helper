package com.helper.auth.service;

import com.helper.auth.entity.OtpToken;
import com.helper.auth.exception.AuthExceptions;
import com.helper.auth.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes:10}")
    private int expirationMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Value("${app.platform.name:Helper}")
    private String platformName;

    @Value("${spring.mail.username:noreply@helper.com}")
    private String fromEmail;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate and send OTP to the given email
     */
    @Transactional
    public void generateAndSendOtp(String email, String purpose) {
        // Rate limit check: prevent OTP flooding
        long recentOtps = otpTokenRepository.countRecentOtpsByEmail(
                email, purpose, LocalDateTime.now().minusSeconds(resendCooldownSeconds));
        if (recentOtps > 0) {
            throw new AuthExceptions.TooManyRequestsException(
                    "Please wait " + resendCooldownSeconds + " seconds before requesting a new OTP");
        }

        // Invalidate any existing OTPs for this email and purpose
        otpTokenRepository.invalidateAllByEmailAndPurpose(email, purpose);

        // Generate OTP
        String otpCode = generateOtpCode();

        // Save to database
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otpCode)
                .purpose(purpose)
                .maxAttempts(maxAttempts)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .build();
        otpTokenRepository.save(otpToken);

        // Send email
        sendOtpEmail(email, otpCode, purpose);

        log.info("OTP generated and sent for email: {} purpose: {}", maskEmail(email), purpose);
    }

    /**
     * Verify OTP code
     */
    @Transactional
    public boolean verifyOtp(String email, String otpCode, String purpose) {
        OtpToken otpToken = otpTokenRepository
                .findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new AuthExceptions.BadRequestException(
                        "No active OTP found. Please request a new one."));

        // Check if expired
        if (otpToken.isExpired()) {
            throw new AuthExceptions.BadRequestException(
                    "OTP has expired. Please request a new one.");
        }

        // Check max attempts
        if (otpToken.getAttempts() >= otpToken.getMaxAttempts()) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new AuthExceptions.TooManyRequestsException(
                    "Maximum verification attempts exceeded. Please request a new OTP.");
        }

        // Increment attempts
        otpToken.incrementAttempts();

        // Verify OTP
        if (!otpToken.getOtpCode().equals(otpCode)) {
            otpTokenRepository.save(otpToken);
            int remainingAttempts = otpToken.getMaxAttempts() - otpToken.getAttempts();
            throw new AuthExceptions.BadRequestException(
                    "Invalid OTP. " + remainingAttempts + " attempt(s) remaining.");
        }

        // Mark as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        log.info("OTP verified successfully for email: {} purpose: {}", maskEmail(email), purpose);
        return true;
    }

    private String generateOtpCode() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private void sendOtpEmail(String email, String otpCode, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);

            String subjectText;
            String bodyText;

            switch (purpose.toUpperCase()) {
                case "REGISTRATION":
                    subjectText = platformName + " - Verify Your Email";
                    bodyText = String.format(
                            "Welcome to %s!\n\n" +
                            "Your email verification code is: %s\n\n" +
                            "This code expires in %d minutes.\n\n" +
                            "If you did not create an account, please ignore this email.\n\n" +
                            "- The %s Team",
                            platformName, otpCode, expirationMinutes, platformName);
                    break;
                case "PASSWORD_RESET":
                    subjectText = platformName + " - Password Reset Code";
                    bodyText = String.format(
                            "You requested a password reset for your %s account.\n\n" +
                            "Your password reset code is: %s\n\n" +
                            "This code expires in %d minutes.\n\n" +
                            "If you did not request this, please ignore this email and ensure your account is secure.\n\n" +
                            "- The %s Team",
                            platformName, otpCode, expirationMinutes, platformName);
                    break;
                default:
                    subjectText = platformName + " - Verification Code";
                    bodyText = String.format(
                            "Your %s verification code is: %s\n\n" +
                            "This code expires in %d minutes.\n\n" +
                            "- The %s Team",
                            platformName, otpCode, expirationMinutes, platformName);
            }

            message.setSubject(subjectText);
            message.setText(bodyText);
            mailSender.send(message);

            log.info("OTP email sent to: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", maskEmail(email), e.getMessage());
            // In dev mode, log the OTP for testing
            log.warn("DEV MODE - OTP for {}: {}", maskEmail(email), otpCode);
        }
    }

    /**
     * Cleanup expired/used OTPs every hour
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpTokenRepository.deleteExpiredAndUsed(LocalDateTime.now());
        log.debug("Cleaned up expired/used OTP tokens");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) return name + "@" + parts[1];
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + parts[1];
    }
}
