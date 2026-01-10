package com.example.spbn3.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin extends User {

    private String position;

    public Admin() {
        this.role = Role.ADMIN;
    }

    public Admin(Long id, String username, String password, String fullName, String email, String position) {
        super(id, username, password, fullName, email, Role.ADMIN);
        this.position = position;
    }

    public Admin(String username, String password, String fullName, String email, String position) {
        super(username, password, fullName, email, Role.ADMIN);
        this.position = position;
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
