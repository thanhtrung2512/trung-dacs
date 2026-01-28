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

    // 🟢 1. HIỂN THỊ DANH SÁCH (Tích hợp Modal Thêm/Sửa)
    @GetMapping
    public String listSubjects(@RequestParam(required = false) String keyword, Model model) {
        List<Subject> list;

        // Logic tìm kiếm
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = subjectService.searchSubjects(keyword);
        } else {
            list = subjectService.getAllSubjects();
        }

        model.addAttribute("subjects", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "subjects");

        // 🔥 QUAN TRỌNG: Phải có dòng này thì Modal mới hoạt động được!
        // Nó tạo một đối tượng rỗng để Form trong Modal hứng dữ liệu.
        model.addAttribute("subject", new Subject()); 

        return "admin/subject-list";
    }

    // 🟢 2. LƯU DỮ LIỆU (Xử lý cho cả Thêm mới và Cập nhật từ Modal)
    @PostMapping("/save")
    public String saveSubject(@ModelAttribute("subject") Subject subject) {
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