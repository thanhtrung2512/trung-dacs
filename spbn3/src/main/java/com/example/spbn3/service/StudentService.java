package com.example.spbn3.service;

import com.example.spbn3.entity.Student;
import com.example.spbn3.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // 🟢 1. Lấy student theo username (Dùng cho Login)
    public Optional<Student> getStudentByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    // 🟢 2. Lấy student theo ID (Dùng cho Sửa)
    // Lưu ý: Tôi đã sửa thành Optional để khớp với AdminUserController
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    // 🟢 3. Lấy danh sách (Dùng cho trang User List)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // 🔥 4. MỚI: Lưu Sinh viên (Dùng cho Thêm mới & Cập nhật)
    public void saveStudent(Student student) {
        // Vì Student kế thừa User, JPA sẽ tự động lưu thông tin vào cả 2 bảng
        studentRepository.save(student);
    }

    // 🔥 5. MỚI: Xóa Sinh viên (Dùng cho nút Xóa)
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}