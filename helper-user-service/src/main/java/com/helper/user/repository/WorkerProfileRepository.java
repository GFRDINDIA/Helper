package com.helper.user.repository;

import com.helper.user.entity.WorkerProfile;
import com.helper.user.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {
    List<WorkerProfile> findByVerificationStatus(VerificationStatus status);
    long countByVerificationStatus(VerificationStatus status);
    boolean existsByWorkerId(UUID workerId);
}
