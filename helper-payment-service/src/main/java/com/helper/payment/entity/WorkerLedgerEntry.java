package com.helper.payment.entity;

import com.helper.payment.enums.LedgerEntryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "worker_ledger", indexes = {
        @Index(name = "idx_ledger_worker", columnList = "worker_id, created_at"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkerLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ledger_id")
    private UUID ledgerId;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private LedgerEntryType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Positive = owed to platform, negative = paid/credited

    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter; // Running balance after this entry

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
