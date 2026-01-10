package com.example.spbn3.recommend;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.LearningHistoryRepository;
import com.example.spbn3.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final LearningHistoryRepository historyRepository;
    private final TopicRepository topicRepository;

    public RecommendationService(LearningHistoryRepository historyRepository,
                                 TopicRepository topicRepository) {
        this.historyRepository = historyRepository;
        this.topicRepository = topicRepository;
    }

    /**
     * Gợi ý topic dựa trên lịch sử học tập của sinh viên
     */
    public List<Topic> recommendTopics(Student student, int limit) {
        if(student == null) return List.of();

        // 1. Lấy lịch sử học tập
        List<LearningHistory> histories = historyRepository.findByStudentId(student.getId());

        // 2. Topic đã học
        Set<Long> learnedTopicIds = histories.stream()
                .map(h -> h.getTopic().getId())
                .collect(Collectors.toSet());

        // 3. Subject của các topic đã học
        Set<Long> subjectIds = histories.stream()
                .map(h -> h.getTopic().getSubject().getId())
                .collect(Collectors.toSet());

        // 4. Lọc topic cùng subject, chưa học
        List<Topic> recommended = topicRepository.findAll().stream()
                .filter(t -> subjectIds.contains(t.getSubject().getId()))
                .filter(t -> !learnedTopicIds.contains(t.getId()))
                .limit(limit)
                .collect(Collectors.toList());

        return recommended;
    }

    /**
     * Gợi ý topic phổ biến nhất trên hệ thống
     */
    public List<Topic> recommendPopularTopics(int limit) {
        // 1. Đếm lượt xem topic
        Map<Long, Long> topicCount = new HashMap<>();
        historyRepository.findAll().forEach(h -> {
            Long topicId = h.getTopic().getId();
            topicCount.put(topicId, topicCount.getOrDefault(topicId, 0L) + 1);
        });

        // 2. Sắp xếp giảm dần
        List<Long> sortedTopicIds = topicCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 3. Lấy top topic
        List<Topic> topTopics = sortedTopicIds.stream()
                .map(id -> topicRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .limit(limit)
                .collect(Collectors.toList());

        return topTopics;
    }
}
