package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassSubject model đại diện cho mối quan hệ giữa Class và Subject.
 * Map với node "ClassSubjects" trong Firebase.
 *
 * Một class có thể có nhiều subjects, một subject có thể được dạy ở nhiều classes.
 */
@Entity(tableName = "class_subjects_local",
        foreignKeys = {
                @ForeignKey(entity = Class.class,
                        parentColumns = "id",
                        childColumns = "classId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Subject.class,
                        parentColumns = "id",
                        childColumns = "subjectId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("classId"), @Index("subjectId")})
public class ClassSubject {

    @PrimaryKey
    @NonNull
    private String id; // ClassSubject ID (PK)

    @NonNull
    private String classId; // Foreign key to Class

    @NonNull
    private String subjectId; // Foreign key to Subject

    @Nullable
    private String teacherId; // Teacher assigned to this class-subject (optional)

    @NonNull
    private String schedule; // Lịch học (ví dụ: "Mon 7:30-9:30, Wed 13:30-15:30")

    @Nullable
    private String room; // Phòng học (ví dụ: "301-DE", "B201")

    private long createdAt; // Timestamp

    private boolean isActive; // Trạng thái

    /**
     * Constructor rỗng
     */
    public ClassSubject() {
        this.isActive = true;
    }

    /**
     * Constructor đầy đủ
     */
    @Ignore
    public ClassSubject(@NonNull String id, @NonNull String classId, @NonNull String subjectId,
            @Nullable String teacherId, @NonNull String schedule, @Nullable String room,
            long createdAt, boolean isActive) {
        this.id = id;
        this.classId = classId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.schedule = schedule;
        this.room = room;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // === GETTERS ===

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getClassId() {
        return classId;
    }

    @NonNull
    public String getSubjectId() {
        return subjectId;
    }

    @Nullable
    public String getTeacherId() {
        return teacherId;
    }

    @NonNull
    public String getSchedule() {
        return schedule;
    }

    @Nullable
    public String getRoom() {
        return room;
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

    public void setClassId(@NonNull String classId) {
        this.classId = classId;
    }

    public void setSubjectId(@NonNull String subjectId) {
        this.subjectId = subjectId;
    }

    public void setTeacherId(@Nullable String teacherId) {
        this.teacherId = teacherId;
    }

    public void setSchedule(@NonNull String schedule) {
        this.schedule = schedule;
    }

    public void setRoom(@Nullable String room) {
        this.room = room;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // === FIREBASE METHODS ===

    /**
     * Chuyển ClassSubject object thành Map để lưu vào Firebase
     */
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ClassSubjectId", id);
        map.put("ClassId", classId);
        map.put("SubjectId", subjectId);
        map.put("TeacherId", teacherId != null ? teacherId : "");
        map.put("Schedule", schedule);
        map.put("Room", room != null ? room : "");
        map.put("CreatedAt", createdAt);
        map.put("IsActive", isActive);
        return map;
    }

    /**
     * Tạo ClassSubject object từ Firebase DataSnapshot
     */
    public static ClassSubject fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            ClassSubject cs = new ClassSubject();

            cs.setId(snapshot.child("ClassSubjectId").getValue(String.class));
            cs.setClassId(snapshot.child("ClassId").getValue(String.class));
            cs.setSubjectId(snapshot.child("SubjectId").getValue(String.class));
            cs.setTeacherId(snapshot.child("TeacherId").getValue(String.class));
            cs.setSchedule(snapshot.child("Schedule").getValue(String.class));
            cs.setRoom(snapshot.child("Room").getValue(String.class));

            Long timestamp = snapshot.child("CreatedAt").getValue(Long.class);
            cs.setCreatedAt(timestamp != null ? timestamp : System.currentTimeMillis());

            Boolean isActive = snapshot.child("IsActive").getValue(Boolean.class);
            cs.setActive(isActive != null ? isActive : true);

            // Validation
            if (cs.getId() == null || cs.getClassId() == null || cs.getSubjectId() == null) {
                return null;
            }

            return cs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo ClassSubject ID
     * Format: CLASSID_SUBJECTID
     */
    public static String generateId(String classId, String subjectId) {
        return classId + "_" + subjectId;
    }

    @Override
    public String toString() {
        return "ClassSubject{" +
                "id='" + id + '\'' +
                ", classId='" + classId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", schedule='" + schedule + '\'' +
                ", room='" + room + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
}