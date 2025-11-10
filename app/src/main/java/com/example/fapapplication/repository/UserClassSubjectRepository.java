package com.example.fapapplication.repository;

import androidx.annotation.NonNull;

import com.example.fapapplication.entity.UserClassSubject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class để quản lý enrollment của users vào class-subjects.
 * Handles the many-to-many relationship between Users and ClassSubjects.
 */
public class UserClassSubjectRepository {

    private final DatabaseReference userClassSubjectsRef;

    /**
     * Callback interface cho danh sách enrollments
     */
    public interface OnUserClassSubjectsLoadedListener {
        void onUserClassSubjectsLoaded(List<UserClassSubject> enrollments);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho một enrollment
     */
    public interface OnUserClassSubjectLoadedListener {
        void onUserClassSubjectLoaded(UserClassSubject enrollment);
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
    public UserClassSubjectRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.userClassSubjectsRef = database.getReference("UserClassSubjects");
    }

    /**
     * Lấy tất cả enrollments
     *
     * @param listener Callback để nhận kết quả
     */
    public void getAllEnrollments(@NonNull final OnUserClassSubjectsLoadedListener listener) {
        userClassSubjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserClassSubject> enrollments = new ArrayList<>();

                try {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserClassSubject ucs = UserClassSubject.fromFirebaseSnapshot(snapshot);
                        if (ucs != null) {
                            enrollments.add(ucs);
                        }
                    }
                    listener.onUserClassSubjectsLoaded(enrollments);
                } catch (Exception e) {
                    listener.onError("Error parsing enrollments: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Lấy enrollment theo ID
     *
     * @param enrollmentId ID của enrollment
     * @param listener Callback để nhận kết quả
     */
    public void getEnrollmentById(@NonNull String enrollmentId,
            @NonNull final OnUserClassSubjectLoadedListener listener) {
        userClassSubjectsRef.child(enrollmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        UserClassSubject ucs = UserClassSubject.fromFirebaseSnapshot(dataSnapshot);
                        if (ucs != null) {
                            listener.onUserClassSubjectLoaded(ucs);
                        } else {
                            listener.onError("Failed to parse enrollment data");
                        }
                    } else {
                        listener.onError("Enrollment not found");
                    }
                } catch (Exception e) {
                    listener.onError("Error loading enrollment: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Lấy tất cả class-subjects mà user đã enroll
     *
     * @param userId ID của user
     * @param listener Callback để nhận kết quả
     */
    public void getEnrollmentsByUserId(@NonNull String userId,
            @NonNull final OnUserClassSubjectsLoadedListener listener) {
        getAllEnrollments(new OnUserClassSubjectsLoadedListener() {
            @Override
            public void onUserClassSubjectsLoaded(List<UserClassSubject> enrollments) {
                List<UserClassSubject> filtered = new ArrayList<>();
                for (UserClassSubject ucs : enrollments) {
                    if (ucs.getUserId().equals(userId)) {
                        filtered.add(ucs);
                    }
                }
                listener.onUserClassSubjectsLoaded(filtered);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Lấy tất cả users đã enroll vào một class-subject
     *
     * @param classSubjectId ID của class-subject
     * @param listener Callback để nhận kết quả
     */
    public void getEnrollmentsByClassSubjectId(@NonNull String classSubjectId,
            @NonNull final OnUserClassSubjectsLoadedListener listener) {
        getAllEnrollments(new OnUserClassSubjectsLoadedListener() {
            @Override
            public void onUserClassSubjectsLoaded(List<UserClassSubject> enrollments) {
                List<UserClassSubject> filtered = new ArrayList<>();
                for (UserClassSubject ucs : enrollments) {
                    if (ucs.getClassSubjectId().equals(classSubjectId)) {
                        filtered.add(ucs);
                    }
                }
                listener.onUserClassSubjectsLoaded(filtered);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Lấy students trong một class-subject
     *
     * @param classSubjectId ID của class-subject
     * @param listener Callback để nhận kết quả
     */
    public void getStudentsByClassSubjectId(@NonNull String classSubjectId,
            @NonNull final OnUserClassSubjectsLoadedListener listener) {
        getEnrollmentsByClassSubjectId(classSubjectId, new OnUserClassSubjectsLoadedListener() {
            @Override
            public void onUserClassSubjectsLoaded(List<UserClassSubject> enrollments) {
                List<UserClassSubject> students = new ArrayList<>();
                for (UserClassSubject ucs : enrollments) {
                    if ("Student".equalsIgnoreCase(ucs.getRole())) {
                        students.add(ucs);
                    }
                }
                listener.onUserClassSubjectsLoaded(students);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError(errorMessage);
            }
        });
    }

    /**
     * Enroll user vào class-subject
     *
     * @param enrollment UserClassSubject object cần tạo
     * @param listener Callback để nhận kết quả
     */
    public void enrollUserToClassSubject(@NonNull UserClassSubject enrollment,
            @NonNull final OnOperationCompleteListener listener) {
        try {
            // Validate
            if (enrollment.getId() == null || enrollment.getId().isEmpty()) {
                listener.onError("Enrollment ID cannot be empty");
                return;
            }

            if (enrollment.getUserId() == null || enrollment.getUserId().isEmpty()) {
                listener.onError("User ID cannot be empty");
                return;
            }

            if (enrollment.getClassSubjectId() == null || enrollment.getClassSubjectId().isEmpty()) {
                listener.onError("ClassSubject ID cannot be empty");
                return;
            }

            if (enrollment.getRole() == null || enrollment.getRole().isEmpty()) {
                listener.onError("Role cannot be empty");
                return;
            }

            // Set timestamp nếu chưa có
            if (enrollment.getEnrolledAt() == 0) {
                enrollment.setEnrolledAt(System.currentTimeMillis());
            }

            // Lưu vào Firebase
            userClassSubjectsRef.child(enrollment.getId())
                    .setValue(enrollment.toFirebaseMap())
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Failed to enroll user: " + e.getMessage()));

        } catch (Exception e) {
            listener.onError("Error enrolling user: " + e.getMessage());
        }
    }

    /**
     * Cập nhật enrollment
     *
     * @param enrollment UserClassSubject object với thông tin mới
     * @param listener Callback để nhận kết quả
     */
    public void updateEnrollment(@NonNull UserClassSubject enrollment,
            @NonNull final OnOperationCompleteListener listener) {
        try {
            if (enrollment.getId() == null || enrollment.getId().isEmpty()) {
                listener.onError("Enrollment ID cannot be empty");
                return;
            }

            userClassSubjectsRef.child(enrollment.getId())
                    .updateChildren(enrollment.toFirebaseMap())
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Failed to update enrollment: " + e.getMessage()));

        } catch (Exception e) {
            listener.onError("Error updating enrollment: " + e.getMessage());
        }
    }

    /**
     * Activate hoặc deactivate enrollment
     *
     * @param enrollmentId ID của enrollment
     * @param isActive Trạng thái mới
     * @param listener Callback để nhận kết quả
     */
    public void setEnrollmentActiveStatus(@NonNull String enrollmentId, boolean isActive,
            @NonNull final OnOperationCompleteListener listener) {
        userClassSubjectsRef.child(enrollmentId).child("IsActive")
                .setValue(isActive)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to update status: " + e.getMessage()));
    }

    /**
     * Unenroll user khỏi class-subject (xóa enrollment)
     *
     * @param enrollmentId ID của enrollment cần xóa
     * @param listener Callback để nhận kết quả
     */
    public void unenrollUser(@NonNull String enrollmentId,
            @NonNull final OnOperationCompleteListener listener) {
        userClassSubjectsRef.child(enrollmentId)
                .removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to unenroll user: " + e.getMessage()));
    }

    /**
     * Kiểm tra xem user đã enroll vào class-subject chưa
     *
     * @param userId ID của user
     * @param classSubjectId ID của class-subject
     * @param listener Callback để nhận kết quả (onUserClassSubjectLoaded nếu đã enroll, onError nếu chưa)
     */
    public void checkEnrollmentExists(@NonNull String userId, @NonNull String classSubjectId,
            @NonNull final OnUserClassSubjectLoadedListener listener) {
        String enrollmentId = UserClassSubject.generateId(userId, classSubjectId);
        getEnrollmentById(enrollmentId, listener);
    }
}