package com.example.spbn3.controller;

import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Subject;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.recommend.RecommendationService;
import com.example.spbn3.repository.SubjectRepository;
import com.example.spbn3.service.StudentService;
import com.example.spbn3.service.TopicService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentSubjectController {

    private final StudentService studentService;
    private final SubjectRepository subjectRepository; // Dùng trực tiếp Repo cho tiện tìm kiếm
    private final TopicService topicService;
    private final RecommendationService recommendationService;

    @Autowired
    public StudentSubjectController(StudentService studentService,
                                    SubjectRepository subjectRepository,
                                    TopicService topicService,
                                    RecommendationService recommendationService) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
        this.topicService = topicService;
        this.recommendationService = recommendationService;
    }

    // =================================================================
    // 1. TRANG DANH SÁCH MÔN HỌC (CÓ LỘ TRÌNH GỢI Ý)
    // =================================================================
    @GetMapping("/subjects")
    public String showSubjects(@RequestParam(value = "keyword", required = false) String keyword,
                               HttpSession session, Model model) {

        // 1. Kiểm tra đăng nhập
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";
        Student student = studentService.getStudentByUsername(username).orElseThrow();

        // 2. Lấy danh sách LỘ TRÌNH GỢI Ý (Quan trọng nhất)
        // Hàm này sẽ gọi Service -> Repository (đã sửa LIKE và ORDER BY) 
        // -> Trả về 3 môn tiếp theo đúng lộ trình ngành.
        List<Subject> recommendations = recommendationService.getSubjectRecommendations(student);

        // 3. Lấy danh sách TẤT CẢ môn (Cho lưới bên dưới)
        List<Subject> allSubjects;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm theo tên
            allSubjects = subjectRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            // Lấy tất cả
            allSubjects = subjectRepository.findAll();
        }

        // 4. Đẩy ra View
        model.addAttribute("student", student);
        model.addAttribute("recommendations", recommendations); // List 3 môn lộ trình
        model.addAttribute("subjects", allSubjects);           // List tất cả môn
        model.addAttribute("keyword", keyword);

        return "student/subjects"; // File HTML danh sách môn
    }

    // =================================================================
    // 2. TRANG CHI TIẾT MÔN HỌC (DANH SÁCH BÀI HỌC)
    // =================================================================
    @GetMapping("/subjects/{id}")
    public String subjectDetails(@PathVariable Long id, HttpSession session, Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Student student = studentService.getStudentByUsername(username).orElseThrow();
        
        // Lấy thông tin môn học
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new RuntimeException("Môn không tồn tại"));

        // Lấy danh sách bài học (Topic)
        List<Topic> topics = topicService.getTopicsBySubjectId(id);

        // 🔥 GỌI AI: Lấy tiến độ % & Gợi ý bài học tiếp theo (Micro Recommendation)
        // Map này chứa: "progress", "completedIds", "nextTopic"
        Map<String, Object> aiContext = recommendationService.getSubjectContext(student.getId(), id);

        model.addAttribute("student", student);
        model.addAttribute("subject", subject);
        model.addAttribute("topics", topics);
        
        // Đẩy toàn bộ dữ liệu AI ra HTML
        model.addAllAttributes(aiContext);

        return "student/topic"; // File HTML chi tiết bài học (List video)
    }

    // =================================================================
    // 3. API JSON: GỢI Ý TÌM KIẾM (CHO THANH SEARCH AJAX)
    // =================================================================
    @GetMapping("/api/suggestions")
    @ResponseBody
    public List<Map<String, Object>> getSearchSuggestions(@RequestParam String keyword) {
        List<Subject> subjects = subjectRepository.findByNameContainingIgnoreCase(keyword);
        List<Map<String, Object>> results = new ArrayList<>();
        for (Subject s : subjects) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            results.add(map);
        }
        return results;
    }
}