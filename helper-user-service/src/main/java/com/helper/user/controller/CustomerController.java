package com.helper.user.controller;

import com.helper.user.dto.request.AddressRequest;
import com.helper.user.dto.request.CustomerProfileRequest;
import com.helper.user.dto.response.ApiResponse;
import com.helper.user.dto.response.CustomerProfileResponse;
import com.helper.user.security.AuthenticatedUser;
import com.helper.user.service.CustomerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Profiles", description = "Customer profile, saved addresses, and payment methods")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerProfileService service;

    @PutMapping("/profile")
    @Operation(summary = "Create or update customer profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(
            @Valid @RequestBody CustomerProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                service.createOrUpdateProfile(request, user)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my customer profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Your profile",
                service.getProfile(user.getUserId())));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer profile by ID")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success("Customer profile",
                service.getProfile(customerId)));
    }

    // ===== ADDRESSES =====

    @PostMapping("/addresses")
    @Operation(summary = "Add a saved address", description = "Save an address for quick task creation (Home, Office, etc.)")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Address added",
                service.addAddress(request, user)));
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Remove a saved address")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> removeAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Address removed",
                service.removeAddress(addressId, user)));
    }
}
