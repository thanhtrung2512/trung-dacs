package com.example.spbn3.service;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.repository.LearningHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningHistoryService {

    private final LearningHistoryRepository learningHistoryRepository;

    public LearningHistoryService(LearningHistoryRepository learningHistoryRepository) {
        this.learningHistoryRepository = learningHistoryRepository;
    }

    public void saveHistory(LearningHistory history) {
        learningHistoryRepository.save(history);
    }

    public List<LearningHistory> getHistoryByStudent(Long studentId) {
        return learningHistoryRepository.findByStudentId(studentId);
    }
}
