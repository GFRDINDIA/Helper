package com.helper.user.repository;

import com.helper.user.entity.WorkerSkill;
import com.helper.user.enums.TaskDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerSkillRepository extends JpaRepository<WorkerSkill, UUID> {

    List<WorkerSkill> findByWorkerProfile_WorkerId(UUID workerId);

    Optional<WorkerSkill> findByWorkerProfile_WorkerIdAndDomain(UUID workerId, TaskDomain domain);

    void deleteByWorkerProfile_WorkerIdAndDomain(UUID workerId, TaskDomain domain);

    // Find nearby workers by domain using Haversine (H2 compatible)
    @Query("SELECT ws FROM WorkerSkill ws JOIN FETCH ws.workerProfile wp " +
            "WHERE ws.domain = :domain AND ws.isAvailable = true " +
            "AND wp.verificationStatus = 'VERIFIED' AND wp.isAvailable = true " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(ws.latitude)) * " +
            "cos(radians(ws.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(ws.latitude)))) <= ws.serviceRadiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(ws.latitude)) * " +
            "cos(radians(ws.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(ws.latitude)))) ASC")
    List<WorkerSkill> findNearbyWorkersByDomain(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("domain") TaskDomain domain);

    // Find nearby workers across all domains
    @Query("SELECT ws FROM WorkerSkill ws JOIN FETCH ws.workerProfile wp " +
            "WHERE ws.isAvailable = true " +
            "AND wp.verificationStatus = 'VERIFIED' AND wp.isAvailable = true " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(ws.latitude)) * " +
            "cos(radians(ws.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(ws.latitude)))) <= ws.serviceRadiusKm " +
            "ORDER BY wp.averageRating DESC")
    List<WorkerSkill> findNearbyWorkers(
            @Param("lat") double latitude,
            @Param("lng") double longitude);
}
