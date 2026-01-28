package com.example.spbn3.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "study_groups")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 1. THÔNG TIN CƠ BẢN ---
    @Column(nullable = false)
    private String name;

    @Column(name = "subject_tag")
    private String subjectTag;

    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String rules;

    // --- 2. QUẢN LÝ THÀNH VIÊN ---
    
    // 🔥 SỬA: Chuyển int -> Integer để chấp nhận NULL
    private Integer members;

    @Column(name = "max_members")
    private Integer maxMembers = 500; 

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private Student creator;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> participants = new HashSet<>();

    // --- 3. TRẠNG THÁI & THỜI GIAN ---
    
    // 🔥 SỬA: Chuyển boolean -> Boolean để sửa lỗi Whitelabel Error
    @Column(name = "is_private")
    private Boolean isPrivate = false; 

    @Column(name = "join_code")
    private String joinCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- 4. CONSTRUCTORS ---
    public StudyGroup() {}

    public StudyGroup(String name, String subjectTag, String image, Integer members, String description, Student creator) {
        this.name = name;
        this.subjectTag = subjectTag;
        this.image = image;
        this.members = members; 
        this.description = description;
        this.creator = creator;
        this.isPrivate = false;
        
        if (creator != null) {
            this.participants.add(creator);
        }
    }

    // --- 5. LIFECYCLE CALLBACKS ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Đảm bảo không bao giờ lưu null xuống DB
        if (this.isPrivate == null) this.isPrivate = false;
        if (this.members == null) this.members = 0;
        if (this.maxMembers == null) this.maxMembers = 500;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- 6. HÀM HỖ TRỢ ---
    public boolean hasJoined(Student student) {
        return participants.contains(student);
    }

    // --- 7. GETTERS & SETTERS (Đã cập nhật kiểu Wrapper) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubjectTag() { return subjectTag; }
    public void setSubjectTag(String subjectTag) { this.subjectTag = subjectTag; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public Integer getMembers() { return (members != null) ? members : 0; }
    public void setMembers(Integer members) { this.members = members; }

    public Integer getMaxMembers() { return (maxMembers != null) ? maxMembers : 500; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

    public Student getCreator() { return creator; }
    public void setCreator(Student creator) { this.creator = creator; }

    public Set<Student> getParticipants() { return participants; }
    public void setParticipants(Set<Student> participants) { this.participants = participants; }

    // Dùng getIsPrivate để tương thích chuẩn Java Bean cho kiểu Boolean
    public Boolean getIsPrivate() { return (isPrivate != null) ? isPrivate : false; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}