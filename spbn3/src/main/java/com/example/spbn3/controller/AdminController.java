package com.example.spbn3.controller;

import com.example.spbn3.entity.Admin;
import com.example.spbn3.entity.User;
import com.example.spbn3.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admins")
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsersForAdmin();
    }

    @DeleteMapping("/user/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return "Xóa người dùng thành công";
    }
}
