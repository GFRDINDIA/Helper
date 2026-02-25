package com.helper.rating.controller;

import com.helper.rating.dto.request.SubmitRatingRequest;
import com.helper.rating.dto.response.ApiResponse;
import com.helper.rating.dto.response.RatingResponse;
import com.helper.rating.dto.response.UserRatingSummaryResponse;
import com.helper.rating.security.AuthenticatedUser;
import com.helper.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Submit and view ratings (1-5 stars + feedback)")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @Operation(summary = "Submit a rating",
            description = "Both customer and worker can rate each other after task PAYMENT_DONE. " +
                    "1-5 stars + optional text feedback. One rating per direction per task.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @Valid @RequestBody SubmitRatingRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rating submitted",
                        ratingService.submitRating(request, user)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all ratings received by a user (PUBLIC)",
            description = "Returns only visible ratings, paginated, newest first")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getUserRatings(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("User ratings",
                ratingService.getUserRatings(userId, pageable)));
    }

    @GetMapping("/summary/{userId}")
    @Operation(summary = "Get user's rating summary (PUBLIC)",
            description = "Average, weighted average, star distribution, total count. " +
                    "Score is only public after 5+ ratings (PRD threshold).")
    public ResponseEntity<ApiResponse<UserRatingSummaryResponse>> getUserSummary(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Rating summary",
                ratingService.getUserSummary(userId)));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get all ratings for a task", description = "Both customer→worker and worker→customer")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getTaskRatings(
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task ratings",
                ratingService.getTaskRatings(taskId)));
    }

    @GetMapping("/my/given")
    @Operation(summary = "Ratings I've given", description = "Your rating history")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getMyGivenRatings(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Ratings you've given",
                ratingService.getRatingsGivenBy(user.getUserId(), pageable)));
    }

    @GetMapping("/my/received")
    @Operation(summary = "Ratings I've received", description = "Your received rating history")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getMyReceivedRatings(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Ratings you've received",
                ratingService.getUserRatings(user.getUserId(), pageable)));
    }
}
