package com.example.fapapplication.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.fapapplication.adapter.ClassSubjectAdapter;
import com.example.fapapplication.entity.ClassSubject;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.entity.User;
import com.example.fapapplication.repository.ClassSubjectRepository;
import com.example.fapapplication.repository.SubjectRepository;
import com.example.fapapplication.repository.UserRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSubjectsFragment extends Fragment {

    private int loadedCount = 0;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private FloatingActionButton fabAddSubject;

    private ClassSubjectAdapter adapter;
    private ClassSubjectRepository classSubjectRepository;
    private SubjectRepository subjectRepository;
    private UserRepository userRepository;

    private String classId;
    private List<ClassSubject> classSubjects;
    private Map<String, Subject> subjectMap;
    private List<Subject> availableSubjects;
    private Map<String, User> teacherMap;
    private List<User> availableTeachers;

    public static ClassSubjectsFragment newInstance(String classId) {
        ClassSubjectsFragment fragment = new ClassSubjectsFragment();
        fragment.classId = classId;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_subjects, container, false);
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
        fabAddSubject = view.findViewById(R.id.fabAddSubject);
    }

    private void setupRepositories() {
        classSubjectRepository = new ClassSubjectRepository();
        subjectRepository = SubjectRepository.getInstance();
        userRepository = new UserRepository();

        classSubjects = new ArrayList<>();
        subjectMap = new HashMap<>();
        availableSubjects = new ArrayList<>();
        teacherMap = new HashMap<>();
        availableTeachers = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ClassSubjectAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnClassSubjectActionListener(new ClassSubjectAdapter.OnClassSubjectActionListener() {
            @Override
            public void onEditClicked(ClassSubject classSubject, int position) {
                showEditDialog(classSubject, position);
            }

            @Override
            public void onDeleteClicked(ClassSubject classSubject, int position) {
                showDeleteConfirmationDialog(classSubject, position);
            }
        });
    }

    private void setupClickListeners() {
        fabAddSubject.setOnClickListener(v -> showAddDialog());
    }

    private void loadData() {
        if (classId == null) return;

        showLoading(true);

        final boolean[] subjectsLoaded = {false};
        final boolean[] teachersLoaded = {false};

        loadSubjects();
        loadTeachers();

        new android.os.Handler().postDelayed(() -> {
            loadClassSubjects();
        }, 500);
    }

    private void loadClassSubjects() {
        classSubjectRepository.getSubjectsByClassId(classId, new ClassSubjectRepository.OnClassSubjectsLoadedListener() {
            @Override
            public void onClassSubjectsLoaded(List<ClassSubject> subjects) {
                classSubjects = subjects;
                adapter.updateClassSubjects(classSubjects);
                checkDataLoadedAndUpdate();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(getContext(), "Error loading subjects: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubjects() {
        subjectRepository.getAllSubjectsOnce(new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                availableSubjects = subjects;
                subjectMap.clear();
                for (Subject subject : subjects) {
                    subjectMap.put(subject.getId(), subject);
                }
                adapter.updateSubjectMap(subjectMap);
                checkDataLoadedAndUpdate();
            }

            @Override
            public void onError(String errorMessage) {
                checkDataLoadedAndUpdate();
            }
        });
    }

    private void loadTeachers() {
        userRepository.getAllUsers(new UserRepository.OnUsersLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                availableTeachers = new ArrayList<>();
                teacherMap.clear();
                for (User user : users) {
                    if ("Teacher".equals(user.getRole())) {
                        availableTeachers.add(user);
                        teacherMap.put(user.getId(), user);
                    }
                }
                adapter.updateTeacherMap(teacherMap);
                checkDataLoadedAndUpdate();
            }

            @Override
            public void onError(String errorMessage) {
                checkDataLoadedAndUpdate();
            }
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_class_subject, null);

        Spinner spinnerSubject = dialogView.findViewById(R.id.spinnerSubject);
        Spinner spinnerTeacher = dialogView.findViewById(R.id.spinnerTeacher);
        EditText etSchedule = dialogView.findViewById(R.id.etSchedule);
        EditText etRoom = dialogView.findViewById(R.id.etRoom);
        SwitchCompat switchActive = dialogView.findViewById(R.id.switchActive);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, getSubjectNames());
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, getTeacherNames());
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeacher.setAdapter(teacherAdapter);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int subjectPos = spinnerSubject.getSelectedItemPosition();
            int teacherPos = spinnerTeacher.getSelectedItemPosition();
            String schedule = etSchedule.getText().toString().trim();
            String room = etRoom.getText().toString().trim();
            boolean isActive = switchActive.isChecked();

            if (subjectPos < 0 || subjectPos >= availableSubjects.size()) {
                Toast.makeText(getContext(), "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }

            if (teacherPos < 0 || teacherPos >= availableTeachers.size()) {
                Toast.makeText(getContext(), "Please select a teacher", Toast.LENGTH_SHORT).show();
                return;
            }

            if (schedule.isEmpty()) {
                etSchedule.setError("Schedule is required");
                return;
            }

            if (room.isEmpty()) {
                etRoom.setError("Room is required");
                return;
            }

            Subject selectedSubject = availableSubjects.get(subjectPos);
            User selectedTeacher = availableTeachers.get(teacherPos);

            ClassSubject newClassSubject = new ClassSubject();
            newClassSubject.setClassId(classId);
            newClassSubject.setSubjectId(selectedSubject.getId());
            newClassSubject.setTeacherId(selectedTeacher.getId());
            newClassSubject.setSchedule(schedule);
            newClassSubject.setRoom(room);
            newClassSubject.setActive(isActive);
            newClassSubject.setCreatedAt(System.currentTimeMillis());

            String generatedId = ClassSubject.generateId(classId, selectedSubject.getId());
            newClassSubject.setId(generatedId);

            saveClassSubject(newClassSubject, dialog);
        });

        dialog.show();
    }

    private void showEditDialog(ClassSubject classSubject, int position) {
        Toast.makeText(getContext(), "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(ClassSubject classSubject, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Subject?")
                .setMessage("Are you sure you want to remove this subject from the class?")
                .setPositiveButton("Remove", (dialog, which) -> deleteClassSubject(classSubject, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveClassSubject(ClassSubject classSubject, AlertDialog dialog) {
        progressBar.setVisibility(View.VISIBLE);

        classSubjectRepository.assignSubjectToClass(classSubject, new ClassSubjectRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
                Toast.makeText(getContext(), "Subject added successfully", Toast.LENGTH_SHORT).show();
                loadClassSubjects();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteClassSubject(ClassSubject classSubject, int position) {
        progressBar.setVisibility(View.VISIBLE);

        classSubjectRepository.deleteClassSubject(classSubject.getId(),
                new ClassSubjectRepository.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Subject removed successfully", Toast.LENGTH_SHORT).show();
                        loadClassSubjects();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<String> getSubjectNames() {
        List<String> names = new ArrayList<>();
        for (Subject subject : availableSubjects) {
            names.add(subject.getSubjectCode() + " - " + subject.getSubjectName());
        }
        return names;
    }

    private List<String> getTeacherNames() {
        List<String> names = new ArrayList<>();
        for (User teacher : availableTeachers) {
            names.add(teacher.getName());
        }
        return names;
    }


    private void checkDataLoadedAndUpdate() {
        loadedCount++;
        if (loadedCount >= 3) {
            loadedCount = 0;
            updateUI();
        }
    }

    private void updateUI() {
        showLoading(false);

        if (classSubjects.isEmpty()) {
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