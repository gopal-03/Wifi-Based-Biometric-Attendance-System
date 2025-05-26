package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.AdminRegistrationRequest;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.model.Admin;
import com.example.demo.service.AdminService;
import com.example.demo.service.AttendanceService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    
    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRegistrationRequest request) {
        try {
            Admin admin = adminService.registerAdmin(request);
            return ResponseEntity.ok("Admin registered successfully with username: " + admin.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest request) {
        try {
            Admin admin = adminService.loginAdmin(request);
            return ResponseEntity.ok(admin.getCollegeName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}
