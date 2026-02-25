package com.helper.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddTipRequest {

    @NotNull(message = "Tip amount is required")
    @DecimalMin(value = "0.01", message = "Tip must be positive")
    private BigDecimal tipAmount;
}
