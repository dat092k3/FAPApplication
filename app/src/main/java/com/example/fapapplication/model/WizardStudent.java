package com.example.fapapplication.model;

import java.util.ArrayList;
import java.util.List;

public class WizardStudent {
    private String userId;
    private String studentName;
    private String studentId;
    private List<String> assignedSubjectIds;
    private boolean isActive;

    public WizardStudent() {
        this.assignedSubjectIds = new ArrayList<>();
        this.isActive = true;
    }

    public WizardStudent(String userId, String studentName, String studentId, List<String> assignedSubjectIds) {
        this.userId = userId;
        this.studentName = studentName;
        this.studentId = studentId;
        this.assignedSubjectIds = assignedSubjectIds != null ? assignedSubjectIds : new ArrayList<>();
        this.isActive = true;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<String> getAssignedSubjectIds() {
        return assignedSubjectIds;
    }

    public void setAssignedSubjectIds(List<String> assignedSubjectIds) {
        this.assignedSubjectIds = assignedSubjectIds;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}