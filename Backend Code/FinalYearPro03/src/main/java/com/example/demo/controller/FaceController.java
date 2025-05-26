package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Attendance;
import com.example.demo.model.FaceData;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.FaceDataRepository;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.FaceRecognitionService;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FaceController {

    @Autowired
    private FaceRecognitionService faceService;
    
    @Autowired
    private FaceDataRepository faceDataRepository;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
            @RequestParam String name,
            @RequestParam String username,
            @RequestParam long mobNo,
            @RequestParam String dept,
            @RequestParam String college,
            @RequestParam String collegeUsername,
            @RequestParam int age,
            @RequestParam String password,
            @RequestParam MultipartFile file) {
        try {
            String result = faceService.registerFace(username, name, mobNo, dept, college, collegeUsername, age, password, file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeFace(@RequestParam MultipartFile file,
                                           @RequestParam String routerIP,
                                           @RequestParam String wifiSignalStrength) {
        try {
            String recognizedUserName = faceService.recognizeFace(file);
            FaceData identifiedFaceData = faceDataRepository.findByUsername(recognizedUserName);
            if (identifiedFaceData == null) {
                return ResponseEntity.badRequest().body("User not found in database.");
            }
            
            System.out.println("Wifi ip "+routerIP);
            System.out.println("Wifi signal strength "+wifiSignalStrength);
            
            // Record attendance
            Attendance attendanceMarked = attendanceService.recordAttendance(
                identifiedFaceData.getName(),
                identifiedFaceData.getMobno(),
                identifiedFaceData.getDept(),
                identifiedFaceData.getAge(),
                identifiedFaceData.getCollege()
            );
            
            if(attendanceMarked != null) {
                return ResponseEntity.ok("User already marked attendance");
            }
            
            return ResponseEntity.ok("Attendance recorded for user: " + recognizedUserName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/recognizeout")
    public ResponseEntity<?> recognizeFaceOut(
            @RequestParam MultipartFile file,
            @RequestParam String routerIP,
            @RequestParam String wifiSignalStrength) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty or missing.");
            }
            
            String recognizedUserName = faceService.recognizeFace(file);
            if (recognizedUserName == null || recognizedUserName.isEmpty()) {
                return ResponseEntity.badRequest().body("Face could not be recognized.");
            }
            
            FaceData identifiedFaceData = faceDataRepository.findByUsername(recognizedUserName);
            System.out.println("Wifi ip "+routerIP);
            System.out.println("Wifi signal strength "+wifiSignalStrength);
            if (identifiedFaceData == null) {
                return ResponseEntity.badRequest()
                        .body("user did not mark Attendance In (or) User not found in database.");
            }
            
            Long userMobNo = identifiedFaceData.getMobno();
            Attendance attendeeOutReq = attendanceService.getDetailsByMobNo(userMobNo);
            if (attendeeOutReq == null) {
                return ResponseEntity.badRequest()
                        .body("Attendance record not found for user: " + recognizedUserName);
            }
            
            if (attendeeOutReq.getOutTime() == null) {
                attendeeOutReq.setOutTime(LocalTime.now());
                attendanceRepository.save(attendeeOutReq);
                return ResponseEntity.ok("Attendance Out recorded for user: " + recognizedUserName);
            } else {
                return ResponseEntity.ok("User already marked as Attendance Out: " + recognizedUserName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/registered-users")
    public List<FaceData> getRegisteredUsers() {
        return faceDataRepository.findAll();
    }
}
