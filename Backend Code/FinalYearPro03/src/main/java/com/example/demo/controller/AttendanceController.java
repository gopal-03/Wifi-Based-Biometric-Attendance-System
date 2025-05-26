package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.model.Attendance;
import com.example.demo.service.AttendanceService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    
    @GetMapping("/attendancelist")
    public ResponseEntity<List<Attendance>> getAttendanceRecords(
            @RequestParam(value="date", required = false) String dateStr,
            @RequestParam(value="department", required = false) String department) {
        LocalDate date;
        if (dateStr == null || dateStr.isEmpty()) {
            date = LocalDate.now();
        } else {
            date = LocalDate.parse(dateStr);
        }
        
        List<Attendance> attendanceRecords;
        if (department != null && !department.equalsIgnoreCase("All")) {
            attendanceRecords = attendanceService.getAttendanceByDateAndDepartment(date, department);
        } else {
            attendanceRecords = attendanceService.getAttendanceByDate(date);
        }
        return ResponseEntity.ok(attendanceRecords);
    }
}
