package com.example.spbn3.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student extends User {

    @Column(unique = true)
    private String studentCode;

    private String major;

    private int year;

    public Student() {
        this.role = Role.STUDENT;
    }

    public Student(Long id, String username, String password, String fullName, String email,
                   String studentCode, String major, int year) {
        super(id, username, password, fullName, email, Role.STUDENT);
        this.studentCode = studentCode;
        this.major = major;
        this.year = year;
    }

    public Student(String username, String password, String fullName, String email,
                   String studentCode, String major, int year) {
        super(username, password, fullName, email, Role.STUDENT);
        this.studentCode = studentCode;
        this.major = major;
        this.year = year;
    }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    // 🔥 PHẦN QUAN TRỌNG MỚI THÊM VÀO 🔥
    // Giúp Java so sánh sinh viên dựa trên ID thay vì địa chỉ vùng nhớ
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        // Ép kiểu về Student để so sánh
        Student student = (Student) o;
        
        // So sánh ID (ID được kế thừa từ class User)
        return getId() != null && getId().equals(student.getId());
    }

    @Override
    public int hashCode() {
        // Trả về hashcode của class để đảm bảo tính nhất quán trong Hibernate
        return getClass().hashCode();
    }
}