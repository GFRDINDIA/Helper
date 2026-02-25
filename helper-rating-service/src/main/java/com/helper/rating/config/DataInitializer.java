package com.helper.rating.config;

import com.helper.rating.entity.Flag;
import com.helper.rating.entity.Rating;
import com.helper.rating.entity.UserRatingSummary;
import com.helper.rating.enums.FlagReason;
import com.helper.rating.enums.FlagStatus;
import com.helper.rating.enums.RatingType;
import com.helper.rating.repository.FlagRepository;
import com.helper.rating.repository.RatingRepository;
import com.helper.rating.repository.UserRatingSummaryRepository;
import com.helper.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RatingRepository ratingRepo;
    private final FlagRepository flagRepo;
    private final UserRatingSummaryRepository summaryRepo;

    // Match UUIDs from other services
    private static final UUID CUSTOMER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID WORKER_1   = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID WORKER_2   = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID TASK_1     = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TASK_2     = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TASK_3     = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID TASK_4     = UUID.fromString("00000000-0000-0000-0000-000000000103");
    private static final UUID TASK_5     = UUID.fromString("00000000-0000-0000-0000-000000000104");
    private static final UUID TASK_6     = UUID.fromString("00000000-0000-0000-0000-000000000105");

    @Override
    public void run(String... args) {
        if (ratingRepo.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();

        // Worker 1 receives 6 ratings from customers (crosses 5 threshold → public)
        ratingRepo.save(Rating.builder().taskId(TASK_1).givenBy(CUSTOMER_1).givenTo(WORKER_1)
                .score(5).feedback("Excellent plumbing work! Fixed the leak perfectly.")
                .ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());
        ratingRepo.save(Rating.builder().taskId(TASK_2).givenBy(CUSTOMER_1).givenTo(WORKER_1)
                .score(4).feedback("Good electrician, was a bit late though.")
                .ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());
        ratingRepo.save(Rating.builder().taskId(TASK_3).givenBy(CUSTOMER_2).givenTo(WORKER_1)
                .score(5).feedback("Very professional").ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());
        ratingRepo.save(Rating.builder().taskId(TASK_4).givenBy(CUSTOMER_2).givenTo(WORKER_1)
                .score(5).feedback("Quick and efficient").ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());
        ratingRepo.save(Rating.builder().taskId(TASK_5).givenBy(CUSTOMER_1).givenTo(WORKER_1)
                .score(4).feedback("Good work").ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());
        ratingRepo.save(Rating.builder().taskId(TASK_6).givenBy(CUSTOMER_2).givenTo(WORKER_1)
                .score(5).feedback("Best plumber in Mumbai!").ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());

        // Worker 1 rates customers back
        ratingRepo.save(Rating.builder().taskId(TASK_1).givenBy(WORKER_1).givenTo(CUSTOMER_1)
                .score(5).feedback("Great customer, clear instructions")
                .ratingType(RatingType.WORKER_TO_CUSTOMER).isVisible(true).build());
        ratingRepo.save(Rating.builder().taskId(TASK_2).givenBy(WORKER_1).givenTo(CUSTOMER_1)
                .score(4).feedback("Payment was on time").ratingType(RatingType.WORKER_TO_CUSTOMER).isVisible(true).build());

        // Worker 2 gets 2 ratings (below threshold → not public yet)
        ratingRepo.save(Rating.builder().taskId(TASK_3).givenBy(CUSTOMER_1).givenTo(WORKER_2)
                .score(3).feedback("Average delivery service").ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());
        Rating flaggedRating = ratingRepo.save(Rating.builder().taskId(TASK_4).givenBy(CUSTOMER_1).givenTo(WORKER_2)
                .score(1).feedback("Very rude behavior").ratingType(RatingType.CUSTOMER_TO_WORKER).isVisible(true).build());

        // One flag against Worker 2
        flagRepo.save(Flag.builder().ratingId(flaggedRating.getRatingId()).taskId(TASK_4)
                .reporterId(WORKER_2).reportedUserId(CUSTOMER_1)
                .reason(FlagReason.FAKE_REVIEW).description("This review is unfair, the customer was not home")
                .status(FlagStatus.PENDING).build());

        // Precompute summaries
        // Worker 1: 6 ratings → 5+4+5+5+4+5 = 28/6 = 4.67 avg, IS PUBLIC
        summaryRepo.save(UserRatingSummary.builder().userId(WORKER_1)
                .averageRating(new BigDecimal("4.67")).weightedRating(new BigDecimal("4.70"))
                .totalRatings(6).totalFiveStar(4).totalFourStar(2).totalThreeStar(0)
                .totalTwoStar(0).totalOneStar(0).totalFlagsReceived(0).isPublic(true).build());

        // Worker 2: 2 ratings → 3+1 = 4/2 = 2.00, NOT PUBLIC
        summaryRepo.save(UserRatingSummary.builder().userId(WORKER_2)
                .averageRating(new BigDecimal("2.00")).weightedRating(new BigDecimal("2.00"))
                .totalRatings(2).totalFiveStar(0).totalFourStar(0).totalThreeStar(1)
                .totalTwoStar(0).totalOneStar(1).totalFlagsReceived(0).isPublic(false).build());

        // Customer 1: 2 ratings from Worker 1 → 5+4 = 4.50
        summaryRepo.save(UserRatingSummary.builder().userId(CUSTOMER_1)
                .averageRating(new BigDecimal("4.50")).weightedRating(new BigDecimal("4.50"))
                .totalRatings(2).totalFiveStar(1).totalFourStar(1).totalThreeStar(0)
                .totalTwoStar(0).totalOneStar(0).totalFlagsReceived(1).isPublic(false).build());

        log.info("============================================");
        log.info("  Rating Service - Sample data created:");
        log.info("  - 10 Ratings (6 for Worker 1, 2 for Worker 2, 2 for Customer 1)");
        log.info("  - 1 Flag (PENDING, Worker 2 flagged Customer 1's review)");
        log.info("  - 3 User Summaries (Worker 1: 4.67★ PUBLIC, Worker 2: 2.00★, Customer 1: 4.50★)");
        log.info("  - Worker 1 has 6 ratings → above 5 threshold → PUBLIC");
        log.info("  - Worker 2 has 2 ratings → below threshold → NOT PUBLIC");
        log.info("============================================");
    }
}
