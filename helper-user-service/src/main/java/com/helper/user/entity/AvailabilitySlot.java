package com.helper.user.entity;

import com.helper.user.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "availability_slots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_worker_day", columnNames = {"worker_id", "day_of_week"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_id")
    private UUID slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile workerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;
}
