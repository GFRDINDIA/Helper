package com.helper.rating.controller;

import com.helper.rating.dto.request.ReviewFlagRequest;
import com.helper.rating.dto.response.ApiResponse;
import com.helper.rating.dto.response.FlagResponse;
import com.helper.rating.dto.response.RatingResponse;
import com.helper.rating.dto.response.RatingStatsResponse;
import com.helper.rating.security.AuthenticatedUser;
import com.helper.rating.service.FlagService;
import com.helper.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Ratings & Flags", description = "Moderation, flag review, stats (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRatingController {

    private final RatingService ratingService;
    private final FlagService flagService;

    // ===== STATS =====
    @GetMapping("/ratings/stats")
    @Operation(summary = "Rating & flag statistics",
            description = "Total ratings, platform average, hidden count, flag breakdown by status")
    public ResponseEntity<ApiResponse<RatingStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Rating statistics",
                ratingService.getStats()));
    }

    // ===== RATING MODERATION =====
    @PutMapping("/ratings/{ratingId}/hide")
    @Operation(summary = "Hide a rating", description = "Makes rating invisible and recalculates user summary")
    public ResponseEntity<ApiResponse<RatingResponse>> hideRating(
            @PathVariable UUID ratingId,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ApiResponse.success("Rating hidden",
                ratingService.toggleRatingVisibility(ratingId, false, admin)));
    }

    @PutMapping("/ratings/{ratingId}/show")
    @Operation(summary = "Restore a hidden rating")
    public ResponseEntity<ApiResponse<RatingResponse>> showRating(
            @PathVariable UUID ratingId,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ApiResponse.success("Rating restored",
                ratingService.toggleRatingVisibility(ratingId, true, admin)));
    }

    // ===== FLAG REVIEW =====
    @GetMapping("/flags/pending")
    @Operation(summary = "Pending flags queue", description = "Flags waiting for admin review, newest first")
    public ResponseEntity<ApiResponse<Page<FlagResponse>>> getPendingFlags(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Pending flags",
                flagService.getPendingFlags(pageable)));
    }

    @GetMapping("/flags/user/{userId}")
    @Operation(summary = "All flags for a specific user")
    public ResponseEntity<ApiResponse<Page<FlagResponse>>> getFlagsForUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("User flags",
                flagService.getFlagsForUser(userId, pageable)));
    }

    @PutMapping("/flags/{flagId}/review")
    @Operation(summary = "Review a flag",
            description = "Set status to DISMISSED or ACTION_TAKEN. Optionally hide the associated rating.")
    public ResponseEntity<ApiResponse<FlagResponse>> reviewFlag(
            @PathVariable UUID flagId,
            @Valid @RequestBody ReviewFlagRequest request,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ApiResponse.success("Flag reviewed",
                flagService.reviewFlag(flagId, request, admin)));
    }
}
