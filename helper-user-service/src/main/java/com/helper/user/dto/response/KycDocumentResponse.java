package com.helper.user.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycDocumentResponse {

    private UUID documentId;
    private UUID workerId;
    private String documentType;
    private String kycLevel;
    private String documentUrl;
    private String documentNumber;
    private String status;
    private UUID reviewedBy;
    private String reviewComments;
    private LocalDateTime reviewedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
