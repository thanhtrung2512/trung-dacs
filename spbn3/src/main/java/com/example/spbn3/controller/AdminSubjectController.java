package com.example.spbn3.controller;

import com.example.spbn3.entity.Subject;
import com.example.spbn3.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/subjects")
public class AdminSubjectController {

    @Autowired
    private SubjectService subjectService;

    // 🟢 1. HIỂN THỊ DANH SÁCH & TÌM KIẾM
    @GetMapping
    public String listSubjects(@RequestParam(required = false) String keyword, Model model) {
        List<Subject> list;

        // Logic tìm kiếm: Nếu có từ khóa thì tìm, không thì lấy hết
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = subjectService.searchSubjects(keyword);
        } else {
            list = subjectService.getAllSubjects();
        }

        // Đưa dữ liệu ra View
        model.addAttribute("subjects", list);
        model.addAttribute("keyword", keyword); // Để giữ lại từ khóa trong ô search

        // 🔥 QUAN TRỌNG: Tạo đối tượng rỗng để Modal "Thêm mới" hứng dữ liệu
        model.addAttribute("subject", new Subject()); 

        return "admin/subject-list";
    }

    // 🟢 2. LƯU DỮ LIỆU (Dùng chung cho cả Thêm mới và Cập nhật)
    @PostMapping("/save")
    public String saveSubject(@ModelAttribute("subject") Subject subject) {
        // Service sẽ tự kiểm tra: Nếu subject.id có giá trị -> Update, nếu null -> Insert
        subjectService.addSubject(subject);
        return "redirect:/admin/subjects";
    }

    // 🟢 3. XÓA MÔN HỌC
    @GetMapping("/delete/{id}")
    public String deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return "redirect:/admin/subjects";
    }
}