package com.example.fapapplication.dto;

import com.example.fapapplication.entity.Schedule;

/**
 * ScheduleDTO - Dùng để hiển thị thông tin Schedule với đầy đủ tên Class, Subject, Teacher
 * (Kết hợp data từ nhiều bảng)
 */
public class ScheduleDTO {
    private Schedule schedule;
    private String className;
    private String subjectName;
    private String teacherName;

    public ScheduleDTO() {
    }

    public ScheduleDTO(Schedule schedule, String className, String subjectName, String teacherName) {
        this.schedule = schedule;
        this.className = className;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
    }

    // Getters
    public Schedule getSchedule() { return schedule; }
    public String getClassName() { return className; }
    public String getSubjectName() { return subjectName; }
    public String getTeacherName() { return teacherName; }

    // Setters
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public void setClassName(String className) { this.className = className; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}