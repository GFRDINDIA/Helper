package com.helper.rating.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRatingSummaryResponse {
    private UUID userId;
    private BigDecimal averageRating;
    private BigDecimal weightedRating;
    private Integer totalRatings;
    private Boolean isPublic;
    private StarDistribution starDistribution;
    private Integer totalFlagsReceived;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StarDistribution {
        private Integer fiveStar;
        private Integer fourStar;
        private Integer threeStar;
        private Integer twoStar;
        private Integer oneStar;
    }
}
