package com.rewards.points_service.controller;

import com.rewards.points_service.entity.Event;
import com.rewards.points_service.responsemodel.ResponseModel;
import com.rewards.points_service.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ResponseModel<Event>> createEvent(
        @RequestParam UUID studentId,
        @RequestParam(required = false) UUID badgeId,
        @RequestParam(required = false) UUID certificateId,
        @RequestParam(required = false) UUID rewardId,
        @RequestParam Integer points) {

        // Call the service to create the event and receive the response model
        ResponseModel<Event> response = eventService.createEvent(studentId, badgeId, certificateId, rewardId, points);

        // Return the response with status 201 (Created)
        return ResponseEntity.status(201).body(response);
    }
}
