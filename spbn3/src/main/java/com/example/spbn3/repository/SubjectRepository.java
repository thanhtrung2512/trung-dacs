package com.example.spbn3.repository;

import com.example.spbn3.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    // ==========================================
    // PHẦN 1: CÁC HÀM TÌM KIẾM CƠ BẢN (SEARCH & FILTER)
    // ==========================================
    
    // 1. Tìm kiếm theo tên (Search Bar)
    List<Subject> findByNameContainingIgnoreCase(String keyword);

    // 2. Tìm kiếm bắt đầu bằng
    List<Subject> findByNameStartingWithIgnoreCase(String prefix);

    // 3. Tìm theo chuyên ngành chính xác
    List<Subject> findByTargetMajor(String major);

    // 4. Tìm theo mã môn
    Subject findBySubjectCode(String subjectCode);

    // ==========================================
    // PHẦN 2: CÁC HÀM NÂNG CAO CHO AI & GỢI Ý
    // ==========================================

    /**
     * 🚀 QUERY 1: GỢI Ý ĐÚNG LỘ TRÌNH (Đã Fix lỗi SQL + Ẩn môn rỗng)
     * - Sửa lỗi: Dùng CONCAT('%', :major, '%') để tìm kiếm đúng.
     * - Tối ưu: Dùng JOIN topics (thay vì LEFT JOIN) để chỉ hiện môn ĐÃ CÓ BÀI HỌC.
     * - Sắp xếp: Ưu tiên Semester nhỏ trước (Kỳ 1 -> Kỳ 2...).
     */
    @Query(value = """
        SELECT s.*, COUNT(lh.id) as total_views 
        FROM subjects s
        JOIN topics t ON s.id = t.subject_id        -- CHỈ LẤY MÔN CÓ BÀI HỌC
        LEFT JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE s.target_major LIKE CONCAT('%', :major, '%')  -- FIX LỖI LIKE
        AND s.id NOT IN (
            SELECT DISTINCT t2.subject_id 
            FROM learning_histories lh2 
            JOIN topics t2 ON lh2.topic_id = t2.id 
            WHERE lh2.student_id = :studentId
        )
        GROUP BY s.id
        ORDER BY s.semester ASC, s.id ASC          -- SẮP XẾP CUỐN CHIẾU THEO KỲ
        LIMIT :limit
    """, nativeQuery = true)
    List<Subject> findRecommendedSubjects(@Param("studentId") Long studentId, 
                                          @Param("major") String major, 
                                          @Param("limit") int limit);

    /**
     * 🌍 QUERY 2: GỢI Ý MỞ RỘNG (Fallback)
     * - Dùng khi không tìm thấy môn chuyên ngành.
     */
    @Query(value = """
        SELECT s.*, COUNT(lh.id) as total_views 
        FROM subjects s
        JOIN topics t ON s.id = t.subject_id        -- CHỈ LẤY MÔN CÓ BÀI HỌC
        LEFT JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE s.id NOT IN (
            SELECT DISTINCT t2.subject_id 
            FROM learning_histories lh2 
            JOIN topics t2 ON lh2.topic_id = t2.id 
            WHERE lh2.student_id = :studentId
        )
        GROUP BY s.id
        ORDER BY total_views DESC, s.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Subject> findGlobalHotSubjects(@Param("studentId") Long studentId, 
                                        @Param("limit") int limit);

    // ==========================================
    // 🔥 PHẦN 3: CÁC HÀM BỔ SUNG CHO DASHBOARD
    // ==========================================

    // 1. Đếm tổng số môn (Dùng cho Progress Bar)
    long countByTargetMajorContainingIgnoreCase(String targetMajor);
    long countByTargetMajor(String targetMajor);

    // 2. Lấy danh sách môn theo ngành (Dùng cho Roadmap đầy đủ)
    List<Subject> findByTargetMajorContainingIgnoreCaseOrderBySemesterAsc(String targetMajor);
    List<Subject> findByTargetMajorOrderBySemesterAsc(String targetMajor);
}