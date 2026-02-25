package com.helper.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_notification_preferences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserNotificationPreference {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    @Builder.Default
    private Boolean smsEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;

    // Quiet hours
    @Column(name = "quiet_hours_enabled", nullable = false)
    @Builder.Default
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_start_hour")
    @Builder.Default
    private Integer quietStartHour = 22; // 10 PM

    @Column(name = "quiet_end_hour")
    @Builder.Default
    private Integer quietEndHour = 7; // 7 AM

    @Column(name = "promotional_enabled", nullable = false)
    @Builder.Default
    private Boolean promotionalEnabled = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
