package com.helper.rating.dto.request;

import com.helper.rating.enums.FlagStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewFlagRequest {

    @NotNull(message = "Status is required")
    private FlagStatus status; // DISMISSED or ACTION_TAKEN

    private String adminNotes;

    private Boolean hideRating; // If true, sets rating.isVisible = false
}
