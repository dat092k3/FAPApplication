package com.example.fapapplication.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.adapter.StudentAssignmentAdapter;
import com.example.fapapplication.entity.ClassSubject;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.entity.User;
import com.example.fapapplication.entity.UserClassSubject;
import com.example.fapapplication.repository.ClassSubjectRepository;
import com.example.fapapplication.repository.SubjectRepository;
import com.example.fapapplication.repository.UserClassSubjectRepository;
import com.example.fapapplication.repository.UserRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassStudentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private FloatingActionButton fabAssignStudent;

    private StudentAssignmentAdapter adapter;
    private UserClassSubjectRepository userClassSubjectRepository;
    private ClassSubjectRepository classSubjectRepository;
    private SubjectRepository subjectRepository;
    private UserRepository userRepository;

    private String classId;
    private List<UserClassSubject> enrollments;
    private List<ClassSubject> classSubjects;
    private Map<String, User> userMap;
    private Map<String, Subject> subjectMap;
    private List<User> availableStudents;

    private int loadedCount = 0;

    public static ClassStudentsFragment newInstance(String classId) {
        ClassStudentsFragment fragment = new ClassStudentsFragment();
        fragment.classId = classId;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRepositories();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        fabAssignStudent = view.findViewById(R.id.fabAssignStudent);
    }

    private void setupRepositories() {
        userClassSubjectRepository = new UserClassSubjectRepository();
        classSubjectRepository = new ClassSubjectRepository();
        subjectRepository = SubjectRepository.getInstance();
        userRepository = new UserRepository();

        enrollments = new ArrayList<>();
        classSubjects = new ArrayList<>();
        userMap = new HashMap<>();
        subjectMap = new HashMap<>();
        availableStudents = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new StudentAssignmentAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnStudentActionListener(new StudentAssignmentAdapter.OnStudentActionListener() {
            @Override
            public void onViewDetailsClicked(StudentAssignmentAdapter.StudentAssignmentInfo studentInfo, int position) {
                showStudentDetailsDialog(studentInfo);
            }

            @Override
            public void onRemoveClicked(StudentAssignmentAdapter.StudentAssignmentInfo studentInfo, int position) {
                showRemoveConfirmationDialog(studentInfo);
            }
        });
    }

    private void setupClickListeners() {
        fabAssignStudent.setOnClickListener(v -> showAssignDialog());
    }

    private void loadData() {
        if (classId == null) return;

        showLoading(true);
        loadedCount = 0;

        loadClassSubjects();
        loadUsers();
        loadSubjects();
    }

    private void loadClassSubjects() {
        classSubjectRepository.getSubjectsByClassId(classId, new ClassSubjectRepository.OnClassSubjectsLoadedListener() {
            @Override
            public void onClassSubjectsLoaded(List<ClassSubject> subjects) {
                classSubjects = subjects;
                loadEnrollments();
            }

            @Override
            public void onError(String errorMessage) {
                checkDataLoadedAndUpdate();
            }
        });
    }

    private void loadEnrollments() {
        if (classSubjects.isEmpty()) {
            checkDataLoadedAndUpdate();
            return;
        }

        List<String> classSubjectIds = new ArrayList<>();
        for (ClassSubject cs : classSubjects) {
            classSubjectIds.add(cs.getId());
        }

        enrollments.clear();
        final int[] processedCount = {0};

        for (String csId : classSubjectIds) {
            userClassSubjectRepository.getEnrollmentsByClassSubjectId(csId,
                    new UserClassSubjectRepository.OnUserClassSubjectsLoadedListener() {
                        @Override
                        public void onUserClassSubjectsLoaded(List<UserClassSubject> userClassSubjects) {
                            enrollments.addAll(userClassSubjects);
                            processedCount[0]++;
                            if (processedCount[0] >= classSubjectIds.size()) {
                                checkDataLoadedAndUpdate();
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            processedCount[0]++;
                            if (processedCount[0] >= classSubjectIds.size()) {
                                checkDataLoadedAndUpdate();
                            }
                        }
                    });
        }
    }

    private void loadUsers() {
        userRepository.getAllUsers(new UserRepository.OnUsersLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                userMap.clear();
                availableStudents.clear();
                for (User user : users) {
                    userMap.put(user.getId(), user);
                    if ("Student".equals(user.getRole())) {
                        availableStudents.add(user);
                    }
                }
                checkDataLoadedAndUpdate();
            }

            @Override
            public void onError(String errorMessage) {
                checkDataLoadedAndUpdate();
            }
        });
    }

    private void loadSubjects() {
        subjectRepository.getAllSubjectsOnce(new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                subjectMap.clear();
                for (Subject subject : subjects) {
                    subjectMap.put(subject.getId(), subject);
                }
                checkDataLoadedAndUpdate();
            }

            @Override
            public void onError(String errorMessage) {
                checkDataLoadedAndUpdate();
            }
        });
    }

    private void checkDataLoadedAndUpdate() {
        loadedCount++;
        if (loadedCount >= 3) {
            loadedCount = 0;
            processAndDisplayData();
        }
    }

    private void processAndDisplayData() {
        Map<String, StudentAssignmentAdapter.StudentAssignmentInfo> studentInfoMap = new HashMap<>();

        for (UserClassSubject enrollment : enrollments) {
            String userId = enrollment.getUserId();
            User user = userMap.get(userId);
            if (user == null) continue;

            String classSubjectId = enrollment.getClassSubjectId();
            ClassSubject cs = findClassSubjectById(classSubjectId);
            if (cs == null) continue;

            Subject subject = subjectMap.get(cs.getSubjectId());
            String subjectCode = subject != null ? subject.getSubjectCode() : "Unknown";

            if (!studentInfoMap.containsKey(userId)) {
                StudentAssignmentAdapter.StudentAssignmentInfo info =
                        new StudentAssignmentAdapter.StudentAssignmentInfo(
                                userId,
                                user.getName(),
                                user.getStudentId(),
                                new ArrayList<>(),
                                enrollment.getEnrolledAt(),
                                enrollment.isActive()
                        );
                studentInfoMap.put(userId, info);
            }

            StudentAssignmentAdapter.StudentAssignmentInfo info = studentInfoMap.get(userId);
            if (info != null && !info.subjectCodes.contains(subjectCode)) {
                info.subjectCodes.add(subjectCode);
            }
        }

        List<StudentAssignmentAdapter.StudentAssignmentInfo> studentList =
                new ArrayList<>(studentInfoMap.values());
        adapter.updateStudentAssignments(studentList);
        updateUI();
    }

    private ClassSubject findClassSubjectById(String classSubjectId) {
        for (ClassSubject cs : classSubjects) {
            if (cs.getId().equals(classSubjectId)) {
                return cs;
            }
        }
        return null;
    }

    private void showAssignDialog() {
        if (classSubjects.isEmpty()) {
            Toast.makeText(getContext(), "No subjects available. Please add subjects first.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_assign_student, null);

        Spinner spinnerStudent = dialogView.findViewById(R.id.spinnerStudent);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinnerSubject);
        SwitchCompat switchActive = dialogView.findViewById(R.id.switchActive);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, getStudentNames());
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudent.setAdapter(studentAdapter);

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, getClassSubjectNames());
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int studentPos = spinnerStudent.getSelectedItemPosition();
            int subjectPos = spinnerSubject.getSelectedItemPosition();
            boolean isActive = switchActive.isChecked();

            if (studentPos < 0 || studentPos >= availableStudents.size()) {
                Toast.makeText(getContext(), "Please select a student", Toast.LENGTH_SHORT).show();
                return;
            }

            if (subjectPos < 0 || subjectPos >= classSubjects.size()) {
                Toast.makeText(getContext(), "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }

            User selectedStudent = availableStudents.get(studentPos);
            ClassSubject selectedClassSubject = classSubjects.get(subjectPos);

            UserClassSubject enrollment = new UserClassSubject();
            enrollment.setUserId(selectedStudent.getId());
            enrollment.setClassSubjectId(selectedClassSubject.getId());
            enrollment.setRole("Student");
            enrollment.setEnrolledAt(System.currentTimeMillis());
            enrollment.setActive(isActive);

            String enrollmentId = UserClassSubject.generateId(selectedStudent.getId(), selectedClassSubject.getId());
            enrollment.setId(enrollmentId);

            assignStudent(enrollment, dialog);
        });

        dialog.show();
    }

    private void assignStudent(UserClassSubject enrollment, AlertDialog dialog) {
        progressBar.setVisibility(View.VISIBLE);

        userClassSubjectRepository.enrollUserToClassSubject(enrollment,
                new UserClassSubjectRepository.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Student assigned successfully", Toast.LENGTH_SHORT).show();
                        loadData();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showStudentDetailsDialog(StudentAssignmentAdapter.StudentAssignmentInfo studentInfo) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(studentInfo.studentName).append("\n");
        details.append("ID: ").append(studentInfo.studentId).append("\n");
        details.append("Subjects: ").append(String.join(", ", studentInfo.subjectCodes)).append("\n");
        details.append("Status: ").append(studentInfo.isActive ? "Active" : "Inactive");

        new AlertDialog.Builder(getContext())
                .setTitle("Student Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showRemoveConfirmationDialog(StudentAssignmentAdapter.StudentAssignmentInfo studentInfo) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Student?")
                .setMessage("Remove " + studentInfo.studentName + " from all subjects in this class?")
                .setPositiveButton("Remove", (dialog, which) -> removeStudent(studentInfo))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeStudent(StudentAssignmentAdapter.StudentAssignmentInfo studentInfo) {
        progressBar.setVisibility(View.VISIBLE);

        List<String> enrollmentIds = new ArrayList<>();
        for (UserClassSubject enrollment : enrollments) {
            if (enrollment.getUserId().equals(studentInfo.userId)) {
                enrollmentIds.add(enrollment.getId());
            }
        }

        if (enrollmentIds.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No enrollments found", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] removedCount = {0};
        for (String enrollmentId : enrollmentIds) {
            userClassSubjectRepository.unenrollUser(enrollmentId, new UserClassSubjectRepository.OnOperationCompleteListener() {
                @Override
                public void onSuccess() {
                    removedCount[0]++;
                    if (removedCount[0] >= enrollmentIds.size()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Student removed successfully", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    removedCount[0]++;
                    if (removedCount[0] >= enrollmentIds.size()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error removing some enrollments", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                }
            });
        }
    }

    private List<String> getStudentNames() {
        List<String> names = new ArrayList<>();
        for (User student : availableStudents) {
            names.add(student.getName() + " (" + student.getStudentId() + ")");
        }
        return names;
    }

    private List<String> getClassSubjectNames() {
        List<String> names = new ArrayList<>();
        for (ClassSubject cs : classSubjects) {
            Subject subject = subjectMap.get(cs.getSubjectId());
            String subjectName = subject != null ? subject.getSubjectCode() : "Unknown";
            names.add(subjectName);
        }
        return names;
    }

    private void updateUI() {
        showLoading(false);

        if (enrollments.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
}