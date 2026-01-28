package com.example.spbn3.controller;

import com.example.spbn3.entity.*;
import com.example.spbn3.repository.TopicRepository;
import com.example.spbn3.service.*;
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

    // Giữ nguyên DTO nội bộ của bạn
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
        
        List<LearningHistory> allHistory = learningHistoryService.getStudentHistory(student.getId());

        Map<Subject, List<LearningHistory>> historyBySubject = allHistory.stream()
                .collect(Collectors.groupingBy(h -> h.getTopic().getSubject()));

        List<SubjectStats> inProgressList = new ArrayList<>();
        List<SubjectStats> completedList = new ArrayList<>();
        Set<String> keywords = new HashSet<>();

        for (Map.Entry<Subject, List<LearningHistory>> entry : historyBySubject.entrySet()) {
            Subject subject = entry.getKey();
            List<LearningHistory> subjectHistories = entry.getValue();

            // Thu thập từ khóa từ môn đã học
            keywords.add(subject.getName().toLowerCase());
            if (subject.getTargetMajor() != null) {
                keywords.add(subject.getTargetMajor().toLowerCase());
            }

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

        // Logic AI gợi ý nhóm
        List<StudyGroup> allGroups = studyGroupService.getAllGroups();
        Set<StudyGroup> aiResults = new LinkedHashSet<>();

        if (allGroups != null) {
            for (StudyGroup group : allGroups) {
                if (group.getSubjectTag() == null) continue;
                String tag = group.getSubjectTag().toLowerCase();

                // So khớp từ khóa môn học với Tag của nhóm
                boolean isMatch = keywords.stream().anyMatch(k -> k.contains(tag));
                
                if (isMatch) {
                    aiResults.add(group);
                }
            }
            // 🔥 ĐÃ XÓA PHẦN FALLBACK (Vòng lặp tự thêm nhóm cho đủ 3)
            // Việc xóa này giúp trang History chỉ hiện đúng những nhóm liên quan đến môn đã học.
        }

        model.addAttribute("student", student);
        model.addAttribute("inProgressList", inProgressList);
        model.addAttribute("completedList", completedList);
        model.addAttribute("aiGroups", new ArrayList<>(aiResults));

        return "student/history";
    }

    // Giữ nguyên các hàm xử lý nhóm bên dưới của bạn...
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