package com.rewards.points_service.mapper;

import com.rewards.points_service.dto.PointsDTO;
import com.rewards.points_service.entity.Points;
import java.util.Map;
 
public class PointsMapper {
 
   
    public static PointsDTO toDTO(Points points, Object userObj) {
        String studentName = null;
        String profilePic = null;
 
        if (userObj instanceof Map) { // Because RestTemplate returns LinkedHashMap
            Map<?, ?> userMap = (Map<?, ?>) userObj;
            studentName = (String) userMap.get("name"); // Assuming JSON field is 'name'
            profilePic = (String) userMap.get("profileImage"); // Assuming JSON field is 'image'
        }
 
        return PointsDTO.builder()
                .id(points.getId())
                .studentName(studentName)
                .profilePic(profilePic)
                .pointBalance(points.getPointBalance())
                .totalPoints(points.getTotalPoints())
                .totalSpent(points.getTotalSpent())
                .build();
    }
   
    public static Points toEntity(PointsDTO pointsDTO) {
        Points points = new Points();
        points.setPointBalance(pointsDTO.getPointBalance());
        points.setTotalPoints(pointsDTO.getTotalPoints());
        points.setTotalSpent(pointsDTO.getTotalSpent());
        return points;
    }
}