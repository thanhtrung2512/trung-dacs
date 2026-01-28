package com.example.spbn3.controller;

import com.example.spbn3.entity.Admin;
import com.example.spbn3.entity.Student;
import com.example.spbn3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // 🟢 1. HIỂN THỊ DANH SÁCH + TÌM KIẾM
    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "STUDENT") String tab,
                            @RequestParam(required = false) String keyword, // 🔥 Thêm biến nhận từ khóa
                            Model model) {
        
        // Gọi hàm tìm kiếm thông minh trong Service
        model.addAttribute("users", userService.getUsersByRoleAndKeyword(tab, keyword));
        
        // Gửi các biến cần thiết ra giao diện
        model.addAttribute("currentTab", tab);
        model.addAttribute("keyword", keyword); // Giữ lại từ khóa trong ô input
        
        // Gửi object rỗng để Form thêm mới không bị lỗi
        model.addAttribute("student", new Student());
        model.addAttribute("admin", new Admin());
        
        return "admin/user-list";
    }

    // 🟢 2. LƯU SINH VIÊN (URL riêng)
    @PostMapping("/save/student")
    public String saveStudent(@ModelAttribute Student student) {
        userService.saveStudent(student);
        return "redirect:/admin/users?tab=STUDENT"; // Load lại đúng tab Sinh viên
    }

    // 🟢 3. LƯU ADMIN (URL riêng)
    @PostMapping("/save/admin")
    public String saveAdmin(@ModelAttribute Admin admin) {
        userService.saveAdmin(admin);
        return "redirect:/admin/users?tab=ADMIN"; // Load lại đúng tab Admin
    }

    // 🟢 4. XÓA USER
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, @RequestParam(defaultValue = "STUDENT") String tab) {
        userService.deleteUser(id);
        return "redirect:/admin/users?tab=" + tab; // Xóa xong ở lại tab cũ
    }
}