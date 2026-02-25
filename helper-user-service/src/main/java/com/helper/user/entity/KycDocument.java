package com.helper.user.entity;

import com.helper.user.enums.KycDocumentType;
import com.helper.user.enums.KycLevel;
import com.helper.user.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_documents", indexes = {
        @Index(name = "idx_kyc_worker", columnList = "worker_id"),
        @Index(name = "idx_kyc_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile workerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private KycDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level", nullable = false, length = 30)
    private KycLevel kycLevel;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_number", length = 50)
    private String documentNumber; // Aadhaar number, PAN number, license number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private KycStatus status = KycStatus.SUBMITTED;

    // Admin review
    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Expiry for periodic re-verification
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
