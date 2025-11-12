package com.example.fapapplication.model;

public class Schedule {
    public int schedule_id;
    public int ucs_id;

    public String date; // yyyy-MM-dd
    public int slot_id;
    public String room;
    public int subject_id;
    public boolean is_exam;

    public Schedule() {
    }

    public Schedule(int schedule_id, int ucs_id, String date, int slot_id, String room, int subject_id, boolean is_exam) {
        this.schedule_id = schedule_id;
        this.ucs_id = ucs_id;
        this.date = date;
        this.slot_id = slot_id;
        this.room = room;
        this.subject_id = subject_id;
        this.is_exam = is_exam;
    }

    public int getSchedule_id() {
        return schedule_id;
    }

    public void setSchedule_id(int schedule_id) {
        this.schedule_id = schedule_id;
    }

    public int getUcs_id() {
        return ucs_id;
    }

    public void setUcs_id(int ucs_id) {
        this.ucs_id = ucs_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSlot_id() {
        return slot_id;
    }

    public void setSlot_id(int slot_id) {
        this.slot_id = slot_id;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(int subject_id) {
        this.subject_id = subject_id;
    }

    public boolean isIs_exam() {
        return is_exam;
    }

    public void setIs_exam(boolean is_exam) {
        this.is_exam = is_exam;
    }
}
