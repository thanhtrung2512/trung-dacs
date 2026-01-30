package com.example.spbn3.controller;

import com.example.spbn3.entity.Subject;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.service.SubjectService;
import com.example.spbn3.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/topics")
public class AdminTopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private SubjectService subjectService;

    // 🟢 1. HIỂN THỊ DANH SÁCH BÀI HỌC (Bắt buộc phải có subjectId)
    @GetMapping
    public String listTopics(@RequestParam Long subjectId, Model model) {
        
        // Lấy thông tin môn học hiện tại (Để hiển thị tên môn trên tiêu đề)
        Subject subject = subjectService.getSubjectById(subjectId);
        
        // Lấy danh sách bài học CHỈ thuộc về môn này
        model.addAttribute("topics", topicService.getTopicsBySubjectId(subjectId));
        model.addAttribute("currentSubject", subject);
        
        // Tạo object rỗng cho Modal thêm mới (Gán sẵn môn học)
        Topic newTopic = new Topic();
        newTopic.setSubject(subject); 
        model.addAttribute("topic", newTopic);

        return "admin/topic-list";
    }

    // 🟢 2. LƯU BÀI HỌC (Thêm mới & Cập nhật)
    @PostMapping("/save")
    public String saveTopic(@ModelAttribute("topic") Topic topic, @RequestParam Long subjectId) {
        // Gán thủ công Subject ID để đảm bảo quan hệ đúng
        Subject s = new Subject();
        s.setId(subjectId);
        topic.setSubject(s);
        
        // Lưu xuống DB (Hàm này bạn đã thêm vào Service rồi đúng không?)
        topicService.saveTopic(topic);
        
        // Lưu xong thì load lại đúng trang danh sách của môn đó
        return "redirect:/admin/topics?subjectId=" + subjectId;
    }

    // 🟢 3. XÓA BÀI HỌC
    @GetMapping("/delete/{id}")
    public String deleteTopic(@PathVariable Long id, @RequestParam Long subjectId) {
        topicService.deleteTopic(id);
        // Xóa xong quay lại trang cũ
        return "redirect:/admin/topics?subjectId=" + subjectId;
    }
}