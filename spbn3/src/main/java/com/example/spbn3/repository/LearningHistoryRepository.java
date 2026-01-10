package com.example.spbn3.repository;

import com.example.spbn3.entity.LearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {
    List<LearningHistory> findByStudentId(Long studentId);
}
