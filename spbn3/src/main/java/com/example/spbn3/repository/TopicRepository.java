package com.example.spbn3.repository;

import com.example.spbn3.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findBySubjectId(Long subjectId);
    List<Topic> findByTitleContainingIgnoreCase(String keyword);
}
