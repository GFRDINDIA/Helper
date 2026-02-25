package com.helper.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerProfile {

    @Id
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "total_tasks_posted")
    @Builder.Default
    private Integer totalTasksPosted = 0;

    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomerAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SavedPaymentMethod> paymentMethods = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
