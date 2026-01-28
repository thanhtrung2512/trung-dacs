package com.example.spbn3.controller;

import com.example.spbn3.service.UserService;
import com.example.spbn3.service.StudyGroupService;
import com.example.spbn3.service.SubjectService;
import com.example.spbn3.repository.LearningHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Sửa StudentService thành UserService để lấy tổng số user chung
    @Autowired
    private UserService userService; 

    @Autowired
    private StudyGroupService studyGroupService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LearningHistoryRepository historyRepo;

    // 🟢 1. TRANG DASHBOARD (QUAN TRỌNG NHẤT)
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        
        // --- Thống kê ---
        int totalStudents = 0;
        try { totalStudents = userService.getAllUsers().size(); } catch (Exception e) {}

        int totalGroups = 0;
        try { totalGroups = studyGroupService.getAllGroups().size(); } catch (Exception e) {}

        int totalSubjects = 0; 
        try { totalSubjects = subjectService.getAllSubjects().size(); } catch (Exception e) {}

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalGroups", totalGroups);
        model.addAttribute("totalSubjects", totalSubjects);
        
        return "admin/dashboard"; 
    }

    // 🟢 2. TỰ ĐỘNG CHUYỂN HƯỚNG (Vào /admin tự sang /admin/dashboard)
    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // 🟢 3. LỊCH SỬ HỌC TẬP (Giữ lại cái này)
    @GetMapping("/history")
    public String listGlobalHistory(Model model) {
        var allHistory = historyRepo.findAll(Sort.by(Sort.Direction.DESC, "viewedAt"));
        model.addAttribute("historyList", allHistory);
        return "admin/history-list";
    }

    // 🟢 4. CHUYỂN HƯỚNG NHÓM HỌC (Giữ lại cái này)
    @GetMapping("/groups")
    public String redirectGroups() {
        return "redirect:/admin/groups"; 
    }

    // ❌ ĐÃ XÓA: Hàm /users (Vì AdminUserController đã lo)
    // ❌ ĐÃ XÓA: Hàm /subjects (Vì AdminSubjectController đã lo)
}