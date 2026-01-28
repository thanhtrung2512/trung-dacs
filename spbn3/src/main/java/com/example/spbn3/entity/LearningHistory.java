package com.example.spbn3.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_histories")
public class LearningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // 🔥 1. Thêm mapping chính xác với tên cột trong SQL (viewed_at)
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    // 🔥 2. Tự động gán thời gian hiện tại ngay trước khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        if (this.viewedAt == null) {
            this.viewedAt = LocalDateTime.now();
        }
    }

    public LearningHistory() {}

    // Constructor rút gọn để dùng trong Service cho nhanh
    public LearningHistory(Student student, Topic topic) {
        this.student = student;
        this.topic = topic;
    }

    public LearningHistory(Long id, Student student, Topic topic, LocalDateTime viewedAt) {
        this.id = id;
        this.student = student;
        this.topic = topic;
        this.viewedAt = viewedAt;
    }

    // Getters & Setters giữ nguyên
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}