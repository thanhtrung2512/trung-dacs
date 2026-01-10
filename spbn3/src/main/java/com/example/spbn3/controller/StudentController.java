package com.example.spbn3.controller;

import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.Subject;
import com.example.spbn3.entity.Topic;
import com.example.spbn3.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/all")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/subjects")
    public List<Subject> browseSubjects() {
        return studentService.getAllSubjects();
    }

    @GetMapping("/topics/{subjectId}")
    public List<Topic> getTopics(@PathVariable Long subjectId) {
        return studentService.getTopicsBySubject(subjectId);
    }

    @GetMapping("/search")
    public List<Topic> search(@RequestParam String keyword) {
        return studentService.searchTopic(keyword);
    }
}
