package com.helper.rating.entity;

import com.helper.rating.enums.RatingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ratings", indexes = {
        @Index(name = "idx_rating_given_to", columnList = "given_to, created_at"),
        @Index(name = "idx_rating_given_by", columnList = "given_by"),
        @Index(name = "idx_rating_task", columnList = "task_id"),
}, uniqueConstraints = {
        // One rating per direction per task (customer→worker AND worker→customer allowed)
        @UniqueConstraint(name = "uk_rating_task_by_to", columnNames = {"task_id", "given_by", "given_to"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id")
    private UUID ratingId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "given_by", nullable = false)
    private UUID givenBy;

    @Column(name = "given_to", nullable = false)
    private UUID givenTo;

    @Column(nullable = false)
    private Integer score; // 1-5 stars

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating_type", nullable = false, length = 25)
    private RatingType ratingType;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true; // Admin can hide inappropriate ratings

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
