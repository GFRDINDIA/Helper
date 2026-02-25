package com.helper.user.entity;

import com.helper.user.enums.PricingModel;
import com.helper.user.enums.TaskDomain;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "worker_skills", indexes = {
        @Index(name = "idx_ws_worker", columnList = "worker_id"),
        @Index(name = "idx_ws_domain", columnList = "domain"),
        @Index(name = "idx_ws_location", columnList = "latitude, longitude"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_worker_domain", columnNames = {"worker_id", "domain"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "skill_id")
    private UUID skillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile workerProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_model", nullable = false, length = 20)
    private PricingModel priceModel;

    @Column(name = "fixed_rate", precision = 10, scale = 2)
    private BigDecimal fixedRate;

    // Geo location for this skill (can differ per domain)
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "service_radius_km")
    @Builder.Default
    private Integer serviceRadiusKm = 10;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;
}
