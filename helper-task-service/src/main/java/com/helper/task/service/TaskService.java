package com.helper.task.service;

import com.helper.task.dto.request.*;
import com.helper.task.dto.response.TaskResponse;
import com.helper.task.dto.response.TaskStatsResponse;
import com.helper.task.entity.Task;
import com.helper.task.enums.*;
import com.helper.task.exception.TaskExceptions;
import com.helper.task.repository.BidRepository;
import com.helper.task.repository.TaskRepository;
import com.helper.task.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final BidRepository bidRepository;

    @Value("${app.task.default-search-radius-km:10}")
    private double defaultRadiusKm;

    @Value("${app.task.max-search-radius-km:50}")
    private double maxRadiusKm;

    /**
     * Create a new task (CUSTOMER only)
     */
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, AuthenticatedUser user) {
        if (!user.isCustomer()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("Only customers can create tasks");
        }

        Task task = Task.builder()
                .customerId(user.getUserId())
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .domain(request.getDomain())
                .pricingModel(request.getPricingModel())
                .status(TaskStatus.POSTED)
                .budget(request.getBudget())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress().trim())
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .scheduledAt(request.getScheduledAt())
                .build();

        task = taskRepository.save(task);

        log.info("Task created: {} by customer: {} domain: {}", task.getTaskId(), user.getUserId(), task.getDomain());

        return mapToResponse(task);
    }

    /**
     * Get task by ID (public)
     */
    public TaskResponse getTaskById(UUID taskId) {
        Task task = findTaskOrThrow(taskId);
        return mapToResponse(task);
    }

    /**
     * Update task (only POSTED/OPEN tasks, by task owner)
     */
    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request, AuthenticatedUser user) {
        Task task = findTaskOrThrow(taskId);
        validateTaskOwner(task, user);

        if (!task.canBeModified()) {
            throw new TaskExceptions.InvalidTaskStateException(
                    "Task can only be modified when in POSTED or OPEN status. Current: " + task.getStatus());
        }

        if (request.getTitle() != null) task.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) task.setDescription(request.getDescription().trim());
        if (request.getBudget() != null) task.setBudget(request.getBudget());
        if (request.getLatitude() != null) task.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) task.setLongitude(request.getLongitude());
        if (request.getAddress() != null) task.setAddress(request.getAddress().trim());
        if (request.getImages() != null) task.setImages(request.getImages());
        if (request.getScheduledAt() != null) task.setScheduledAt(request.getScheduledAt());

        task = taskRepository.save(task);
        log.info("Task updated: {}", taskId);

        return mapToResponse(task);
    }

    /**
     * Update task status with full lifecycle validation
     */
    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, UpdateTaskStatusRequest request, AuthenticatedUser user) {
        Task task = findTaskOrThrow(taskId);
        TaskStatus newStatus = request.getStatus();
        TaskStatus currentStatus = task.getStatus();

        // Validate the transition
        validateStatusTransition(task, newStatus, user);

        // Apply status-specific logic
        switch (newStatus) {
            case OPEN:
                // Only system or customer can open (move from POSTED to OPEN)
                if (!user.isCustomer() && !user.isAdmin()) {
                    throw new TaskExceptions.UnauthorizedTaskAccessException("Only task owner or admin can open tasks");
                }
                validateTaskOwnerOrAdmin(task, user);
                break;

            case ACCEPTED:
                // Worker accepts a fixed-price task directly
                if (user.isWorker() && task.getPricingModel() == PricingModel.FIXED) {
                    task.setAssignedWorkerId(user.getUserId());
                    task.setFinalPrice(task.getBudget()); // Fixed price = budget
                }
                break;

            case IN_PROGRESS:
                // Only assigned worker can start
                if (!user.getUserId().equals(task.getAssignedWorkerId())) {
                    throw new TaskExceptions.UnauthorizedTaskAccessException("Only the assigned worker can start this task");
                }
                break;

            case COMPLETED:
                // Worker marks as done
                if (!user.getUserId().equals(task.getAssignedWorkerId())) {
                    throw new TaskExceptions.UnauthorizedTaskAccessException("Only the assigned worker can complete this task");
                }
                task.setCompletedAt(LocalDateTime.now());
                break;

            case CANCELLED:
                if (!task.canBeCancelled()) {
                    throw new TaskExceptions.InvalidTaskStateException("Task cannot be cancelled in current state: " + currentStatus);
                }
                task.setCancellationReason(request.getReason());
                task.setCancelledBy(user.getUserId());
                break;

            case DISPUTED:
                if (currentStatus != TaskStatus.IN_PROGRESS && currentStatus != TaskStatus.COMPLETED) {
                    throw new TaskExceptions.InvalidTaskStateException("Disputes can only be raised for IN_PROGRESS or COMPLETED tasks");
                }
                task.setDisputeReason(request.getReason());
                break;

            case PAYMENT_DONE:
                // System triggers this after payment confirmation
                if (!user.isAdmin()) {
                    throw new TaskExceptions.UnauthorizedTaskAccessException("Only system/admin can mark payment as done");
                }
                break;

            case CLOSED:
                // System triggers after ratings are done
                if (!user.isAdmin()) {
                    throw new TaskExceptions.UnauthorizedTaskAccessException("Only system/admin can close tasks");
                }
                break;

            default:
                break;
        }

        task.setStatus(newStatus);
        task = taskRepository.save(task);

        log.info("Task {} status changed: {} -> {} by user: {}", taskId, currentStatus, newStatus, user.getUserId());

        return mapToResponse(task);
    }

    /**
     * Cancel a task
     */
    @Transactional
    public TaskResponse cancelTask(UUID taskId, String reason, AuthenticatedUser user) {
        Task task = findTaskOrThrow(taskId);

        // Customer can cancel own task, worker can cancel if assigned, admin can cancel any
        if (!user.isAdmin() && !user.getUserId().equals(task.getCustomerId()) &&
                !user.getUserId().equals(task.getAssignedWorkerId())) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("You cannot cancel this task");
        }

        if (!task.canBeCancelled()) {
            throw new TaskExceptions.InvalidTaskStateException("Task cannot be cancelled in status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.CANCELLED);
        task.setCancellationReason(reason);
        task.setCancelledBy(user.getUserId());
        task = taskRepository.save(task);

        log.info("Task {} cancelled by: {} reason: {}", taskId, user.getUserId(), reason);

        return mapToResponse(task);
    }

    /**
     * Search tasks with geo-location and filters
     */
    public List<TaskResponse> searchTasks(TaskSearchRequest request) {
        double radius = Math.min(request.getRadiusKm(), maxRadiusKm);

        List<Task> tasks;

        if (request.getLatitude() != null && request.getLongitude() != null) {
            // Geo search
            TaskStatus searchStatus = request.getStatus() != null ? request.getStatus() : TaskStatus.OPEN;

            if (request.getDomain() != null) {
                tasks = taskRepository.findNearbyTasksByStatusAndDomain(
                        request.getLatitude(), request.getLongitude(), radius, searchStatus, request.getDomain());
            } else {
                tasks = taskRepository.findNearbyTasksByStatus(
                        request.getLatitude(), request.getLongitude(), radius, searchStatus);
            }

            // Calculate distance for each task
            return tasks.stream()
                    .map(t -> {
                        TaskResponse response = mapToResponse(t);
                        response.setDistanceKm(calculateDistance(
                                request.getLatitude(), request.getLongitude(),
                                t.getLatitude(), t.getLongitude()));
                        return response;
                    })
                    .collect(Collectors.toList());
        } else {
            // Non-geo search with pagination
            Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                    ? Sort.by(request.getSortBy()).ascending()
                    : Sort.by(request.getSortBy()).descending();
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Page<Task> page = taskRepository.findWithFilters(
                    request.getStatus(), request.getDomain(), null, pageable);

            return page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        }
    }

    /**
     * Get current user's tasks (customer or worker)
     */
    public Page<TaskResponse> getMyTasks(AuthenticatedUser user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Task> tasks;
        if (user.isCustomer()) {
            tasks = taskRepository.findByCustomerId(user.getUserId(), pageable);
        } else if (user.isWorker()) {
            tasks = taskRepository.findByAssignedWorkerId(user.getUserId(), pageable);
        } else {
            // Admin sees all
            tasks = taskRepository.findAll(pageable);
        }

        return tasks.map(this::mapToResponse);
    }

    /**
     * Get task statistics (admin)
     */
    public TaskStatsResponse getTaskStats() {
        Map<String, Long> byDomain = new HashMap<>();
        for (TaskDomain domain : TaskDomain.values()) {
            byDomain.put(domain.name(), taskRepository.countByDomain(domain));
        }

        Map<String, Long> byStatus = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            byStatus.put(status.name(), taskRepository.countByStatus(status));
        }

        return TaskStatsResponse.builder()
                .totalTasks(taskRepository.count())
                .openTasks(taskRepository.countByStatus(TaskStatus.OPEN))
                .inProgressTasks(taskRepository.countByStatus(TaskStatus.IN_PROGRESS))
                .completedTasks(taskRepository.countByStatus(TaskStatus.COMPLETED))
                .cancelledTasks(taskRepository.countByStatus(TaskStatus.CANCELLED))
                .disputedTasks(taskRepository.countByStatus(TaskStatus.DISPUTED))
                .tasksByDomain(byDomain)
                .tasksByStatus(byStatus)
                .build();
    }

    // ===== Private Helpers =====

    private Task findTaskOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExceptions.TaskNotFoundException("Task not found: " + taskId));
    }

    private void validateTaskOwner(Task task, AuthenticatedUser user) {
        if (!user.getUserId().equals(task.getCustomerId()) && !user.isAdmin()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("You are not the owner of this task");
        }
    }

    private void validateTaskOwnerOrAdmin(Task task, AuthenticatedUser user) {
        if (!user.getUserId().equals(task.getCustomerId()) && !user.isAdmin()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("Access denied");
        }
    }

    private void validateStatusTransition(Task task, TaskStatus newStatus, AuthenticatedUser user) {
        TaskStatus current = task.getStatus();

        // Define valid transitions
        Map<TaskStatus, Set<TaskStatus>> validTransitions = Map.of(
                TaskStatus.POSTED, Set.of(TaskStatus.OPEN, TaskStatus.CANCELLED),
                TaskStatus.OPEN, Set.of(TaskStatus.ACCEPTED, TaskStatus.CANCELLED),
                TaskStatus.ACCEPTED, Set.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
                TaskStatus.IN_PROGRESS, Set.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED, TaskStatus.DISPUTED),
                TaskStatus.COMPLETED, Set.of(TaskStatus.PAYMENT_DONE, TaskStatus.DISPUTED),
                TaskStatus.PAYMENT_DONE, Set.of(TaskStatus.CLOSED),
                TaskStatus.DISPUTED, Set.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED, TaskStatus.CLOSED)
        );

        Set<TaskStatus> allowed = validTransitions.getOrDefault(current, Set.of());

        // Admin can force any transition
        if (!user.isAdmin() && !allowed.contains(newStatus)) {
            throw new TaskExceptions.InvalidTaskStateException(
                    String.format("Cannot transition from %s to %s. Allowed: %s", current, newStatus, allowed));
        }
    }

    private TaskResponse mapToResponse(Task task) {
        long bidCount = bidRepository.countByTask_TaskId(task.getTaskId());

        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .customerId(task.getCustomerId())
                .title(task.getTitle())
                .description(task.getDescription())
                .domain(task.getDomain())
                .pricingModel(task.getPricingModel())
                .status(task.getStatus())
                .budget(task.getBudget())
                .finalPrice(task.getFinalPrice())
                .latitude(task.getLatitude())
                .longitude(task.getLongitude())
                .address(task.getAddress())
                .images(task.getImages())
                .assignedWorkerId(task.getAssignedWorkerId())
                .scheduledAt(task.getScheduledAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .cancellationReason(task.getCancellationReason())
                .disputeReason(task.getDisputeReason())
                .bidCount((int) bidCount)
                .build();
    }

    /**
     * Haversine formula - calculate distance between two points in km
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0; // Round to 2 decimals
    }
}
