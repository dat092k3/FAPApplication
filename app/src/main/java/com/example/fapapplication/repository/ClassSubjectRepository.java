package com.example.fapapplication.repository;

import androidx.annotation.NonNull;

import com.example.fapapplication.entity.ClassSubject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class để quản lý các thao tác với ClassSubject trong Firebase Realtime Database.
 * Handles the relationship between Classes and Subjects.
 */
public class ClassSubjectRepository {

    private final DatabaseReference classSubjectsRef;

    /**
     * Callback interface cho danh sách class-subjects
     */
    public interface OnClassSubjectsLoadedListener {
        void onClassSubjectsLoaded(List<ClassSubject> classSubjects);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho một class-subject
     */
    public interface OnClassSubjectLoadedListener {
        void onClassSubjectLoaded(ClassSubject classSubject);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho các thao tác CRUD
     */
    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Constructor
     */
    public ClassSubjectRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.classSubjectsRef = database.getReference("ClassSubjects");
    }

    /**
     * Lấy tất cả class-subjects
     *
     * @param listener Callback để nhận kết quả
     */
    public void getAllClassSubjects(@NonNull final OnClassSubjectsLoadedListener listener) {
        classSubjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ClassSubject> classSubjectList = new ArrayList<>();

                try {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ClassSubject cs = ClassSubject.fromFirebaseSnapshot(snapshot);
                        if (cs != null) {
                            classSubjectList.add(cs);
                        }
                    }
                    listener.onClassSubjectsLoaded(classSubjectList);
                } catch (Exception e) {
                    listener.onError("Error parsing class-subjects: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Lấy class-subject theo ID
     *
     * @param classSubjectId ID của class-subject
     * @param listener Callback để nhận kết quả
     */
    public void getClassSubjectById(@NonNull String classSubjectId,
            @NonNull final OnClassSubjectLoadedListener listener) {
        classSubjectsRef.child(classSubjectId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        ClassSubject cs = ClassSubject.fromFirebaseSnapshot(dataSnapshot);
                        if (cs != null) {
                            listener.onClassSubjectLoaded(cs);
                        } else {
                            listener.onError("Failed to parse class-subject data");
                        }
                    } else {
                        listener.onError("Class-subject not found");
                    }
                } catch (Exception e) {
                    listener.onError("Error loading class-subject: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Lấy tất cả subjects của một class
     *
     * @param classId ID của class
     * @param listener Callback để nhận kết quả
     */
    public void getSubjectsByClassId(@NonNull String classId,
            @NonNull final OnClassSubjectsLoadedListener listener) {
        getAllClassSubjects(new OnClassSubjectsLoadedListener() {
            @Override
            public void onClassSubjectsLoaded(List<ClassSubject> classSubjects) {
                List<ClassSubject> filtered = new ArrayList<>();
                for (ClassSubject cs : classSubjects) {
                    if (cs.getClassId().equals(classId)) {
                        filtered.add(cs);
                    }
                }
                listener.onClassSubjectsLoaded(filtered);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Lấy tất cả classes dạy một subject cụ thể
     *
     * @param subjectId ID của subject
     * @param listener Callback để nhận kết quả
     */
    public void getClassesBySubjectId(@NonNull String subjectId,
            @NonNull final OnClassSubjectsLoadedListener listener) {
        getAllClassSubjects(new OnClassSubjectsLoadedListener() {
            @Override
            public void onClassSubjectsLoaded(List<ClassSubject> classSubjects) {
                List<ClassSubject> filtered = new ArrayList<>();
                for (ClassSubject cs : classSubjects) {
                    if (cs.getSubjectId().equals(subjectId)) {
                        filtered.add(cs);
                    }
                }
                listener.onClassSubjectsLoaded(filtered);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Lấy tất cả class-subjects được dạy bởi teacher cụ thể
     *
     * @param teacherId ID của teacher
     * @param listener Callback để nhận kết quả
     */
    public void getClassSubjectsByTeacherId(@NonNull String teacherId,
            @NonNull final OnClassSubjectsLoadedListener listener) {
        getAllClassSubjects(new OnClassSubjectsLoadedListener() {
            @Override
            public void onClassSubjectsLoaded(List<ClassSubject> classSubjects) {
                List<ClassSubject> filtered = new ArrayList<>();
                for (ClassSubject cs : classSubjects) {
                    if (teacherId.equals(cs.getTeacherId())) {
                        filtered.add(cs);
                    }
                }
                listener.onClassSubjectsLoaded(filtered);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Assign một subject vào class (tạo class-subject mới)
     *
     * @param classSubject ClassSubject object cần tạo
     * @param listener Callback để nhận kết quả
     */
    public void assignSubjectToClass(@NonNull ClassSubject classSubject,
            @NonNull final OnOperationCompleteListener listener) {
        try {
            // Validate
            if (classSubject.getId() == null || classSubject.getId().isEmpty()) {
                listener.onError("ClassSubject ID cannot be empty");
                return;
            }

            if (classSubject.getClassId() == null || classSubject.getClassId().isEmpty()) {
                listener.onError("Class ID cannot be empty");
                return;
            }

            if (classSubject.getSubjectId() == null || classSubject.getSubjectId().isEmpty()) {
                listener.onError("Subject ID cannot be empty");
                return;
            }

            if (classSubject.getSchedule() == null || classSubject.getSchedule().isEmpty()) {
                listener.onError("Schedule cannot be empty");
                return;
            }

            // Set timestamp nếu chưa có
            if (classSubject.getCreatedAt() == 0) {
                classSubject.setCreatedAt(System.currentTimeMillis());
            }

            // Lưu vào Firebase
            classSubjectsRef.child(classSubject.getId())
                    .setValue(classSubject.toFirebaseMap())
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Failed to assign subject: " + e.getMessage()));

        } catch (Exception e) {
            listener.onError("Error assigning subject: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin class-subject (schedule, room, teacher, etc.)
     *
     * @param classSubject ClassSubject object với thông tin mới
     * @param listener Callback để nhận kết quả
     */
    public void updateClassSubject(@NonNull ClassSubject classSubject,
            @NonNull final OnOperationCompleteListener listener) {
        try {
            if (classSubject.getId() == null || classSubject.getId().isEmpty()) {
                listener.onError("ClassSubject ID cannot be empty");
                return;
            }

            classSubjectsRef.child(classSubject.getId())
                    .updateChildren(classSubject.toFirebaseMap())
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Failed to update class-subject: " + e.getMessage()));

        } catch (Exception e) {
            listener.onError("Error updating class-subject: " + e.getMessage());
        }
    }

    /**
     * Assign teacher vào class-subject
     *
     * @param classSubjectId ID của class-subject
     * @param teacherId ID của teacher
     * @param listener Callback để nhận kết quả
     */
    public void assignTeacherToClassSubject(@NonNull String classSubjectId, @NonNull String teacherId,
            @NonNull final OnOperationCompleteListener listener) {
        classSubjectsRef.child(classSubjectId).child("TeacherId")
                .setValue(teacherId)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to assign teacher: " + e.getMessage()));
    }

    /**
     * Activate hoặc deactivate class-subject
     *
     * @param classSubjectId ID của class-subject
     * @param isActive Trạng thái mới
     * @param listener Callback để nhận kết quả
     */
    public void setClassSubjectActiveStatus(@NonNull String classSubjectId, boolean isActive,
            @NonNull final OnOperationCompleteListener listener) {
        classSubjectsRef.child(classSubjectId).child("IsActive")
                .setValue(isActive)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to update status: " + e.getMessage()));
    }

    /**
     * Xóa class-subject
     *
     * @param classSubjectId ID của class-subject cần xóa
     * @param listener Callback để nhận kết quả
     */
    public void deleteClassSubject(@NonNull String classSubjectId,
            @NonNull final OnOperationCompleteListener listener) {
        classSubjectsRef.child(classSubjectId)
                .removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to delete class-subject: " + e.getMessage()));
    }
}