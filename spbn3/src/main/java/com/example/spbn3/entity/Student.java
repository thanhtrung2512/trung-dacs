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
}
