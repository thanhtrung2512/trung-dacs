package com.example.spbn3.service;

import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Subject;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.repository.StudentRepository;
import com.example.spbn3.repository.SubjectRepository;
import com.example.spbn3.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public StudentService(StudentRepository studentRepository,
                          SubjectRepository subjectRepository,
                          TopicRepository topicRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Topic> getTopicsBySubject(Long subjectId) {
        return topicRepository.findBySubjectId(subjectId);
    }

    public List<Topic> searchTopic(String keyword) {
        return topicRepository.findByTitleContainingIgnoreCase(keyword);
    }
}
