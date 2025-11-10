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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.adapter.StudentGradeAdapter;
import com.example.fapapplication.model.Grade;
import com.example.fapapplication.model.Student;
import com.example.fapapplication.model.StudentGradeItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherInputGradesActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private TextView tvClassName, tvSubject, tvTotalStudents;
    private EditText etSearch;
    private RecyclerView rvStudentGrades;
    private Button btnSaveAll;
    private ProgressBar progressBar;

    // Data
    private FirebaseFirestore db;
    private StudentGradeAdapter adapter;
    private List<StudentGradeItem> gradeList;

    // Class info
    private String className;
    private String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_input_grades);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get class info from Intent
        getClassInfo();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load data
        loadStudentsAndGrades();
    }

    private void getClassInfo() {
        className = getIntent().getStringExtra("className");
        subject = getIntent().getStringExtra("subject");

        // Default values for testing
        if (className == null || className.isEmpty()) {
            className = "SE1856";
            subject = "PRM392";
        }

        android.util.Log.d("TeacherGrades", "ClassName: " + className);
        android.util.Log.d("TeacherGrades", "Subject: " + subject);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvClassName = findViewById(R.id.tvClassName);
        tvSubject = findViewById(R.id.tvSubject);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        etSearch = findViewById(R.id.etSearch);
        rvStudentGrades = findViewById(R.id.rvStudentGrades);
        btnSaveAll = findViewById(R.id.btnSaveAll);
        progressBar = findViewById(R.id.progressBar);

        // Set class info
        tvClassName.setText(className);
        tvSubject.setText("Subject: " + subject);
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
        gradeList = new ArrayList<>();
        adapter = new StudentGradeAdapter(gradeList);
        rvStudentGrades.setLayoutManager(new LinearLayoutManager(this));
        rvStudentGrades.setAdapter(adapter);
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

        // Save all grades
        btnSaveAll.setOnClickListener(v -> showSaveConfirmDialog());
    }

    private void loadStudentsAndGrades() {
        showLoading(true);

        // TẮT HARDCODE - Load hoàn toàn từ Firebase
        loadStudentsFromFirebase();
    }

    private List<Student> getHardcodedStudents() {
        // TRẢ VỀ EMPTY LIST - Không dùng hardcode nữa
        return new ArrayList<>();
        
        /* COMMENT CODE CŨ
        List<Student> students = new ArrayList<>();
        
        if ("SE1856".equals(className)) {
            students.add(new Student("HE170188", "Nguyen Van A", "he170188@fpt.edu.vn"));
            students.add(new Student("HE170189", "Tran Thi B", "he170189@fpt.edu.vn"));
            students.add(new Student("HE170190", "Le Van C", "he170190@fpt.edu.vn"));
            students.add(new Student("HE170191", "Pham Thi D", "he170191@fpt.edu.vn"));
            students.add(new Student("HE170192", "Hoang Van E", "he170192@fpt.edu.vn"));
        }
        
        return students;
        */
    }

    private void loadStudentsFromFirebase() {
        db.collection("students")
                .whereEqualTo("className", className)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Student> students = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            students.add(student);
                        }
                    }

                    if (students.isEmpty()) {
                        showLoading(false);
                        Toast.makeText(this, "No students found in this class",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    loadGradesForStudents(students);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error loading students: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGradesForStudents(List<Student> students) {
        gradeList.clear();

        // Load existing grades from Firebase
        db.collection("grades")
                .whereEqualTo("className", className)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a map of existing grades
                    Map<String, Grade> gradesMap = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Grade grade = doc.toObject(Grade.class);
                        if (grade != null) {
                            gradesMap.put(grade.getStudentId(), grade);
                        }
                    }

                    // Create StudentGradeItem for each student
                    for (Student student : students) {
                        Grade grade = gradesMap.get(student.getStudentId());
                        if (grade == null) {
                            // Create new grade if not exists
                            grade = new Grade(student.getStudentId(), className, subject);
                        }
                        gradeList.add(new StudentGradeItem(student, grade));
                    }

                    // Update UI
                    tvTotalStudents.setText(String.valueOf(students.size()));
                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    android.util.Log.d("TeacherGrades", "Loaded " + gradeList.size() + " students");
                })
                .addOnFailureListener(e -> {
                    // If no grades exist yet, create empty grades
                    for (Student student : students) {
                        Grade grade = new Grade(student.getStudentId(), className, subject);
                        gradeList.add(new StudentGradeItem(student, grade));
                    }

                    tvTotalStudents.setText(String.valueOf(students.size()));
                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    android.util.Log.d("TeacherGrades", "No existing grades, created empty grades");
                });
    }

    private void showSaveConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Save Grades")
                .setMessage("Are you sure you want to save all grades? This will overwrite existing grades.")
                .setPositiveButton("Save", (dialog, which) -> saveAllGrades())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveAllGrades() {
        showLoading(true);

        WriteBatch batch = db.batch();
        int savedCount = 0;

        for (StudentGradeItem item : adapter.getGradeList()) {
            Grade grade = item.getGrade();

            // Calculate average before saving
            grade.calculateAverage();

            // Document ID: studentId_className
            String documentId = grade.getStudentId() + "_" + className;

            // Create data map
            Map<String, Object> gradeData = new HashMap<>();
            gradeData.put("studentId", grade.getStudentId());
            gradeData.put("className", grade.getClassName());
            gradeData.put("subject", grade.getSubject());
            gradeData.put("pt1", grade.getPt1());
            gradeData.put("pt2", grade.getPt2());
            gradeData.put("participation", grade.getParticipation());
            gradeData.put("pe", grade.getPe());
            gradeData.put("fe", grade.getFe());
            gradeData.put("average", grade.getAverage());
            gradeData.put("lastUpdated", com.google.firebase.firestore.FieldValue.serverTimestamp());

            batch.set(db.collection("grades").document(documentId), gradeData);
            savedCount++;
        }

        final int finalSavedCount = savedCount;
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this,
                            "✓ Successfully saved grades for " + finalSavedCount + " students!",
                            Toast.LENGTH_LONG).show();

                    android.util.Log.d("TeacherGrades", "Saved " + finalSavedCount + " grades");
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error saving grades: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    android.util.Log.e("TeacherGrades", "Error: " + e.getMessage(), e);
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveAll.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("Do you want to save changes before leaving?")
                .setPositiveButton("Save", (dialog, which) -> {
                    saveAllGrades();
                    finish();
                })
                .setNegativeButton("Discard", (dialog, which) -> finish())
                .setNeutralButton("Cancel", null)
                .show();
    }
}