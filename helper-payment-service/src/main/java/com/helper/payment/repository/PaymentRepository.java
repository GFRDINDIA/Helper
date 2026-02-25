package com.helper.payment.repository;

import com.helper.payment.entity.Payment;
import com.helper.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByTaskId(UUID taskId);

    boolean existsByTaskId(UUID taskId);

    Page<Payment> findByPayerIdOrderByCreatedAtDesc(UUID payerId, Pageable pageable);

    Page<Payment> findByPayeeIdOrderByCreatedAtDesc(UUID payeeId, Pageable pageable);

    // My transactions: customer sees payments they made, worker sees payments they received
    Page<Payment> findByPayerIdOrPayeeIdOrderByCreatedAtDesc(UUID payerId, UUID payeeId, Pageable pageable);

    long countByStatus(PaymentStatus status);

    // Admin reporting
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.commission), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal sumTotalCommission();

    @Query("SELECT COALESCE(SUM(p.tax), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal sumTotalTax();

    @Query("SELECT COALESCE(SUM(p.tip), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal sumTotalTips();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.method = :method")
    long countByMethodAndCompleted(@Param("method") com.helper.payment.enums.PaymentMethod method);

    // Date-range queries for admin dashboard
    @Query("SELECT COALESCE(SUM(p.commission), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt BETWEEN :start AND :end")
    BigDecimal sumCommissionBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Admin: all transactions with filtering
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);
}
