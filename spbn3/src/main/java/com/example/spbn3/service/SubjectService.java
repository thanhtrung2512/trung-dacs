package com.example.spbn3.service;

import com.example.spbn3.entity.Subject;
import com.example.spbn3.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /**
     * Lấy tất cả môn học
     */
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Lấy môn học theo id
     */
    public Subject getSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    /**
     * 🔥 ĐÃ SỬA: Tìm kiếm môn học BẮT ĐẦU BẰNG từ khóa
     * (Ví dụ: Gõ 'J' -> Ra 'Java', không ra 'Ajax')
     */
    public List<Subject> searchSubjects(String keyword) {
        // Dùng hàm StartingWith thay vì Containing
        return subjectRepository.findByNameStartingWithIgnoreCase(keyword);
    }

    /**
     * Lọc môn học theo chữ cái đầu (Vẫn giữ lại nếu cần dùng sau này, hoặc có thể xóa)
     */
    public List<Subject> filterSubjectsByLetter(String letter) {
        return subjectRepository.findByNameStartingWithIgnoreCase(letter);
    }

    /**
     * Thêm môn học (Admin dùng)
     */
    public Subject addSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    /**
     * Xoá môn học (Admin dùng)
     */
    public void deleteSubject(Long subjectId) {
        subjectRepository.deleteById(subjectId);
    }
}