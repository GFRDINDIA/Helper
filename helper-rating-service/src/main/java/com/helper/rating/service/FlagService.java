package com.helper.rating.service;

import com.helper.rating.dto.request.ReviewFlagRequest;
import com.helper.rating.dto.request.SubmitFlagRequest;
import com.helper.rating.dto.response.FlagResponse;
import com.helper.rating.entity.Flag;
import com.helper.rating.entity.Rating;
import com.helper.rating.entity.UserRatingSummary;
import com.helper.rating.enums.FlagStatus;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlagService {

    private final FlagRepository flagRepo;
    private final RatingRepository ratingRepo;
    private final UserRatingSummaryRepository summaryRepo;
    private final RatingService ratingService;

    @Value("${app.rating.flagging-auto-hide-threshold:3}")
    private int autoHideThreshold;

    // ===== SUBMIT FLAG =====
    @Transactional
    public FlagResponse submitFlag(SubmitFlagRequest request, AuthenticatedUser user) {
        // Cannot flag yourself
        if (user.getUserId().equals(request.getReportedUserId())) {
            throw new RatingExceptions.InvalidRatingException("You cannot flag yourself");
        }

        // Check duplicate: one flag per task per reporter
        if (flagRepo.existsByTaskIdAndReporterId(request.getTaskId(), user.getUserId())) {
            throw new RatingExceptions.DuplicateFlagException(
                    "You have already flagged for task: " + request.getTaskId());
        }

        Flag flag = Flag.builder()
                .ratingId(request.getRatingId())
                .taskId(request.getTaskId())
                .reporterId(user.getUserId())
                .reportedUserId(request.getReportedUserId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(FlagStatus.PENDING)
                .build();

        flag = flagRepo.save(flag);

        // Update flagged user's flag count
        UserRatingSummary summary = summaryRepo.findById(request.getReportedUserId())
                .orElse(UserRatingSummary.builder().userId(request.getReportedUserId()).build());
        summary.setTotalFlagsReceived(summary.getTotalFlagsReceived() + 1);
        summaryRepo.save(summary);

        // Auto-hide rating if too many flags (threshold check)
        if (request.getRatingId() != null) {
            long flagCount = flagRepo.countByReportedUserIdAndStatus(
                    request.getReportedUserId(), FlagStatus.PENDING);
            if (flagCount >= autoHideThreshold) {
                ratingRepo.findById(request.getRatingId()).ifPresent(r -> {
                    r.setIsVisible(false);
                    ratingRepo.save(r);
                    ratingService.recalculateSummary(r.getGivenTo());
                    log.warn("Auto-hidden rating {} due to {} pending flags", request.getRatingId(), flagCount);
                });
            }
        }

        log.info("Flag submitted by {} against user {} for task {} reason: {}",
                user.getUserId(), request.getReportedUserId(), request.getTaskId(), request.getReason());

        return mapToResponse(flag);
    }

    // ===== ADMIN: GET PENDING FLAGS =====
    public Page<FlagResponse> getPendingFlags(Pageable pageable) {
        return flagRepo.findByStatusOrderByCreatedAtDesc(FlagStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    // ===== ADMIN: GET ALL FLAGS FOR A USER =====
    public Page<FlagResponse> getFlagsForUser(UUID userId, Pageable pageable) {
        return flagRepo.findByReportedUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    // ===== ADMIN: REVIEW FLAG =====
    @Transactional
    public FlagResponse reviewFlag(UUID flagId, ReviewFlagRequest request, AuthenticatedUser admin) {
        Flag flag = flagRepo.findById(flagId)
                .orElseThrow(() -> new RatingExceptions.FlagNotFoundException("Flag not found: " + flagId));

        flag.setStatus(request.getStatus());
        flag.setAdminNotes(request.getAdminNotes());
        flag.setReviewedBy(admin.getUserId());
        flag.setReviewedAt(LocalDateTime.now());
        flag = flagRepo.save(flag);

        // If admin wants to hide the associated rating
        if (Boolean.TRUE.equals(request.getHideRating()) && flag.getRatingId() != null) {
            UUID hiddenRatingId = flag.getRatingId();
            ratingRepo.findById(hiddenRatingId).ifPresent(r -> {
                r.setIsVisible(false);
                ratingRepo.save(r);
                ratingService.recalculateSummary(r.getGivenTo());
                log.info("Rating {} hidden by admin action on flag {}", hiddenRatingId, flagId);
            });
        }

        log.info("Flag {} reviewed by admin {}: status={}", flagId, admin.getUserId(), request.getStatus());
        return mapToResponse(flag);
    }

    // ===== MAPPER =====
    private FlagResponse mapToResponse(Flag f) {
        return FlagResponse.builder()
                .flagId(f.getFlagId()).ratingId(f.getRatingId()).taskId(f.getTaskId())
                .reporterId(f.getReporterId()).reportedUserId(f.getReportedUserId())
                .reason(f.getReason().name()).description(f.getDescription())
                .status(f.getStatus().name()).adminNotes(f.getAdminNotes())
                .reviewedBy(f.getReviewedBy()).reviewedAt(f.getReviewedAt())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
