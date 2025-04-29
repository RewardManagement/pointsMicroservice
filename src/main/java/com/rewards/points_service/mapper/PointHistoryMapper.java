package com.rewards.points_service.mapper;

import com.rewards.points_service.dto.PointHistoryDTO;
import com.rewards.points_service.entity.PointHistory;
import org.springframework.stereotype.Component;

@Component
public class PointHistoryMapper {
    public static PointHistoryDTO toDTO(PointHistory pointHistory) {
        String fileName = "Unknown";

        if (pointHistory.getEvent().getCertificateId() != null) {
            fileName = "Certificate"; // or you can later fetch the real file name if you want
        } else if (pointHistory.getEvent().getBadgeId() != null) {
            fileName = "Badge"; // simple label
        } else if (pointHistory.getEvent().getRewardId() != null) {
            fileName = "Reward"; // simple label
        }

        return PointHistoryDTO.builder()
                .id(pointHistory.getId())
                .fileName(fileName)
                .pointsChanged(pointHistory.getPointsChanged())
                .createdAt(pointHistory.getCreatedAt())
                .isReward(pointHistory.getEvent().getRewardId() != null)
                .studentId(pointHistory.getStudentId()) 
                .eventId(pointHistory.getEvent().getId())
                .build();
    }
}
