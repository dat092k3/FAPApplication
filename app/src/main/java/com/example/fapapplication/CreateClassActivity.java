package com.example.fapapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.WizardStudentAdapter;
import com.example.fapapplication.adapter.WizardSubjectAdapter;
import com.example.fapapplication.entity.Class;
import com.example.fapapplication.entity.ClassSubject;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.entity.User;
import com.example.fapapplication.entity.UserClassSubject;
import com.example.fapapplication.model.WizardStudent;
import com.example.fapapplication.model.WizardSubject;
import com.example.fapapplication.repository.ClassRepository;
import com.example.fapapplication.repository.ClassSubjectRepository;
import com.example.fapapplication.repository.SubjectRepository;
import com.example.fapapplication.repository.UserClassSubjectRepository;
import com.example.fapapplication.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateClassActivity extends AppCompatActivity {

    // UI Components
    private ViewFlipper viewFlipper;
    private ProgressBar progressBar;
    private Button btnBack, btnSkip, btnNext;
    private TextView tvStep1Number, tvStep2Number, tvStep3Number;

    // Step 1 - Basic Info
    private EditText etClassName, etSemester, etDescription;
    private SwitchCompat switchActive;

    // Step 2 - Subjects
    private RecyclerView recyclerViewSubjects;
    private View emptyStateSubjects;
    private Button btnAddSubject;
    private WizardSubjectAdapter subjectAdapter;

    // Step 3 - Students
    private RecyclerView recyclerViewStudents;
    private View emptyStateStudents;
    private View warningNoSubjects;
    private Button btnAssignStudent;
    private WizardStudentAdapter studentAdapter;

    // Data
    private List<WizardSubject> wizardSubjects;
    private List<WizardStudent> wizardStudents;
    private List<Subject> availableSubjects;
    private List<User> availableTeachers;
    private List<User> availableStudents;
    private Map<String, String> subjectCodeMap;

    // Repositories
    private ClassRepository classRepository;
    private ClassSubjectRepository classSubjectRepository;
    private SubjectRepository subjectRepository;
    private UserRepository userRepository;
    private UserClassSubjectRepository userClassSubjectRepository;

    // State
    private int currentStep = 0;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        initializeRepositories();
        initializeViews();
        initializeData();
        setupListeners();
        loadInitialData();
        updateStepIndicator();
        updateNavigationButtons();
    }

    private void initializeRepositories() {
        classRepository = ClassRepository.getInstance();
        classSubjectRepository = new ClassSubjectRepository();
        subjectRepository = SubjectRepository.getInstance();
        userRepository = new UserRepository();
        userClassSubjectRepository = new UserClassSubjectRepository();
    }

    private void initializeViews() {
        // Header
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> handleBackPressed());

        // Navigation
        viewFlipper = findViewById(R.id.viewFlipper);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);

        // Step indicators
        tvStep1Number = findViewById(R.id.tvStep1Number);
        tvStep2Number = findViewById(R.id.tvStep2Number);
        tvStep3Number = findViewById(R.id.tvStep3Number);

        // Step 1 views
        etClassName = findViewById(R.id.etClassName);
        etSemester = findViewById(R.id.etSemester);
        etDescription = findViewById(R.id.etDescription);
        switchActive = findViewById(R.id.switchActive);

        // Step 2 views
        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        emptyStateSubjects = findViewById(R.id.emptyStateLayout);
        btnAddSubject = findViewById(R.id.btnAddSubject);

        // Step 3 views
        View step3Layout = findViewById(R.id.step3Layout);
        recyclerViewStudents = step3Layout.findViewById(R.id.recyclerViewStudents);
        emptyStateStudents = step3Layout.findViewById(R.id.emptyStateLayout);
        warningNoSubjects = step3Layout.findViewById(R.id.warningNoSubjects);
        btnAssignStudent = step3Layout.findViewById(R.id.btnAssignStudent);
    }

    private void initializeData() {
        wizardSubjects = new ArrayList<>();
        wizardStudents = new ArrayList<>();
        availableSubjects = new ArrayList<>();
        availableTeachers = new ArrayList<>();
        availableStudents = new ArrayList<>();
        subjectCodeMap = new HashMap<>();

        // Setup adapters
        subjectAdapter = new WizardSubjectAdapter();
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubjects.setAdapter(subjectAdapter);

        studentAdapter = new WizardStudentAdapter();
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStudents.setAdapter(studentAdapter);

        subjectAdapter.setOnSubjectActionListener((subject, position) -> {
            wizardSubjects.remove(position);
            updateStep2UI();
            hasUnsavedChanges = true;
        });

        studentAdapter.setOnStudentActionListener((student, position) -> {
            wizardStudents.remove(position);
            updateStep3UI();
            hasUnsavedChanges = true;
        });
    }

    private void setupListeners() {
        // Navigation buttons
        btnBack.setOnClickListener(v -> navigateBack());
        btnSkip.setOnClickListener(v -> navigateNext());
        btnNext.setOnClickListener(v -> handleNextClick());

        // Step 2 - Add Subject
        btnAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        // Step 3 - Assign Student
        btnAssignStudent.setOnClickListener(v -> showAssignStudentDialog());

        // Text change listeners
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasUnsavedChanges = true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etClassName.addTextChangedListener(textWatcher);
        etSemester.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> hasUnsavedChanges = true);
    }

    private void loadInitialData() {
        showLoading(true);
        final int[] loadedCount = {0};
        final int totalLoads = 3;

        // Load subjects
        subjectRepository.getAllSubjectsOnce(new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                availableSubjects = subjects;
                for (Subject subject : subjects) {
                    subjectCodeMap.put(subject.getId(), subject.getSubjectCode());
                }
                studentAdapter.setSubjectCodeMap(subjectCodeMap);
                loadedCount[0]++;
                if (loadedCount[0] >= totalLoads) {
                    showLoading(false);
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadedCount[0]++;
                if (loadedCount[0] >= totalLoads) {
                    showLoading(false);
                }
            }
        });

        // Load users (teachers and students)
        userRepository.getAllUsers(new UserRepository.OnUsersLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                availableTeachers.clear();
                availableStudents.clear();
                for (User user : users) {
                    if ("Teacher".equals(user.getRole())) {
                        availableTeachers.add(user);
                    } else if ("Student".equals(user.getRole())) {
                        availableStudents.add(user);
                    }
                }
                loadedCount[0] += 2;
                if (loadedCount[0] >= totalLoads) {
                    showLoading(false);
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadedCount[0] += 2;
                if (loadedCount[0] >= totalLoads) {
                    showLoading(false);
                }
                Toast.makeText(CreateClassActivity.this, "Error loading data: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigation Methods
    private void handleNextClick() {
        if (currentStep == 0) {
            if (validateStep1()) {
                navigateNext();
            }
        } else if (currentStep == 1) {
            navigateNext();
        } else if (currentStep == 2) {
            // Final step - create class
            createClass();
        }
    }

    private void navigateNext() {
        if (currentStep < 2) {
            currentStep++;
            viewFlipper.setDisplayedChild(currentStep);
            updateStepIndicator();
            updateNavigationButtons();
            updateStepUI();
        }
    }

    private void navigateBack() {
        if (currentStep > 0) {
            currentStep--;
            viewFlipper.setDisplayedChild(currentStep);
            updateStepIndicator();
            updateNavigationButtons();
        }
    }

    private void updateStepIndicator() {
        // Reset all steps
        tvStep1Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCCCCCC));
        tvStep2Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCCCCCC));
        tvStep3Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCCCCCC));

        // Highlight current step
        switch (currentStep) {
            case 0:
                tvStep1Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFC107));
                break;
            case 1:
                tvStep1Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                tvStep2Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFC107));
                break;
            case 2:
                tvStep1Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                tvStep2Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                tvStep3Number.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFC107));
                break;
        }
    }

    private void updateNavigationButtons() {
        // Back button
        btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);

        // Skip button (only show on steps 2 and 3)
        btnSkip.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);

        // Next button text
        if (currentStep == 2) {
            btnNext.setText("Finish");
        } else {
            btnNext.setText("Next");
        }
    }

    private void updateStepUI() {
        switch (currentStep) {
            case 1:
                updateStep2UI();
                break;
            case 2:
                updateStep3UI();
                break;
        }
    }

    // Step 1 - Validation
    private boolean validateStep1() {
        String className = etClassName.getText().toString().trim();
        String semester = etSemester.getText().toString().trim();

        if (className.isEmpty()) {
            etClassName.setError("Class name is required");
            etClassName.requestFocus();
            return false;
        }

        if (semester.isEmpty()) {
            etSemester.setError("Semester is required");
            etSemester.requestFocus();
            return false;
        }

        return true;
    }

    // Step 2 - Subject Management
    private void updateStep2UI() {
        if (wizardSubjects.isEmpty()) {
            recyclerViewSubjects.setVisibility(View.GONE);
            emptyStateSubjects.setVisibility(View.VISIBLE);
        } else {
            recyclerViewSubjects.setVisibility(View.VISIBLE);
            emptyStateSubjects.setVisibility(View.GONE);
            subjectAdapter.updateSubjects(wizardSubjects);
        }
    }

    private void showAddSubjectDialog() {
        if (availableSubjects.isEmpty()) {
            Toast.makeText(this, "No subjects available", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_class_subject, null);

        Spinner spinnerSubject = dialogView.findViewById(R.id.spinnerSubject);
        Spinner spinnerTeacher = dialogView.findViewById(R.id.spinnerTeacher);
        EditText etSchedule = dialogView.findViewById(R.id.etSchedule);
        EditText etRoom = dialogView.findViewById(R.id.etRoom);
        SwitchCompat switchActive = dialogView.findViewById(R.id.switchActive);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Setup subject spinner
        List<String> subjectNames = new ArrayList<>();
        for (Subject subject : availableSubjects) {
            subjectNames.add(subject.getSubjectCode() + " - " + subject.getSubjectName());
        }
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjectNames);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        // Setup teacher spinner
        List<String> teacherNames = new ArrayList<>();
        teacherNames.add("No teacher assigned");
        for (User teacher : availableTeachers) {
            teacherNames.add(teacher.getName());
        }
        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teacherNames);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeacher.setAdapter(teacherAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int subjectPos = spinnerSubject.getSelectedItemPosition();
            int teacherPos = spinnerTeacher.getSelectedItemPosition();

            if (subjectPos < 0 || subjectPos >= availableSubjects.size()) {
                Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }

            Subject selectedSubject = availableSubjects.get(subjectPos);
            String teacherId = null;
            String teacherName = null;
            if (teacherPos > 0 && teacherPos <= availableTeachers.size()) {
                User selectedTeacher = availableTeachers.get(teacherPos - 1);
                teacherId = selectedTeacher.getId();
                teacherName = selectedTeacher.getName();
            }

            String schedule = etSchedule.getText().toString().trim();
            String room = etRoom.getText().toString().trim();
            boolean isActive = switchActive.isChecked();

            WizardSubject wizardSubject = new WizardSubject(
                    selectedSubject.getId(),
                    selectedSubject.getSubjectCode(),
                    selectedSubject.getSubjectName(),
                    teacherId,
                    teacherName,
                    schedule,
                    room
            );
            wizardSubject.setActive(isActive);

            wizardSubjects.add(wizardSubject);
            updateStep2UI();
            hasUnsavedChanges = true;
            dialog.dismiss();
            Toast.makeText(this, "Subject added", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    // Step 3 - Student Assignment
    private void updateStep3UI() {
        // Show warning if no subjects
        if (wizardSubjects.isEmpty()) {
            warningNoSubjects.setVisibility(View.VISIBLE);
            btnAssignStudent.setEnabled(false);
        } else {
            warningNoSubjects.setVisibility(View.GONE);
            btnAssignStudent.setEnabled(true);
        }

        // Update list
        if (wizardStudents.isEmpty()) {
            recyclerViewStudents.setVisibility(View.GONE);
            emptyStateStudents.setVisibility(View.VISIBLE);
        } else {
            recyclerViewStudents.setVisibility(View.VISIBLE);
            emptyStateStudents.setVisibility(View.GONE);
            studentAdapter.updateStudents(wizardStudents);
        }
    }

    private void showAssignStudentDialog() {
        if (wizardSubjects.isEmpty()) {
            Toast.makeText(this, "Please add subjects first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (availableStudents.isEmpty()) {
            Toast.makeText(this, "No students available", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_assign_student_wizard, null);

        Spinner spinnerStudent = dialogView.findViewById(R.id.spinnerStudent);
        LinearLayout subjectsContainer = dialogView.findViewById(R.id.subjectsContainer);
        SwitchCompat switchActive = dialogView.findViewById(R.id.switchActive);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Setup student spinner
        List<String> studentNames = new ArrayList<>();
        for (User student : availableStudents) {
            studentNames.add(student.getName() + " (" + student.getStudentId() + ")");
        }
        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, studentNames);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudent.setAdapter(studentAdapter);

        // Create checkboxes for subjects
        Map<String, android.widget.CheckBox> subjectCheckboxes = new HashMap<>();
        for (WizardSubject subject : wizardSubjects) {
            android.widget.CheckBox checkBox = new android.widget.CheckBox(this);
            checkBox.setText(subject.getSubjectCode() + " - " + subject.getSubjectName());
            checkBox.setTextSize(16);
            checkBox.setPadding(0, 8, 0, 8);
            subjectsContainer.addView(checkBox);
            subjectCheckboxes.put(subject.getSubjectId(), checkBox);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int studentPos = spinnerStudent.getSelectedItemPosition();
            if (studentPos < 0 || studentPos >= availableStudents.size()) {
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> selectedSubjectIds = new ArrayList<>();
            for (Map.Entry<String, android.widget.CheckBox> entry : subjectCheckboxes.entrySet()) {
                if (entry.getValue().isChecked()) {
                    selectedSubjectIds.add(entry.getKey());
                }
            }

            if (selectedSubjectIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one subject", Toast.LENGTH_SHORT).show();
                return;
            }

            User selectedStudent = availableStudents.get(studentPos);
            WizardStudent wizardStudent = new WizardStudent(
                    selectedStudent.getId(),
                    selectedStudent.getName(),
                    selectedStudent.getStudentId(),
                    selectedSubjectIds
            );
            wizardStudent.setActive(switchActive.isChecked());

            wizardStudents.add(wizardStudent);
            updateStep3UI();
            hasUnsavedChanges = true;
            dialog.dismiss();
            Toast.makeText(this, "Student assigned", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    // Class Creation
    private void createClass() {
        if (!validateStep1()) {
            Toast.makeText(this, "Please fix errors in basic information", Toast.LENGTH_SHORT).show();
            viewFlipper.setDisplayedChild(0);
            currentStep = 0;
            updateStepIndicator();
            updateNavigationButtons();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Create Class?")
                .setMessage("Are you sure you want to create this class?")
                .setPositiveButton("Create", (dialog, which) -> performClassCreation())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performClassCreation() {
        showLoading(true);

        // Step 1: Create basic class
        String className = etClassName.getText().toString().trim();
        String semester = etSemester.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        // Generate ClassId
        String classId = className + "_" + semester;

        Class newClass = new Class();
        newClass.setId(classId);
        newClass.setClassName(className);
        newClass.setSemester(semester);
        newClass.setDescription(description);
        newClass.setActive(isActive);
        newClass.setCreatedAt(System.currentTimeMillis());

        // Save class to Firebase
        classRepository.createClass(newClass, new ClassRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                // Step 2: Create ClassSubjects if any
                if (!wizardSubjects.isEmpty()) {
                    createClassSubjects(classId);
                } else {
                    showLoading(false);
                    showSuccessAndFinish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(CreateClassActivity.this,
                        "Error creating class: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createClassSubjects(String classId) {
        final int[] createdCount = {0};
        final int totalSubjects = wizardSubjects.size();
        final List<String> createdClassSubjectIds = new ArrayList<>();
        final boolean[] hasError = {false};

        for (WizardSubject wizardSubject : wizardSubjects) {
            ClassSubject classSubject = new ClassSubject();
            classSubject.setClassId(classId);
            classSubject.setSubjectId(wizardSubject.getSubjectId());
            classSubject.setTeacherId(wizardSubject.getTeacherId());
            classSubject.setSchedule(wizardSubject.getSchedule());
            classSubject.setRoom(wizardSubject.getRoom());
            classSubject.setActive(wizardSubject.isActive());
            classSubject.setCreatedAt(System.currentTimeMillis());

            String classSubjectId = ClassSubject.generateId(classId, wizardSubject.getSubjectId());
            classSubject.setId(classSubjectId);

            classSubjectRepository.assignSubjectToClass(classSubject,
                    new ClassSubjectRepository.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess() {
                            createdClassSubjectIds.add(classSubjectId);
                            createdCount[0]++;

                            if (createdCount[0] >= totalSubjects) {
                                // All class subjects created
                                if (!wizardStudents.isEmpty() && !createdClassSubjectIds.isEmpty()) {
                                    createUserClassSubjects(createdClassSubjectIds);
                                } else {
                                    showLoading(false);
                                    showSuccessAndFinish();
                                }
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            hasError[0] = true;
                            createdCount[0]++;

                            if (createdCount[0] >= totalSubjects) {
                                showLoading(false);
                                if (hasError[0]) {
                                    Toast.makeText(CreateClassActivity.this,
                                            "Class created, but some subjects failed to assign",
                                            Toast.LENGTH_LONG).show();
                                }
                                showSuccessAndFinish();
                            }
                        }
                    });
        }
    }

    private void createUserClassSubjects(List<String> classSubjectIds) {
        // Build a map of subjectId -> classSubjectId
        Map<String, String> subjectToClassSubjectMap = new HashMap<>();
        for (WizardSubject wizardSubject : wizardSubjects) {
            String classSubjectId = null;
            for (String csId : classSubjectIds) {
                if (csId.contains(wizardSubject.getSubjectId())) {
                    classSubjectId = csId;
                    break;
                }
            }
            if (classSubjectId != null) {
                subjectToClassSubjectMap.put(wizardSubject.getSubjectId(), classSubjectId);
            }
        }

        // Create all UserClassSubject entries
        List<UserClassSubject> enrollmentsToCreate = new ArrayList<>();
        for (WizardStudent wizardStudent : wizardStudents) {
            for (String subjectId : wizardStudent.getAssignedSubjectIds()) {
                String classSubjectId = subjectToClassSubjectMap.get(subjectId);
                if (classSubjectId != null) {
                    UserClassSubject enrollment = new UserClassSubject();
                    enrollment.setUserId(wizardStudent.getUserId());
                    enrollment.setClassSubjectId(classSubjectId);
                    enrollment.setRole("Student");
                    enrollment.setEnrolledAt(System.currentTimeMillis());
                    enrollment.setActive(wizardStudent.isActive());

                    String enrollmentId = UserClassSubject.generateId(
                            wizardStudent.getUserId(), classSubjectId);
                    enrollment.setId(enrollmentId);

                    enrollmentsToCreate.add(enrollment);
                }
            }
        }

        if (enrollmentsToCreate.isEmpty()) {
            showLoading(false);
            showSuccessAndFinish();
            return;
        }

        final int[] createdCount = {0};
        final int totalEnrollments = enrollmentsToCreate.size();
        final boolean[] hasError = {false};

        for (UserClassSubject enrollment : enrollmentsToCreate) {
            userClassSubjectRepository.enrollUserToClassSubject(enrollment,
                    new UserClassSubjectRepository.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess() {
                            createdCount[0]++;
                            if (createdCount[0] >= totalEnrollments) {
                                showLoading(false);
                                showSuccessAndFinish();
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            hasError[0] = true;
                            createdCount[0]++;
                            if (createdCount[0] >= totalEnrollments) {
                                showLoading(false);
                                if (hasError[0]) {
                                    Toast.makeText(CreateClassActivity.this,
                                            "Class created, but some student assignments failed",
                                            Toast.LENGTH_LONG).show();
                                }
                                showSuccessAndFinish();
                            }
                        }
                    });
        }
    }

    private void showSuccessAndFinish() {
        hasUnsavedChanges = false;
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Class created successfully")
                .setPositiveButton("OK", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // Utility Methods
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnBack.setEnabled(!show);
        btnSkip.setEnabled(!show);
        btnNext.setEnabled(!show);
    }

    private void handleBackPressed() {
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to go back?")
                    .setPositiveButton("Discard", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }
}