package com.helper.notification.dto.request;

import com.helper.notification.enums.NotificationEvent;
import com.helper.notification.enums.NotificationPriority;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendNotificationRequest {

    @NotNull(message = "At least one recipient required")
    @Size(min = 1, message = "At least one recipient required")
    private List<UUID> userIds;

    @NotNull(message = "Event type is required")
    private NotificationEvent event;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 5000)
    private String body;

    private Map<String, String> data; // taskId, paymentId, etc.

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
}
