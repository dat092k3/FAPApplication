package com.example.fapapplication.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Grade extends SemesterGrade {

    private Double pt1;
    private Double pt2;
    private Double participation;
    private Double pe;
    private Double fe;

    @ServerTimestamp
    private Date lastUpdated;

    public Grade() {
        super();
    }

    public Grade(String studentId, String className, String subject) {
        super(studentId, className, subject);
    }

    // Tính trung bình
    public void calculateAverage() {
        double sum = 0;
        double totalWeight = 0;

        if (pt1 != null) { sum += pt1 * 0.1; totalWeight += 0.1; }
        if (pt2 != null) { sum += pt2 * 0.1; totalWeight += 0.1; }
        if (participation != null) { sum += participation * 0.1; totalWeight += 0.1; }
        if (pe != null) { sum += pe * 0.3; totalWeight += 0.3; }
        if (fe != null) { sum += fe * 0.4; totalWeight += 0.4; }

        if (totalWeight > 0) {
            setAverage(Math.round((sum / totalWeight) * 10.0) / 10.0);
        } else {
            setAverage(null);
        }
    }

    // Getters & setters
    public Double getPt1() { return pt1; }
    public void setPt1(Double pt1) { this.pt1 = pt1; }

    public Double getPt2() { return pt2; }
    public void setPt2(Double pt2) { this.pt2 = pt2; }

    public Double getParticipation() { return participation; }
    public void setParticipation(Double participation) { this.participation = participation; }

    public Double getPe() { return pe; }
    public void setPe(Double pe) { this.pe = pe; }

    public Double getFe() { return fe; }
    public void setFe(Double fe) { this.fe = fe; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
}
