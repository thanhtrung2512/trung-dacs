package com.example.spbn3.repository;

import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Student
 * Kế thừa JpaRepository giúp tự động CRUD (save, findAll, findById, delete, ...)
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Tìm Student theo username
     * Dùng cho việc lấy dữ liệu của student đã đăng nhập
     * 
     * @param username username của student
     * @return Optional chứa Student nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<Student> findByUsername(String username);

    /**
     * Lấy danh sách tất cả môn học (Subjects) mà student có thể học
     * Nếu sau này muốn gợi ý môn học theo major hoặc năm học có thể chỉnh query này
     *
     * @return List<Subject> tất cả môn học
     */
    @Query("SELECT s FROM Subject s")
    List<Subject> findAllSubjects();
}
