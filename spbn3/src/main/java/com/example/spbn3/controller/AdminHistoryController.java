package com.example.spbn3.controller;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.service.LearningHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/history")
public class AdminHistoryController {

    @Autowired
    private LearningHistoryService historyService;

    @GetMapping
    public String listHistory(@RequestParam(required = false) String keyword, Model model) {
        List<LearningHistory> list;

        // Nếu có từ khóa -> Tìm kiếm theo Tên hoặc Mã SV
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = historyService.searchHistory(keyword);
        } else {
            // Không có từ khóa -> Lấy tất cả (Mới nhất lên đầu)
            list = historyService.getAllHistory();
        }

        model.addAttribute("histories", list);
        model.addAttribute("keyword", keyword); // Giữ lại từ khóa ở ô input

        return "admin/history-list";
    }
}