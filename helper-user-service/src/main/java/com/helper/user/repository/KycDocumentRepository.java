package com.helper.user.repository;

import com.helper.user.entity.KycDocument;
import com.helper.user.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {
    List<KycDocument> findByWorkerProfile_WorkerIdOrderByCreatedAtDesc(UUID workerId);
    List<KycDocument> findByStatus(KycStatus status);
    List<KycDocument> findByStatusIn(List<KycStatus> statuses);
    long countByStatus(KycStatus status);
    List<KycDocument> findByExpiresAtBeforeAndStatusNot(LocalDateTime date, KycStatus status);
}
