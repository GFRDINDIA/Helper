package com.helper.task.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBidRequest {

    @NotNull(message = "Proposed price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal proposedPrice;

    @Size(max = 1000, message = "Message must be under 1000 characters")
    private String message;
}
