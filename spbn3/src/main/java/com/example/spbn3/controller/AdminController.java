package com.example.spbn3.controller;

import com.example.spbn3.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private StudyGroupRepository groupRepo;
    @Autowired private LearningHistoryRepository historyRepo;

    // ==========================================================
    // CHỈ GIỮ LẠI DASHBOARD (THỐNG KÊ TỔNG QUAN)
    // ==========================================================
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        
        // 1. Số liệu thống kê nhanh (Dùng count cho nhẹ DB)
        long totalStudents = studentRepo.count();
        long totalSubjects = subjectRepo.count();
        long totalGroups = groupRepo.count();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalSubjects", totalSubjects);
        model.addAttribute("totalGroups", totalGroups);

        // 2. Bảng "Hoạt động gần đây" (Chỉ lấy 5 dòng để Admin xem lướt)
        var recentHistory = historyRepo.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "viewedAt"))
        ).getContent();
        
        model.addAttribute("recentHistory", recentHistory);

        return "admin/dashboard"; 
    }

    // Tự động chuyển hướng về dashboard khi vào /admin
    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // ❌ QUAN TRỌNG: Đã XÓA các hàm /subjects, /users, /groups ở đây.
    // Các đường dẫn đó giờ sẽ do AdminSubjectController, AdminUserController... xử lý.
}