package com.example.spbn3.repository;

import com.example.spbn3.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    // Tìm nhóm theo Tag (ví dụ: tìm tất cả nhóm có tag là 'Java')
    List<StudyGroup> findBySubjectTag(String subjectTag);
    
    // Tìm nhóm theo tên (nếu cần sau này)
    List<StudyGroup> findByNameContaining(String name);
}