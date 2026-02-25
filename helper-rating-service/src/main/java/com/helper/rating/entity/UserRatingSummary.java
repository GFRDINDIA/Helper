package com.helper.rating.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Precomputed rating summary per user.
 * Updated whenever a new rating is submitted.
 * Stores both simple average and time-weighted average (PRD: recent ratings weigh more).
 */
@Entity
@Table(name = "user_rating_summaries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRatingSummary {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "weighted_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal weightedRating = BigDecimal.ZERO; // Time-weighted: recent ratings weigh more

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "total_five_star")
    @Builder.Default
    private Integer totalFiveStar = 0;

    @Column(name = "total_four_star")
    @Builder.Default
    private Integer totalFourStar = 0;

    @Column(name = "total_three_star")
    @Builder.Default
    private Integer totalThreeStar = 0;

    @Column(name = "total_two_star")
    @Builder.Default
    private Integer totalTwoStar = 0;

    @Column(name = "total_one_star")
    @Builder.Default
    private Integer totalOneStar = 0;

    @Column(name = "total_flags_received")
    @Builder.Default
    private Integer totalFlagsReceived = 0;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false; // Only true after min-public-threshold (5) ratings

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
