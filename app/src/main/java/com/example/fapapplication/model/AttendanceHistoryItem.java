package com.example.project.model;

public class AttendanceHistoryItem {
    private String date;
    private String time;
    private String className;
    private String subject;
    private boolean status; // true = present, false = absent
    private String classSessionId;

    public AttendanceHistoryItem() {}

    public AttendanceHistoryItem(String date, String time, String className,
                                 String subject, boolean status, String classSessionId) {
        this.date = date;
        this.time = time;
        this.className = className;
        this.subject = subject;
        this.status = status;
        this.classSessionId = classSessionId;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getClassSessionId() {
        return classSessionId;
    }

    public void setClassSessionId(String classSessionId) {
        this.classSessionId = classSessionId;
    }
}