package com.example.spbn3.service;

import com.example.spbn3.entity.Subject;
import com.example.spbn3.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public void addSubject(Subject subject) {
        subjectRepository.save(subject);
    }

    public void deleteSubject(Long subjectId) {
        subjectRepository.deleteById(subjectId);
    }

    public Optional<Subject> getSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId);
    }
}
