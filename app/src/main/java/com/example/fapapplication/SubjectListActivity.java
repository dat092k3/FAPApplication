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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fapapplication.adapter.SubjectAdapter;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.repository.SubjectRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SubjectListActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerView;
    private SubjectAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvError;
    private EditText searchEditText;
    private Spinner spinnerStatusFilter;
    private ImageButton btnClearFilters;
    private FloatingActionButton fabAddSubject;
    private ImageButton backButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Data
    private List<Subject> allSubjects;
    private SubjectRepository subjectRepository;
    private ValueEventListener subjectsListener;

    // Filter state
    private String currentSearchQuery = "";
    private String currentStatusFilter = "All";

    private static final int REQUEST_CODE_SUBJECT_DETAIL = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);

        // Kiểm tra authentication
        // Check authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo SubjectRepository
        // Initialize SubjectRepository
        subjectRepository = SubjectRepository.getInstance();

        // Khởi tạo views
        // Initialize views
        initializeViews();

        // Thiết lập RecyclerView
        // Setup RecyclerView
        setupRecyclerView();

        // Load filter options
        loadFilterOptions();

        // Thiết lập listeners
        // Setup listeners
        setupListeners();

        // Load dữ liệu từ Firebase
        // Load data from Firebase
        loadSubjectsFromFirebase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener để tránh memory leaks
        // Remove Firebase listener to avoid memory leaks
        if (subjectsListener != null) {
            subjectRepository.removeListener(subjectsListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL && resultCode == RESULT_OK) {
            // Refresh danh sách sau khi edit
            refreshData();
        }
    }

    /**
     * Khởi tạo tất cả các views
     * Initialize all views
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewSubjects);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvError = findViewById(R.id.tvError);
        searchEditText = findViewById(R.id.searchEditText);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        fabAddSubject = findViewById(R.id.fabAddSubject);
        backButton = findViewById(R.id.backButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        allSubjects = new ArrayList<>();
    }

    /**
     * Thiết lập RecyclerView với adapter và layout manager
     * Setup RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        adapter = new SubjectAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Thiết lập click listener cho items
        adapter.setOnSubjectClickListener((subject, position) -> {
            Intent intent = new Intent(SubjectListActivity.this, SubjectDetailActivity.class);
            intent.putExtra("SUBJECT_ID", subject.getId());
            startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
        });

        // Thiết lập long-click listener cho context menu
        fabAddSubject.setOnClickListener(v -> {
            // TODO: Navigate to Create Subject screen (Task 10)
            Toast.makeText(this, "Create Subject screen coming soon", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(SubjectListActivity.this, CreateSubjectActivity.class);
            // startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
            // overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    /**
     * Load filter options vào spinners
     * Load filter options into spinners
     */
    private void loadFilterOptions() {
        // Status filter options (All, Active, Inactive)
        List<String> statusOptions = Arrays.asList("All", "Active", "Inactive");
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
    }

    /**
     * Thiết lập tất cả event listeners
     * Setup all event listeners
     */
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

        // Floating Action Button - Add new subject
        fabAddSubject.setOnClickListener(v -> {
            // TODO: Navigate to Create Subject screen (Task 10)
            Toast.makeText(this, "Create Subject screen coming soon", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(SubjectListActivity.this, CreateSubjectActivity.class);
            // startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    /**
     * Load subjects từ Firebase Realtime Database
     * Load subjects from Firebase Realtime Database
     */
    private void loadSubjectsFromFirebase() {
        showLoading(true);

        subjectsListener = subjectRepository.getAllSubjects(new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                showLoading(false);
                allSubjects = subjects;

                if (subjects.isEmpty()) {
                    showEmptyState(true);
                    showError(false);
                } else {
                    showEmptyState(false);
                    showError(false);
                    applyFilters();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showEmptyState(false);
                showError(true);
                tvError.setText(errorMessage);
                Toast.makeText(SubjectListActivity.this,
                        "Error: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Refresh data từ Firebase (dùng cho swipe-to-refresh)
     * Refresh data from Firebase (used for swipe-to-refresh)
     */
    private void refreshData() {
        subjectRepository.getAllSubjectsOnce(new SubjectRepository.SubjectListCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                swipeRefreshLayout.setRefreshing(false);
                allSubjects = subjects;

                if (subjects.isEmpty()) {
                    showEmptyState(true);
                    showError(false);
                } else {
                    showEmptyState(false);
                    showError(false);
                    applyFilters();
                }

                Toast.makeText(SubjectListActivity.this,
                        "Refreshed: " + subjects.size() + " subjects",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(SubjectListActivity.this,
                        "Refresh failed: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Apply filters (search + status) to subject list
     * Áp dụng filters (tìm kiếm + trạng thái) cho danh sách subjects
     */
    private void applyFilters() {
        List<Subject> filteredList = new ArrayList<>(allSubjects);

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            List<Subject> searchFiltered = new ArrayList<>();
            String query = currentSearchQuery.toLowerCase().trim();

            for (Subject subject : filteredList) {
                boolean matchesCode = subject.getSubjectCode() != null &&
                        subject.getSubjectCode().toLowerCase().contains(query);
                boolean matchesName = subject.getSubjectName() != null &&
                        subject.getSubjectName().toLowerCase().contains(query);

                if (matchesCode || matchesName) {
                    searchFiltered.add(subject);
                }
            }
            filteredList = searchFiltered;
        }

        // Apply status filter
        if (!currentStatusFilter.equals("All")) {
            List<Subject> statusFiltered = new ArrayList<>();
            boolean filterActive = currentStatusFilter.equals("Active");

            for (Subject subject : filteredList) {
                if (subject.isActive() == filterActive) {
                    statusFiltered.add(subject);
                }
            }
            filteredList = statusFiltered;
        }

        // Update adapter with filtered list
        adapter.updateSubjectList(filteredList);

        // Show empty state if no results
        if (filteredList.isEmpty() && !allSubjects.isEmpty()) {
            tvEmptyState.setText("No subjects match your filters");
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }

    /**
     * Clear all filters và reset về trạng thái ban đầu
     * Clear all filters and reset to initial state
     */
    private void clearFilters() {
        // Clear search
        searchEditText.setText("");
        currentSearchQuery = "";

        // Reset status filter to "All"
        spinnerStatusFilter.setSelection(0);
        currentStatusFilter = "All";

        // Apply filters (which will show all subjects)
        applyFilters();

        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Hiển thị dialog với options cho subject (Edit, Delete, View Details)
     * Show dialog with options for subject (Edit, Delete, View Details)
     */
    private void showSubjectOptionsDialog(Subject subject) {
        String[] options = {"View Details", "Edit Subject", "Delete Subject"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(subject.getSubjectName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // View Details
                    Toast.makeText(this, "View subject: " + subject.getSubjectName(),
                            Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to Subject Detail screen
                    break;
                case 1: // Edit
                    Toast.makeText(this, "Edit subject: " + subject.getSubjectName(),
                            Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to Edit Subject screen
                    break;
                case 2: // Delete
                    showDeleteConfirmationDialog(subject);
                    break;
            }
        });
        builder.show();
    }

    /**
     * Hiển thị confirmation dialog trước khi delete subject
     * Show confirmation dialog before deleting subject
     */
    private void showDeleteConfirmationDialog(Subject subject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Subject");
        builder.setMessage("Are you sure you want to delete \"" + subject.getSubjectName() + "\"?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteSubject(subject));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Delete subject from Firebase
     * Xóa subject khỏi Firebase
     */
    private void deleteSubject(Subject subject) {
        // Hiển thị progress dialog
        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Deleting Subject")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // Gọi repository để delete
        // Call repository to delete
        subjectRepository.deleteSubject(subject.getId(), new SubjectRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(SubjectListActivity.this,
                        "Subject \"" + subject.getSubjectName() + "\" deleted successfully",
                        Toast.LENGTH_SHORT).show();

                // Refresh data sau khi delete
                // Refresh data after delete
                refreshData();
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();

                // Hiển thị error dialog
                // Show error dialog
                new AlertDialog.Builder(SubjectListActivity.this)
                        .setTitle("Delete Failed")
                        .setMessage("Failed to delete subject: " + errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    /**
     * Show/hide loading indicator
     * Hiển thị/ẩn loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Show/hide empty state message
     * Hiển thị/ẩn empty state message
     */
    private void showEmptyState(boolean show) {
        tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Show/hide error message
     * Hiển thị/ẩn error message
     */
    private void showError(boolean show) {
        tvError.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}