package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Subject model class đại diện cho một môn học trong hệ thống.
 * Class này map với node "Subjects" trong Firebase Realtime Database.
 *
 * Model này cũng có thể được dùng với Room database cho local storage.
 */
@Entity(tableName = "subjects_local")
public class Subject {

    @PrimaryKey
    @NonNull
    private String id; // Subject ID (PK)

    @NonNull
    private String subjectCode; // Mã môn học (ví dụ: "PRM392", "SE101")

    @NonNull
    private String subjectName; // Tên môn học (ví dụ: "Mobile Programming")

    @Nullable
    private String description; // Mô tả môn học

    private long createdAt; // Timestamp khi tạo

    private boolean isActive; // Trạng thái active/inactive

    /**
     * Constructor rỗng bắt buộc cho Room và Firebase
     */
    public Subject() {
        this.isActive = true; // Default là active
    }

    /**
     * Constructor đầy đủ để tạo Subject object
     *
     * @param id Subject ID
     * @param subjectCode Mã môn học
     * @param subjectName Tên môn học
     * @param description Mô tả môn học
     * @param createdAt Timestamp tạo
     * @param isActive Trạng thái active
     */
    @Ignore
    public Subject(@NonNull String id, @NonNull String subjectCode, @NonNull String subjectName,
            @Nullable String description, long createdAt, boolean isActive) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
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
    public String getSubjectCode() {
        return subjectCode;
    }

    @NonNull
    public String getSubjectName() {
        return subjectName;
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

    public void setSubjectCode(@NonNull String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public void setSubjectName(@NonNull String subjectName) {
        this.subjectName = subjectName;
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
     * Chuyển Subject object thành Map để lưu vào Firebase
     * Method này loại bỏ các field không cần thiết (như Room annotations)
     *
     * @return Map chứa dữ liệu subject để lưu vào Firebase
     */
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("SubjectId", id);
        map.put("SubjectCode", subjectCode);
        map.put("SubjectName", subjectName);
        map.put("Description", description != null ? description : "");
        map.put("CreatedAt", createdAt);
        map.put("IsActive", isActive);
        return map;
    }

    /**
     * Tạo Subject object từ Firebase DataSnapshot
     * Method này parse dữ liệu từ Firebase và tạo Subject object
     *
     * @param snapshot DataSnapshot từ Firebase
     * @return Subject object hoặc null nếu data không hợp lệ
     */
    public static Subject fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            Subject subject = new Subject();

            subject.setId(snapshot.child("SubjectId").getValue(String.class));
            subject.setSubjectCode(snapshot.child("SubjectCode").getValue(String.class));
            subject.setSubjectName(snapshot.child("SubjectName").getValue(String.class));
            subject.setDescription(snapshot.child("Description").getValue(String.class));

            // Parse timestamp
            Long timestamp = snapshot.child("CreatedAt").getValue(Long.class);
            subject.setCreatedAt(timestamp != null ? timestamp : System.currentTimeMillis());

            // Parse active status (default true nếu không có)
            Boolean isActive = snapshot.child("IsActive").getValue(Boolean.class);
            subject.setActive(isActive != null ? isActive : true);

            // Validation: Cần có ít nhất ID và code
            if (subject.getId() == null || subject.getSubjectCode() == null) {
                return null;
            }

            return subject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo Subject ID tự động dựa trên subject code và timestamp
     * Format: SUBJECTCODE_TIMESTAMP
     *
     * @param subjectCode Mã môn học
     * @return Subject ID duy nhất
     */
    public static String generateId(String subjectCode) {
        return subjectCode.toUpperCase() + "_" + System.currentTimeMillis();
    }

    // === UTILITY METHODS ===

    @Override
    public String toString() {
        return "Subject{" +
                "id='" + id + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id.equals(subject.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}