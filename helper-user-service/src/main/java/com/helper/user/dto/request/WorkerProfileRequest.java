package com.helper.user.dto.request;

import com.helper.user.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

// ===== WORKER DTOs =====

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkerProfileRequest {

    @Size(max = 1000, message = "Bio must be under 1000 characters")
    private String bio;

    private String profileImageUrl;

    @NotNull(message = "Latitude is required")
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double longitude;

    private String baseAddress;

    @Valid
    private List<SkillRequest> skills;

    @Valid
    private List<AvailabilityRequest> availability;
}
