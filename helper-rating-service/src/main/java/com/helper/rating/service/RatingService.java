package com.helper.rating.service;

import com.helper.rating.dto.request.SubmitRatingRequest;
import com.helper.rating.dto.response.RatingResponse;
import com.helper.rating.dto.response.RatingStatsResponse;
import com.helper.rating.dto.response.UserRatingSummaryResponse;
import com.helper.rating.entity.Rating;
import com.helper.rating.entity.UserRatingSummary;
import com.helper.rating.exception.RatingExceptions;
import com.helper.rating.repository.FlagRepository;
import com.helper.rating.repository.RatingRepository;
import com.helper.rating.repository.UserRatingSummaryRepository;
import com.helper.rating.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepo;
    private final UserRatingSummaryRepository summaryRepo;
    private final FlagRepository flagRepo;

    @Value("${app.rating.min-public-threshold:5}")
    private int minPublicThreshold;

    @Value("${app.rating.weight-decay-days:180}")
    private int weightDecayDays;

    // ===== SUBMIT RATING =====
    @Transactional
    public RatingResponse submitRating(SubmitRatingRequest request, AuthenticatedUser user) {
        // Cannot rate yourself
        if (user.getUserId().equals(request.getGivenTo())) {
            throw new RatingExceptions.InvalidRatingException("You cannot rate yourself");
        }

        // Check duplicate: one rating per direction per task
        if (ratingRepo.existsByTaskIdAndGivenByAndGivenTo(
                request.getTaskId(), user.getUserId(), request.getGivenTo())) {
            throw new RatingExceptions.DuplicateRatingException(
                    "You have already rated this user for task: " + request.getTaskId());
        }

        // Create rating
        Rating rating = Rating.builder()
                .taskId(request.getTaskId())
                .givenBy(user.getUserId())
                .givenTo(request.getGivenTo())
                .score(request.getScore())
                .feedback(request.getFeedback())
                .ratingType(request.getRatingType())
                .isVisible(true)
                .build();

        rating = ratingRepo.save(rating);

        // Recalculate the rated user's summary
        recalculateSummary(request.getGivenTo());

        log.info("Rating submitted: {} stars from {} to {} for task {}",
                request.getScore(), user.getUserId(), request.getGivenTo(), request.getTaskId());

        return mapToResponse(rating);
    }

    // ===== GET RATINGS FOR A USER (PUBLIC) =====
    public Page<RatingResponse> getUserRatings(UUID userId, Pageable pageable) {
        return ratingRepo.findByGivenToAndIsVisibleTrueOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    // ===== GET RATINGS GIVEN BY A USER =====
    public Page<RatingResponse> getRatingsGivenBy(UUID userId, Pageable pageable) {
        return ratingRepo.findByGivenByOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    // ===== GET RATINGS FOR A TASK =====
    public List<RatingResponse> getTaskRatings(UUID taskId) {
        return ratingRepo.findByTaskId(taskId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    // ===== GET USER RATING SUMMARY (PUBLIC) =====
    public UserRatingSummaryResponse getUserSummary(UUID userId) {
        UserRatingSummary summary = summaryRepo.findById(userId)
                .orElse(UserRatingSummary.builder().userId(userId).build());

        return UserRatingSummaryResponse.builder()
                .userId(userId)
                .averageRating(summary.getAverageRating())
                .weightedRating(summary.getWeightedRating())
                .totalRatings(summary.getTotalRatings())
                .isPublic(summary.getIsPublic())
                .totalFlagsReceived(summary.getTotalFlagsReceived())
                .starDistribution(UserRatingSummaryResponse.StarDistribution.builder()
                        .fiveStar(summary.getTotalFiveStar())
                        .fourStar(summary.getTotalFourStar())
                        .threeStar(summary.getTotalThreeStar())
                        .twoStar(summary.getTotalTwoStar())
                        .oneStar(summary.getTotalOneStar())
                        .build())
                .build();
    }

    // ===== ADMIN: HIDE/SHOW RATING =====
    @Transactional
    public RatingResponse toggleRatingVisibility(UUID ratingId, boolean visible, AuthenticatedUser admin) {
        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new RatingExceptions.RatingNotFoundException("Rating not found: " + ratingId));

        rating.setIsVisible(visible);
        rating = ratingRepo.save(rating);

        // Recalculate summary for the rated user
        recalculateSummary(rating.getGivenTo());

        log.info("Rating {} {} by admin {}", ratingId, visible ? "shown" : "hidden", admin.getUserId());
        return mapToResponse(rating);
    }

    // ===== ADMIN: STATS =====
    public RatingStatsResponse getStats() {
        return RatingStatsResponse.builder()
                .totalRatings(ratingRepo.count())
                .visibleRatings(ratingRepo.countByIsVisibleTrue())
                .hiddenRatings(ratingRepo.countByIsVisibleFalse())
                .platformAverageRating(ratingRepo.findPlatformAverageRating())
                .pendingFlags(flagRepo.countByStatus(com.helper.rating.enums.FlagStatus.PENDING))
                .reviewedFlags(flagRepo.countByStatus(com.helper.rating.enums.FlagStatus.REVIEWED))
                .dismissedFlags(flagRepo.countByStatus(com.helper.rating.enums.FlagStatus.DISMISSED))
                .actionTakenFlags(flagRepo.countByStatus(com.helper.rating.enums.FlagStatus.ACTION_TAKEN))
                .build();
    }

    // ===== RECALCULATE SUMMARY =====
    @Transactional
    public void recalculateSummary(UUID userId) {
        List<Rating> visibleRatings = ratingRepo.findVisibleRatingsForUser(userId);

        BigDecimal simpleAvg = WeightedRatingCalculator.calculateSimpleAverage(visibleRatings);
        BigDecimal weightedAvg = WeightedRatingCalculator.calculateWeightedAverage(visibleRatings, weightDecayDays);
        int[] dist = WeightedRatingCalculator.getStarDistribution(visibleRatings);
        int total = visibleRatings.size();
        boolean isPublic = total >= minPublicThreshold;

        UserRatingSummary summary = summaryRepo.findById(userId)
                .orElse(UserRatingSummary.builder().userId(userId).build());

        summary.setAverageRating(simpleAvg);
        summary.setWeightedRating(weightedAvg);
        summary.setTotalRatings(total);
        summary.setTotalFiveStar(dist[5]);
        summary.setTotalFourStar(dist[4]);
        summary.setTotalThreeStar(dist[3]);
        summary.setTotalTwoStar(dist[2]);
        summary.setTotalOneStar(dist[1]);
        summary.setIsPublic(isPublic);

        summaryRepo.save(summary);
        log.debug("Summary recalculated for user {}: avg={} weighted={} total={} public={}",
                userId, simpleAvg, weightedAvg, total, isPublic);
    }

    // ===== MAPPER =====
    private RatingResponse mapToResponse(Rating r) {
        return RatingResponse.builder()
                .ratingId(r.getRatingId()).taskId(r.getTaskId())
                .givenBy(r.getGivenBy()).givenTo(r.getGivenTo())
                .score(r.getScore()).feedback(r.getFeedback())
                .ratingType(r.getRatingType().name())
                .isVisible(r.getIsVisible())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
