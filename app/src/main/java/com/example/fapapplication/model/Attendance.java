package com.example.fapapplication.model;

public class Attendance {
    private String studentId;
    private String classSessionId;
    private String className;
    private String date;
    private String subject;
    private boolean status; // true = present, false = absent
    private String time;

    public Attendance() {
        // Constructor rỗng cho Firebase
    }

    public Attendance(String studentId, String classSessionId, String className,
                      String date, String subject, boolean status, String time) {
        this.studentId = studentId;
        this.classSessionId = classSessionId;
        this.className = className;
        this.date = date;
        this.subject = subject;
        this.status = status;
        this.time = time;
    }

    // Getters và Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getClassSessionId() {
        return classSessionId;
    }

    public void setClassSessionId(String classSessionId) {
        this.classSessionId = classSessionId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
