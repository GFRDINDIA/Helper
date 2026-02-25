package com.helper.rating.entity;

import com.helper.rating.enums.FlagReason;
import com.helper.rating.enums.FlagStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "flags", indexes = {
        @Index(name = "idx_flag_status", columnList = "status, created_at"),
        @Index(name = "idx_flag_reported", columnList = "reported_user_id"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Flag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "flag_id")
    private UUID flagId;

    @Column(name = "rating_id")
    private UUID ratingId; // nullable — can flag without a rating (flag the task/person directly)

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId; // Who flagged

    @Column(name = "reported_user_id", nullable = false)
    private UUID reportedUserId; // Who is being flagged

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FlagReason reason;

    @Column(columnDefinition = "TEXT")
    private String description; // Reporter's explanation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FlagStatus status = FlagStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
