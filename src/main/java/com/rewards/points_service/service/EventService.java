package com.rewards.points_service.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rewards.points_service.entity.Event;
import com.rewards.points_service.repository.EventRepository;
import com.rewards.points_service.responsemodel.ResponseModel;
import com.rewards.points_service.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final PointHistoryService pointHistoryService;
    private final RestTemplate restTemplate;

    public EventService(EventRepository eventRepository, PointHistoryService pointHistoryService) {
        this.eventRepository = eventRepository;
        this.pointHistoryService = pointHistoryService;
        this.restTemplate = new RestTemplate(); // instantiate directly
    }

    private String getCurrentJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        throw new SecurityException("No JWT token found");
    }

    private boolean checkUserExists(UUID userId, String token) {
        String userServiceUrl = "http://localhost:8081/api/users/" + userId + "/exists"; // Same as your PointsService

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

            return response.getStatusCode() == HttpStatus.OK && (boolean) response.getBody().getResponse();
        } catch (Exception e) {
            System.out.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public ResponseModel<Event> createEvent(UUID studentId, UUID badgeId, UUID certificateId, UUID rewardId, Integer points) {

        if (studentId == null) {
            throw new ResourceNotFoundException("Student ID must be provided.");
        }
        if (points == null) {
            throw new ResourceNotFoundException("Points must be provided.");
        }

        // Check if student exists using the new checkUserExists method
        if (!checkUserExists(studentId, getCurrentJwtToken())) {
            throw new ResourceNotFoundException("Student not found or has been deleted.");
        }

        // Validate badge
        if (badgeId != null) {
            String badgeServiceUrl = "http://reward-service/badges/" + badgeId;
            ResponseEntity<Object> badgeResponse = restTemplate.getForEntity(badgeServiceUrl, Object.class);
            if (!badgeResponse.getStatusCode().is2xxSuccessful() || badgeResponse.getBody() == null) {
                throw new ResourceNotFoundException("Badge not found from Reward Service.");
            }
        }

        // Validate certificate
        if (certificateId != null) {
            String certificateServiceUrl = "http://reward-service/certificates/" + certificateId;
            ResponseEntity<Object> certificateResponse = restTemplate.getForEntity(certificateServiceUrl, Object.class);
            if (!certificateResponse.getStatusCode().is2xxSuccessful() || certificateResponse.getBody() == null) {
                throw new ResourceNotFoundException("Certificate not found from Reward Service.");
            }
        }

        // Validate reward
        if (rewardId != null) {
            String rewardServiceUrl = "http://reward-service/rewards/" + rewardId;
            ResponseEntity<Object> rewardResponse = restTemplate.getForEntity(rewardServiceUrl, Object.class);
            if (!rewardResponse.getStatusCode().is2xxSuccessful() || rewardResponse.getBody() == null) {
                throw new ResourceNotFoundException("Reward not found from Reward Service.");
            }
        }

        // Create the event after validations
        Event event = Event.builder()
                .studentId(studentId)
                .badgeId(badgeId)
                .certificateId(certificateId)
                .rewardId(rewardId)
                .build();

        event = eventRepository.save(event);

        // Record points
        pointHistoryService.recordPointHistory(studentId, event.getId(), points);

        return ResponseModel.success(201, "Event created successfully", event);
    }
}
