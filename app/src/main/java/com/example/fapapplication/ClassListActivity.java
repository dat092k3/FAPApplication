package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fapapplication.adapter.ClassAdapter;
import com.example.fapapplication.entity.Class;
import com.example.fapapplication.repository.ClassRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassListActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvError;
    private EditText searchEditText;
    private Spinner spinnerSemesterFilter;
    private Spinner spinnerStatusFilter;
    private ImageButton btnClearFilters;
    private FloatingActionButton fabAddClass;
    private ImageButton backButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Data
    private List<Class> allClasses;
    private ClassRepository classRepository;
    private ValueEventListener classesListener;

    // Filter state
    private String currentSearchQuery = "";
    private String currentSemesterFilter = "All";
    private String currentStatusFilter = "All";

    private static final int REQUEST_CODE_CLASS_DETAIL = 201;
    private static final int REQUEST_CODE_CREATE_CLASS = 2001;

    // State preservation keys
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final String KEY_SEMESTER_FILTER = "semester_filter";
    private static final String KEY_STATUS_FILTER = "status_filter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        // Kiểm tra authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo ClassRepository
        classRepository = ClassRepository.getInstance();

        // Khởi tạo views
        initializeViews();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Load filter options
        loadFilterOptions();

        // Thiết lập listeners
        setupListeners();

        // Load dữ liệu từ Firebase
        loadClassesFromFirebase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener để tránh memory leaks
        if (classesListener != null) {
            classRepository.removeListener(classesListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CLASS_DETAIL && resultCode == RESULT_OK) {
            // Refresh danh sách sau khi edit
            refreshData();
        } else if (requestCode == REQUEST_CODE_CREATE_CLASS && resultCode == RESULT_OK) {
            // Refresh danh sách sau khi create
            refreshData();
            Toast.makeText(this, "Lớp học đã được tạo thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    // Khởi tạo tất cả các views
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewClasses);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvError = findViewById(R.id.tvError);
        searchEditText = findViewById(R.id.searchEditText);
        spinnerSemesterFilter = findViewById(R.id.spinnerSemesterFilter);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        fabAddClass = findViewById(R.id.fabAddClass);
        backButton = findViewById(R.id.backButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        allClasses = new ArrayList<>();
    }

    // Thiết lập RecyclerView với adapter và layout manager
    private void setupRecyclerView() {
        adapter = new ClassAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Thiết lập click listener cho items
        adapter.setOnClassClickListener((classItem, position) -> {
            Intent intent = new Intent(ClassListActivity.this, ClassDetailActivity.class);
            intent.putExtra("CLASS_ID", classItem.getId());
            startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
        });

        // Thiết lập long-click listener cho context menu
        adapter.setOnClassLongClickListener((classItem, position) -> {
            showClassOptionsDialog(classItem);
        });
    }

    // Load filter options vào spinners
    private void loadFilterOptions() {
        // Status filter options
        List<String> statusOptions = Arrays.asList("All", "Active", "Inactive");
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);

        // Semester filter sẽ được populate động sau khi load data
        List<String> semesterOptions = new ArrayList<>();
        semesterOptions.add("All");
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                semesterOptions
        );
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesterFilter.setAdapter(semesterAdapter);
    }

    // Thiết lập tất cả event listeners
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Search text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Semester filter spinner
        spinnerSemesterFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSemesterFilter = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Status filter spinner
        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Floating Action Button - Tạo class mới
        fabAddClass.setOnClickListener(v -> {
            Intent intent = new Intent(ClassListActivity.this, CreateClassActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CREATE_CLASS);
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    // Load classes từ Firebase Realtime Database
    private void loadClassesFromFirebase() {
        showLoading(true);

        classesListener = classRepository.getAllClasses(new ClassRepository.ClassListCallback() {
            @Override
            public void onSuccess(List<Class> classes) {
                showLoading(false);
                allClasses = classes;

                if (classes.isEmpty()) {
                    showEmptyState(true);
                    showError(false);
                } else {
                    showEmptyState(false);
                    showError(false);
                    updateSemesterFilter(classes);
                    applyFilters();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showEmptyState(false);
                showError(true);
                tvError.setText(errorMessage);
                Toast.makeText(ClassListActivity.this,
                        "Error: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Refresh data từ Firebase
    private void refreshData() {
        classRepository.getAllClassesOnce(new ClassRepository.ClassListCallback() {
            @Override
            public void onSuccess(List<Class> classes) {
                swipeRefreshLayout.setRefreshing(false);
                allClasses = classes;

                if (classes.isEmpty()) {
                    showEmptyState(true);
                    showError(false);
                } else {
                    showEmptyState(false);
                    showError(false);
                    updateSemesterFilter(classes);
                    applyFilters();
                }

                Toast.makeText(ClassListActivity.this,
                        "Refreshed: " + classes.size() + " classes",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ClassListActivity.this,
                        "Refresh failed: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update semester filter options dựa trên data
    private void updateSemesterFilter(List<Class> classes) {
        Set<String> semesters = new HashSet<>();
        semesters.add("All");

        for (Class classObj : classes) {
            if (classObj.getSemester() != null && !classObj.getSemester().isEmpty()) {
                semesters.add(classObj.getSemester());
            }
        }

        List<String> semesterList = new ArrayList<>(semesters);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                semesterList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesterFilter.setAdapter(adapter);
    }

    // Áp dụng filters cho danh sách classes
    private void applyFilters() {
        List<Class> filteredList = new ArrayList<>(allClasses);

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            List<Class> searchFiltered = new ArrayList<>();
            String query = currentSearchQuery.toLowerCase().trim();

            for (Class classObj : filteredList) {
                boolean matchesName = classObj.getClassName() != null &&
                        classObj.getClassName().toLowerCase().contains(query);
                boolean matchesDescription = classObj.getDescription() != null &&
                        classObj.getDescription().toLowerCase().contains(query);

                if (matchesName || matchesDescription) {
                    searchFiltered.add(classObj);
                }
            }
            filteredList = searchFiltered;
        }

        // Apply semester filter
        if (!currentSemesterFilter.equals("All")) {
            List<Class> semesterFiltered = new ArrayList<>();

            for (Class classObj : filteredList) {
                if (classObj.getSemester() != null &&
                        classObj.getSemester().equals(currentSemesterFilter)) {
                    semesterFiltered.add(classObj);
                }
            }
            filteredList = semesterFiltered;
        }

        // Apply status filter
        if (!currentStatusFilter.equals("All")) {
            List<Class> statusFiltered = new ArrayList<>();
            boolean filterActive = currentStatusFilter.equals("Active");

            for (Class classObj : filteredList) {
                if (classObj.isActive() == filterActive) {
                    statusFiltered.add(classObj);
                }
            }
            filteredList = statusFiltered;
        }

        // Update adapter với filtered list
        adapter.updateClassList(filteredList);

        // Hiển thị empty state nếu không có kết quả
        if (filteredList.isEmpty() && !allClasses.isEmpty()) {
            tvEmptyState.setText("No classes match your filters");
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }

    // Clear all filters
    private void clearFilters() {
        searchEditText.setText("");
        currentSearchQuery = "";

        spinnerSemesterFilter.setSelection(0);
        currentSemesterFilter = "All";

        spinnerStatusFilter.setSelection(0);
        currentStatusFilter = "All";

        applyFilters();

        // Scroll to top sau khi clear filters
        recyclerView.smoothScrollToPosition(0);

        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    // Hiển thị/ẩn loading indicator
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // Hiển thị/ẩn empty state message
    private void showEmptyState(boolean show) {
        View emptyStateLayout = findViewById(R.id.emptyStateLayout);
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // Hiển thị/ẩn error message
    private void showError(boolean show) {
        tvError.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // Hiển thị dialog với options cho class
    private void showClassOptionsDialog(Class classItem) {
        String[] options = {"View Details", "Edit Class", "Delete Class"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(classItem.getClassName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Xem chi tiết
                    Intent intent = new Intent(ClassListActivity.this, ClassDetailActivity.class);
                    intent.putExtra("CLASS_ID", classItem.getId());
                    startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
                    break;
                case 1: // Edit
                    Toast.makeText(this, "Edit class: " + classItem.getClassName(),
                            Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to Edit Class screen (Task 14)
                    break;
                case 2: // Delete
                    showDeleteConfirmationDialog(classItem);
                    break;
            }
        });
        builder.show();
    }

    // Hiển thị confirmation dialog trước khi delete class
    private void showDeleteConfirmationDialog(Class classItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Class");
        builder.setMessage("Are you sure you want to delete \"" + classItem.getClassName() + "\"?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteClass(classItem));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Xóa class khỏi Firebase
    private void deleteClass(Class classItem) {
        // Hiển thị progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Deleting Class")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // Gọi repository để delete
        classRepository.deleteClass(classItem.getId(), new ClassRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(ClassListActivity.this,
                        "Class \"" + classItem.getClassName() + "\" deleted successfully",
                        Toast.LENGTH_SHORT).show();

                // Refresh data sau khi delete
                refreshData();
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();

                // Hiển thị error dialog
                new AlertDialog.Builder(ClassListActivity.this)
                        .setTitle("Delete Failed")
                        .setMessage("Failed to delete class: " + errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_SEARCH_QUERY, currentSearchQuery);
        outState.putString(KEY_SEMESTER_FILTER, currentSemesterFilter);
        outState.putString(KEY_STATUS_FILTER, currentStatusFilter);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
            currentSemesterFilter = savedInstanceState.getString(KEY_SEMESTER_FILTER, "All");
            currentStatusFilter = savedInstanceState.getString(KEY_STATUS_FILTER, "All");

            // Restore UI state
            searchEditText.setText(currentSearchQuery);
        }
    }
}