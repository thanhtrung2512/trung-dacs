package com.example.spbn3.service;

import com.example.spbn3.entity.Admin;
import com.example.spbn3.entity.User;
import com.example.spbn3.repository.AdminRepository;
import com.example.spbn3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public AdminService(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public List<User> getAllUsersForAdmin() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
