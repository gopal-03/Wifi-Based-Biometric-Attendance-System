package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private long mobno;
    private String dept;
    private int age;
    private String college;
    private LocalDate date;
    private LocalTime inTime;
    private LocalTime outTime;

    public Attendance() {
    }
    
    public Attendance(LocalTime outTime) {
    	this.outTime = outTime;
    }

    public Attendance(String name, long mobno, String dept, int age, String college, LocalDate date, LocalTime inTime) {
        this.name = name;
        this.mobno = mobno;
        this.dept = dept;
        this.age = age;
        this.college = college;
        this.date = date;
        this.inTime = inTime;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getMobno() { return mobno; }
    public void setMobno(long mobno) { this.mobno = mobno; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

	public LocalTime getInTime() {
		return inTime;
	}

	public void setInTime(LocalTime inTime) {
		this.inTime = inTime;
	}

	public LocalTime getOutTime() {
		return outTime;
	}

	public void setOutTime(LocalTime outTime) {
		this.outTime = outTime;
	}

	@Override
	public String toString() {
		return "Attendance [id=" + id + ", name=" + name + ", mobno=" + mobno + ", dept=" + dept + ", age=" + age
				+ ", college=" + college + ", date=" + date + ", inTime=" + inTime + ", outTime=" + outTime + "]";
	}

	
    
}