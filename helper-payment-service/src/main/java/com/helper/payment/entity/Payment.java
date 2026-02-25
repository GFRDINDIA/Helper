package com.helper.payment.entity;

import com.helper.payment.enums.PaymentMethod;
import com.helper.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_pay_task", columnList = "task_id"),
        @Index(name = "idx_pay_payer", columnList = "payer_id"),
        @Index(name = "idx_pay_payee", columnList = "payee_id"),
        @Index(name = "idx_pay_status_date", columnList = "status, created_at"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_pay_task", columnNames = "task_id"),
        @UniqueConstraint(name = "uk_pay_invoice", columnNames = "invoice_number"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "task_id", nullable = false, unique = true)
    private UUID taskId;

    @Column(name = "payer_id", nullable = false)
    private UUID payerId; // Customer

    @Column(name = "payee_id", nullable = false)
    private UUID payeeId; // Worker

    // ===== Money fields — ALL BigDecimal, scale 2 =====

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Task final price (before tip)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commission; // amount * commissionRate

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate; // e.g. 0.0200

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax; // commission * taxRate (GST)

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate; // e.g. 0.1800

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tip = BigDecimal.ZERO;

    @Column(name = "worker_payout", nullable = false, precision = 10, scale = 2)
    private BigDecimal workerPayout; // amount - commission - tax + tip

    // ===== Payment metadata =====

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "invoice_number", length = 50, unique = true)
    private String invoiceNumber;

    @Column(name = "invoice_url", columnDefinition = "TEXT")
    private String invoiceUrl;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference; // Razorpay payment_id (Phase 2)

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
