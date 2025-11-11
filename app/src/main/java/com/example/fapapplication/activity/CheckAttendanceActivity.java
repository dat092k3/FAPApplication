package com.example.fapapplication.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.adapter.StudentAttendanceAdapter;
import com.example.fapapplication.model.Student;
import com.example.fapapplication.model.StudentAttendanceItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckAttendanceActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private TextView tvClassName, tvSubject, tvDate, tvTime;
    private TextView tvTotalStudents, tvPresentCount, tvAbsentCount;
    private EditText etSearch;
    private Button btnMarkAllPresent, btnMarkAllAbsent, btnSave;
    private RecyclerView rvStudents;
    private ProgressBar progressBar;

    // Data
    private FirebaseFirestore db;
    private StudentAttendanceAdapter adapter;
    private List<StudentAttendanceItem> studentList;

    // Class info (nhận từ Intent)
    private String className;
    private String classId;
    private String classSubjectId;
    private String subject;
    private String date;
    private String time;
    private String classSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get data from Intent
        getIntentData();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load data
        loadStudentsAndAttendance();
    }

    private void getIntentData() {
        className = getIntent().getStringExtra("className");
        classId = getIntent().getStringExtra("CLASS_ID");
        classSubjectId = getIntent().getStringExtra("CLASS_SUBJECT_ID");
        subject = getIntent().getStringExtra("subject");
        if (subject == null || subject.isEmpty()) {
            subject = getIntent().getStringExtra("SUBJECT_CODE");
        }
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        android.util.Log.d("CheckAttendance", "==== INTENT DATA ====");
        android.util.Log.d("CheckAttendance", "className: " + className);
        android.util.Log.d("CheckAttendance", "classId: " + classId);
        android.util.Log.d("CheckAttendance", "classSubjectId: " + classSubjectId);
        android.util.Log.d("CheckAttendance", "subject: " + subject);
        android.util.Log.d("CheckAttendance", "date: " + date);
        android.util.Log.d("CheckAttendance", "time: " + time);

        if (className == null || className.isEmpty()) {
            if (classId != null && !classId.isEmpty()) {
                android.util.Log.w("CheckAttendance", "className missing, deriving from classId");
                className = classId.contains("_") ? classId.split("_")[0] : classId;
            } else {
                android.util.Log.w("CheckAttendance", "⚠️ className is NULL! Setting default to SE1856");
                className = "SE1856"; // Default value để test
            }
        }

        if (subject == null || subject.isEmpty()) {
            android.util.Log.w("CheckAttendance", "⚠️ subject is NULL! Setting default");
            subject = "PRM392";
        }

        // Nếu không có date, dùng ngày hiện tại
        if (date == null || date.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            date = sdf.format(new Date());
            android.util.Log.w("CheckAttendance", "⚠️ date is NULL! Using today: " + date);
        }

        if (time == null || time.isEmpty()) {
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY); // 0-23
            int minute = now.get(Calendar.MINUTE);    // 0-59
            time = String.format("%02d:%02d", hour, minute);
            android.util.Log.w("CheckAttendance", "⚠️ time is NULL! Setting current time: " + time);
        }

        // Tạo classSessionId
        classSessionId = className + "_" + date.replace("/", "");
        android.util.Log.d("CheckAttendance", "classSessionId: " + classSessionId);
        android.util.Log.d("CheckAttendance", "====================");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvClassName = findViewById(R.id.tvClassName);
        tvSubject = findViewById(R.id.tvSubject);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        etSearch = findViewById(R.id.etSearch);
        btnMarkAllPresent = findViewById(R.id.btnMarkAllPresent);
        btnMarkAllAbsent = findViewById(R.id.btnMarkAllAbsent);
        btnSave = findViewById(R.id.btnSave);
        rvStudents = findViewById(R.id.rvStudents);
        progressBar = findViewById(R.id.progressBar);

        // Set class info
        String classDisplay = className;
        if ((classDisplay == null || classDisplay.isEmpty()) && classId != null && !classId.isEmpty()) {
            classDisplay = classId;
        }
        tvClassName.setText(classDisplay);
        tvSubject.setText("Subject: " + subject);
        tvDate.setText("Date: " + date);
        tvTime.setText("Time: " + time);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        studentList = new ArrayList<>();
        adapter = new StudentAttendanceAdapter(studentList, this::updateStatistics);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);
    }

    private void setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Mark all present
        btnMarkAllPresent.setOnClickListener(v -> {
            adapter.markAll(true);
            updateStatistics();
        });

        // Mark all absent
        btnMarkAllAbsent.setOnClickListener(v -> {
            adapter.markAll(false);
            updateStatistics();
        });

        // Save attendance
        btnSave.setOnClickListener(v -> saveAttendance());
    }

    private void loadStudentsAndAttendance() {
        showLoading(true);

        android.util.Log.d("CheckAttendance", "=== START LOADING ===");
        android.util.Log.d("CheckAttendance", "ClassName: " + className);
        android.util.Log.d("CheckAttendance", "Subject: " + subject);
        android.util.Log.d("CheckAttendance", "Date: " + date);
        android.util.Log.d("CheckAttendance", "ClassSessionId: " + classSessionId);

        // OPTION 1: Danh sách students cứng (hard-coded)
        List<Student> hardcodedStudents = getHardcodedStudents();

        android.util.Log.d("CheckAttendance", "Hardcoded students count: " + hardcodedStudents.size());

        if (!hardcodedStudents.isEmpty()) {
            android.util.Log.d("CheckAttendance", "Using hardcoded students");
            loadAttendanceForStudents(hardcodedStudents);
            return;
        }

        android.util.Log.d("CheckAttendance", "No hardcoded students, trying Firebase...");

        // OPTION 2: Load từ Firebase collection "students"
        Query studentQuery;
        boolean attemptedClassIdQuery = false;

        if (classId != null && !classId.isEmpty()) {
            attemptedClassIdQuery = true;
            studentQuery = db.collection("students")
                    .whereEqualTo("classId", classId);
            android.util.Log.d("CheckAttendance", "Loading students by classId: " + classId);
        } else {
            studentQuery = db.collection("students")
                    .whereEqualTo("className", className);
            android.util.Log.d("CheckAttendance", "Loading students by className: " + className);
        }

        final boolean shouldFallbackToClassName = attemptedClassIdQuery && className != null && !className.isEmpty();

        studentQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("CheckAttendance", "Firebase query success");
                    android.util.Log.d("CheckAttendance", "Documents found: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        if (shouldFallbackToClassName) {
                            android.util.Log.w("CheckAttendance", "No students found with classId. Falling back to className query.");
                            loadStudentsByClassNameFallback();
                        } else {
                            showLoading(false);
                            Toast.makeText(this,
                                    "No students found. Please add students to the list.",
                                    Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    List<Student> students = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            students.add(student);
                        }
                    }

                    loadAttendanceForStudents(students);
                })
                .addOnFailureListener(e -> {
                    if (shouldFallbackToClassName) {
                        android.util.Log.w("CheckAttendance", "ClassId student query failed, retrying with className", e);
                        loadStudentsByClassNameFallback();
                    } else {
                        showLoading(false);
                        String errorMsg = "Error loading students: " + e.getMessage();
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        android.util.Log.e("LoadStudents", "Error details: ", e);
                        e.printStackTrace();
                    }
                });
    }

    private void loadStudentsByClassNameFallback() {
        if (className == null || className.isEmpty()) {
            showLoading(false);
            Toast.makeText(this,
                    "No class information available to load students.",
                    Toast.LENGTH_LONG).show();
            android.util.Log.e("CheckAttendance", "Fallback to className failed: className is empty");
            return;
        }

        android.util.Log.d("CheckAttendance", "Fallback loading students by className: " + className);

        db.collection("students")
                .whereEqualTo("className", className)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("CheckAttendance", "Fallback query success");
                    android.util.Log.d("CheckAttendance", "Documents found: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        showLoading(false);
                        Toast.makeText(this,
                                "No students found for class " + className,
                                Toast.LENGTH_LONG).show();
                        android.util.Log.w("CheckAttendance", "Fallback query returned empty list");
                        return;
                    }

                    List<Student> students = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            students.add(student);
                        }
                    }

                    loadAttendanceForStudents(students);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    String errorMsg = "Error loading students: " + e.getMessage();
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    android.util.Log.e("LoadStudentsFallback", "Error details: ", e);
                });
    }

    // Danh sách students cứng - Thay đổi theo class của bạn
    private List<Student> getHardcodedStudents() {
        return new ArrayList<>(); // Trả về rỗng → tự động load từ Firebase
    }

    // Load attendance status cho danh sách students
    private void loadAttendanceForStudents(List<Student> students) {
        android.util.Log.d("CheckAttendance", "Loading attendance for " + students.size() + " students");

        studentList.clear();

        // Load existing attendance records
        Query attendanceQuery = db.collection("attendances")
                .whereEqualTo("classSessionId", classSessionId);

        if (subject != null && !subject.isEmpty()) {
            attendanceQuery = attendanceQuery.whereEqualTo("subject", subject);
        }

        attendanceQuery.get()
                .addOnSuccessListener(attendanceSnapshots -> {
                    android.util.Log.d("CheckAttendance", "Attendance query success");
                    android.util.Log.d("CheckAttendance", "Attendance records found: " + attendanceSnapshots.size());

                    // Create a map of existing attendance
                    Map<String, Boolean> attendanceMap = new HashMap<>();
                    for (DocumentSnapshot doc : attendanceSnapshots.getDocuments()) {
                        String studentId = doc.getString("studentId");
                        Boolean status = doc.getBoolean("status");
                        if (studentId != null && status != null) {
                            attendanceMap.put(studentId, status);
                            android.util.Log.d("CheckAttendance",
                                    "Student " + studentId + " status: " + status);
                        }
                    }

                    // Create student attendance items
                    for (Student student : students) {
                        boolean isPresent = attendanceMap.getOrDefault(student.getStudentId(), false);
                        studentList.add(new StudentAttendanceItem(student, isPresent));
                        android.util.Log.d("CheckAttendance",
                                "Added student: " + student.getStudentId() + " - " +
                                        student.getStudentName() + " (Present: " + isPresent + ")");
                    }

                    adapter.notifyDataSetChanged();
                    updateStatistics();
                    showLoading(false);

                    android.util.Log.d("CheckAttendance", "✓ Successfully loaded " + studentList.size() + " students");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.w("CheckAttendance", "No attendance records found, marking all as absent");

                    // Nếu không có attendance record, tất cả là absent (false)
                    for (Student student : students) {
                        studentList.add(new StudentAttendanceItem(student, false));
                        android.util.Log.d("CheckAttendance",
                                "Added student (no attendance): " + student.getStudentId() + " - " +
                                        student.getStudentName());
                    }

                    adapter.notifyDataSetChanged();
                    updateStatistics();
                    showLoading(false);

                    android.util.Log.d("CheckAttendance", "✓ Loaded " + studentList.size() + " students (all absent)");
                });
    }

    private void saveAttendance() {
        showLoading(true);

        // Sử dụng WriteBatch để lưu nhiều documents cùng lúc (hiệu quả hơn)
        WriteBatch batch = db.batch();

        for (StudentAttendanceItem item : adapter.getStudentList()) {
            Student student = item.getStudent();

            // Tạo document ID theo format của bạn: studentId_classSessionId
            // Ví dụ: HE170188_SE1856_20251106
            String documentId = student.getStudentId() + "_" + classSessionId;

            // Tạo Map để đẩy dữ liệu lên Firebase
            Map<String, Object> attendanceData = new HashMap<>();
            attendanceData.put("studentId", student.getStudentId());
            attendanceData.put("classSessionId", classSessionId);
            attendanceData.put("className", className);
            if (classId != null && !classId.isEmpty()) {
                attendanceData.put("classId", classId);
            }
            if (classSubjectId != null && !classSubjectId.isEmpty()) {
                attendanceData.put("classSubjectId", classSubjectId);
            }
            attendanceData.put("date", date);
            attendanceData.put("subject", subject);
            attendanceData.put("status", item.isPresent()); // true = present, false = absent
            attendanceData.put("time", time);

            // Thêm vào batch
            batch.set(db.collection("attendances").document(documentId), attendanceData);
        }

        // Commit tất cả changes cùng lúc
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "✓ Attendance saved to Firebase successfully!",
                            Toast.LENGTH_LONG).show();

                    // Log để kiểm tra
                    android.util.Log.d("AttendanceSave", "Successfully saved " +
                            adapter.getStudentList().size() + " attendance records");

                    finish(); // Quay lại màn hình trước
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "✗ Error saving attendance: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Log error để debug
                    android.util.Log.e("AttendanceSave", "Error: " + e.getMessage(), e);
                });
    }

    private void updateStatistics() {
        int total = studentList.size();
        int present = 0;
        int absent = 0;

        for (StudentAttendanceItem item : studentList) {
            if (item.isPresent()) {
                present++;
            } else {
                absent++;
            }
        }

        tvTotalStudents.setText(String.valueOf(total));
        tvPresentCount.setText(String.valueOf(present));
        tvAbsentCount.setText(String.valueOf(absent));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}