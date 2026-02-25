package com.helper.payment.repository;

import com.helper.payment.entity.WorkerLedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerLedgerRepository extends JpaRepository<WorkerLedgerEntry, UUID> {

    Page<WorkerLedgerEntry> findByWorkerIdOrderByCreatedAtDesc(UUID workerId, Pageable pageable);

    // Get latest entry to read running balance
    Optional<WorkerLedgerEntry> findTopByWorkerIdOrderByCreatedAtDesc(UUID workerId);

    // Current balance for a worker
    @Query("SELECT COALESCE(e.balanceAfter, 0) FROM WorkerLedgerEntry e WHERE e.workerId = :wid ORDER BY e.createdAt DESC LIMIT 1")
    Optional<BigDecimal> findCurrentBalance(@Param("wid") UUID workerId);
}
