package com.helper.user.dto.request;

import com.helper.user.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycReviewRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private KycStatus status;

    private String comments;
}
