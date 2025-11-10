package com.example.fapapplication.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fapapplication.entity.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository class for managing User data operations with Firebase Realtime Database.
 * Provides methods for CRUD operations on user accounts.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String USERS_NODE = "Users";

    private final DatabaseReference usersRef;

    /**
     * Constructor - Initializes Firebase Database reference
     */
    public UserRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(USERS_NODE);
    }

    /**
     * Interface for callback when fetching all users
     */
    public interface OnUsersLoadedListener {
        /**
         * Called when users are successfully loaded
         */
        void onSuccess(List<User> users);

        /**
         * Called when an error occurs
         */
        void onError(String errorMessage);
    }

    /**
     * Interface for callback when fetching a single user
     */
    public interface OnUserLoadedListener {
        /**
         * Called when user is successfully loaded
         */
        void onSuccess(User user);

        /**
         * Called when an error occurs
         */
        void onError(String errorMessage);
    }

    /**
     * Interface for callback when user operation completes
     */
    public interface OnUserOperationListener {
        /**
         * Called when operation is successful
         */
        void onSuccess();

        /**
         * Called when an error occurs
         */
        void onError(String errorMessage);
    }

    /**
     * Lấy tất cả các user từ Firebase
     *
     * @param listener Callback để nhận kết quả
     */
    public void getAllUsers(@NonNull OnUsersLoadedListener listener) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();

                // Duyệt qua tất cả các user trong snapshot
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = User.fromFirebaseSnapshot(userSnapshot);
                    if (user.getId() != null && !user.getId().isEmpty()) {
                        users.add(user);
                    }
                }

                Log.d(TAG, "Loaded " + users.size() + " users");
                listener.onSuccess(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String errorMessage = "Failed to load users: " + error.getMessage();
                Log.e(TAG, errorMessage, error.toException());
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Lấy một user cụ thể theo UID
     *
     * @param uid UID của user cần lấy
     * @param listener Callback để nhận kết quả
     */
    public void getUserByUid(@NonNull String uid, @NonNull OnUserLoadedListener listener) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = User.fromFirebaseSnapshot(snapshot);
                    Log.d(TAG, "Loaded user: " + user.getEmail());
                    listener.onSuccess(user);
                } else {
                    String errorMessage = "User not found with UID: " + uid;
                    Log.w(TAG, errorMessage);
                    listener.onError(errorMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String errorMessage = "Failed to load user: " + error.getMessage();
                Log.e(TAG, errorMessage, error.toException());
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Tạo một user mới trong Firebase
     *
     * @param user User object cần tạo
     * @param listener Callback để nhận kết quả
     */
    public void createUser(@NonNull User user, @NonNull OnUserOperationListener listener) {
        // Kiểm tra UID không được null
        if (user.getId() == null || user.getId().isEmpty()) {
            listener.onError("UID cannot be null or empty");
            return;
        }

        // Kiểm tra user đã tồn tại chưa
        usersRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onError("User already exists with UID: " + user.getId());
                } else {
                    // Tạo user mới
                    Map<String, Object> userMap = user.toFirebaseMap();
                    usersRef.child(user.getId()).setValue(userMap)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User created successfully: " + user.getId());
                                listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                String errorMessage = "Failed to create user: " + e.getMessage();
                                Log.e(TAG, errorMessage, e);
                                listener.onError(errorMessage);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String errorMessage = "Failed to check user existence: " + error.getMessage();
                Log.e(TAG, errorMessage, error.toException());
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Cập nhật thông tin của một user
     *
     * @param user User object với thông tin đã cập nhật
     * @param listener Callback để nhận kết quả
     */
    public void updateUser(@NonNull User user, @NonNull OnUserOperationListener listener) {
        if (user.getId() == null || user.getId().isEmpty()) {
            listener.onError("UID cannot be null or empty");
            return;
        }

        // Chỉ cập nhật các trường được phép, không cập nhật UID và CreatedAt
        Map<String, Object> updateMap = user.toFirebaseMap();
        // Loại bỏ UID và CreatedAt khỏi update map (không được phép cập nhật)
        updateMap.remove("UID");
        updateMap.remove("CreatedAt");

        usersRef.child(user.getId()).updateChildren(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully: " + user.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Failed to update user: " + e.getMessage();
                    Log.e(TAG, errorMessage, e);
                    listener.onError(errorMessage);
                });
    }

    /**
     * Kích hoạt hoặc vô hiệu hóa một user
     *
     * @param uid UID của user cần thay đổi trạng thái
     * @param isActive true để kích hoạt, false để vô hiệu hóa
     * @param listener Callback để nhận kết quả
     */
    public void setUserActiveStatus(@NonNull String uid, boolean isActive,
            @NonNull OnUserOperationListener listener) {
        usersRef.child(uid).child("IsActive").setValue(isActive)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User " + uid + " active status set to: " + isActive);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Failed to update user status: " + e.getMessage();
                    Log.e(TAG, errorMessage, e);
                    listener.onError(errorMessage);
                });
    }

    /**
     * Xóa một user khỏi Firebase (chỉ nên dùng khi thực sự cần)
     *
     * @param uid UID của user cần xóa
     * @param listener Callback để nhận kết quả
     */
    public void deleteUser(@NonNull String uid, @NonNull OnUserOperationListener listener) {
        usersRef.child(uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted successfully: " + uid);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Failed to delete user: " + e.getMessage();
                    Log.e(TAG, errorMessage, e);
                    listener.onError(errorMessage);
                });
    }

    /**
     * Tạo user mới với Firebase Authentication và lưu vào Realtime Database
     * Phương thức này tích hợp với Firebase Auth để tạo user hoàn chỉnh
     *
     * @param email Email của user
     * @param password Mật khẩu của user
     * @param fullName Tên đầy đủ
     * @param role Vai trò (Admin, Teacher, Student)
     * @param studentId Mã số sinh viên/giáo viên
     * @param campus Campus (có thể null)
     * @param address Địa chỉ (có thể null)
     * @param birthdate Ngày sinh (có thể null)
     * @param authListener Listener cho Firebase Auth operation
     * @param dbListener Listener cho Database operation
     */
    public void createUserWithAuth(String email, String password, String fullName, String role,
            String studentId, String campus, String address, String birthdate,
            com.google.firebase.auth.FirebaseAuth auth,
            OnUserOperationListener authListener,
            OnUserOperationListener dbListener) {
        // Tạo user trong Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Tạo User object từ FirebaseUser
                            User user = User.fromFirebaseUser(firebaseUser, fullName, role, studentId);
                            user.setCampus(campus);
                            user.setAddress(address);
                            user.setBirthdate(birthdate);

                            // Lưu vào Realtime Database
                            Map<String, Object> userMap = user.toFirebaseMap();
                            usersRef.child(user.getUid()).setValue(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User created with Auth: " + user.getUid());
                                        if (authListener != null) authListener.onSuccess();
                                        if (dbListener != null) dbListener.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        String errorMessage = "Failed to save user data: " + e.getMessage();
                                        Log.e(TAG, errorMessage, e);
                                        // Xóa user khỏi Auth nếu không lưu được vào DB
                                        firebaseUser.delete();
                                        if (dbListener != null) dbListener.onError(errorMessage);
                                    });
                        }
                    } else {
                        String errorMessage = "Failed to create user in Auth: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, errorMessage);
                        if (authListener != null) authListener.onError(errorMessage);
                    }
                });
    }

    /**
     * Lấy User object từ Firebase Authentication user hiện tại
     *
     * @param firebaseUser FirebaseUser từ Firebase Authentication
     * @param listener Callback để nhận kết quả
     */
    public void getUserFromAuthUser(com.google.firebase.auth.FirebaseUser firebaseUser,
            OnUserLoadedListener listener) {
        if (firebaseUser == null) {
            listener.onError("FirebaseUser is null");
            return;
        }

        // Lấy user từ Realtime Database bằng UID
        getUserByUid(firebaseUser.getUid(), listener);
    }

    /**
     * Đồng bộ thông tin từ Firebase Authentication sang Realtime Database
     * Cập nhật email nếu đã thay đổi trong Auth
     *
     * @param firebaseUser FirebaseUser từ Firebase Authentication
     * @param listener Callback để nhận kết quả
     */
    public void syncAuthUserToDatabase(com.google.firebase.auth.FirebaseUser firebaseUser,
            OnUserOperationListener listener) {
        if (firebaseUser == null) {
            listener.onError("FirebaseUser is null");
            return;
        }

        // Cập nhật email trong Realtime Database nếu đã thay đổi
        usersRef.child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Synced Auth user email to database: " + firebaseUser.getUid());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Failed to sync Auth user: " + e.getMessage();
                    Log.e(TAG, errorMessage, e);
                    listener.onError(errorMessage);
                });
    }
}