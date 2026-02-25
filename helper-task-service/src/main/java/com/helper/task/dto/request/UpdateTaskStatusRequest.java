package com.helper.task.dto.request;

import com.helper.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;

    private String reason; // For cancellation or dispute
}
