package com.example.fapapplication.entity;

public class TimeSlot {
    private String id;
    private String timeRange;  // "7:30 - 9:50"
    private int slotNumber;    // 1, 2, 3, 4

    // Constructor rỗng cho Firebase
    public TimeSlot() {
    }

    public TimeSlot(String id, String timeRange, int slotNumber) {
        this.id = id;
        this.timeRange = timeRange;
        this.slotNumber = slotNumber;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }
}