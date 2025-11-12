package com.example.fapapplication.model;

public class SemesterGrade {
    private String studentId;
    private String term;
    private String subject;
    private String className;
    private Double average;

    public SemesterGrade() {}

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public SemesterGrade(String studentId, String term, String subject, String className, Double average) {
        this.studentId = studentId;
        this.term = term;
        this.subject = subject;
        this.className = className;
        this.average = average;
    }

    public SemesterGrade(String studentId, String term, String subject) {
        this.studentId = studentId;
        this.term = term;
        this.subject = subject;
    }

}
