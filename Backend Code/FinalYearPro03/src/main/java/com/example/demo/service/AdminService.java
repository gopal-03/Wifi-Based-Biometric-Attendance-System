package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.AdminRegistrationRequest;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.model.Admin;
import com.example.demo.repository.AdminRepository;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(AdminRepository adminRepository, BCryptPasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Admin registerAdmin(AdminRegistrationRequest request) {
        // Check if password and confirmation match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        // Check if username already exists
        Optional<Admin> existing = adminRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setName(request.getName());
        admin.setMobileNumber(request.getMobileNumber());
        admin.setCollegeName(request.getCollegeName());
        // Hash the password before saving
        admin.setPassword(passwordEncoder.encode(request.getPassword()));

        return adminRepository.save(admin);
    }

    public Admin loginAdmin(LoginRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(request.getUsername());
        if (!adminOpt.isPresent()) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        Admin admin = adminOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        return admin;
    }
}
