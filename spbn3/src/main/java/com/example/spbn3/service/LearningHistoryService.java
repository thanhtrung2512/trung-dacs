package com.example.spbn3.service;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.LearningHistoryRepository;
import com.example.spbn3.repository.StudentRepository;
import com.example.spbn3.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearningHistoryService {

    private final LearningHistoryRepository learningHistoryRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;

    public LearningHistoryService(LearningHistoryRepository learningHistoryRepository,
                                  StudentRepository studentRepository,
                                  TopicRepository topicRepository) {
        this.learningHistoryRepository = learningHistoryRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
    }

    /**
     * Lấy lịch sử học của student (Sắp xếp mới nhất lên đầu)
     */
    public List<LearningHistory> getStudentHistory(Long studentId) {
        // Sử dụng hàm đã sửa ở Repository để có dữ liệu chuẩn cho trang History
        return learningHistoryRepository.findByStudentIdOrderByViewedAtDesc(studentId);
    }

    /**
     * Lấy danh sách topicId đã học (Dùng để hiển thị tích xanh ở danh sách bài học)
     */
    public List<Long> getCompletedTopicIds(Long studentId) {
        return learningHistoryRepository.findByStudentIdOrderByViewedAtDesc(studentId)
                .stream()
                .map(h -> h.getTopic().getId())
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra topic đã học chưa (Tối ưu bằng cách gọi thẳng DB)
     */
    public boolean isTopicCompleted(Long studentId, Long topicId) {
        return learningHistoryRepository.existsByStudentIdAndTopicId(studentId, topicId);
    }

    /**
     * Lưu lịch sử học khi sinh viên nhấn nút "Hoàn thành"
     */
    @Transactional
    public void markTopicAsCompleted(Long studentId, Long topicId) {
        // 1. Kiểm tra tránh lưu trùng lặp dữ liệu
        if (learningHistoryRepository.existsByStudentIdAndTopicId(studentId, topicId)) {
            return;
        }

        // 2. Lấy thông tin Student và Topic
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên ID: " + studentId));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học ID: " + topicId));

        // 3. Tạo bản ghi lịch sử mới
        // viewedAt sẽ tự động gán là LocalDateTime.now() nhờ @PrePersist trong Entity
        LearningHistory history = new LearningHistory();
        history.setStudent(student);
        history.setTopic(topic);

        learningHistoryRepository.save(history);
    }
}