package com.helper.rating.dto.request;

import com.helper.rating.enums.RatingType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmitRatingRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Rated user ID is required")
    private UUID givenTo;

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be between 1 and 5")
    @Max(value = 5, message = "Score must be between 1 and 5")
    private Integer score;

    @Size(max = 2000, message = "Feedback must be 2000 characters or fewer")
    private String feedback;

    @NotNull(message = "Rating type is required")
    private RatingType ratingType;
}
