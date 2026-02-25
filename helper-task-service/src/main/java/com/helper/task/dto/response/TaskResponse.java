package com.helper.task.dto.response;

import com.helper.task.enums.PricingModel;
import com.helper.task.enums.TaskDomain;
import com.helper.task.enums.TaskStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID taskId;
    private UUID customerId;
    private String title;
    private String description;
    private TaskDomain domain;
    private PricingModel pricingModel;
    private TaskStatus status;
    private BigDecimal budget;
    private BigDecimal finalPrice;
    private Double latitude;
    private Double longitude;
    private String address;
    private List<String> images;
    private UUID assignedWorkerId;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String cancellationReason;
    private String disputeReason;
    private int bidCount;
    private Double distanceKm; // Calculated field for geo searches
}
