package com.example.spbn3.repository;

import com.example.spbn3.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // ========================================================
    // 🔥 1. CÁC HÀM MỚI CHO TRANG TOPIC DETAIL (FOCUS MODE)
    // ========================================================

    // Lấy danh sách bài học làm Playlist (Sắp xếp theo ID tăng dần)
    List<Topic> findAllBySubjectIdOrderByIdAsc(Long subjectId);

    // Đếm tổng số bài học trong 1 môn (Để tính % tiến độ môn học)
    long countBySubjectId(Long subjectId);


    // ========================================================
    // 2. CÁC HÀM CƠ BẢN & TÌM KIẾM (LOGIC CŨ)
    // ========================================================

    List<Topic> findBySubjectIdOrderByIdAsc(Long subjectId);
    
    List<Topic> findBySubjectId(Long subjectId);

    List<Topic> findByTitleContainingIgnoreCase(String keyword);

    List<Topic> findBySubjectIdAndTitleContainingIgnoreCase(Long subjectId, String keyword);

    Topic findFirstBySubjectIdAndIdGreaterThanOrderByIdAsc(Long subjectId, Long currentTopicId);


    // ========================================================
    // 🔥 3. CÁC QUERY CHO AI & DASHBOARD (ĐÃ FIX LỖI 0%)
    // ========================================================

    // 🔴 [QUAN TRỌNG]: Đã sửa thành LIKE để tìm "gần đúng"
    // Giúp tìm được bài của "Công nghệ thông tin" dù input là "Công nghệ"
    @Query("SELECT COUNT(t) FROM Topic t JOIN t.subject s WHERE s.targetMajor LIKE %:major%")
    long countTotalTopicsByMajor(@Param("major") String major);

    // Các hàm Native Query giữ nguyên
    @Query(value = "SELECT t.* FROM topics t JOIN subjects s ON t.subject_id = s.id " +
                   "WHERE s.target_major = :major ORDER BY s.semester ASC, t.id ASC LIMIT 3", nativeQuery = true)
    List<Topic> findIntroTopicsByMajor(@Param("major") String major);

    @Query(value = "SELECT t.* FROM topics t JOIN subjects s ON t.subject_id = s.id " +
                   "WHERE s.target_major = :major AND t.id NOT IN :viewedIds LIMIT 4", nativeQuery = true)
    List<Topic> findTopTopicsByMajor(@Param("major") String major, @Param("viewedIds") List<Long> viewedIds);

    @Query(value = """
        SELECT t.* FROM topics t
        JOIN subjects s ON t.subject_id = s.id
        JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE s.target_major = :major 
        AND lh.viewed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY t.id
        ORDER BY COUNT(lh.id) DESC LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findTrendingTopicsByMajor(@Param("major") String major, @Param("limit") int limit);


    // ========================================================
    // 🔥 4. CÁC HÀM GỢI Ý THÔNG MINH (CORE AI)
    // ========================================================

    @Query(value = """
        SELECT t.* FROM topics t 
        WHERE t.subject_id = (SELECT subject_id FROM topics WHERE id = :currentTopicId)
        AND t.id > :currentTopicId 
        AND t.id NOT IN (
            SELECT lh.topic_id FROM learning_histories lh WHERE lh.student_id = :studentId
        )
        ORDER BY t.id ASC 
        LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findNextLogicalTopics(@Param("currentTopicId") Long currentTopicId,
                                      @Param("studentId") Long studentId,
                                      @Param("limit") int limit);

    @Query(value = """
        SELECT t.*, COUNT(lh.id) as view_count 
        FROM topics t 
        JOIN subjects s ON t.subject_id = s.id
        LEFT JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE s.target_major = :major
        GROUP BY t.id 
        ORDER BY view_count DESC 
        LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findTrendingTopics(@Param("major") String major, @Param("limit") int limit);

    @Query(value = """
        SELECT t.* FROM topics t JOIN learning_histories lh_others ON t.id = lh_others.topic_id 
        WHERE lh_others.student_id IN (
            SELECT DISTINCT lh.student_id FROM learning_histories lh 
            WHERE lh.topic_id IN :viewedTopicIds AND lh.student_id != :currentStudentId
        ) 
        AND t.id NOT IN :viewedTopicIds 
        GROUP BY t.id 
        ORDER BY COUNT(lh_others.student_id) DESC 
        LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findCollaborativeSuggestions(@Param("currentStudentId") Long id, 
                                             @Param("viewedTopicIds") List<Long> ids, 
                                             @Param("limit") int limit);

    // Hàm overload cũ
    @Query(value = "SELECT t.* FROM topics t WHERE id > :currentTopicId LIMIT 1", nativeQuery = true)
    List<Topic> findNextLogicalTopics(@Param("currentTopicId") Long currentTopicId);

    @Query(value = """
        SELECT t.* FROM topics t
        LEFT JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE t.id NOT IN :viewedIds
        GROUP BY t.id
        ORDER BY COUNT(lh.id) DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findGlobalTrendingTopics(@Param("viewedIds") List<Long> viewedIds, 
                                         @Param("limit") int limit);

    // ========================================================
    // 🔥 5. CÁC HÀM BỔ SUNG BẮT BUỘC (MỚI THÊM VÀO)
    // ========================================================

    // 1. Lấy bài học đầu tiên của môn (Để tạo nút "Bắt đầu học" trong Roadmap)
    Optional<Topic> findFirstBySubjectIdOrderByIdAsc(Long subjectId);

    // 2. Hàm hỗ trợ tính toán khác (Nếu cần dùng JPA Method Name)
    // Lưu ý: RecommendationService hiện đang dùng countTotalTopicsByMajor (Query ở trên) nên hàm này để dự phòng
    long countBySubjectTargetMajor(String major);

    // 3. Tìm các bài học HOT nhất hệ thống (Trending Section)
    @Query("SELECT h.topic FROM LearningHistory h GROUP BY h.topic ORDER BY COUNT(h) DESC")
    List<Topic> findTopPopularTopics();
}