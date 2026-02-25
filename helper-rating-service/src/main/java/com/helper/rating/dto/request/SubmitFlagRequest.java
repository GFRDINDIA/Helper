package com.helper.rating.dto.request;

import com.helper.rating.enums.FlagReason;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmitFlagRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Reported user ID is required")
    private UUID reportedUserId;

    private UUID ratingId; // Optional — can flag a specific rating

    @NotNull(message = "Flag reason is required")
    private FlagReason reason;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;
}
