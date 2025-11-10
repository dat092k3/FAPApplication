package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Class model đại diện cho một lớp học trong hệ thống.
 * Map với node "Classes" trong Firebase Realtime Database.
 *
 * Ví dụ: SE1856, AI1801, etc.
 */
@Entity(tableName = "classes_local")
public class Class {

    @PrimaryKey
    @NonNull
    private String id; // Class ID (PK)

    @NonNull
    private String className; // Tên lớp (ví dụ: "SE1856", "AI1801")

    @NonNull
    private String semester; // Học kỳ (ví dụ: "Fall 2024", "Spring 2025")

    @Nullable
    private String description; // Mô tả lớp học (optional)

    private long createdAt; // Timestamp khi tạo

    private boolean isActive; // Trạng thái active/inactive

    /**
     * Constructor rỗng bắt buộc cho Room và Firebase
     */
    public Class() {
        this.isActive = true; // Default là active
    }

    /**
     * Constructor đầy đủ
     */
    @Ignore
    public Class(@NonNull String id, @NonNull String className, @NonNull String semester,
            @Nullable String description, long createdAt, boolean isActive) {
        this.id = id;
        this.className = className;
        this.semester = semester;
        this.description = description;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // === GETTERS ===

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getClassName() {
        return className;
    }

    @NonNull
    public String getSemester() {
        return semester;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    // === SETTERS ===

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setClassName(@NonNull String className) {
        this.className = className;
    }

    public void setSemester(@NonNull String semester) {
        this.semester = semester;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // === FIREBASE METHODS ===

    /**
     * Chuyển Class object thành Map để lưu vào Firebase
     */
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ClassId", id);
        map.put("ClassName", className);
        map.put("Semester", semester);
        map.put("Description", description != null ? description : "");
        map.put("CreatedAt", createdAt);
        map.put("IsActive", isActive);
        return map;
    }

    /**
     * Tạo Class object từ Firebase DataSnapshot
     */
    public static Class fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            Class classObj = new Class();

            classObj.setId(snapshot.child("ClassId").getValue(String.class));
            classObj.setClassName(snapshot.child("ClassName").getValue(String.class));
            classObj.setSemester(snapshot.child("Semester").getValue(String.class));
            classObj.setDescription(snapshot.child("Description").getValue(String.class));

            Long timestamp = snapshot.child("CreatedAt").getValue(Long.class);
            classObj.setCreatedAt(timestamp != null ? timestamp : System.currentTimeMillis());

            Boolean isActive = snapshot.child("IsActive").getValue(Boolean.class);
            classObj.setActive(isActive != null ? isActive : true);

            // Validation
            if (classObj.getId() == null || classObj.getClassName() == null) {
                return null;
            }

            return classObj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo Class ID tự động
     * Format: CLASSNAME_TIMESTAMP
     */
    public static String generateId(String className) {
        return className.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Class{" +
                "id='" + id + '\'' +
                ", className='" + className + '\'' +
                ", semester='" + semester + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Class aClass = (Class) o;
        return id.equals(aClass.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}