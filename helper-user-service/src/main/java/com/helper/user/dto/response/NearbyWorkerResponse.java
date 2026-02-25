package com.helper.user.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NearbyWorkerResponse {

    private UUID workerId;
    private String bio;
    private String profileImageUrl;
    private Double averageRating;
    private Integer totalRatings;
    private Integer totalTasksCompleted;
    private String verificationStatus;
    private String domain;
    private String priceModel;
    private BigDecimal fixedRate;
    private Double distanceKm;
    private Integer serviceRadiusKm;
}
