package com.example.spbn3.service;

import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<Topic> getTopicsBySubject(Long subjectId) {
        return topicRepository.findBySubjectId(subjectId);
    }

    public List<Topic> searchTopic(String keyword) {
        return topicRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public void addTopic(Topic topic) {
        topicRepository.save(topic);
    }
}
