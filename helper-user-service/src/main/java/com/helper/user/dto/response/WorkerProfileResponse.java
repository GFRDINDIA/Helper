package com.helper.user.dto.response;

import com.helper.user.enums.VerificationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkerProfileResponse {

    private UUID workerId;
    private String bio;
    private String profileImageUrl;
    private Double latitude;
    private Double longitude;
    private String baseAddress;
    private Double averageRating;
    private Integer totalRatings;
    private Integer totalTasksCompleted;
    private VerificationStatus verificationStatus;
    private Boolean isAvailable;
    private List<SkillResponse> skills;
    private List<AvailabilityResponse> availability;
    private LocalDateTime createdAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SkillResponse {
        private UUID skillId;
        private String domain;
        private String priceModel;
        private java.math.BigDecimal fixedRate;
        private Double latitude;
        private Double longitude;
        private Integer serviceRadiusKm;
        private Boolean isAvailable;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AvailabilityResponse {
        private String dayOfWeek;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private Boolean isAvailable;
    }
}
