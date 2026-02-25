package com.helper.rating.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlagResponse {
    private UUID flagId;
    private UUID ratingId;
    private UUID taskId;
    private UUID reporterId;
    private UUID reportedUserId;
    private String reason;
    private String description;
    private String status;
    private String adminNotes;
    private UUID reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
