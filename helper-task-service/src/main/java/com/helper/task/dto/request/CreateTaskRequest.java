package com.helper.task.dto.request;

import com.helper.task.enums.PricingModel;
import com.helper.task.enums.TaskDomain;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @NotNull(message = "Domain is required")
    private TaskDomain domain;

    @NotNull(message = "Pricing model is required")
    private PricingModel pricingModel;

    @DecimalMin(value = "0.01", message = "Budget must be positive")
    private BigDecimal budget;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    @NotBlank(message = "Address is required")
    private String address;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> images;

    private LocalDateTime scheduledAt;
}
