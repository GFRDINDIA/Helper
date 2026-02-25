package com.helper.task.controller;

import com.helper.task.dto.request.*;
import com.helper.task.dto.response.ApiResponse;
import com.helper.task.dto.response.TaskResponse;
import com.helper.task.dto.response.TaskStatsResponse;
import com.helper.task.enums.PricingModel;
import com.helper.task.enums.TaskDomain;
import com.helper.task.enums.TaskStatus;
import com.helper.task.security.AuthenticatedUser;
import com.helper.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task creation, search, lifecycle management, and geo-based discovery")
public class TaskController {

    private final TaskService taskService;

    // ==================== TASK CRUD ====================

    @PostMapping
    @Operation(summary = "Create a new task", description = "Customers post a new task with domain, location, pricing model, and details",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        TaskResponse task = taskService.createTask(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task details", description = "Retrieve full details of a task by ID (public)")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable UUID taskId) {
        TaskResponse task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved", task));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update task", description = "Update task details (only POSTED/OPEN tasks, by owner)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        TaskResponse task = taskService.updateTask(taskId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Task updated", task));
    }

    // ==================== TASK STATUS ====================

    @PutMapping("/{taskId}/status")
    @Operation(summary = "Update task status", description = "Move task through lifecycle: POSTED→OPEN→ACCEPTED→IN_PROGRESS→COMPLETED→PAYMENT_DONE→CLOSED",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        TaskResponse task = taskService.updateTaskStatus(taskId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Task status updated to " + request.getStatus(), task));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Cancel a task", description = "Cancel a task with reason (owner, assigned worker, or admin)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TaskResponse>> cancelTask(
            @PathVariable UUID taskId,
            @RequestParam(required = false, defaultValue = "Cancelled by user") String reason,
            @AuthenticationPrincipal AuthenticatedUser user) {
        TaskResponse task = taskService.cancelTask(taskId, reason, user);
        return ResponseEntity.ok(ApiResponse.success("Task cancelled", task));
    }

    // ==================== TASK SEARCH ====================

    @GetMapping
    @Operation(summary = "Search tasks", description = "Search tasks by location (lat/lng/radius), domain, status. Geo-search returns results sorted by distance.")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> searchTasks(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "10") Double radius,
            @RequestParam(required = false) TaskDomain domain,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) PricingModel pricingModel,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {

        TaskSearchRequest request = TaskSearchRequest.builder()
                .latitude(lat).longitude(lng).radiusKm(radius)
                .domain(domain).status(status).pricingModel(pricingModel)
                .page(page).size(size).sortBy(sortBy).sortDir(sortDir)
                .build();

        List<TaskResponse> tasks = taskService.searchTasks(request);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved", tasks));
    }

    @GetMapping("/my-tasks")
    @Operation(summary = "Get my tasks", description = "Customers see posted tasks; Workers see assigned tasks",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        Page<TaskResponse> tasks = taskService.getMyTasks(user, page, size);
        return ResponseEntity.ok(ApiResponse.paged(
                "My tasks retrieved", tasks.getContent(),
                page, size, tasks.getTotalElements(), tasks.getTotalPages()));
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get task statistics", description = "Platform-wide task statistics by domain and status",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TaskStatsResponse>> getStats() {
        TaskStatsResponse stats = taskService.getTaskStats();
        return ResponseEntity.ok(ApiResponse.success("Task statistics", stats));
    }
}
