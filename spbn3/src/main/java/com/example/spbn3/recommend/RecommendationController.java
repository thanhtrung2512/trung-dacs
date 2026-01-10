package com.example.spbn3.recommend;

import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final StudentRepository studentRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    StudentRepository studentRepository) {
        this.recommendationService = recommendationService;
        this.studentRepository = studentRepository;
    }

    /**
     * Gợi ý topic cho sinh viên dựa trên lịch sử học tập
     */
    @GetMapping("/student/{studentId}")
    public List<Topic> recommendForStudent(@PathVariable Long studentId,
                                           @RequestParam(defaultValue = "5") int limit) {
        Student student = studentRepository.findById(studentId).orElse(null);
        return recommendationService.recommendTopics(student, limit);
    }

    /**
     * Gợi ý topic phổ biến trên hệ thống
     */
    @GetMapping("/popular")
    public List<Topic> recommendPopular(@RequestParam(defaultValue = "5") int limit) {
        return recommendationService.recommendPopularTopics(limit);
    }
}
