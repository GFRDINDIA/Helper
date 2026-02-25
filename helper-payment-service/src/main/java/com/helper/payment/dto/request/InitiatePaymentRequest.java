package com.helper.payment.dto.request;

import com.helper.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InitiatePaymentRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @DecimalMin(value = "0.00", message = "Tip must be non-negative")
    private BigDecimal tipAmount; // Optional
}
