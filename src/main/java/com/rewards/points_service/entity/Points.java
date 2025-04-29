package com.rewards.points_service.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
import java.util.UUID;
 
@Entity
@Table(name = "points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Points {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;  
 
    @Column(name = "student_id", nullable = false)
    private UUID studentId;
 
    @Column(name = "point_balance", nullable = true)
    private Integer pointBalance;  
 
    @Column(name = "total_points", nullable = true)
    private Integer totalPoints;
 
    @Column(name = "total_spent", nullable = true)
    private Integer totalSpent;  
 
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;  
 
    @PrePersist
    @PreUpdate
    public void setTimestamps() {
        this.updatedAt = LocalDateTime.now();
    }
}
