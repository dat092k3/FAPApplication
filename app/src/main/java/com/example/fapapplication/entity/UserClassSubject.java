package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
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
 * UserClassSubject model đại diện cho việc enroll của một user vào một class-subject cụ thể.
 * Map với node "UserClassSubjects" trong Firebase.
 *
 * Được dùng để track students và teachers được assign vào class-subject nào.
 */
@Entity(tableName = "user_class_subjects_local",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = ClassSubject.class,
                        parentColumns = "id",
                        childColumns = "classSubjectId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("userId"), @Index("classSubjectId")})
public class UserClassSubject {

    @PrimaryKey
    @NonNull
    private String id; // UserClassSubject ID (PK)

    @NonNull
    private String userId; // Foreign key to User (student or teacher)

    @NonNull
    private String classSubjectId; // Foreign key to ClassSubject

    @NonNull
    private String role; // Role trong class-subject này (Teacher/Student)

    private long enrolledAt; // Timestamp khi enroll

    private boolean isActive; // Trạng thái enrollment

    /**
     * Constructor rỗng
     */
    public UserClassSubject() {
        this.isActive = true;
    }

    /**
     * Constructor đầy đủ
     */
    @Ignore
    public UserClassSubject(@NonNull String id, @NonNull String userId,
            @NonNull String classSubjectId, @NonNull String role,
            long enrolledAt, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.classSubjectId = classSubjectId;
        this.role = role;
        this.enrolledAt = enrolledAt;
        this.isActive = isActive;
    }

    // === GETTERS ===

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getClassSubjectId() {
        return classSubjectId;
    }

    @NonNull
    public String getRole() {
        return role;
    }

    public long getEnrolledAt() {
        return enrolledAt;
    }

    public boolean isActive() {
        return isActive;
    }

    // === SETTERS ===

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public void setClassSubjectId(@NonNull String classSubjectId) {
        this.classSubjectId = classSubjectId;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    public void setEnrolledAt(long enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // === FIREBASE METHODS ===

    /**
     * Chuyển UserClassSubject object thành Map để lưu vào Firebase
     */
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("UserClassSubjectId", id);
        map.put("UserId", userId);
        map.put("ClassSubjectId", classSubjectId);
        map.put("Role", role);
        map.put("EnrolledAt", enrolledAt);
        map.put("IsActive", isActive);
        return map;
    }

    /**
     * Tạo UserClassSubject object từ Firebase DataSnapshot
     */
    public static UserClassSubject fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            UserClassSubject ucs = new UserClassSubject();

            ucs.setId(snapshot.child("UserClassSubjectId").getValue(String.class));
            ucs.setUserId(snapshot.child("UserId").getValue(String.class));
            ucs.setClassSubjectId(snapshot.child("ClassSubjectId").getValue(String.class));
            ucs.setRole(snapshot.child("Role").getValue(String.class));

            Long timestamp = snapshot.child("EnrolledAt").getValue(Long.class);
            ucs.setEnrolledAt(timestamp != null ? timestamp : System.currentTimeMillis());

            Boolean isActive = snapshot.child("IsActive").getValue(Boolean.class);
            ucs.setActive(isActive != null ? isActive : true);

            // Validation
            if (ucs.getId() == null || ucs.getUserId() == null || ucs.getClassSubjectId() == null) {
                return null;
            }

            return ucs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo UserClassSubject ID
     * Format: USERID_CLASSSUBJECTID
     */
    public static String generateId(String userId, String classSubjectId) {
        return userId + "_" + classSubjectId;
    }

    @Override
    public String toString() {
        return "UserClassSubject{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", classSubjectId='" + classSubjectId + '\'' +
                ", role='" + role + '\'' +
                ", enrolledAt=" + enrolledAt +
                ", isActive=" + isActive +
                '}';
    }
}