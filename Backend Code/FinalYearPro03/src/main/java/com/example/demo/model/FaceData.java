package com.example.demo.model;

import com.example.demo.converter.FloatArrayConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class FaceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    private String name;
    private long mobno;
    private String dept;
    private String college;
    private String collegeUsername;
    private int age;
    private String password;
    
    @Lob
    private byte[] faceImage;
    
    @Lob
    @Convert(converter = FloatArrayConverter.class)
    @Column(columnDefinition = "LONGBLOB")
    private float[] faceEmbedding;

    
    public FaceData() {
        // Default constructor for JPA
    }
    
    public FaceData(String username, String name, long mobno, String dept, 
                   String college, String collegeUsername, int age, String password, byte[] faceImage) {
        this.username = username;
        this.name = name;
        this.mobno = mobno;
        this.dept = dept;
        this.college = college;
        this.collegeUsername = collegeUsername;
        this.age = age;
        this.password = password;
        this.faceImage = faceImage;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    
    public long getMobno() {
		return mobno;
	}

	public void setMobno(long mobno) {
		this.mobno = mobno;
	}

	public String getDept() {
        return dept;
    }
    
    public void setDept(String department) {
        this.dept = department;
    }
    
    public String getCollege() {
        return college;
    }
    
    public void setCollege(String college) {
        this.college = college;
    }
    
    public String getCollegeUsername() {
        return collegeUsername;
    }
    
    public void setCollegeUsername(String collegeUsername) {
        this.collegeUsername = collegeUsername;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public byte[] getFaceImage() {
        return faceImage;
    }
    
    public void setFaceImage(byte[] faceImage) {
        this.faceImage = faceImage;
    }
    
    public float[] getFaceEmbedding() {
        return faceEmbedding;
    }
    
    public void setFaceEmbedding(float[] faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }
    
}