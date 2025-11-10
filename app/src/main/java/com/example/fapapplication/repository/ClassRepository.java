package com.example.fapapplication.repository;

import androidx.annotation.NonNull;

import com.example.fapapplication.entity.Class;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class để quản lý các thao tác với Class trong Firebase Realtime Database.
 * Provides methods for CRUD operations on Class entities.
 */
public class ClassRepository {

    private final DatabaseReference classesRef;

    /**
     * Callback interface cho các thao tác bất đồng bộ trả về danh sách classes
     */
    public interface OnClassesLoadedListener {
        void onClassesLoaded(List<Class> classes);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho các thao tác bất đồng bộ trả về một class
     */
    public interface OnClassLoadedListener {
        void onClassLoaded(Class classObj);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho các thao tác create/update/delete
     */
    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Constructor
     */
    public ClassRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.classesRef = database.getReference("Classes");
    }

    /**
     * Lấy tất cả classes từ Firebase
     *
     * @param listener Callback để nhận kết quả
     */
    public void getAllClasses(@NonNull final OnClassesLoadedListener listener) {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Class> classList = new ArrayList<>();

                try {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Class classObj = Class.fromFirebaseSnapshot(snapshot);
                        if (classObj != null) {
                            classList.add(classObj);
                        }
                    }
                    listener.onClassesLoaded(classList);
                } catch (Exception e) {
                    listener.onError("Error parsing classes: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Lấy tất cả classes đang active
     *
     * @param listener Callback để nhận kết quả
     */
    public void getActiveClasses(@NonNull final OnClassesLoadedListener listener) {
        getAllClasses(new OnClassesLoadedListener() {
            @Override
            public void onClassesLoaded(List<Class> classes) {
                List<Class> activeClasses = new ArrayList<>();
                for (Class c : classes) {
                    if (c.isActive()) {
                        activeClasses.add(c);
                    }
                }
                listener.onClassesLoaded(activeClasses);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Lấy một class theo ID
     *
     * @param classId ID của class cần lấy
     * @param listener Callback để nhận kết quả
     */
    public void getClassById(@NonNull String classId, @NonNull final OnClassLoadedListener listener) {
        classesRef.child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        Class classObj = Class.fromFirebaseSnapshot(dataSnapshot);
                        if (classObj != null) {
                            listener.onClassLoaded(classObj);
                        } else {
                            listener.onError("Failed to parse class data");
                        }
                    } else {
                        listener.onError("Class not found");
                    }
                } catch (Exception e) {
                    listener.onError("Error loading class: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Lấy classes theo semester
     *
     * @param semester Học kỳ cần filter
     * @param listener Callback để nhận kết quả
     */
    public void getClassesBySemester(@NonNull String semester, @NonNull final OnClassesLoadedListener listener) {
        getAllClasses(new OnClassesLoadedListener() {
            @Override
            public void onClassesLoaded(List<Class> classes) {
                List<Class> filteredClasses = new ArrayList<>();
                for (Class c : classes) {
                    if (c.getSemester().equalsIgnoreCase(semester)) {
                        filteredClasses.add(c);
                    }
                }
                listener.onClassesLoaded(filteredClasses);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Tạo class mới trong Firebase
     *
     * @param classObj Class object cần tạo
     * @param listener Callback để nhận kết quả
     */
    public void createClass(@NonNull Class classObj, @NonNull final OnOperationCompleteListener listener) {
        try {
            // Validate
            if (classObj.getId() == null || classObj.getId().isEmpty()) {
                listener.onError("Class ID cannot be empty");
                return;
            }

            if (classObj.getClassName() == null || classObj.getClassName().isEmpty()) {
                listener.onError("Class name cannot be empty");
                return;
            }

            if (classObj.getSemester() == null || classObj.getSemester().isEmpty()) {
                listener.onError("Semester cannot be empty");
                return;
            }

            // Set timestamp nếu chưa có
            if (classObj.getCreatedAt() == 0) {
                classObj.setCreatedAt(System.currentTimeMillis());
            }

            // Lưu vào Firebase
            classesRef.child(classObj.getId())
                    .setValue(classObj.toFirebaseMap())
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Failed to create class: " + e.getMessage()));

        } catch (Exception e) {
            listener.onError("Error creating class: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin class
     *
     * @param classObj Class object với thông tin mới
     * @param listener Callback để nhận kết quả
     */
    public void updateClass(@NonNull Class classObj, @NonNull final OnOperationCompleteListener listener) {
        try {
            if (classObj.getId() == null || classObj.getId().isEmpty()) {
                listener.onError("Class ID cannot be empty");
                return;
            }

            classesRef.child(classObj.getId())
                    .updateChildren(classObj.toFirebaseMap())
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Failed to update class: " + e.getMessage()));

        } catch (Exception e) {
            listener.onError("Error updating class: " + e.getMessage());
        }
    }

    /**
     * Activate hoặc deactivate một class
     *
     * @param classId ID của class
     * @param isActive Trạng thái mới (true = active, false = inactive)
     * @param listener Callback để nhận kết quả
     */
    public void setClassActiveStatus(@NonNull String classId, boolean isActive,
            @NonNull final OnOperationCompleteListener listener) {
        classesRef.child(classId).child("IsActive")
                .setValue(isActive)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to update status: " + e.getMessage()));
    }

    /**
     * Xóa class khỏi Firebase
     * Note: Nên deactivate thay vì xóa để giữ lại dữ liệu lịch sử
     *
     * @param classId ID của class cần xóa
     * @param listener Callback để nhận kết quả
     */
    public void deleteClass(@NonNull String classId, @NonNull final OnOperationCompleteListener listener) {
        classesRef.child(classId)
                .removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to delete class: " + e.getMessage()));
    }

    /**
     * Kiểm tra xem class ID đã tồn tại chưa
     *
     * @param classId ID cần kiểm tra
     * @param listener Callback để nhận kết quả
     */
    public void checkClassExists(@NonNull String classId, @NonNull final OnClassLoadedListener listener) {
        getClassById(classId, listener);
    }
}