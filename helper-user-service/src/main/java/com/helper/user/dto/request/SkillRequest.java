package com.helper.user.dto.request;

import com.helper.user.enums.PricingModel;
import com.helper.user.enums.TaskDomain;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillRequest {

    @NotNull(message = "Domain is required")
    private TaskDomain domain;

    @NotNull(message = "Pricing model is required")
    private PricingModel priceModel;

    @DecimalMin(value = "0.01", message = "Fixed rate must be positive")
    private BigDecimal fixedRate;

    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double latitude;

    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double longitude;

    @Min(1) @Max(100)
    private Integer serviceRadiusKm;
}
