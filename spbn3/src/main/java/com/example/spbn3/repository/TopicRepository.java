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
    // 1. CÁC HÀM CƠ BẢN (DÙNG CHO PLAYLIST & DETAIL)
    // ========================================================

    List<Topic> findAllBySubjectIdOrderByIdAsc(Long subjectId);

    long countBySubjectId(Long subjectId);

    List<Topic> findBySubjectIdOrderByIdAsc(Long subjectId);
    
    List<Topic> findBySubjectId(Long subjectId);

    List<Topic> findByTitleContainingIgnoreCase(String keyword);

    List<Topic> findBySubjectIdAndTitleContainingIgnoreCase(Long subjectId, String keyword);

    Topic findFirstBySubjectIdAndIdGreaterThanOrderByIdAsc(Long subjectId, Long currentTopicId);


    // ========================================================
    // 2. CÁC QUERY CHO AI & DASHBOARD (ĐÃ FIX LỖI LIKE %)
    // ========================================================

    // Đếm tổng bài học theo ngành (Dùng LIKE để khớp "Công nghệ" với "Công nghệ thông tin")
    @Query("SELECT COUNT(t) FROM Topic t JOIN t.subject s WHERE s.targetMajor LIKE %:major%")
    long countTotalTopicsByMajor(@Param("major") String major);

    // Tìm bài học nhập môn (Dùng cho Resume bài đầu tiên)
    @Query(value = "SELECT t.* FROM topics t JOIN subjects s ON t.subject_id = s.id " +
                   "WHERE s.target_major LIKE %:major% ORDER BY s.semester ASC, t.id ASC LIMIT 3", nativeQuery = true)
    List<Topic> findIntroTopicsByMajor(@Param("major") String major);

    // Tìm bài học gợi ý theo lộ trình (Roadmap)
    @Query(value = "SELECT t.* FROM topics t JOIN subjects s ON t.subject_id = s.id " +
                   "WHERE s.target_major LIKE %:major% AND t.id NOT IN :viewedIds LIMIT 4", nativeQuery = true)
    List<Topic> findTopTopicsByMajor(@Param("major") String major, @Param("viewedIds") List<Long> viewedIds);


    // ========================================================
    // 3. CÁC HÀM XU THẾ (TRENDING) - PHỤC VỤ DASHBOARD
    // ========================================================

    // Trending theo ngành (Có lọc thời gian 30 ngày gần đây)
    @Query(value = """
        SELECT t.* FROM topics t
        JOIN subjects s ON t.subject_id = s.id
        JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE s.target_major LIKE %:major% 
        AND lh.viewed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY t.id
        ORDER BY COUNT(lh.id) DESC LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findTrendingTopicsByMajor(@Param("major") String major, @Param("limit") int limit);

    // 🔥 HÀM QUAN TRỌNG NHẤT: Trending đơn giản để hiện lên Dashboard ngay
    @Query(value = """
        SELECT t.*, COUNT(lh.id) as view_count 
        FROM topics t 
        JOIN subjects s ON t.subject_id = s.id
        LEFT JOIN learning_histories lh ON t.id = lh.topic_id
        WHERE s.target_major LIKE %:major%
        GROUP BY t.id 
        ORDER BY view_count DESC 
        LIMIT :limit
    """, nativeQuery = true)
    List<Topic> findTrendingTopics(@Param("major") String major, @Param("limit") int limit);

    // Trending toàn hệ thống (Dùng khi ngành của sinh viên chưa có ai học bài nào)
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
    // 4. CÁC HÀM GỢI Ý THÔNG MINH (LOGIC TIẾP THEO)
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


    // ========================================================
    // 5. CÁC HÀM BỔ SUNG & BACKUP
    // ========================================================

    Optional<Topic> findFirstBySubjectIdOrderByIdAsc(Long subjectId);

    @Query("SELECT h.topic FROM LearningHistory h GROUP BY h.topic ORDER BY COUNT(h) DESC")
    List<Topic> findTopPopularTopics();
}