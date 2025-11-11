package com.example.fapapplication.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.adapter.AttendanceHistoryAdapter;
import com.example.fapapplication.model.Attendance;
import com.example.fapapplication.model.AttendanceHistoryItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentAttendanceActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private TextView tvAvatar, tvStudentName, tvStudentId, tvEmail;
    private TextView tvTotalSessions, tvPresentCount, tvAbsentCount, tvAttendanceRate;
    private Button btnFilterAll, btnFilterPresent, btnFilterAbsent;
    private RecyclerView rvAttendanceHistory;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    // Data
    private FirebaseFirestore db;
    private DatabaseReference realtimeDb;
    private AttendanceHistoryAdapter adapter;
    private List<AttendanceHistoryItem> attendanceList;

    // Student info
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference();

        // Get student info
        getStudentInfo();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load student data from Realtime Database first
        loadStudentFromRealtimeDatabase();
    }

    private void getStudentInfo() {
        // OPTION 1: Lấy từ Intent (nếu có)
        studentId = getIntent().getStringExtra("studentId");

        // OPTION 2: Lấy từ SharedPreferences
        if (studentId == null || studentId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            studentId = prefs.getString("studentId", "");
        }

        if (studentId == null || studentId.isEmpty()) {
            studentId = "he170188"; // Test
            android.util.Log.w("StudentAttendance", "Using fallback test studentId");
        }

        android.util.Log.d("StudentAttendance", "Final studentId: " + studentId);
    }

    private void loadStudentFromRealtimeDatabase() {
        showLoading(true);

        android.util.Log.d("StudentAttendance", "Loading student data from Realtime Database...");
        android.util.Log.d("StudentAttendance", "Searching for StudentId: " + studentId);

        // Query by StudentId field
        realtimeDb.child("Users")
                .orderByChild("StudentId")
                .equalTo(studentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Get student data from Realtime DB
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                studentName = userSnapshot.child("FullName").getValue(String.class);
                                studentEmail = userSnapshot.child("Email").getValue(String.class);
                                className = userSnapshot.child("Campus").getValue(String.class);

                                if (studentName == null || studentName.isEmpty()) {
                                    studentName = "Unknown Student";
                                }
                                if (studentEmail == null || studentEmail.isEmpty()) {
                                    studentEmail = studentId + "@fpt.edu.vn";
                                }
                                if (className == null || className.isEmpty()) {
                                    className = "N/A";
                                }

                                android.util.Log.d("StudentAttendance", "✓ Loaded from Realtime DB");
                                updateStudentUI();
                                loadAttendanceData();
                                break;
                            }
                        } else {
                            // NẾU KHÔNG TÌM THẤY TRONG REALTIME DB
                            // Fallback: Lấy từ Firebase Auth
                            android.util.Log.w("StudentAttendance", "Student not found in Realtime DB, using Firebase Auth data");
                            loadFromFirebaseAuth();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        android.util.Log.e("StudentAttendance", "Database error, using Firebase Auth fallback");
                        loadFromFirebaseAuth();
                    }
                });
    }

    private void loadFromFirebaseAuth() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            studentEmail = currentUser.getEmail();
            studentName = currentUser.getDisplayName();

            if (studentName == null || studentName.isEmpty()) {
                studentName = studentEmail != null ? studentEmail.split("@")[0] : "Student";
            }

            className = "N/A";

            android.util.Log.d("StudentAttendance", "✓ Using Firebase Auth data");
            android.util.Log.d("StudentAttendance", "  Name: " + studentName);
            android.util.Log.d("StudentAttendance", "  Email: " + studentEmail);

            updateStudentUI();
            loadAttendanceData();
        } else {
            showLoading(false);
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateStudentUI() {
        if (studentName != null && !studentName.isEmpty()) {
            tvAvatar.setText(studentName.substring(0, 1).toUpperCase());
            tvStudentName.setText(studentName);
        }
        tvStudentId.setText(studentId.toUpperCase()); // Display uppercase
        tvEmail.setText(studentEmail);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvEmail = findViewById(R.id.tvEmail);
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        tvAttendanceRate = findViewById(R.id.tvAttendanceRate);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPresent = findViewById(R.id.btnFilterPresent);
        btnFilterAbsent = findViewById(R.id.btnFilterAbsent);
        rvAttendanceHistory = findViewById(R.id.rvAttendanceHistory);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        progressBar = findViewById(R.id.progressBar);
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
        attendanceList = new ArrayList<>();
        adapter = new AttendanceHistoryAdapter(attendanceList);
        rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(this));
        rvAttendanceHistory.setAdapter(adapter);
    }

    private void setupListeners() {
        // Filter All
        btnFilterAll.setOnClickListener(v -> {
            adapter.filterAll();
            updateFilterButtons(btnFilterAll);
            updateEmptyState();
        });

        // Filter Present
        btnFilterPresent.setOnClickListener(v -> {
            adapter.filterPresent();
            updateFilterButtons(btnFilterPresent);
            updateEmptyState();
        });

        // Filter Absent
        btnFilterAbsent.setOnClickListener(v -> {
            adapter.filterAbsent();
            updateFilterButtons(btnFilterAbsent);
            updateEmptyState();
        });
    }

    private void updateFilterButtons(Button activeButton) {
        // Reset all buttons
        btnFilterAll.setBackgroundTintList(
                ContextCompat.getColorStateList(this, android.R.color.transparent));
        btnFilterAll.setTextColor(ContextCompat.getColor(this, R.color.purple_500));

        btnFilterPresent.setBackgroundTintList(
                ContextCompat.getColorStateList(this, android.R.color.transparent));
        btnFilterPresent.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));

        btnFilterAbsent.setBackgroundTintList(
                ContextCompat.getColorStateList(this, android.R.color.transparent));
        btnFilterAbsent.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));

        // Highlight active button
        if (activeButton == btnFilterAll) {
            activeButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.purple_500));
            activeButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else if (activeButton == btnFilterPresent) {
            activeButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
            activeButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else if (activeButton == btnFilterAbsent) {
            activeButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
            activeButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void loadAttendanceData() {
        // showLoading đã được gọi trong loadStudentFromRealtimeDatabase()

        android.util.Log.d("StudentAttendance", "Loading attendance for: " + studentId);

        // Query với studentId LOWERCASE (như trong Firestore)
        db.collection("attendances")
                .whereEqualTo("studentId", studentId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    attendanceList.clear();

                    android.util.Log.d("StudentAttendance",
                            "Found " + queryDocumentSnapshots.size() + " attendance records");

                    int totalSessions = 0;
                    int presentCount = 0;
                    int absentCount = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Attendance attendance = doc.toObject(Attendance.class);
                        if (attendance != null) {
                            AttendanceHistoryItem item = new AttendanceHistoryItem(
                                    attendance.getDate(),
                                    attendance.getTime(),
                                    attendance.getClassName(),
                                    attendance.getSubject(),
                                    attendance.isStatus(),
                                    attendance.getClassSessionId()
                            );
                            attendanceList.add(item);

                            totalSessions++;
                            if (attendance.isStatus()) {
                                presentCount++;
                            } else {
                                absentCount++;
                            }
                        }
                    }

                    // Update statistics
                    updateStatistics(totalSessions, presentCount, absentCount);

                    // Update adapter
                    adapter.notifyDataSetChanged();

                    // Update empty state
                    updateEmptyState();

                    showLoading(false);

                    android.util.Log.d("StudentAttendance", "✓ Attendance loaded successfully");
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error loading attendance: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    android.util.Log.e("StudentAttendance", "Error: " + e.getMessage(), e);
                    updateEmptyState();
                });
    }

    private void updateStatistics(int total, int present, int absent) {
        tvTotalSessions.setText(String.valueOf(total));
        tvPresentCount.setText(String.valueOf(present));
        tvAbsentCount.setText(String.valueOf(absent));

        // Calculate attendance rate
        if (total > 0) {
            double rate = (present * 100.0) / total;
            tvAttendanceRate.setText(String.format(Locale.getDefault(), "%.0f%%", rate));
        } else {
            tvAttendanceRate.setText("0%");
        }
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvAttendanceHistory.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvAttendanceHistory.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAttendanceHistory.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}