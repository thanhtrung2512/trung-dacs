package com.example.spbn3.repository;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.entity.Subject; 
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {

    // =========================================================================
    // 🔥 PHẦN 1: DÀNH CHO ADMIN (QUẢN LÝ & TÌM KIẾM)
    // =========================================================================

    // 1. Admin: Lấy toàn bộ lịch sử (Mới nhất lên đầu)
    List<LearningHistory> findAllByOrderByViewedAtDesc();

    // 2. Admin: Tìm kiếm theo Tên hoặc Mã sinh viên
    @Query("SELECT h FROM LearningHistory h WHERE " +
           "LOWER(h.student.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.student.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY h.viewedAt DESC")
    List<LearningHistory> searchByKeyword(@Param("keyword") String keyword);


    // =========================================================================
    // 🔥 PHẦN 2: DÀNH CHO SINH VIÊN (FOCUS MODE & TOPIC DETAIL)
    // =========================================================================

    // Kiểm tra đã học chưa
    boolean existsByStudentIdAndTopicId(Long studentId, Long topicId);

    // Lấy danh sách ID các bài đã học TRONG MỘT MÔN CỤ THỂ
    @Query("SELECT h.topic.id FROM LearningHistory h WHERE h.student.id = :studentId AND h.topic.subject.id = :subjectId")
    List<Long> findCompletedTopicIdsByStudentAndSubject(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    // Đếm số bài đã học TRONG MỘT MÔN CỤ THỂ
    @Query("SELECT COUNT(h) FROM LearningHistory h WHERE h.student.id = :studentId AND h.topic.subject.id = :subjectId")
    long countCompletedBySubject(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);


    // =========================================================================
    // 🔥 PHẦN 3: DASHBOARD & THỐNG KÊ
    // =========================================================================

    // Lấy tất cả lịch sử của SV
    List<LearningHistory> findByStudentId(Long studentId);

    // Lấy lịch sử sắp xếp mới nhất
    List<LearningHistory> findByStudentIdOrderByViewedAtDesc(Long studentId);

    // Lấy danh sách TOÀN BỘ ID bài đã học (Để lọc gợi ý AI)
    @Query("SELECT lh.topic.id FROM LearningHistory lh WHERE lh.student.id = :studentId")
    List<Long> findCompletedTopicIds(@Param("studentId") Long studentId);

    // Lấy bài học gần đây nhất (Hỗ trợ Dashboard Resume)
    @Query("SELECT lh FROM LearningHistory lh WHERE lh.student.id = :studentId ORDER BY lh.viewedAt DESC")
    List<LearningHistory> findRecentHistory(@Param("studentId") Long studentId, Pageable pageable);

    // Lấy lịch sử chi tiết trong một môn
    @Query("SELECT h FROM LearningHistory h WHERE h.student.id = :studentId AND h.topic.subject.id = :subjectId")
    List<LearningHistory> findByStudentIdAndSubjectId(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    // Đếm số bài đã học theo ngành (Fix lỗi 0% Progress)
    @Query("SELECT COUNT(DISTINCT lh.topic.id) FROM LearningHistory lh " +
           "JOIN lh.topic t JOIN t.subject s " +
           "WHERE lh.student.id = :studentId AND s.targetMajor LIKE %:major%")
    long countCompletedTopicsInMajor(@Param("studentId") Long studentId, @Param("major") String major);

    // Lấy các ngày đã học để tính Streak
    @Query(value = "SELECT DISTINCT DATE(viewed_at) FROM learning_histories " +
           "WHERE student_id = :studentId ORDER BY DATE(viewed_at) DESC", nativeQuery = true)
    List<java.sql.Date> findDistinctLearningDates(@Param("studentId") Long studentId);

    // Lấy bài mới học nhất (Dự phòng)
    @Query(value = "SELECT * FROM learning_histories WHERE student_id = :studentId " +
           "ORDER BY viewed_at DESC LIMIT 1", nativeQuery = true)
    LearningHistory findLatestHistory(@Param("studentId") Long studentId);

    // Đếm tổng số bài đã học của SV
    long countByStudentId(Long studentId);

    // Lấy danh sách các MÔN HỌC mà sinh viên đã từng xem
    @Query("SELECT DISTINCT h.topic.subject FROM LearningHistory h WHERE h.student.id = :studentId")
    List<Subject> findLearnedSubjects(@Param("studentId") Long studentId);
}