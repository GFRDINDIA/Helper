package com.helper.rating.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RatingStatsResponse {
    private long totalRatings;
    private long visibleRatings;
    private long hiddenRatings;
    private BigDecimal platformAverageRating;
    private long pendingFlags;
    private long reviewedFlags;
    private long dismissedFlags;
    private long actionTakenFlags;
}
