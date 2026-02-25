package com.helper.user.service;

import com.helper.user.dto.request.KycDocumentRequest;
import com.helper.user.dto.request.KycReviewRequest;
import com.helper.user.dto.response.KycDocumentResponse;
import com.helper.user.entity.KycDocument;
import com.helper.user.entity.WorkerProfile;
import com.helper.user.entity.WorkerSkill;
import com.helper.user.enums.*;
import com.helper.user.exception.UserExceptions;
import com.helper.user.repository.KycDocumentRepository;
import com.helper.user.repository.WorkerProfileRepository;
import com.helper.user.repository.WorkerSkillRepository;
import com.helper.user.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final KycDocumentRepository kycRepo;
    private final WorkerProfileRepository workerRepo;
    private final WorkerSkillRepository skillRepo;

    @Value("${app.kyc.re-verification-months:12}")
    private int reVerificationMonths;

    // ===== WORKER: SUBMIT KYC DOCUMENTS =====

    @Transactional
    public List<KycDocumentResponse> submitKycDocuments(List<KycDocumentRequest> requests, AuthenticatedUser user) {
        if (!user.isWorker()) {
            throw new UserExceptions.UnauthorizedAccessException("Only workers can submit KYC documents");
        }

        WorkerProfile profile = workerRepo.findById(user.getUserId())
                .orElseThrow(() -> new UserExceptions.ProfileNotFoundException(
                        "Create your worker profile before submitting KYC documents"));

        // Determine required KYC level based on worker's registered domains
        List<WorkerSkill> skills = skillRepo.findByWorkerProfile_WorkerId(user.getUserId());
        if (skills.isEmpty()) {
            throw new UserExceptions.InvalidKycException("Register at least one skill domain before submitting KYC");
        }

        KycLevel requiredLevel = getHighestKycLevel(skills);

        // Validate required documents are present
        validateDocumentsForLevel(requests, requiredLevel);

        List<KycDocument> documents = new ArrayList<>();
        for (KycDocumentRequest req : requests) {
            KycDocument doc = KycDocument.builder()
                    .workerProfile(profile)
                    .documentType(req.getDocumentType())
                    .kycLevel(requiredLevel)
                    .documentUrl(req.getDocumentUrl())
                    .documentNumber(req.getDocumentNumber())
                    .status(KycStatus.SUBMITTED)
                    .build();
            documents.add(doc);
        }

        documents = kycRepo.saveAll(documents);

        // Update profile status
        profile.setVerificationStatus(VerificationStatus.PENDING);
        workerRepo.save(profile);

        log.info("KYC documents submitted by worker: {} level: {} docs: {}",
                user.getUserId(), requiredLevel, documents.size());

        return documents.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ===== WORKER: VIEW OWN KYC STATUS =====

    public List<KycDocumentResponse> getMyKycDocuments(AuthenticatedUser user) {
        return kycRepo.findByWorkerProfile_WorkerIdOrderByCreatedAtDesc(user.getUserId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ===== ADMIN: GET PENDING KYC QUEUE =====

    public List<KycDocumentResponse> getPendingKycDocuments() {
        return kycRepo.findByStatusIn(List.of(KycStatus.SUBMITTED, KycStatus.UNDER_REVIEW))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ===== ADMIN: REVIEW KYC DOCUMENT =====

    @Transactional
    public KycDocumentResponse reviewKycDocument(UUID documentId, KycReviewRequest request, AuthenticatedUser admin) {
        if (!admin.isAdmin()) {
            throw new UserExceptions.UnauthorizedAccessException("Only admins can review KYC documents");
        }

        KycDocument doc = kycRepo.findById(documentId)
                .orElseThrow(() -> new UserExceptions.DocumentNotFoundException("KYC document not found: " + documentId));

        if (doc.getStatus() != KycStatus.SUBMITTED && doc.getStatus() != KycStatus.UNDER_REVIEW) {
            throw new UserExceptions.InvalidKycException("Document is not pending review. Status: " + doc.getStatus());
        }

        if (request.getStatus() != KycStatus.APPROVED && request.getStatus() != KycStatus.REJECTED) {
            throw new UserExceptions.InvalidKycException("Review status must be APPROVED or REJECTED");
        }

        doc.setStatus(request.getStatus());
        doc.setReviewedBy(admin.getUserId());
        doc.setReviewComments(request.getComments());
        doc.setReviewedAt(LocalDateTime.now());

        if (request.getStatus() == KycStatus.APPROVED) {
            doc.setExpiresAt(LocalDateTime.now().plusMonths(reVerificationMonths));
        }

        doc = kycRepo.save(doc);

        // Check if ALL documents for this worker are approved
        updateWorkerVerificationStatus(doc.getWorkerProfile().getWorkerId());

        log.info("KYC document {} reviewed: {} by admin: {} comments: {}",
                documentId, request.getStatus(), admin.getUserId(), request.getComments());

        return mapToResponse(doc);
    }

    // ===== ADMIN: STATS =====

    public Map<String, Long> getKycStats() {
        Map<String, Long> stats = new HashMap<>();
        for (KycStatus status : KycStatus.values()) {
            stats.put(status.name(), kycRepo.countByStatus(status));
        }
        return stats;
    }

    // ===== SCHEDULED: EXPIRE OLD KYC =====

    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    @Transactional
    public void expireOldKycDocuments() {
        List<KycDocument> expired = kycRepo.findByExpiresAtBeforeAndStatusNot(
                LocalDateTime.now(), KycStatus.EXPIRED);
        for (KycDocument doc : expired) {
            doc.setStatus(KycStatus.EXPIRED);
            kycRepo.save(doc);
            updateWorkerVerificationStatus(doc.getWorkerProfile().getWorkerId());
            log.info("KYC document expired: {} for worker: {}", doc.getDocumentId(), doc.getWorkerProfile().getWorkerId());
        }
    }

    // ===== PRIVATE HELPERS =====

    private KycLevel getHighestKycLevel(List<WorkerSkill> skills) {
        KycLevel highest = KycLevel.BASIC;
        for (WorkerSkill skill : skills) {
            KycLevel level = KycLevel.forDomain(skill.getDomain());
            if (level.ordinal() > highest.ordinal()) {
                highest = level;
            }
        }
        return highest;
    }

    private void validateDocumentsForLevel(List<KycDocumentRequest> docs, KycLevel level) {
        Set<KycDocumentType> types = docs.stream()
                .map(KycDocumentRequest::getDocumentType).collect(Collectors.toSet());

        // BASIC: Aadhaar + PAN + Selfie
        if (!types.contains(KycDocumentType.AADHAAR_CARD)) {
            throw new UserExceptions.InvalidKycException("Aadhaar card is required for all KYC levels");
        }
        if (!types.contains(KycDocumentType.PAN_CARD)) {
            throw new UserExceptions.InvalidKycException("PAN card is required for all KYC levels");
        }
        if (!types.contains(KycDocumentType.SELFIE)) {
            throw new UserExceptions.InvalidKycException("Selfie verification is required for all KYC levels");
        }

        // PROFESSIONAL: + Professional License
        if (level == KycLevel.PROFESSIONAL || level == KycLevel.PROFESSIONAL_PLUS_LICENSE) {
            if (!types.contains(KycDocumentType.PROFESSIONAL_LICENSE)) {
                throw new UserExceptions.InvalidKycException(
                        "Professional license/certification is required for " + level + " KYC level");
            }
        }

        // PROFESSIONAL_PLUS_LICENSE: + Regulatory License
        if (level == KycLevel.PROFESSIONAL_PLUS_LICENSE) {
            if (!types.contains(KycDocumentType.REGULATORY_LICENSE)) {
                throw new UserExceptions.InvalidKycException(
                        "Regulatory license is required for PROFESSIONAL_PLUS_LICENSE KYC level (Medical/Finance/Education)");
            }
        }
    }

    private void updateWorkerVerificationStatus(UUID workerId) {
        List<KycDocument> allDocs = kycRepo.findByWorkerProfile_WorkerIdOrderByCreatedAtDesc(workerId);

        if (allDocs.isEmpty()) return;

        WorkerProfile profile = workerRepo.findById(workerId).orElse(null);
        if (profile == null) return;

        boolean anyRejected = allDocs.stream().anyMatch(d -> d.getStatus() == KycStatus.REJECTED);
        boolean allApproved = allDocs.stream().allMatch(d -> d.getStatus() == KycStatus.APPROVED);
        boolean anyExpired = allDocs.stream().anyMatch(d -> d.getStatus() == KycStatus.EXPIRED);

        if (anyRejected) {
            profile.setVerificationStatus(VerificationStatus.REJECTED);
        } else if (anyExpired) {
            profile.setVerificationStatus(VerificationStatus.PENDING);
        } else if (allApproved) {
            profile.setVerificationStatus(VerificationStatus.VERIFIED);
        } else {
            profile.setVerificationStatus(VerificationStatus.PENDING);
        }

        workerRepo.save(profile);
        log.info("Worker {} verification status updated: {}", workerId, profile.getVerificationStatus());
    }

    private KycDocumentResponse mapToResponse(KycDocument doc) {
        return KycDocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .workerId(doc.getWorkerProfile().getWorkerId())
                .documentType(doc.getDocumentType().name())
                .kycLevel(doc.getKycLevel().name())
                .documentUrl(doc.getDocumentUrl())
                .documentNumber(doc.getDocumentNumber())
                .status(doc.getStatus().name())
                .reviewedBy(doc.getReviewedBy())
                .reviewComments(doc.getReviewComments())
                .reviewedAt(doc.getReviewedAt())
                .expiresAt(doc.getExpiresAt())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
