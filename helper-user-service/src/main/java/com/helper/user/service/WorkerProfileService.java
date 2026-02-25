package com.helper.user.service;

import com.helper.user.dto.request.*;
import com.helper.user.dto.response.NearbyWorkerResponse;
import com.helper.user.dto.response.WorkerProfileResponse;
import com.helper.user.dto.response.WorkerProfileResponse.AvailabilityResponse;
import com.helper.user.dto.response.WorkerProfileResponse.SkillResponse;
import com.helper.user.entity.*;
import com.helper.user.enums.TaskDomain;
import com.helper.user.enums.VerificationStatus;
import com.helper.user.exception.UserExceptions;
import com.helper.user.repository.*;
import com.helper.user.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerProfileService {

    private final WorkerProfileRepository workerRepo;
    private final WorkerSkillRepository skillRepo;
    private final PortfolioItemRepository portfolioRepo;

    // ===== CREATE OR UPDATE WORKER PROFILE =====

    @Transactional
    public WorkerProfileResponse createOrUpdateProfile(WorkerProfileRequest request, AuthenticatedUser user) {
        if (!user.isWorker()) {
            throw new UserExceptions.UnauthorizedAccessException("Only workers can manage worker profiles");
        }

        WorkerProfile profile = workerRepo.findById(user.getUserId())
                .orElse(WorkerProfile.builder().workerId(user.getUserId()).build());

        // Update fields
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getProfileImageUrl() != null) profile.setProfileImageUrl(request.getProfileImageUrl());
        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());
        if (request.getBaseAddress() != null) profile.setBaseAddress(request.getBaseAddress());

        profile = workerRepo.save(profile);

        // Update skills if provided
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            updateSkills(profile, request.getSkills());
        }

        // Update availability if provided
        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            updateAvailability(profile, request.getAvailability());
        }

        log.info("Worker profile created/updated: {}", user.getUserId());
        return mapToResponse(workerRepo.findById(profile.getWorkerId()).orElse(profile));
    }

    // ===== ADD / UPDATE SKILLS =====

    @Transactional
    public WorkerProfileResponse addSkill(SkillRequest request, AuthenticatedUser user) {
        WorkerProfile profile = getProfileOrThrow(user.getUserId());

        // Check duplicate
        skillRepo.findByWorkerProfile_WorkerIdAndDomain(user.getUserId(), request.getDomain())
                .ifPresent(s -> { throw new UserExceptions.DuplicateSkillException(
                        "You already have skill for domain: " + request.getDomain()); });

        WorkerSkill skill = WorkerSkill.builder()
                .workerProfile(profile)
                .domain(request.getDomain())
                .priceModel(request.getPriceModel())
                .fixedRate(request.getFixedRate())
                .latitude(request.getLatitude() != null ? request.getLatitude() : profile.getLatitude())
                .longitude(request.getLongitude() != null ? request.getLongitude() : profile.getLongitude())
                .serviceRadiusKm(request.getServiceRadiusKm() != null ? request.getServiceRadiusKm() : 10)
                .build();

        skillRepo.save(skill);
        log.info("Skill added: {} for worker: {}", request.getDomain(), user.getUserId());

        return mapToResponse(workerRepo.findById(profile.getWorkerId()).orElse(profile));
    }

    @Transactional
    public void removeSkill(TaskDomain domain, AuthenticatedUser user) {
        skillRepo.findByWorkerProfile_WorkerIdAndDomain(user.getUserId(), domain)
                .orElseThrow(() -> new UserExceptions.InvalidProfileException("Skill not found: " + domain));
        skillRepo.deleteByWorkerProfile_WorkerIdAndDomain(user.getUserId(), domain);
        log.info("Skill removed: {} for worker: {}", domain, user.getUserId());
    }

    // ===== AVAILABILITY =====

    @Transactional
    public WorkerProfileResponse updateAvailability(List<AvailabilityRequest> slots, AuthenticatedUser user) {
        WorkerProfile profile = getProfileOrThrow(user.getUserId());
        updateAvailability(profile, slots);
        return mapToResponse(workerRepo.findById(profile.getWorkerId()).orElse(profile));
    }

    @Transactional
    public WorkerProfileResponse toggleAvailability(boolean available, AuthenticatedUser user) {
        WorkerProfile profile = getProfileOrThrow(user.getUserId());
        profile.setIsAvailable(available);
        workerRepo.save(profile);
        log.info("Worker {} availability: {}", user.getUserId(), available);
        return mapToResponse(profile);
    }

    // ===== PORTFOLIO =====

    @Transactional
    public void addPortfolioItem(PortfolioRequest request, AuthenticatedUser user) {
        WorkerProfile profile = getProfileOrThrow(user.getUserId());

        long count = portfolioRepo.countByWorkerProfile_WorkerId(user.getUserId());
        if (count >= 20) {
            throw new UserExceptions.InvalidProfileException("Maximum 20 portfolio items allowed");
        }

        PortfolioItem item = PortfolioItem.builder()
                .workerProfile(profile)
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .domain(request.getDomain())
                .build();
        portfolioRepo.save(item);
        log.info("Portfolio item added for worker: {}", user.getUserId());
    }

    @Transactional
    public void removePortfolioItem(UUID itemId, AuthenticatedUser user) {
        PortfolioItem item = portfolioRepo.findById(itemId)
                .orElseThrow(() -> new UserExceptions.InvalidProfileException("Portfolio item not found"));
        if (!item.getWorkerProfile().getWorkerId().equals(user.getUserId())) {
            throw new UserExceptions.UnauthorizedAccessException("You can only delete your own portfolio items");
        }
        portfolioRepo.delete(item);
    }

    public List<com.helper.user.dto.response.PortfolioItemResponse> getPortfolio(UUID workerId) {
        return portfolioRepo.findByWorkerProfile_WorkerIdOrderByCreatedAtDesc(workerId).stream()
                .map(p -> com.helper.user.dto.response.PortfolioItemResponse.builder()
                        .itemId(p.getItemId()).imageUrl(p.getImageUrl())
                        .description(p.getDescription())
                        .domain(p.getDomain() != null ? p.getDomain().name() : null)
                        .createdAt(p.getCreatedAt()).build())
                .collect(Collectors.toList());
    }

    // ===== GET PROFILE =====

    public WorkerProfileResponse getProfile(UUID workerId) {
        WorkerProfile profile = getProfileOrThrow(workerId);
        return mapToResponse(profile);
    }

    // ===== GEO SEARCH: FIND NEARBY WORKERS =====

    public List<NearbyWorkerResponse> findNearbyWorkers(double lat, double lng, TaskDomain domain) {
        List<WorkerSkill> skills;
        if (domain != null) {
            skills = skillRepo.findNearbyWorkersByDomain(lat, lng, domain);
        } else {
            skills = skillRepo.findNearbyWorkers(lat, lng);
        }

        return skills.stream().map(ws -> {
            double dist = calculateDistance(lat, lng, ws.getLatitude(), ws.getLongitude());
            WorkerProfile wp = ws.getWorkerProfile();
            return NearbyWorkerResponse.builder()
                    .workerId(wp.getWorkerId())
                    .bio(wp.getBio())
                    .profileImageUrl(wp.getProfileImageUrl())
                    .averageRating(wp.getAverageRating())
                    .totalRatings(wp.getTotalRatings())
                    .totalTasksCompleted(wp.getTotalTasksCompleted())
                    .verificationStatus(wp.getVerificationStatus().name())
                    .domain(ws.getDomain().name())
                    .priceModel(ws.getPriceModel().name())
                    .fixedRate(ws.getFixedRate())
                    .distanceKm(dist)
                    .serviceRadiusKm(ws.getServiceRadiusKm())
                    .build();
        }).collect(Collectors.toList());
    }

    // ===== PRIVATE HELPERS =====

    private WorkerProfile getProfileOrThrow(UUID workerId) {
        return workerRepo.findById(workerId)
                .orElseThrow(() -> new UserExceptions.ProfileNotFoundException("Worker profile not found: " + workerId));
    }

    private void updateSkills(WorkerProfile profile, List<SkillRequest> skillRequests) {
        // Clear existing and re-add
        profile.getSkills().clear();
        workerRepo.save(profile); // flush removals

        for (SkillRequest sr : skillRequests) {
            WorkerSkill skill = WorkerSkill.builder()
                    .workerProfile(profile)
                    .domain(sr.getDomain())
                    .priceModel(sr.getPriceModel())
                    .fixedRate(sr.getFixedRate())
                    .latitude(sr.getLatitude() != null ? sr.getLatitude() : profile.getLatitude())
                    .longitude(sr.getLongitude() != null ? sr.getLongitude() : profile.getLongitude())
                    .serviceRadiusKm(sr.getServiceRadiusKm() != null ? sr.getServiceRadiusKm() : 10)
                    .build();
            skillRepo.save(skill);
        }
    }

    private void updateAvailability(WorkerProfile profile, List<AvailabilityRequest> slots) {
        profile.getAvailabilitySlots().clear();
        workerRepo.save(profile);

        for (AvailabilityRequest ar : slots) {
            AvailabilitySlot slot = AvailabilitySlot.builder()
                    .workerProfile(profile)
                    .dayOfWeek(ar.getDayOfWeek())
                    .startTime(ar.getStartTime())
                    .endTime(ar.getEndTime())
                    .isAvailable(ar.getIsAvailable() != null ? ar.getIsAvailable() : true)
                    .build();
            profile.getAvailabilitySlots().add(slot);
        }
        workerRepo.save(profile);
    }

    private WorkerProfileResponse mapToResponse(WorkerProfile p) {
        List<WorkerSkill> skills = skillRepo.findByWorkerProfile_WorkerId(p.getWorkerId());
        List<SkillResponse> skillDtos = skills.stream().map(s -> SkillResponse.builder()
                .skillId(s.getSkillId()).domain(s.getDomain().name())
                .priceModel(s.getPriceModel().name()).fixedRate(s.getFixedRate())
                .latitude(s.getLatitude()).longitude(s.getLongitude())
                .serviceRadiusKm(s.getServiceRadiusKm()).isAvailable(s.getIsAvailable()).build()
        ).collect(Collectors.toList());

        List<AvailabilityResponse> availDtos = p.getAvailabilitySlots().stream().map(a -> AvailabilityResponse.builder()
                .dayOfWeek(a.getDayOfWeek().name()).startTime(a.getStartTime())
                .endTime(a.getEndTime()).isAvailable(a.getIsAvailable()).build()
        ).collect(Collectors.toList());

        return WorkerProfileResponse.builder()
                .workerId(p.getWorkerId()).bio(p.getBio())
                .profileImageUrl(p.getProfileImageUrl())
                .latitude(p.getLatitude()).longitude(p.getLongitude())
                .baseAddress(p.getBaseAddress())
                .averageRating(p.getAverageRating()).totalRatings(p.getTotalRatings())
                .totalTasksCompleted(p.getTotalTasksCompleted())
                .verificationStatus(p.getVerificationStatus())
                .isAvailable(p.getIsAvailable())
                .skills(skillDtos).availability(availDtos)
                .createdAt(p.getCreatedAt()).build();
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)) * 100.0) / 100.0;
    }
}
