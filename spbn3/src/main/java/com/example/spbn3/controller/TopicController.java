package com.example.spbn3.controller;

import com.example.spbn3.entity.Topic;
import com.example.spbn3.service.TopicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/subject/{subjectId}")
    public List<Topic> getTopicsBySubject(@PathVariable Long subjectId) {
        return topicService.getTopicsBySubject(subjectId);
    }

    @GetMapping("/search")
    public List<Topic> search(@RequestParam String keyword) {
        return topicService.searchTopic(keyword);
    }

    @PostMapping
    public String addTopic(@RequestBody Topic topic) {
        topicService.addTopic(topic);
        return "Thêm chủ đề thành công";
    }
}
