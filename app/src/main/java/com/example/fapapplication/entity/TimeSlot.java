package com.example.fapapplication.entity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

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

    // --- Firebase mapping methods ---
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("Id", id);
        map.put("TimeRange", timeRange);
        map.put("SlotNumber", slotNumber);
        return map;
    }

    public static TimeSlot fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            TimeSlot slot = new TimeSlot();
            slot.setId(snapshot.child("Id").getValue(String.class));
            slot.setTimeRange(snapshot.child("TimeRange").getValue(String.class));

            Integer number = snapshot.child("SlotNumber").getValue(Integer.class);
            slot.setSlotNumber(number != null ? number : 0);

            if (slot.getId() == null || slot.getTimeRange() == null) {
                return null;
            }

            return slot;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}