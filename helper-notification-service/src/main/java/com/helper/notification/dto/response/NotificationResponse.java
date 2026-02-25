package com.helper.notification.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private UUID notificationId;
    private UUID userId;
    private String event;
    private String title;
    private String body;
    private String dataJson;
    private String priority;
    private String status;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Boolean pushSent;
    private Boolean smsSent;
    private Boolean emailSent;
    private LocalDateTime createdAt;
}
