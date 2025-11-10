package com.example.fapapplication.model;

public class StudentGradeItem {
    private Student student;
    private Grade grade;

    public StudentGradeItem(Student student, Grade grade) {
        this.student = student;
        this.grade = grade;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }
}
