package com.helper.notification.entity;

import com.helper.notification.enums.NotificationEvent;
import com.helper.notification.enums.NotificationPriority;
import com.helper.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user_status", columnList = "user_id, status, created_at"),
        @Index(name = "idx_notif_user_read", columnList = "user_id, is_read, created_at"),
        @Index(name = "idx_notif_event", columnList = "event"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID notificationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // Recipient

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationEvent event;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson; // JSON payload: taskId, paymentId, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Delivery tracking per channel
    @Column(name = "push_sent")
    @Builder.Default
    private Boolean pushSent = false;

    @Column(name = "sms_sent")
    @Builder.Default
    private Boolean smsSent = false;

    @Column(name = "email_sent")
    @Builder.Default
    private Boolean emailSent = false;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
