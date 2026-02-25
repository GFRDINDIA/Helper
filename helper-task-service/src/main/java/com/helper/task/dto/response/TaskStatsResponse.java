package com.helper.task.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatsResponse {

    private long totalTasks;
    private long openTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long cancelledTasks;
    private long disputedTasks;
    private Map<String, Long> tasksByDomain;
    private Map<String, Long> tasksByStatus;
}
