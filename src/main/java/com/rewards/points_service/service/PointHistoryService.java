package com.rewards.points_service.service;

import com.rewards.points_service.dto.PointHistoryDTO;
import com.rewards.points_service.entity.Event;
import com.rewards.points_service.entity.PointHistory;
import com.rewards.points_service.exception.ResourceNotFoundException;
import com.rewards.points_service.mapper.PointHistoryMapper;
import com.rewards.points_service.repository.EventRepository;
import com.rewards.points_service.repository.PointHistoryRepository;
import com.rewards.points_service.responsemodel.ResponseModel;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final EventRepository eventRepository;

    public PointHistoryService(PointHistoryRepository pointHistoryRepository,
                               EventRepository eventRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
        this.eventRepository = eventRepository;
    }

    private String getCurrentJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        throw new SecurityException("No JWT token found");
    }

    private boolean checkUserExists(UUID userId, String token) {
        RestTemplate restTemplate = new RestTemplate();
        String userServiceUrl = "http://localhost:8081/api/users/" + userId + "/exists";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ResponseModel> response = restTemplate.exchange(
                    userServiceUrl,
                    HttpMethod.GET,
                    entity,
                    ResponseModel.class
            );
            return response.getStatusCode() == HttpStatus.OK &&
                   response.getBody() != null &&
                   (boolean) response.getBody().getResponse();
        } catch (Exception e) {
            System.out.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public void recordPointHistory(UUID studentId, UUID eventId, int points) {
        String token = getCurrentJwtToken();

        if (!checkUserExists(studentId, token)) {
            throw new ResourceNotFoundException("Student not found or deleted with ID: " + studentId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        PointHistory pointHistory = PointHistory.builder()
                .studentId(studentId)
                .event(event)
                .pointsChanged(points)
                .build();

        pointHistoryRepository.save(pointHistory);
    }

    @Transactional
    public ResponseModel<List<PointHistoryDTO>> getPointHistoryByStudent(UUID studentId) {
        String token = getCurrentJwtToken();

        if (!checkUserExists(studentId, token)) {
            throw new ResourceNotFoundException("Student not found or deleted with ID: " + studentId);
        }

        List<PointHistory> pointHistories = pointHistoryRepository.findByStudentId(studentId);

        if (pointHistories.isEmpty()) {
            throw new ResourceNotFoundException("No point history found for student ID: " + studentId);
        }

        List<PointHistoryDTO> pointHistoryDTOs = pointHistories.stream()
                .map(PointHistoryMapper::toDTO)
                .toList();

        return ResponseModel.success(200, "Point history retrieved successfully", pointHistoryDTOs);
    }
}
