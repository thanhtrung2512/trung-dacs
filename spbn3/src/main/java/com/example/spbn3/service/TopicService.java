package com.example.spbn3.service;

import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    // 1. Lấy tất cả topic
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    // 2. Lấy topic theo ID
    public Topic getTopicById(Long id) {
        return topicRepository.findById(id).orElse(null);
    }

    // 3. Lấy danh sách topic theo môn học
    public List<Topic> getTopicsBySubjectId(Long subjectId) {
        return topicRepository.findBySubjectId(subjectId);
    }

    // 4. Tìm kiếm topic (Toàn bộ)
    public List<Topic> searchTopics(String keyword) {
        return topicRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // =======================================================
    // 🔥 HÀM SỬA LỖI: TÌM KIẾM TRONG MÔN HỌC (Đang bị thiếu)
    // =======================================================
    public List<Topic> searchTopicsInSubject(Long subjectId, String keyword) {
        // Gọi hàm repository tương ứng
        return topicRepository.findBySubjectIdAndTitleContainingIgnoreCase(subjectId, keyword);
    }
    
    // =======================================================
    // 5. LẤY BÀI TIẾP THEO (Logic fallback)
    // =======================================================
    public Topic getNextTopic(Long subjectId, Long currentTopicId) {
        // Gọi hàm findFirst... mới cập nhật trong Repository
        return topicRepository.findFirstBySubjectIdAndIdGreaterThanOrderByIdAsc(subjectId, currentTopicId);
    }
}