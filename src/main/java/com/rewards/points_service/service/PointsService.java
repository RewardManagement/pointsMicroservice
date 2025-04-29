package com.rewards.points_service.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.rewards.points_service.responsemodel.ResponseModel;
import com.rewards.points_service.mapper.PointsMapper;
import com.rewards.points_service.dto.PointsDTO;
import com.rewards.points_service.entity.Points;
import com.rewards.points_service.repository.PointsRepository;
import com.rewards.points_service.exception.ResourceNotFoundException;
import com.rewards.points_service.exception.BadRequestException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
public class PointsService {
 
    private final PointsRepository pointsRepository;
 
    public PointsService(PointsRepository pointsRepository) {
        this.pointsRepository = pointsRepository;
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
        String userServiceUrl = "http://localhost:8081/api/users/" + userId + "/exists";  // User microservice endpoint
 
        // Set up HTTP headers with JWT token for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // Assuming you're passing JWT token for authentication
        HttpEntity<String> entity = new HttpEntity<>(headers);
 
        try {
            // Make the GET request to check if the user exists and is not deleted
            ResponseEntity<ResponseModel> response = restTemplate.exchange(
                    userServiceUrl,
                    HttpMethod.GET,
                    entity,
                    ResponseModel.class
            );
 
            return response.getStatusCode() == HttpStatus.OK && (boolean) response.getBody().getResponse(); // If user exists and is not deleted
        } catch (Exception e) {
            // Log the error
            System.out.println("Error checking user: " + e.getMessage());
            return false;
        }
    }
 
    private Object findUserById(UUID userId, String token) {
        RestTemplate restTemplate = new RestTemplate();
        String userServiceUrl = "http://localhost:8081/api/users?userId=" + userId; // <-- notice the ?userId=...
    
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
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody().getResponse() != null) {
                return response.getBody().getResponse(); // user data
            } else {
                return null; // user not found
            }
        } catch (Exception e) {
            System.out.println("Error fetching user: " + e.getMessage());
            return null;
        }
    }
    
 
    @Transactional
public ResponseModel<?> getPoints(UUID studentId, UUID teacherId) {
    if (studentId != null) {
        if (!checkUserExists(studentId, getCurrentJwtToken())) {
            throw new ResourceNotFoundException("Student not found or has been deleted: " + studentId);
        }
 
        List<Points> pointsList = pointsRepository.findByStudentId(studentId);
        if (pointsList.isEmpty()) {
            throw new ResourceNotFoundException("No points found for student ID: " + studentId);
        }
        Object studentUserObj = findUserById(studentId, getCurrentJwtToken()); // fetch student data once
 
        List<PointsDTO> pointsDTOList = pointsList.stream()
                .map(points -> PointsMapper.toDTO(points, studentUserObj)) // pass user object also
                .collect(Collectors.toList());
 
        return new ResponseModel<>(200, "SUCCESS", "Student points retrieved successfully.", pointsDTOList);
    }
    else if (teacherId != null) {
        if (!checkUserExists(teacherId, getCurrentJwtToken())) {
            throw new ResourceNotFoundException("Teacher not found or has been deleted: " + teacherId);
        }
 
        List<Points> pointsList = pointsRepository.findByTeacherId(teacherId);
        if (pointsList.isEmpty()) {
            throw new ResourceNotFoundException("No students found under teacher ID: " + teacherId);
        }
 
        List<PointsDTO> pointsDTOList = pointsList.stream()
                .map(points -> {
                    Object studentUserObj = findUserById(points.getStudentId(), getCurrentJwtToken()); // fetch each student
                    return PointsMapper.toDTO(points, studentUserObj);
                })
                .collect(Collectors.toList());
 
        return new ResponseModel<>(200, "SUCCESS", "Points for all students under teacher retrieved successfully.", pointsDTOList);
    }
    else {
        List<Points> pointsList = pointsRepository.findAllValidPoints();
        if (pointsList.isEmpty()) {
            throw new ResourceNotFoundException("No points records found.");
        }
 
        List<PointsDTO> pointsDTOList = pointsList.stream()
                .map(points -> {
                    Object studentUserObj = findUserById(points.getStudentId(), getCurrentJwtToken()); // fetch each student
                    return PointsMapper.toDTO(points, studentUserObj);
                })
                .collect(Collectors.toList());
 
        return new ResponseModel<>(200, "SUCCESS", "All students' points retrieved successfully.", pointsDTOList);
    }
}
 
 
    @Transactional
    public ResponseModel<PointsDTO> updateStudentPoints(UUID studentId, int pointsToAdd, int pointsToSpend) {
        if (!checkUserExists(studentId, getCurrentJwtToken())) {
            throw new ResourceNotFoundException("Student not found or has been deleted: " + studentId);
        }
 
        Points points = pointsRepository.findByStudentId(studentId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Points record not found for this student."));
 
        int newBalance = points.getPointBalance() + pointsToAdd - pointsToSpend;
        if (newBalance < 0) {
            throw new BadRequestException("Insufficient point balance.");
        }
 
        points.setPointBalance(newBalance);
        points.setTotalPoints(points.getTotalPoints() + pointsToAdd);
        points.setTotalSpent(points.getTotalSpent() + pointsToSpend);
        pointsRepository.save(points);
        Object studentUserObj = findUserById(studentId, getCurrentJwtToken());
 
        // Map to DTO using new method
        PointsDTO pointsDTO = PointsMapper.toDTO(points, studentUserObj);
 
        return new ResponseModel<>(200, "SUCCESS", "Student points updated successfully.", pointsDTO);
    }
 
    @Transactional
    public ResponseModel<String> createPoints(UUID studentId, String token) {
        // if (!checkUserExists(studentId, getCurrentJwtToken())) {
        //     throw new ResourceNotFoundException("Student not found or has been deleted: " + studentId);
        // }
        PointsDTO pointsDTO = PointsDTO.builder()
                .pointBalance(0)
                .totalPoints(0)
                .totalSpent(0)
                .build();
 
        Points points = PointsMapper.toEntity(pointsDTO);
        points.setStudentId(studentId);
 
        pointsRepository.save(points);
 
        return new ResponseModel<>(201, "SUCCESS", "Points initialized for student", null);
    }
 
    
 
 
 
}
