package com.example.spbn3.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Trạng thái đã học (không lưu DB)
    @Transient
    private boolean completed = false;

    // =====================
    // CONSTRUCTORS
    // =====================
    public Topic() {}

    public Topic(Long id, String title, String content, Subject subject) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.subject = subject;
    }

    public Topic(String title, String content, Subject subject) {
        this.title = title;
        this.content = content;
        this.subject = subject;
    }

    // =====================
    // GETTERS & SETTERS
    // =====================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // =====================
    // TO STRING
    // =====================
    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 30)) + "..." : null) + '\'' +
                ", subject=" + (subject != null ? subject.getName() : null) +
                ", completed=" + completed +
                '}';
    }
}
