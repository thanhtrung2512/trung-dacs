package com.example.spbn3.service;

import com.example.spbn3.entity.Admin;
import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.User;
import com.example.spbn3.repository.AdminRepository;
import com.example.spbn3.repository.StudentRepository;
import com.example.spbn3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository; // 🔥 Thêm để lưu bảng students

    @Autowired
    private AdminRepository adminRepository;     // 🔥 Thêm để lưu bảng admins

    // ==================================================
    // 1. CÁC HÀM HỖ TRỢ ĐĂNG NHẬP (LOGIN)
    // ==================================================
    public User login(String usernameOrEmail, String password) {
        String input = usernameOrEmail.trim();
        String pass = password.trim();

        Optional<User> optionalUser = userRepository.findByUsername(input);
        
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (!user.getPassword().equals(pass)) return null;

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ==================================================
    // 🔥 2. HÀM ĐĂNG KÝ MỚI (XỬ LÝ DỮ LIỆU ĐẦY ĐỦ TỪ MAP)
    // ==================================================
    @Transactional
    public void registerUser(Map<String, String> request) {
        String username = request.get("username");
        
        // Kiểm tra trùng username
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        String role = request.get("role");
        String password = request.get("password");
        if (password == null || password.isEmpty()) password = "123456"; // Mặc định nếu rỗng

        if ("ADMIN".equals(role)) {
            // --- TẠO ADMIN ---
            Admin admin = new Admin();
            // 1. Set thông tin chung (User)
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setFullName(request.get("fullName"));
            admin.setEmail(request.get("email"));
            admin.setRole(User.Role.ADMIN);
            
            // 2. Set thông tin riêng (Admin)
            admin.setPosition(request.get("position"));

            adminRepository.save(admin); // Lưu vào bảng users VÀ admins

        } else {
            // --- TẠO STUDENT ---
            Student student = new Student();
            // 1. Set thông tin chung (User)
            student.setUsername(username);
            student.setPassword(password);
            student.setFullName(request.get("fullName"));
            student.setEmail(request.get("email"));
            student.setRole(User.Role.STUDENT);
            
            // 2. Set thông tin riêng (Student)
            student.setStudentCode("SV" + (System.currentTimeMillis() % 100000)); // Mã tự sinh
            student.setMajor(request.get("major"));
            
            // Xử lý năm học (Chuyển String sang Int)
            String yearStr = request.get("year");
            try {
                if (yearStr != null && !yearStr.isEmpty()) {
                    student.setYear(Integer.parseInt(yearStr));
                } else {
                    student.setYear(1);
                }
            } catch (Exception e) {
                student.setYear(1);
            }

            studentRepository.save(student); // Lưu vào bảng users VÀ students
        }
    }

    // ==================================================
    // 3. CÁC HÀM QUẢN LÝ (CRUD + TÌM KIẾM) CHO ADMIN DASHBOARD
    // ==================================================

    public List<User> getUsersByRoleAndKeyword(String roleName, String keyword) {
        var stream = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals(roleName));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.toLowerCase().trim();
            stream = stream.filter(u -> 
                (u.getUsername() != null && u.getUsername().toLowerCase().contains(k)) ||
                (u.getFullName() != null && u.getFullName().toLowerCase().contains(k)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(k)) ||
                (u instanceof Student && ((Student) u).getStudentCode() != null && ((Student) u).getStudentCode().toLowerCase().contains(k))
            );
        }

        return stream.collect(Collectors.toList());
    }

    // Lưu Sinh viên từ trang quản trị
    public void saveStudent(Student student) {
        student.setRole(User.Role.STUDENT);
        handlePasswordLogic(student);
        studentRepository.save(student); // Dùng studentRepository cho chuẩn
    }

    // Lưu Admin từ trang quản trị
    public void saveAdmin(Admin admin) {
        admin.setRole(User.Role.ADMIN);
        handlePasswordLogic(admin);
        adminRepository.save(admin); // Dùng adminRepository cho chuẩn
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ==================================================
    // 4. LOGIC XỬ LÝ MẬT KHẨU
    // ==================================================
    private void handlePasswordLogic(User user) {
        boolean isPasswordInputEmpty = user.getPassword() == null || user.getPassword().trim().isEmpty();

        if (isPasswordInputEmpty) {
            if (user.getId() == null) {
                user.setPassword("123456");
            } else {
                userRepository.findById(user.getId()).ifPresent(oldUser -> {
                    user.setPassword(oldUser.getPassword());
                });
            }
        } 
    }
}