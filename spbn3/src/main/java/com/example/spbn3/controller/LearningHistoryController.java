package com.example.spbn3.controller;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.service.LearningHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class LearningHistoryController {

    private final LearningHistoryService learningHistoryService;

    public LearningHistoryController(LearningHistoryService learningHistoryService) {
        this.learningHistoryService = learningHistoryService;
    }

    @PostMapping
    public String saveHistory(@RequestBody LearningHistory history) {
        learningHistoryService.saveHistory(history);
        return "Lưu lịch sử học tập thành công";
    }

    @GetMapping("/student/{studentId}")
    public List<LearningHistory> getHistory(@PathVariable Long studentId) {
        return learningHistoryService.getHistoryByStudent(studentId);
    }
}
