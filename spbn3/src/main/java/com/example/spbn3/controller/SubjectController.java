package com.example.spbn3.controller;

import com.example.spbn3.entity.Subject;
import com.example.spbn3.service.SubjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @PostMapping
    public String addSubject(@RequestBody Subject subject) {
        subjectService.addSubject(subject);
        return "Thêm môn học thành công";
    }

    @DeleteMapping("/{subjectId}")
    public String deleteSubject(@PathVariable Long subjectId) {
        subjectService.deleteSubject(subjectId);
        return "Xóa môn học thành công";
    }
}
