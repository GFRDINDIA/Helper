package com.helper.user.controller;

import com.helper.user.dto.request.*;
import com.helper.user.dto.response.*;
import com.helper.user.enums.TaskDomain;
import com.helper.user.security.AuthenticatedUser;
import com.helper.user.service.WorkerProfileService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
@Tag(name = "Worker Profiles", description = "Worker profile management, skills registration, geo-availability, portfolio, and availability schedule")
public class WorkerController {

    private final WorkerProfileService service;

    // ===== PROFILE CRUD =====

    @PutMapping("/profile")
    @Operation(summary = "Create or update worker profile", description = "Set bio, location, skills, and availability schedule",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> updateProfile(
            @Valid @RequestBody WorkerProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                service.createOrUpdateProfile(request, user)));
    }

    @GetMapping("/{workerId}")
    @Operation(summary = "Get worker profile", description = "Public: view worker profile with skills, rating, and verification status")
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> getProfile(@PathVariable UUID workerId) {
        return ResponseEntity.ok(ApiResponse.success("Worker profile", service.getProfile(workerId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my worker profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Your profile", service.getProfile(user.getUserId())));
    }

    // ===== SKILLS =====

    @PostMapping("/skills")
    @Operation(summary = "Add a skill domain", description = "Register for a new task domain with pricing model and service area",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> addSkill(
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill added: " + request.getDomain(), service.addSkill(request, user)));
    }

    @DeleteMapping("/skills/{domain}")
    @Operation(summary = "Remove a skill domain", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @PathVariable TaskDomain domain,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.removeSkill(domain, user);
        return ResponseEntity.ok(ApiResponse.success("Skill removed: " + domain));
    }

    // ===== AVAILABILITY =====

    @PutMapping("/availability")
    @Operation(summary = "Update availability schedule", description = "Set working days and hours",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> updateAvailability(
            @Valid @RequestBody List<AvailabilityRequest> slots,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Availability updated",
                service.updateAvailability(slots, user)));
    }

    @PutMapping("/availability/toggle")
    @Operation(summary = "Toggle online/offline status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> toggleAvailability(
            @RequestParam boolean available,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(
                available ? "You are now available" : "You are now offline",
                service.toggleAvailability(available, user)));
    }

    // ===== PORTFOLIO =====

    @PostMapping("/portfolio")
    @Operation(summary = "Add portfolio item", description = "Add a photo of past work with description",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> addPortfolioItem(
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.addPortfolioItem(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Portfolio item added"));
    }

    @GetMapping("/{workerId}/portfolio")
    @Operation(summary = "View worker portfolio", description = "Public: view photos of past work")
    public ResponseEntity<ApiResponse<List<PortfolioItemResponse>>> getPortfolio(@PathVariable UUID workerId) {
        return ResponseEntity.ok(ApiResponse.success("Portfolio", service.getPortfolio(workerId)));
    }

    @DeleteMapping("/portfolio/{itemId}")
    @Operation(summary = "Remove portfolio item", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> removePortfolioItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.removePortfolioItem(itemId, user);
        return ResponseEntity.ok(ApiResponse.success("Portfolio item removed"));
    }

    // ===== GEO SEARCH =====

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby workers", description = "Public: search verified workers by location and optionally by domain. Results sorted by distance.")
    public ResponseEntity<ApiResponse<List<NearbyWorkerResponse>>> findNearbyWorkers(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) TaskDomain domain) {
        return ResponseEntity.ok(ApiResponse.success("Nearby workers",
                service.findNearbyWorkers(lat, lng, domain)));
    }
}
