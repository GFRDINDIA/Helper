package com.helper.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressRequest {

    @NotBlank private String label;
    @NotBlank private String addressLine1;
    private String addressLine2;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String pinCode;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") private Double latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") private Double longitude;
    private Boolean isDefault;
}
