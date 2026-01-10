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

    private LocalDateTime viewedAt;

    public LearningHistory() {}

    public LearningHistory(Long id, Student student, Topic topic, LocalDateTime viewedAt) {
        this.id = id;
        this.student = student;
        this.topic = topic;
        this.viewedAt = viewedAt;
    }

    public LearningHistory(Student student, Topic topic, LocalDateTime viewedAt) {
        this.student = student;
        this.topic = topic;
        this.viewedAt = viewedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
