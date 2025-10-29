package com.example.fapapplication.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Query;

import com.example.fapapplication.dao.Crud;

import java.util.List;

import com.example.fapapplication.entity.User;

public abstract class UserDao implements Crud<User> {
    @Query("SELECT * FROM users_local ORDER BY name ASC")
    LiveData<List<User>> getAllUsers() {
        return null;
    }

    /**
     * Tìm một user bằng khóa chính (id).
     *
     * @param id UID của người dùng.
     * @return Một LiveData chứa đối tượng User.
     */
    @Query("SELECT * FROM users_local WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(String id) {
        return null;
    }

    /**
     * Xóa tất cả user khỏi bảng.
     */
    @Query("DELETE FROM users_local")
    void deleteAllUsers() {

    }

    /**
     * Đếm số lượng user trong bảng.
     *
     * @return số lượng user.
     */
    @Query("SELECT COUNT(id) FROM users_local")
    int countUsers() {
        return 0;
    }
}
