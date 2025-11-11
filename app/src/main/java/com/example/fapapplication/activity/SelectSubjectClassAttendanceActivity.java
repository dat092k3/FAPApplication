package com.example.fapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fapapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectSubjectClassAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "SelectSubjectClass";

    private Spinner spinnerSubject, spinnerClass;
    private Button btnContinue;
    private ProgressBar progressBar;
    private ImageButton backButton;

    private DatabaseReference realtimeDb;
    private FirebaseFirestore firestore;
    private String currentTeacherId;

    private List<Subject> subjectList = new ArrayList<>();
    private List<ClassSubject> classSubjectList = new ArrayList<>();
    private Subject selectedSubject;
    private ClassSubject selectedClassSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_subject_class_attendance);

        initViews();
        initFirebase();
        loadSubjects();
        setupListeners();
        
        // Setup back button
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerClass = findViewById(R.id.spinnerClass);
        btnContinue = findViewById(R.id.btnContinue);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
    }

    private void initFirebase() {
        realtimeDb = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        // Lấy TeacherId từ Firebase Auth hoặc SharedPreferences
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentTeacherId = auth.getCurrentUser().getUid();
        } else {
            currentTeacherId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("teacherId", "");


        }

        Log.d(TAG, "Teacher ID: " + currentTeacherId);
    }

    private void loadSubjects() {
        showLoading(true);

        // Load tất cả môn học từ Realtime Database
        realtimeDb.child("Subjects")
                .orderByChild("IsActive")
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        subjectList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                Subject subject = new Subject();
                                subject.subjectId = data.child("SubjectId").getValue(String.class);
                                subject.subjectCode = data.child("SubjectCode").getValue(String.class);
                                subject.subjectName = data.child("SubjectName").getValue(String.class);
                                subject.description = data.child("Description").getValue(String.class);

                                if (subject.subjectId != null && subject.subjectCode != null) {
                                    subjectList.add(subject);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing subject", e);
                            }
                        }

                        setupSubjectSpinner();
                        showLoading(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(SelectSubjectClassAttendanceActivity.this,
                                "Lỗi tải môn học: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Load subjects failed", error.toException());
                    }
                });
    }

    private void setupSubjectSpinner() {
        List<String> subjectNames = new ArrayList<>();
        subjectNames.add("-- Select --");

        for (Subject subject : subjectList) {
            subjectNames.add(subject.subjectCode + " - " + subject.subjectName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                subjectNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);
    }

    private void loadClassSubjects(String subjectId) {
        showLoading(true);
        classSubjectList.clear();

        realtimeDb.child("ClassSubjects")
                .orderByChild("SubjectId")
                .equalTo(subjectId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                ClassSubject cs = new ClassSubject();
                                cs.classSubjectId = data.child("ClassSubjectId").getValue(String.class);
                                cs.classId = data.child("ClassId").getValue(String.class);
                                cs.subjectId = data.child("SubjectId").getValue(String.class);
                                cs.teacherId = data.child("TeacherId").getValue(String.class);
                                cs.room = data.child("Room").getValue(String.class);
                                cs.schedule = data.child("Schedule").getValue(String.class);
                                Boolean isActive = data.child("IsActive").getValue(Boolean.class);
                                cs.isActive = isActive != null && isActive;

                                // Lọc theo giáo viên và active
//                                    if (cs.isActive && cs.teacherId != null && cs.teacherId.equals(currentTeacherId)) {
//                                        classSubjectList.add(cs);
//                                    }
                                if (cs.isActive) { // chỉ check active thôi
                                    classSubjectList.add(cs);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing class subject", e);
                            }
                        }

                        setupClassSpinner();
                        showLoading(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(SelectSubjectClassAttendanceActivity.this,
                                "Lỗi tải lớp học: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Load class subjects failed", error.toException());
                    }
                });
    }




    private void setupClassSpinner() {
        List<String> classNames = new ArrayList<>();
        classNames.add("-- Select --"); // Mục mặc định

        // Chỉ thêm các lớp học nếu danh sách không rỗng
        if (!classSubjectList.isEmpty()) {
            for (ClassSubject cs : classSubjectList) {
                String displayName = cs.classId;
                if (cs.room != null && !cs.room.isEmpty()) {
                    displayName += " - Phòng " + cs.room;
                }
                classNames.add(displayName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                classNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);

        // Kích hoạt spinner CHỈ KHI danh sách lớp có dữ liệu
        spinnerClass.setEnabled(!classSubjectList.isEmpty());
    }


    private void setupListeners() {
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Vị trí 0 là item "-- Chọn môn học --"
                if (position > 0) {
                    // Lấy Subject đã được chọn từ subjectList
                    // position - 1 vì subjectList không có item "-- Chọn môn học --"
                    selectedSubject = subjectList.get(position - 1);

                    // Dùng subjectId để tải danh sách lớp học tương ứng
                    if (selectedSubject != null && selectedSubject.subjectCode != null) {
                        Log.d(TAG, "Môn học đã chọn: " + selectedSubject.subjectCode);
                        // Ở DB, ClassSubjects.SubjectId đang lưu SubjectCode (VD: PRN211/PRM392)
                        // nên cần query theo subjectCode để có dữ liệu khớp
                        loadClassSubjects(selectedSubject.subjectCode);
                    }
                } else {
                    // Nếu người dùng chọn lại "-- Chọn môn học --", hãy xóa danh sách lớp cũ
                    classSubjectList.clear();
                    setupClassSpinner(); // Cập nhật spinner lớp để hiển thị trạng thái rỗng
                    selectedSubject = null;
                    btnContinue.setEnabled(false); // Vô hiệu hóa nút Tiếp tục
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý
            }
        });

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Vị trí 0 là "-- Chọn lớp học --"
                if (position > 0 && !classSubjectList.isEmpty()) {
                    selectedClassSubject = classSubjectList.get(position - 1);
                    Log.d(TAG, "Lớp đã chọn: " + selectedClassSubject.classId);
                    btnContinue.setEnabled(true); // Kích hoạt nút Tiếp tục khi đã chọn lớp
                } else {
                    selectedClassSubject = null;
                    btnContinue.setEnabled(false); // Vô hiệu hóa nút nếu chưa chọn lớp
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý
            }
        });

        // Listener cho nút Tiếp tục
        btnContinue.setOnClickListener(v -> {
            if (selectedSubject != null && selectedClassSubject != null) {
                // Chuyển sang Activity tiếp theo và gửi thông tin cần thiết
                Intent intent = new Intent(SelectSubjectClassAttendanceActivity.this, CheckAttendanceActivity.class);                intent.putExtra("SUBJECT_ID", selectedSubject.subjectId);
                intent.putExtra("SUBJECT_CODE", selectedSubject.subjectCode);
                intent.putExtra("CLASS_ID", selectedClassSubject.classId);
                intent.putExtra("CLASS_SUBJECT_ID", selectedClassSubject.classSubjectId);
                intent.putExtra("room", selectedClassSubject.room);

                // Đồng thời truyền theo key mà TeacherInputGradesActivity đang đọc
                // className: lấy phần trước dấu "_" của classId (VD: "SE1856" từ "SE1856_FALL2024")
                String className = selectedClassSubject.classId != null
                        ? selectedClassSubject.classId.split("_")[0]
                        : null;
                intent.putExtra("className", className);
                intent.putExtra("subject", selectedSubject.subjectCode);
                intent.putExtra("room_display", selectedClassSubject.room);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn đầy đủ môn học và lớp", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        spinnerSubject.setEnabled(!show);
        spinnerClass.setEnabled(!show && !classSubjectList.isEmpty());
        btnContinue.setEnabled(!show && selectedSubject != null && selectedClassSubject != null);
    }

    // Model classes
    static class Subject {
        String subjectId;
        String subjectCode;
        String subjectName;
        String description;
    }

    static class ClassSubject {
        String classSubjectId;  // VD: SE1856_FALL2024_PRM392
        String classId;         // VD: SE1856_FALL2024
        String subjectId;       // VD: PRM392
        String teacherId;
        String room;
        String schedule;
        boolean isActive;
    }
}