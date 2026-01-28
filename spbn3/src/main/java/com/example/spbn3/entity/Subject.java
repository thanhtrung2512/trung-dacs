package com.example.spbn3.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã môn học (Ví dụ: IT101, MKT202)
    @Column(name = "subject_code", unique = true)
    private String subjectCode;

    @Column(nullable = false)
    private String name;

    // Số tín chỉ
    private Integer credit;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 🔥 TRƯỜNG QUAN TRỌNG: Để AI biết môn này thuộc ngành nào
    @Column(name = "target_major")
    private String targetMajor;

    // 🔥🔥 TRƯỜNG MỚI (BẮT BUỘC PHẢI THÊM ĐỂ SỬA LỖI) 🔥🔥
    // Dùng để xác định lộ trình: Kỳ 1, Kỳ 2...
    @Column(name = "semester")
    private Integer semester = 1; // Mặc định là 1 để tránh lỗi null

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Topic> topics = new ArrayList<>();

    public Subject() {}

    // Constructor cập nhật (Có thêm semester)
    public Subject(Long id, String subjectCode, String name, Integer credit, String description, String targetMajor, Integer semester) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.name = name;
        this.credit = credit;
        this.description = description;
        this.targetMajor = targetMajor;
        this.semester = semester;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTargetMajor() { return targetMajor; }
    public void setTargetMajor(String targetMajor) { this.targetMajor = targetMajor; }

    // 🔥🔥 GETTER & SETTER CHO SEMESTER (QUAN TRỌNG) 🔥🔥
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }
}