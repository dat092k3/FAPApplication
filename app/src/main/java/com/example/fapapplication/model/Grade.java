package com.example.project.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Grade {
    private String studentId;
    private String className;
    private String subject;
    private Double pt1;
    private Double pt2;
    private Double participation;
    private Double pe;
    private Double fe;
    private Double average;

    @ServerTimestamp
    private Date lastUpdated;

    public Grade() {
        // Constructor rỗng cho Firebase
    }

    public Grade(String studentId, String className, String subject) {
        this.studentId = studentId;
        this.className = className;
        this.subject = subject;
    }

    // Calculate average based on weights
    public void calculateAverage() {
        // Weights: PT1(10%), PT2(10%), Participation(10%), PE(30%), FE(40%)
        double sum = 0;
        double totalWeight = 0;

        if (pt1 != null) {
            sum += pt1 * 0.1;
            totalWeight += 0.1;
        }
        if (pt2 != null) {
            sum += pt2 * 0.1;
            totalWeight += 0.1;
        }
        if (participation != null) {
            sum += participation * 0.1;
            totalWeight += 0.1;
        }
        if (pe != null) {
            sum += pe * 0.3;
            totalWeight += 0.3;
        }
        if (fe != null) {
            sum += fe * 0.4;
            totalWeight += 0.4;
        }

        if (totalWeight > 0) {
            this.average = Math.round((sum / totalWeight) * 10.0) / 10.0; // Round to 1 decimal
        } else {
            this.average = null;
        }
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public Double getPt1() {
        return pt1;
    }

    public void setPt1(Double pt1) {
        this.pt1 = pt1;
    }

    public Double getPt2() {
        return pt2;
    }

    public void setPt2(Double pt2) {
        this.pt2 = pt2;
    }

    public Double getParticipation() {
        return participation;
    }

    public void setParticipation(Double participation) {
        this.participation = participation;
    }

    public Double getPe() {
        return pe;
    }

    public void setPe(Double pe) {
        this.pe = pe;
    }

    public Double getFe() {
        return fe;
    }

    public void setFe(Double fe) {
        this.fe = fe;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}