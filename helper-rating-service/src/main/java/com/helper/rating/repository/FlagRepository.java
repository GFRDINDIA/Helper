package com.helper.rating.repository;

import com.helper.rating.entity.Flag;
import com.helper.rating.enums.FlagStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlagRepository extends JpaRepository<Flag, UUID> {

    Page<Flag> findByStatusOrderByCreatedAtDesc(FlagStatus status, Pageable pageable);

    Page<Flag> findByReportedUserIdOrderByCreatedAtDesc(UUID reportedUserId, Pageable pageable);

    long countByReportedUserIdAndStatus(UUID reportedUserId, FlagStatus status);

    long countByStatus(FlagStatus status);

    boolean existsByTaskIdAndReporterId(UUID taskId, UUID reporterId);
}
