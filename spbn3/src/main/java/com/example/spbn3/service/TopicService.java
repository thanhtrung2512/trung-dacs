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

    // 5. Tìm kiếm trong môn học
    public List<Topic> searchTopicsInSubject(Long subjectId, String keyword) {
        return topicRepository.findBySubjectIdAndTitleContainingIgnoreCase(subjectId, keyword);
    }
    
    // 6. Lấy bài tiếp theo (Logic fallback)
    public Topic getNextTopic(Long subjectId, Long currentTopicId) {
        return topicRepository.findFirstBySubjectIdAndIdGreaterThanOrderByIdAsc(subjectId, currentTopicId);
    }

    // =======================================================
    // 🔥 BỔ SUNG CÁC HÀM CÒN THIẾU CHO ADMIN CONTROLLER
    // =======================================================

    // 7. Lưu bài học (Thêm mới hoặc Cập nhật)
    public void saveTopic(Topic topic) {
        topicRepository.save(topic);
    }

    // 8. Xóa bài học
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }
}