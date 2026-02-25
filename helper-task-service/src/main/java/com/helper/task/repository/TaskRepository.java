package com.helper.task.repository;

import com.helper.task.entity.Task;
import com.helper.task.enums.TaskDomain;
import com.helper.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Find tasks by customer
    Page<Task> findByCustomerId(UUID customerId, Pageable pageable);

    // Find tasks assigned to a worker
    Page<Task> findByAssignedWorkerId(UUID workerId, Pageable pageable);

    // Find tasks by status
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    // Find tasks by domain and status
    Page<Task> findByDomainAndStatus(TaskDomain domain, TaskStatus status, Pageable pageable);

    // Geo-search: Find tasks within radius using Haversine formula (H2 compatible)
    // In production with PostGIS, replace with ST_DWithin for better performance
    @Query("SELECT t FROM Task t WHERE t.status = :status " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude)) * " +
            "cos(radians(t.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude)) * " +
            "cos(radians(t.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(t.latitude)))) ASC")
    List<Task> findNearbyTasksByStatus(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusKm") double radiusKm,
            @Param("status") TaskStatus status);

    // Geo-search with domain filter
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.domain = :domain " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude)) * " +
            "cos(radians(t.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude)) * " +
            "cos(radians(t.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(t.latitude)))) ASC")
    List<Task> findNearbyTasksByStatusAndDomain(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusKm") double radiusKm,
            @Param("status") TaskStatus status,
            @Param("domain") TaskDomain domain);

    // Count by status
    long countByStatus(TaskStatus status);

    // Count by domain
    long countByDomain(TaskDomain domain);

    // Count tasks by customer
    long countByCustomerId(UUID customerId);

    // Count active tasks for a worker
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedWorkerId = :workerId " +
            "AND t.status IN ('ACCEPTED', 'IN_PROGRESS')")
    long countActiveTasksByWorker(@Param("workerId") UUID workerId);

    // Find tasks with multiple filters
    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:domain IS NULL OR t.domain = :domain) AND " +
            "(:customerId IS NULL OR t.customerId = :customerId)")
    Page<Task> findWithFilters(
            @Param("status") TaskStatus status,
            @Param("domain") TaskDomain domain,
            @Param("customerId") UUID customerId,
            Pageable pageable);
}
