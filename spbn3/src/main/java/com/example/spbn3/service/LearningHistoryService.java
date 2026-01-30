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

    // Constructor Injection (Khuyên dùng)
    public LearningHistoryService(LearningHistoryRepository learningHistoryRepository,
                                  StudentRepository studentRepository,
                                  TopicRepository topicRepository) {
        this.learningHistoryRepository = learningHistoryRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
    }

    // =========================================================================
    // 🟢 PHẦN 1: DÀNH CHO ADMIN (QUẢN LÝ & TÌM KIẾM)
    // =========================================================================

    /**
     * Admin: Lấy toàn bộ lịch sử hệ thống (Mới nhất lên đầu)
     */
    public List<LearningHistory> getAllHistory() {
        return learningHistoryRepository.findAllByOrderByViewedAtDesc();
    }

    /**
     * Admin: Tìm kiếm lịch sử theo từ khóa (Tên hoặc Mã SV)
     */
    public List<LearningHistory> searchHistory(String keyword) {
        return learningHistoryRepository.searchByKeyword(keyword);
    }

    // =========================================================================
    // 🟢 PHẦN 2: DÀNH CHO SINH VIÊN (HỌC TẬP & TIẾN ĐỘ)
    // =========================================================================

    /**
     * Student: Lấy lịch sử học của cá nhân (Sắp xếp mới nhất lên đầu)
     */
    public List<LearningHistory> getStudentHistory(Long studentId) {
        return learningHistoryRepository.findByStudentIdOrderByViewedAtDesc(studentId);
    }

    /**
     * Student: Lấy danh sách ID các bài đã học (Để hiển thị tích xanh ✅)
     */
    public List<Long> getCompletedTopicIds(Long studentId) {
        return learningHistoryRepository.findByStudentIdOrderByViewedAtDesc(studentId)
                .stream()
                .map(h -> h.getTopic().getId())
                .collect(Collectors.toList());
    }

    /**
     * Student: Lấy danh sách lịch sử trong 1 môn cụ thể (Dùng cho sidebar trang học)
     */
    public List<LearningHistory> getByStudentAndSubject(Long studentId, Long subjectId) {
        // Hàm này cần được hỗ trợ bởi Repository (findByStudentIdAndSubjectId)
        return learningHistoryRepository.findByStudentIdAndSubjectId(studentId, subjectId);
    }

    /**
     * Student: Kiểm tra 1 bài học cụ thể đã học chưa
     */
    public boolean hasLearned(Long studentId, Long topicId) {
        return learningHistoryRepository.existsByStudentIdAndTopicId(studentId, topicId);
    }

    /**
     * Student: Lưu lịch sử học khi nhấn nút "Hoàn thành"
     */
    @Transactional
    public void markTopicAsCompleted(Long studentId, Long topicId) {
        // 1. Kiểm tra tránh lưu trùng lặp
        if (learningHistoryRepository.existsByStudentIdAndTopicId(studentId, topicId)) {
            return;
        }

        // 2. Lấy thông tin Student và Topic (Check null an toàn)
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên ID: " + studentId));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học ID: " + topicId));

        // 3. Tạo bản ghi mới (viewedAt tự động gán bởi @PrePersist trong Entity)
        LearningHistory history = new LearningHistory();
        history.setStudent(student);
        history.setTopic(topic);

        learningHistoryRepository.save(history);
    }
    
    /**
     * Lưu thủ công 1 đối tượng LearningHistory (Dùng cho các trường hợp đặc biệt)
     */
    public void save(LearningHistory history) {
        learningHistoryRepository.save(history);
    }
}