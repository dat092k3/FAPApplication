package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * User model class representing a user in the Firebase Realtime Database.
 * This class can also be used with Room database for local storage.
 * Maps to the "Users" node in Firebase.
 */
@Entity(tableName = "users_local")
public class User {

    @PrimaryKey // Đánh dấu đây là khóa chính
    @NonNull   // Bắt buộc không được null
    private String id; // Sẽ lưu UID từ Firebase/Backend

    private String name;
    private String email;
    private String birthdate;
    private String role;
    private String campus;

    // Các trường bổ sung cho Firebase (không lưu trong Room)
    @Ignore
    private String address;

    @Ignore
    private String studentId;

    @Ignore
    private Long createdAt;

    @Ignore
    private String password; // Chỉ lưu khi cần (không bắt buộc)

    @Ignore
    private Boolean isActive; // Trạng thái kích hoạt tài khoản

    // Bắt buộc phải có constructor rỗng cho Room (nếu có constructor khác)
    public User() {}

    /**
     * Constructor với các thông tin cơ bản
     */
    public User(@NonNull String id, String name, String email, String birthdate, String role, String campus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthdate = birthdate;
        this.role = role;
        this.campus = campus;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true; // Mặc định là kích hoạt
    }

    /**
     * Constructor đầy đủ với tất cả các trường
     */
    @Ignore
    public User(@NonNull String id, String name, String email, String birthdate, String role,
            String campus, String address, String studentId, Long createdAt,
            String password, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthdate = birthdate;
        this.role = role;
        this.campus = campus;
        this.address = address;
        this.studentId = studentId;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
        this.password = password;
        this.isActive = isActive != null ? isActive : true;
    }

    // Getters và Setters cho các trường Room

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    // Getters và Setters cho các trường Firebase (bổ sung)

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Helper methods để tương thích với Firebase
    /**
     * Lấy FullName (tương đương với name trong Room)
     */
    public String getFullName() {
        return name;
    }

    /**
     * Set FullName (tương đương với name trong Room)
     */
    public void setFullName(String fullName) {
        this.name = fullName;
    }

    /**
     * Lấy UID (tương đương với id trong Room)
     */
    public String getUid() {
        return id;
    }

    /**
     * Set UID (tương đương với id trong Room)
     */
    public void setUid(String uid) {
        this.id = uid;
    }

    /**
     * Chuyển đổi User object thành HashMap để lưu vào Firebase
     * Sử dụng tên trường theo Firebase structure (FullName, UID, etc.)
     */
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("UID", id);
        map.put("FullName", name);
        map.put("Email", email);
        map.put("Role", role);
        if (studentId != null) map.put("StudentId", studentId);
        if (campus != null) map.put("Campus", campus);
        if (address != null) map.put("Address", address);
        if (birthdate != null) map.put("Birthdate", birthdate);
        if (createdAt != null) map.put("CreatedAt", createdAt);
        if (password != null) map.put("Password", password);
        if (isActive != null) map.put("IsActive", isActive);
        return map;
    }

    /**
     * Tạo User object từ DataSnapshot của Firebase
     * Map các trường Firebase (FullName, UID) sang các trường Room (name, id)
     */
    public static User fromFirebaseSnapshot(DataSnapshot snapshot) {
        User user = new User();
        if (snapshot.exists()) {
            // Map từ Firebase structure sang User object
            String uid = snapshot.child("UID").getValue(String.class);
            if (uid == null) uid = snapshot.getKey(); // Fallback to key if UID not found
            user.setId(uid);
            user.setUid(uid);

            user.setName(snapshot.child("FullName").getValue(String.class));
            user.setFullName(snapshot.child("FullName").getValue(String.class));
            user.setEmail(snapshot.child("Email").getValue(String.class));
            user.setRole(snapshot.child("Role").getValue(String.class));
            user.setStudentId(snapshot.child("StudentId").getValue(String.class));
            user.setCampus(snapshot.child("Campus").getValue(String.class));
            user.setAddress(snapshot.child("Address").getValue(String.class));
            user.setBirthdate(snapshot.child("Birthdate").getValue(String.class));
            user.setCreatedAt(snapshot.child("CreatedAt").getValue(Long.class));
            // Handle Password field - có thể là String hoặc Integer/Long trong Firebase
            Object passwordObj = snapshot.child("Password").getValue();
            if (passwordObj != null) {
                user.setPassword(passwordObj.toString()); // Convert to String
            }

            user.setIsActive(snapshot.child("IsActive").getValue(Boolean.class));
        }
        return user;
    }

    /**
     * Tạo User object từ FirebaseUser (Firebase Authentication)
     * Phương thức này hữu ích khi tạo user mới từ Firebase Auth
     *
     * @param firebaseUser FirebaseUser từ Firebase Authentication
     * @param fullName Tên đầy đủ của user
     * @param role Vai trò của user (Admin, Teacher, Student)
     * @param studentId Mã số sinh viên/giáo viên
     * @return User object được tạo từ FirebaseUser
     */
    public static User fromFirebaseUser(com.google.firebase.auth.FirebaseUser firebaseUser,
            String fullName, String role, String studentId) {
        User user = new User();
        if (firebaseUser != null) {
            user.setId(firebaseUser.getUid());
            user.setUid(firebaseUser.getUid());
            user.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
            user.setName(fullName);
            user.setFullName(fullName);
            user.setRole(role);
            user.setStudentId(studentId);
            user.setCreatedAt(System.currentTimeMillis());
            user.setIsActive(true);
        }
        return user;
    }

    /**
     * Check if user is active (primitive boolean)
     * Java convention for boolean getters
     */
    public boolean isActive() {
        return isActive != null ? isActive : true;
    }
}