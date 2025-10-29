package com.example.fapapplication.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

// Mỗi đối tượng của lớp này tương ứng với một hàng trong bảng 'users_local'
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

    // Bắt buộc phải có constructor rỗng cho Room (nếu có constructor khác)
    public User() {}

    public User(@NonNull String id, String name, String email, String birthdate, String role, String campus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthdate = birthdate;
        this.role = role;
        this.campus = campus;
    }

    @NonNull
    public String getId() {
        return id;
    }
    public void setId(@NonNull String id) {
        this.id = id;
    }
    // ... các getters/setters khác
}

