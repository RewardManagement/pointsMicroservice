package com.rewards.points_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryDTO {
    private UUID id;
    private String fileName;
    private Integer pointsChanged;  
    private LocalDateTime createdAt;  
    private Boolean isReward;
    private UUID studentId;  
    private UUID eventId;
}
