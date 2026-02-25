package com.helper.task.repository;

import com.helper.task.entity.Bid;
import com.helper.task.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    // Find all bids for a task
    List<Bid> findByTask_TaskIdOrderByCreatedAtDesc(UUID taskId);

    // Find bids by task and status
    List<Bid> findByTask_TaskIdAndStatus(UUID taskId, BidStatus status);

    // Find bid by worker for a specific task (ensure 1 bid per worker per task)
    Optional<Bid> findByTask_TaskIdAndWorkerId(UUID taskId, UUID workerId);

    // Count bids for a task
    long countByTask_TaskId(UUID taskId);

    // Count pending bids for a task
    long countByTask_TaskIdAndStatus(UUID taskId, BidStatus status);

    // Find bids by worker
    List<Bid> findByWorkerIdOrderByCreatedAtDesc(UUID workerId);

    // Find accepted bid for a task
    @Query("SELECT b FROM Bid b WHERE b.task.taskId = :taskId AND b.status = 'ACCEPTED'")
    Optional<Bid> findAcceptedBidForTask(@Param("taskId") UUID taskId);
}
