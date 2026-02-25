package com.helper.task.entity;

import com.helper.task.enums.PricingModel;
import com.helper.task.enums.TaskDomain;
import com.helper.task.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_status_domain", columnList = "status, domain, created_at"),
        @Index(name = "idx_task_customer", columnList = "customer_id"),
        @Index(name = "idx_task_worker", columnList = "assigned_worker_id"),
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_domain", columnList = "domain"),
        @Index(name = "idx_task_location", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "task_id", updatable = false, nullable = false)
    private UUID taskId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false, length = 20)
    private PricingModel pricingModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.POSTED;

    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;

    // Geo location stored as lat/lng (PostGIS GEOGRAPHY(POINT) in prod)
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    // Images stored as JSON array of URLs
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_images", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(name = "assigned_worker_id")
    private UUID assignedWorkerId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "dispute_reason", columnDefinition = "TEXT")
    private String disputeReason;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // === Business Logic Helpers ===

    public boolean isOpenForBidding() {
        return status == TaskStatus.OPEN && pricingModel == PricingModel.BIDDING;
    }

    public boolean isOpenForAcceptance() {
        return status == TaskStatus.OPEN && pricingModel == PricingModel.FIXED;
    }

    public boolean canBeModified() {
        return status == TaskStatus.POSTED || status == TaskStatus.OPEN;
    }

    public boolean canBeCancelled() {
        return status != TaskStatus.COMPLETED &&
               status != TaskStatus.PAYMENT_DONE &&
               status != TaskStatus.CLOSED &&
               status != TaskStatus.CANCELLED;
    }
}
