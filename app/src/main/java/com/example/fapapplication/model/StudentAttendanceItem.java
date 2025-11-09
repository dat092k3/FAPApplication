package com.example.project.model;

public class StudentAttendanceItem {
    private Student student;
    private boolean isPresent; // true = present, false = absent

    public StudentAttendanceItem(Student student, boolean isPresent) {
        this.student = student;
        this.isPresent = isPresent;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }
}
