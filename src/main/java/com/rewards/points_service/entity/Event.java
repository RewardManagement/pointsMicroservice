package com.rewards.points_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // UUID reference to user (student)
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    // UUID reference to badge
    @Column(name = "badge_id")
    private UUID badgeId;

    // UUID reference to reward
    @Column(name = "reward_id")
    private UUID rewardId;

    // UUID reference to certificate
    @Column(name = "certificate_id")
    private UUID certificateId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
