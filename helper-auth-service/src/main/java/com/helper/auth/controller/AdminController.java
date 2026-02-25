package com.helper.auth.controller;

import com.helper.auth.dto.response.ApiResponse;
import com.helper.auth.dto.response.AuthResponse;
import com.helper.auth.entity.User;
import com.helper.auth.enums.Role;
import com.helper.auth.enums.VerificationStatus;
import com.helper.auth.exception.AuthExceptions;
import com.helper.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only user management and KYC approval endpoints")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Returns all registered users (admin only)")
    public ResponseEntity<ApiResponse<List<AuthResponse.UserInfo>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) VerificationStatus status) {

        List<User> users = userRepository.findAll();

        List<AuthResponse.UserInfo> userInfos = users.stream()
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> status == null || u.getVerificationStatus() == status)
                .map(u -> AuthResponse.UserInfo.builder()
                        .userId(u.getUserId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .role(u.getRole())
                        .verificationStatus(u.getVerificationStatus())
                        .emailVerified(u.isEmailVerified())
                        .profileImageUrl(u.getProfileImageUrl())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Users retrieved", userInfos));
    }

    @PutMapping("/users/{userId}/verify")
    @Operation(summary = "Approve/Reject worker KYC",
            description = "Admin approves or rejects a worker's KYC verification")
    public ResponseEntity<ApiResponse<String>> updateVerificationStatus(
            @PathVariable UUID userId,
            @RequestParam VerificationStatus status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.WORKER) {
            throw new AuthExceptions.BadRequestException("Verification status can only be set for workers");
        }

        user.setVerificationStatus(status);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                "Worker verification status updated to " + status));
    }

    @PutMapping("/users/{userId}/deactivate")
    @Operation(summary = "Deactivate a user account", description = "Soft-deletes a user account")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

        user.setActive(false);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User account deactivated"));
    }

    @PutMapping("/users/{userId}/activate")
    @Operation(summary = "Reactivate a user account")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.ResourceNotFoundException("User not found"));

        user.setActive(true);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User account activated"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform user statistics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCustomers", userRepository.countByRole(Role.CUSTOMER));
        stats.put("totalWorkers", userRepository.countByRole(Role.WORKER));
        stats.put("totalAdmins", userRepository.countByRole(Role.ADMIN));
        stats.put("totalUsers", userRepository.count());

        return ResponseEntity.ok(ApiResponse.success("Platform statistics", stats));
    }
}
