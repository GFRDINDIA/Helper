package com.helper.user.repository;

import com.helper.user.entity.PortfolioItem;
import com.helper.user.enums.TaskDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {
    List<PortfolioItem> findByWorkerProfile_WorkerIdOrderByCreatedAtDesc(UUID workerId);
    List<PortfolioItem> findByWorkerProfile_WorkerIdAndDomain(UUID workerId, TaskDomain domain);
    long countByWorkerProfile_WorkerId(UUID workerId);
}
