package com.example.spbn3.controller;

import com.example.spbn3.entity.*;
import com.example.spbn3.repository.TopicRepository;
import com.example.spbn3.service.*;
import com.example.spbn3.recommend.RecommendationService; // IMPORT AI SERVICE
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentLearningHistoryController {

    @Autowired private LearningHistoryService learningHistoryService;
    @Autowired private StudentService studentService;
    @Autowired private StudyGroupService studyGroupService;
    @Autowired private TopicRepository topicRepository;
    
    // 💉 INJECT BỘ NÃO AI VÀO CONTROLLER
    @Autowired private RecommendationService recommendationService; 

    // Giữ nguyên DTO nội bộ của bạn (Dùng cho giao diện)
    public static class SubjectStats {
        public Subject subject;
        public int progress;
        public Topic lastTopic;
        public LocalDateTime lastActiveTime;

        public SubjectStats(Subject subject, int progress, Topic lastTopic, LocalDateTime lastActiveTime) {
            this.subject = subject;
            this.progress = progress;
            this.lastTopic = lastTopic;
            this.lastActiveTime = lastActiveTime;
        }
    }

    @GetMapping("/history")
    public String showStudentJourney(HttpSession session, Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Student student = studentService.getStudentByUsername(username).orElseThrow();
        
        // ==========================================================
        // 1. LOGIC CƠ BẢN: TÍNH TOÁN TIẾN ĐỘ MÔN HỌC (GIỮ NGUYÊN)
        // ==========================================================
        List<LearningHistory> allHistory = learningHistoryService.getStudentHistory(student.getId());

        Map<Subject, List<LearningHistory>> historyBySubject = allHistory.stream()
                .collect(Collectors.groupingBy(h -> h.getTopic().getSubject()));

        List<SubjectStats> inProgressList = new ArrayList<>();
        List<SubjectStats> completedList = new ArrayList<>();

        for (Map.Entry<Subject, List<LearningHistory>> entry : historyBySubject.entrySet()) {
            Subject subject = entry.getKey();
            List<LearningHistory> subjectHistories = entry.getValue();

            long totalTopics = topicRepository.countBySubjectId(subject.getId());
            long completedCount = subjectHistories.stream()
                    .map(h -> h.getTopic().getId())
                    .distinct()
                    .count();
            
            int percent = (totalTopics > 0) ? (int) ((completedCount * 100) / totalTopics) : 0;

            LearningHistory latestHistory = subjectHistories.stream()
                    .max(Comparator.comparing(LearningHistory::getViewedAt))
                    .orElse(null);

            SubjectStats stats = new SubjectStats(subject, percent, 
                (latestHistory != null ? latestHistory.getTopic() : null), 
                (latestHistory != null ? latestHistory.getViewedAt() : null));

            if (percent >= 100) {
                completedList.add(stats);
            } else {
                inProgressList.add(stats);
            }
        }

        // ==========================================================
        // 🤖 2. LOGIC AI: GỌI THUẬT TOÁN TỪ RECOMMENDATION SERVICE
        // ==========================================================
        
        // Thuật toán 1: KNN Collaborative Filtering (Gợi ý nhóm)
        List<StudyGroup> aiGroups = recommendationService.getKnnGroupRecommendations(student.getId());
        
        // Thuật toán 2: Weighted KNN 3D (Phân tích động lực)
        Map<String, Object> motivation = recommendationService.autoDetectMotivation(student.getId());

        // ==========================================================
        // 3. ĐẨY DỮ LIỆU RA GIAO DIỆN (VIEW)
        // ==========================================================
        model.addAttribute("student", student);
        model.addAttribute("inProgressList", inProgressList);
        model.addAttribute("completedList", completedList);
        
        // Gắn dữ liệu AI vào Model
        model.addAttribute("aiGroups", aiGroups); 
        model.addAttribute("motivation", motivation);

        return "student/history";
    }

    // Giữ nguyên hàm xem chi tiết nhóm
    @GetMapping("/groups/{id}")
    public String showGroupDetail(@PathVariable Long id, HttpSession session, Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";
        Student student = studentService.getStudentByUsername(username).orElseThrow();
        StudyGroup group = studyGroupService.getGroupById(id).orElseThrow();
        model.addAttribute("group", group);
        model.addAttribute("isJoined", group.getParticipants().contains(student));
        return "student/group-detail";
    }
}