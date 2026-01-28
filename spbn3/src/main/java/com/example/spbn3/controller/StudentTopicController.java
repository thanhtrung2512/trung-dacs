package com.example.spbn3.controller;

import com.example.spbn3.entity.LearningHistory;
import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Subject;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.LearningHistoryRepository;
import com.example.spbn3.repository.TopicRepository;
import com.example.spbn3.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentTopicController {

    private final StudentService studentService;
    private final TopicRepository topicRepository;
    private final LearningHistoryRepository historyRepository;

    public StudentTopicController(StudentService studentService,
                                  TopicRepository topicRepository,
                                  LearningHistoryRepository historyRepository) {
        this.studentService = studentService;
        this.topicRepository = topicRepository;
        this.historyRepository = historyRepository;
    }

    // --- 1. TRANG CHI TIẾT BÀI HỌC ---
    @GetMapping("/topics/{topicId}")
    public String showTopicDetail(@PathVariable Long topicId, HttpSession session, Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";
        Student student = studentService.getStudentByUsername(username).orElseThrow();

        // Lấy Topic và Subject
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));
        Subject subject = topic.getSubject();

        // 1. Lấy Playlist (Danh sách tất cả bài học trong môn)
        List<Topic> playlist = topicRepository.findAllBySubjectIdOrderByIdAsc(subject.getId());
        if (playlist == null) playlist = new ArrayList<>();

        // 2. Lấy Tiến độ (Danh sách ID đã học)
        List<Long> completedIds = historyRepository.findCompletedTopicIdsByStudentAndSubject(student.getId(), subject.getId());
        if (completedIds == null) completedIds = new ArrayList<>();

        // 3. Tìm bài tiếp theo
        Topic nextTopic = null;
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getId().equals(topic.getId())) {
                if (i < playlist.size() - 1) {
                    nextTopic = playlist.get(i + 1);
                }
                break;
            }
        }

        // 4. Tính % Tiến độ
        int progress = 0;
        if (!playlist.isEmpty()) {
            progress = (int) ((double) completedIds.size() / playlist.size() * 100);
        }

        // 5. Thời gian đọc
        int contentLength = topic.getContent() != null ? topic.getContent().length() : 0;
        int readingTime = (contentLength / 1000) + 2;

        // 6. Gửi dữ liệu sang View
        model.addAttribute("student", student);
        model.addAttribute("topic", topic);
        model.addAttribute("subject", subject);
        
        // 🔥 QUAN TRỌNG: Gửi biến tên là "playlist" để khớp với HTML cũ của bạn
        model.addAttribute("playlist", playlist);
        
        model.addAttribute("completedIds", completedIds);
        model.addAttribute("nextTopic", nextTopic);
        model.addAttribute("isCompleted", completedIds.contains(topic.getId()));
        model.addAttribute("progress", progress);
        model.addAttribute("readingTime", readingTime);

        // Trả về file HTML (kiểm tra kỹ tên file trong thư mục templates/student/)
        return "student/topic-detail";
    }

    // --- 2. XỬ LÝ HOÀN THÀNH ---
    @PostMapping("/topics/{topicId}/complete")
    public String completeTopic(@PathVariable Long topicId, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username != null) {
            Student student = studentService.getStudentByUsername(username).orElseThrow();
            
            if (!historyRepository.existsByStudentIdAndTopicId(student.getId(), topicId)) {
                Topic topic = topicRepository.findById(topicId).orElseThrow();
                LearningHistory history = new LearningHistory();
                history.setStudent(student);
                history.setTopic(topic);
                history.setViewedAt(LocalDateTime.now());
                historyRepository.save(history);
            }
        }
        return "redirect:/student/topics/" + topicId;
    }
}