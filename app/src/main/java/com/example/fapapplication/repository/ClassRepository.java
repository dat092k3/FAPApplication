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

public class ClassRepository {

    private final DatabaseReference classesRef;
    private static ClassRepository instance;

    public ClassRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.classesRef = database.getReference("Classes");
    }

    public static synchronized ClassRepository getInstance() {
        if (instance == null) {
            instance = new ClassRepository();
        }
        return instance;
    }

    // Callback interfaces
    public interface ClassListCallback {
        void onSuccess(List<Class> classes);
        void onError(String errorMessage);
    }

    public interface ClassCallback {
        void onSuccess(Class classObj);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    // Fetch tất cả classes từ Firebase (realtime)
    public ValueEventListener getAllClasses(@NonNull ClassListCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Class> classes = new ArrayList<>();

                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    Class classObj = Class.fromFirebaseSnapshot(classSnapshot);
                    if (classObj != null) {
                        classes.add(classObj);
                    }
                }

                callback.onSuccess(classes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Error fetching classes: " + databaseError.getMessage());
            }
        };

        classesRef.addValueEventListener(listener);
        return listener;
    }

    // Fetch tất cả classes từ Firebase (one-time)
    public void getAllClassesOnce(@NonNull ClassListCallback callback) {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Class> classes = new ArrayList<>();

                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    Class classObj = Class.fromFirebaseSnapshot(classSnapshot);
                    if (classObj != null) {
                        classes.add(classObj);
                    }
                }

                callback.onSuccess(classes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Error fetching classes: " + databaseError.getMessage());
            }
        });
    }

    // Fetch tất cả active classes
    public void getActiveClasses(@NonNull ClassListCallback callback) {
        classesRef.orderByChild("IsActive").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Class> classes = new ArrayList<>();

                        for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                            Class classObj = Class.fromFirebaseSnapshot(classSnapshot);
                            if (classObj != null) {
                                classes.add(classObj);
                            }
                        }

                        callback.onSuccess(classes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError("Error fetching active classes: " + databaseError.getMessage());
                    }
                });
    }

    // Fetch class theo ID
    public void getClassById(@NonNull String classId, @NonNull ClassCallback callback) {
        classesRef.child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Class classObj = Class.fromFirebaseSnapshot(dataSnapshot);
                    if (classObj != null) {
                        callback.onSuccess(classObj);
                    } else {
                        callback.onError("Failed to parse class data");
                    }
                } else {
                    callback.onError("Class not found with ID: " + classId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Error fetching class: " + databaseError.getMessage());
            }
        });
    }

    // Tạo class mới
    public void createClass(@NonNull Class classObj, @NonNull OperationCallback callback) {
        if (classObj.getId() == null || classObj.getId().isEmpty()) {
            callback.onError("Class ID is required");
            return;
        }

        if (classObj.getClassName() == null || classObj.getClassName().isEmpty()) {
            callback.onError("Class name is required");
            return;
        }

        if (classObj.getCreatedAt() == 0) {
            classObj.setCreatedAt(System.currentTimeMillis());
        }

        classesRef.child(classObj.getId())
                .setValue(classObj.toFirebaseMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error creating class: " + e.getMessage()));
    }

    // Update class
    public void updateClass(@NonNull Class classObj, @NonNull OperationCallback callback) {
        if (classObj.getId() == null || classObj.getId().isEmpty()) {
            callback.onError("Class ID is required");
            return;
        }

        classesRef.child(classObj.getId())
                .updateChildren(classObj.toFirebaseMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error updating class: " + e.getMessage()));
    }

    // Update một field cụ thể
    public void updateClassField(@NonNull String classId, @NonNull String fieldName,
            Object value, @NonNull OperationCallback callback) {
        classesRef.child(classId).child(fieldName)
                .setValue(value)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error updating field: " + e.getMessage()));
    }

    // Activate class
    public void activateClass(@NonNull String classId, @NonNull OperationCallback callback) {
        updateClassField(classId, "IsActive", true, callback);
    }

    // Deactivate class
    public void deactivateClass(@NonNull String classId, @NonNull OperationCallback callback) {
        updateClassField(classId, "IsActive", false, callback);
    }

    // Delete class
    public void deleteClass(@NonNull String classId, @NonNull OperationCallback callback) {
        classesRef.child(classId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error deleting class: " + e.getMessage()));
    }

    // Check xem class name đã tồn tại chưa
    public void isClassNameExists(@NonNull String className, @NonNull ExistsCallback callback) {
        classesRef.orderByChild("ClassName").equalTo(className)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        callback.onResult(dataSnapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onResult(false);
                    }
                });
    }

    // Generate unique class ID
    public String generateClassId() {
        return classesRef.push().getKey();
    }

    // Remove event listener
    public void removeListener(ValueEventListener listener) {
        if (listener != null) {
            classesRef.removeEventListener(listener);
        }
    }

    // Get DatabaseReference
    public DatabaseReference getClassesReference() {
        return classesRef;
    }
}