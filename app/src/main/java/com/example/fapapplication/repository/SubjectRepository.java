package com.example.fapapplication.repository;

import androidx.annotation.NonNull;

import com.example.fapapplication.entity.Subject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class để quản lý các operations với Subject trong Firebase Realtime Database.
 * Class này cung cấp các methods để CRUD subjects.
 */
public class SubjectRepository {

    private final DatabaseReference subjectsRef;
    private static SubjectRepository instance;

    /**
     * Constructor khởi tạo Firebase reference
     */
    public SubjectRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.subjectsRef = database.getReference("Subjects");
    }

    /**
     * Get singleton instance của SubjectRepository
     *
     * @return Instance của SubjectRepository
     */
    public static synchronized SubjectRepository getInstance() {
        if (instance == null) {
            instance = new SubjectRepository();
        }
        return instance;
    }

    // === CALLBACK INTERFACES ===

    /**
     * Callback interface cho operations trả về danh sách subjects
     */
    public interface SubjectListCallback {
        void onSuccess(List<Subject> subjects);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho operations trả về một subject
     */
    public interface SubjectCallback {
        void onSuccess(Subject subject);
        void onError(String errorMessage);
    }

    /**
     * Callback interface cho operations thành công/thất bại
     */
    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    // === READ OPERATIONS ===

    /**
     * Fetch tất cả subjects từ Firebase
     * Method này lắng nghe realtime updates
     *
     * @param callback Callback để xử lý kết quả
     * @return ValueEventListener để có thể remove sau này
     */
    public ValueEventListener getAllSubjects(@NonNull SubjectListCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Subject> subjects = new ArrayList<>();

                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    Subject subject = Subject.fromFirebaseSnapshot(subjectSnapshot);
                    if (subject != null) {
                        subjects.add(subject);
                    }
                }

                callback.onSuccess(subjects);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Error fetching subjects: " + databaseError.getMessage());
            }
        };

        subjectsRef.addValueEventListener(listener);
        return listener;
    }

    /**
     * Fetch tất cả subjects từ Firebase (one-time, không realtime)
     * Useful cho swipe-to-refresh functionality
     *
     * @param callback Callback để xử lý kết quả
     */
    public void getAllSubjectsOnce(@NonNull SubjectListCallback callback) {
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Subject> subjects = new ArrayList<>();

                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    Subject subject = Subject.fromFirebaseSnapshot(subjectSnapshot);
                    if (subject != null) {
                        subjects.add(subject);
                    }
                }

                callback.onSuccess(subjects);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Error fetching subjects: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Fetch tất cả active subjects từ Firebase
     * Chỉ lấy các subjects có IsActive = true
     *
     * @param callback Callback để xử lý kết quả
     */
    public void getActiveSubjects(@NonNull SubjectListCallback callback) {
        subjectsRef.orderByChild("IsActive").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Subject> subjects = new ArrayList<>();

                        for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                            Subject subject = Subject.fromFirebaseSnapshot(subjectSnapshot);
                            if (subject != null) {
                                subjects.add(subject);
                            }
                        }

                        callback.onSuccess(subjects);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError("Error fetching active subjects: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Fetch một subject cụ thể theo ID
     *
     * @param subjectId ID của subject cần fetch
     * @param callback Callback để xử lý kết quả
     */
    public void getSubjectById(@NonNull String subjectId, @NonNull SubjectCallback callback) {
        subjectsRef.child(subjectId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Subject subject = Subject.fromFirebaseSnapshot(dataSnapshot);
                    if (subject != null) {
                        callback.onSuccess(subject);
                    } else {
                        callback.onError("Failed to parse subject data");
                    }
                } else {
                    callback.onError("Subject not found with ID: " + subjectId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Error fetching subject: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Tìm kiếm subject theo subject code
     *
     * @param subjectCode Mã môn học cần tìm
     * @param callback Callback để xử lý kết quả
     */
    public void getSubjectByCode(@NonNull String subjectCode, @NonNull SubjectCallback callback) {
        subjectsRef.orderByChild("SubjectCode").equalTo(subjectCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Lấy subject đầu tiên match
                            for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                                Subject subject = Subject.fromFirebaseSnapshot(subjectSnapshot);
                                if (subject != null) {
                                    callback.onSuccess(subject);
                                    return;
                                }
                            }
                            callback.onError("Failed to parse subject data");
                        } else {
                            callback.onError("Subject not found with code: " + subjectCode);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError("Error searching subject: " + databaseError.getMessage());
                    }
                });
    }

    // === CREATE OPERATION ===

    /**
     * Tạo subject mới trong Firebase
     *
     * @param subject Subject object cần tạo
     * @param callback Callback để xử lý kết quả
     */
    public void createSubject(@NonNull Subject subject, @NonNull OperationCallback callback) {
        // Validate dữ liệu
        if (subject.getId() == null || subject.getId().isEmpty()) {
            callback.onError("Subject ID is required");
            return;
        }

        if (subject.getSubjectCode() == null || subject.getSubjectCode().isEmpty()) {
            callback.onError("Subject code is required");
            return;
        }

        if (subject.getSubjectName() == null || subject.getSubjectName().isEmpty()) {
            callback.onError("Subject name is required");
            return;
        }

        // Set timestamp nếu chưa có
        if (subject.getCreatedAt() == 0) {
            subject.setCreatedAt(System.currentTimeMillis());
        }

        // Lưu vào Firebase
        subjectsRef.child(subject.getId())
                .setValue(subject.toFirebaseMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error creating subject: " + e.getMessage()));
    }

    // === UPDATE OPERATIONS ===

    /**
     * Update thông tin subject
     *
     * @param subject Subject object với thông tin đã update
     * @param callback Callback để xử lý kết quả
     */
    public void updateSubject(@NonNull Subject subject, @NonNull OperationCallback callback) {
        // Validate
        if (subject.getId() == null || subject.getId().isEmpty()) {
            callback.onError("Subject ID is required");
            return;
        }

        // Update vào Firebase
        subjectsRef.child(subject.getId())
                .updateChildren(subject.toFirebaseMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error updating subject: " + e.getMessage()));
    }

    /**
     * Update một field cụ thể của subject
     *
     * @param subjectId ID của subject cần update
     * @param fieldName Tên field cần update
     * @param value Giá trị mới
     * @param callback Callback để xử lý kết quả
     */
    public void updateSubjectField(@NonNull String subjectId, @NonNull String fieldName,
            Object value, @NonNull OperationCallback callback) {
        subjectsRef.child(subjectId).child(fieldName)
                .setValue(value)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error updating field: " + e.getMessage()));
    }

    /**
     * Activate một subject (set IsActive = true)
     *
     * @param subjectId ID của subject cần activate
     * @param callback Callback để xử lý kết quả
     */
    public void activateSubject(@NonNull String subjectId, @NonNull OperationCallback callback) {
        updateSubjectField(subjectId, "IsActive", true, callback);
    }

    /**
     * Deactivate một subject (set IsActive = false)
     * Không xóa khỏi database, chỉ set flag
     *
     * @param subjectId ID của subject cần deactivate
     * @param callback Callback để xử lý kết quả
     */
    public void deactivateSubject(@NonNull String subjectId, @NonNull OperationCallback callback) {
        updateSubjectField(subjectId, "IsActive", false, callback);
    }

    // === DELETE OPERATION ===

    /**
     * Xóa subject khỏi Firebase (PERMANENT DELETE)
     * Cân nhắc dùng deactivateSubject() thay vì delete
     *
     * @param subjectId ID của subject cần xóa
     * @param callback Callback để xử lý kết quả
     */
    public void deleteSubject(@NonNull String subjectId, @NonNull OperationCallback callback) {
        subjectsRef.child(subjectId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error deleting subject: " + e.getMessage()));
    }

    // === UTILITY METHODS ===

    /**
     * Check xem subject code đã tồn tại chưa
     * Useful khi tạo subject mới
     *
     * @param subjectCode Mã môn học cần check
     * @param callback Callback với boolean result (true = đã tồn tại)
     */
    public void isSubjectCodeExists(@NonNull String subjectCode,
            @NonNull ExistsCallback callback) {
        subjectsRef.orderByChild("SubjectCode").equalTo(subjectCode)
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

    /**
     * Callback interface cho exists check
     */
    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    /**
     * Remove event listener để tránh memory leak
     *
     * @param listener ValueEventListener cần remove
     */
    public void removeListener(ValueEventListener listener) {
        if (listener != null) {
            subjectsRef.removeEventListener(listener);
        }
    }

    /**
     * Get DatabaseReference để custom queries
     *
     * @return DatabaseReference của Subjects node
     */
    public DatabaseReference getSubjectsReference() {
        return subjectsRef;
    }
}