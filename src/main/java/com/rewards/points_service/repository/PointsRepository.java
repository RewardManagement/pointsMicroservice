package com.rewards.points_service.repository;

import com.rewards.points_service.entity.Points;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.UUID;
 
@Repository
public interface PointsRepository extends JpaRepository<Points, UUID> {
 
 
    @Query(value = """
        SELECT p.* FROM points p
        JOIN users s ON p.student_id = s.id
        WHERE s.is_deleted = false
    """, nativeQuery = true)
    List<Points> findAllValidPoints();
    
 
    @Query(value = """
        SELECT p.* FROM points p
        JOIN users s ON p.student_id = s.id
        WHERE s.id = :studentId AND s.is_deleted = false
    """, nativeQuery = true)
    List<Points> findByStudentId(@Param("studentId") UUID studentId);
 
    @Query(value = """
        SELECT p.* FROM points p
        JOIN users s ON p.student_id = s.id
        WHERE s.teacher_id = :teacherId AND s.is_deleted = false
    """, nativeQuery = true)
    List<Points> findByTeacherId(@Param("teacherId") UUID teacherId);
 
 
 
 
}
