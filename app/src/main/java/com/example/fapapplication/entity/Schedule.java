package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Schedule model - Lịch học cho từng buổi
 * Liên kết với UserClassSubject (chứa thông tin Class-Subject-Teacher)
 */
@Entity(tableName = "schedules_local")
public class Schedule {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String userClassSubjectId;  // FK to UserClassSubject

    @NonNull
    private String date;  // "2024-11-12"

    private int week;  // 1-10

    @NonNull
    private String slotId;  // FK to TimeSlot

    @NonNull
    private String room;

    private boolean isTemplate;  // Template cho các tuần khác

    private long createdAt;

    private boolean isActive;

    // Constructor rỗng
    public Schedule() {
        this.isActive = true;
    }

    // Constructor đầy đủ
    @Ignore
    public Schedule(@NonNull String id, @NonNull String userClassSubjectId,
                    @NonNull String date, int week, @NonNull String slotId,
                    @NonNull String room, boolean isTemplate, long createdAt, boolean isActive) {
        this.id = id;
        this.userClassSubjectId = userClassSubjectId;
        this.date = date;
        this.week = week;
        this.slotId = slotId;
        this.room = room;
        this.isTemplate = isTemplate;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Getters
    @NonNull
    public String getId() { return id; }

    @NonNull
    public String getUserClassSubjectId() { return userClassSubjectId; }

    @NonNull
    public String getDate() { return date; }

    public int getWeek() { return week; }

    @NonNull
    public String getSlotId() { return slotId; }

    @NonNull
    public String getRoom() { return room; }

    public boolean isTemplate() { return isTemplate; }

    public long getCreatedAt() { return createdAt; }

    public boolean isActive() { return isActive; }

    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setUserClassSubjectId(@NonNull String userClassSubjectId) {
        this.userClassSubjectId = userClassSubjectId;
    }
    public void setDate(@NonNull String date) { this.date = date; }
    public void setWeek(int week) { this.week = week; }
    public void setSlotId(@NonNull String slotId) { this.slotId = slotId; }
    public void setRoom(@NonNull String room) { this.room = room; }
    public void setTemplate(boolean template) { isTemplate = template; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setActive(boolean active) { isActive = active; }

    // Firebase methods
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ScheduleId", id);
        map.put("UserClassSubjectId", userClassSubjectId);
        map.put("Date", date);
        map.put("Week", week);
        map.put("SlotId", slotId);
        map.put("Room", room);
        map.put("IsTemplate", isTemplate);
        map.put("CreatedAt", createdAt);
        map.put("IsActive", isActive);
        return map;
    }

    public static Schedule fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            Schedule schedule = new Schedule();
            schedule.setId(snapshot.child("ScheduleId").getValue(String.class));
            schedule.setUserClassSubjectId(snapshot.child("UserClassSubjectId").getValue(String.class));
            schedule.setDate(snapshot.child("Date").getValue(String.class));

            Integer week = snapshot.child("Week").getValue(Integer.class);
            schedule.setWeek(week != null ? week : 1);

            schedule.setSlotId(snapshot.child("SlotId").getValue(String.class));
            schedule.setRoom(snapshot.child("Room").getValue(String.class));

            Boolean isTemplate = snapshot.child("IsTemplate").getValue(Boolean.class);
            schedule.setTemplate(isTemplate != null ? isTemplate : false);

            Long timestamp = snapshot.child("CreatedAt").getValue(Long.class);
            schedule.setCreatedAt(timestamp != null ? timestamp : System.currentTimeMillis());

            Boolean isActive = snapshot.child("IsActive").getValue(Boolean.class);
            schedule.setActive(isActive != null ? isActive : true);

            if (schedule.getId() == null || schedule.getUserClassSubjectId() == null) {
                return null;
            }

            return schedule;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateId() {
        return "SCH_" + System.currentTimeMillis();
    }
}