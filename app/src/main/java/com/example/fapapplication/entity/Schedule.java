package com.example.fapapplication.entity;

public class Schedule {
    private String id;
    private String ucs_id;
    private String date;
    private String slot;
    private String room;
    private String subject;
    private boolean isExamDate;

    public Schedule() {
    }

    public Schedule(String id, String ucs_id, String date, String slot,
                    String room, String subject, boolean isExamDate) {
        this.id = id;
        this.ucs_id = ucs_id;
        this.date = date;
        this.slot = slot;
        this.room = room;
        this.subject = subject;
        this.isExamDate = isExamDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUcs_id() {
        return ucs_id;
    }

    public void setUcs_id(String ucs_id) {
        this.ucs_id = ucs_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isExamDate() {
        return isExamDate;
    }

    public void setExamDate(boolean examDate) {
        isExamDate = examDate;
    }
}
