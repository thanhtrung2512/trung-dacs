package com.example.spbn3.controller;

import com.example.spbn3.entity.User;
import com.example.spbn3.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;

    // Chỉ cần Inject UserService là đủ (vì UserService đã gọi các Repo con)
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // =========================
    // 1. CÁC TRANG HTML
    // =========================
    @GetMapping("/")
    public String showLoginPage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Trả về file templates/register.html
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // =========================
    // 2. API ĐĂNG NHẬP
    // =========================
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestParam String username, 
                                   @RequestParam String password, 
                                   @RequestParam String role, // Nhận thêm role để check quyền
                                   HttpSession session) {
        
        User user = userService.login(username, password);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu");
        }

        // Kiểm tra xem người dùng có chọn đúng vai trò không
        // VD: Tài khoản là Student nhưng lại chọn đăng nhập Admin -> Chặn
        if (!user.getRole().name().equals(role)) {
            return ResponseEntity.badRequest().body("Bạn không có quyền truy cập với vai trò này!");
        }

        // Lưu session
        session.setAttribute("loggedInUser", user.getUsername());
        session.setAttribute("role", user.getRole().name());

        Map<String, Object> res = new HashMap<>();
        res.put("username", user.getUsername());
        res.put("role", user.getRole().name()); // Trả về role để FE điều hướng

        return ResponseEntity.ok(res);
    }

    // =========================
    // 🔥 3. API ĐĂNG KÝ (SỬA LẠI ĐỂ KHỚP VỚI USER SERVICE)
    // =========================
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            // Gọi hàm registerUser trong UserService (Hàm này đã xử lý Admin/Student)
            userService.registerUser(request);
            return ResponseEntity.ok("Đăng ký thành công! Vui lòng đăng nhập.");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để dễ debug
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}