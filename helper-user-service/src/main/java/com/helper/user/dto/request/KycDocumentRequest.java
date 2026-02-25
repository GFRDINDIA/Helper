package com.helper.user.dto.request;

import com.helper.user.enums.KycDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycDocumentRequest {

    @NotNull(message = "Document type is required")
    private KycDocumentType documentType;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;

    private String documentNumber; // Aadhaar, PAN, license number
}
