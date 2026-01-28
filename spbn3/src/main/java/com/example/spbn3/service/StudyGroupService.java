package com.example.spbn3.service;

import com.example.spbn3.entity.StudyGroup;
import com.example.spbn3.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudyGroupService {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    // Lấy toàn bộ danh sách nhóm học
    public List<StudyGroup> getAllGroups() {
        return studyGroupRepository.findAll();
    }

    // Tìm nhóm theo ID
    public Optional<StudyGroup> getGroupById(Long id) {
        return studyGroupRepository.findById(id);
    }

    // Lưu thông tin nhóm (Dùng khi cập nhật số lượng thành viên)
    public void saveGroup(StudyGroup group) {
        studyGroupRepository.save(group);
    }
}