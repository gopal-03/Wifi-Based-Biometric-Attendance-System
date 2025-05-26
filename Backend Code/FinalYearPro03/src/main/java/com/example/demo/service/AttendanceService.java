package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.Attendance;
import com.example.demo.repository.AttendanceRepository;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    // Record attendance using current date/time
    public Attendance recordAttendance(String name, long mobno, String dept, int age, String college) {
        Attendance existingUser = attendanceRepository.findByMobnoAndDate(mobno, LocalDate.now());
        
        if (existingUser == null) {
            Attendance attendance = new Attendance(name, mobno, dept, age, college, LocalDate.now(), LocalTime.now());
            attendanceRepository.save(attendance);
        }
        
        // Returning existing record instead of null (optional improvement)
        return existingUser;
    }
    
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }
    
    public Attendance getDetailsByMobNo(long mobno) {
    	return attendanceRepository.findByMobnoAndDate(mobno,LocalDate.now());
    }
    
    public List<Attendance> getAttendanceByDateAndDepartment(LocalDate date, String department) {
        return attendanceRepository.findByDateAndDept(date, department);
    }
}

