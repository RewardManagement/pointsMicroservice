package com.rewards.points_service.repository;
 
import com.rewards.points_service.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
 
public interface EventRepository extends JpaRepository<Event, UUID> {
}
