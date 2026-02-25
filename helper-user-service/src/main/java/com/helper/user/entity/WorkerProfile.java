package com.helper.user.entity;

import com.helper.user.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "worker_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkerProfile {

    @Id
    @Column(name = "worker_id")
    private UUID workerId; // Same as user_id from Auth Service

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // Base location
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "base_address", columnDefinition = "TEXT")
    private String baseAddress;

    // Ratings (updated by Rating Service)
    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "total_tasks_completed")
    @Builder.Default
    private Integer totalTasksCompleted = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    // Child collections
    @OneToMany(mappedBy = "workerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkerSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "workerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PortfolioItem> portfolio = new ArrayList<>();

    @OneToMany(mappedBy = "workerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AvailabilitySlot> availabilitySlots = new ArrayList<>();

    @OneToMany(mappedBy = "workerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<KycDocument> kycDocuments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
}
