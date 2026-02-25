package com.helper.task.entity;

import com.helper.task.enums.BidStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_bid_task", columnList = "task_id, status"),
        @Index(name = "idx_bid_worker", columnList = "worker_id"),
        @Index(name = "idx_bid_task_worker", columnList = "task_id, worker_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bid_id", updatable = false, nullable = false)
    private UUID bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "proposed_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal proposedPrice;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BidStatus status = BidStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
