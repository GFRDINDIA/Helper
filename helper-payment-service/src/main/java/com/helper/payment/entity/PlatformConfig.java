package com.helper.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "platform_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatformConfig {

    @Id
    @Column(name = "config_key", length = 50)
    private String configKey; // COMMISSION_RATE, GST_RATE, CANCELLATION_FEE_RATE

    @Column(name = "config_value", nullable = false, length = 100)
    private String configValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Convenience
    public static final String COMMISSION_RATE = "COMMISSION_RATE";
    public static final String GST_RATE = "GST_RATE";
    public static final String CANCELLATION_FEE_RATE = "CANCELLATION_FEE_RATE";
}
