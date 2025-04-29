package com.rewards.points_service.repository;

import com.rewards.points_service.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, UUID> {
    List<PointHistory> findByStudentId(UUID studentId);
}

