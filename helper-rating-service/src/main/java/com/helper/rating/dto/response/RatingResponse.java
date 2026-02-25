package com.helper.rating.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RatingResponse {
    private UUID ratingId;
    private UUID taskId;
    private UUID givenBy;
    private UUID givenTo;
    private Integer score;
    private String feedback;
    private String ratingType;
    private Boolean isVisible;
    private LocalDateTime createdAt;
}
