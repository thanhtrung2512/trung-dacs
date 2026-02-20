package com.example.spbn3.controller;

import com.example.spbn3.entity.User;
import com.example.spbn3.service.UserService;
import com.example.spbn3.repository.SubjectRepository; // Đã thêm
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Đã thêm
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List; // Đã thêm
import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;
    private final SubjectRepository subjectRepository; // Đã thêm để lấy danh sách ngành

    // Inject thêm SubjectRepository vào Constructor
    public AuthController(UserService userService, SubjectRepository subjectRepository) {
        this.userService = userService;
        this.subjectRepository = subjectRepository;
    }

    // =========================
    // 1. CÁC TRANG HTML
    // =========================
    @GetMapping("/")
    public String showLoginPage() {
        return "index";
    }

    // ĐÃ SỬA: Đẩy danh sách ngành học từ Database sang HTML
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        List<String> distinctMajors = subjectRepository.findDistinctTargetMajors();
        model.addAttribute("majors", distinctMajors);
        return "register"; 
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
                                   @RequestParam String role, 
                                   HttpSession session) {
        
        User user = userService.login(username, password);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu");
        }

        if (!user.getRole().name().equals(role)) {
            return ResponseEntity.badRequest().body("Bạn không có quyền truy cập với vai trò này!");
        }

        // Lưu session
        session.setAttribute("loggedInUser", user.getUsername());
        session.setAttribute("role", user.getRole().name());

        Map<String, Object> res = new HashMap<>();
        res.put("username", user.getUsername());
        res.put("role", user.getRole().name()); 

        return ResponseEntity.ok(res);
    }

    // =========================
    // 🔥 3. API ĐĂNG KÝ
    // =========================
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("Đăng ký thành công! Vui lòng đăng nhập.");
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}