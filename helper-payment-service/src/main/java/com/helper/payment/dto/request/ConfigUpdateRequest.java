package com.helper.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConfigUpdateRequest {

    @NotBlank(message = "Config value is required")
    private String configValue;
}
