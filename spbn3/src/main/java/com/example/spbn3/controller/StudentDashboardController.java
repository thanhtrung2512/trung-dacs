package com.example.spbn3.controller;

import com.example.spbn3.entity.Student;
import com.example.spbn3.recommend.RecommendationService;
import com.example.spbn3.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentService studentService;
    private final RecommendationService recommendationService;

    public StudentDashboardController(StudentService studentService, RecommendationService recommendationService) {
        this.studentService = studentService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // 1. Kiểm tra đăng nhập
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login"; 

        // 2. Lấy thông tin sinh viên
        Student student = studentService.getStudentByUsername(username).orElse(null);
        
        if (student != null) {
            // 3. 🔥 LẤY TOÀN BỘ DỮ LIỆU TỪ AI
            // Map này đã bao gồm: progress, streak, smartSuggestions VÀ cảm cả suggestedGroups
            Map<String, Object> dashboardData = recommendationService.getDashboardAnalytics(student);
            
            // 4. Đổ tất cả dữ liệu vào Model (bao gồm cả danh sách nhóm)
            model.addAllAttributes(dashboardData);
            
            // Thêm thông tin sinh viên để hiển thị tên, avatar...
            model.addAttribute("student", student);
            
            
        }
        
        return "student/dashboard";
    }
}