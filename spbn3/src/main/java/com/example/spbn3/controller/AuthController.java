package com.example.spbn3.controller;

import com.example.spbn3.entity.Student;
import com.example.spbn3.entity.User;
import com.example.spbn3.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(@RequestBody Student student) {
        userService.register(student);
        return "Đăng ký thành công";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        User user = userService.login(username, password);
        return user != null ? "Đăng nhập thành công" : "Sai tài khoản hoặc mật khẩu";
    }
}
