package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountListActivity extends AppCompatActivity {

    // UI Components
    private ImageButton backButton;
    private ImageButton searchButton;
    private ImageButton closeSearchButton;
    private ImageButton clearFilterButton;
    private EditText searchEditText;
    private RecyclerView recyclerViewAccounts;
    private FloatingActionButton fabAddAccount;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;
    private LinearLayout searchLayout;
    private LinearLayout filterLayout;
    private Spinner spinnerRoleFilter;
    private Spinner spinnerCampusFilter;

    // Firebase
    private FirebaseAuth auth;

    // Adapter và Data
    // TODO: Sẽ thêm AccountAdapter sau
    // private AccountAdapter accountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);

        // Kiểm tra đăng nhập
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            goToLoginPage();
            return;
        }

        // Ánh xạ views
        initializeViews();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập click listeners
        setupClickListeners();
    }

    /**
     * Ánh xạ tất cả các views từ layout
     */
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);
        closeSearchButton = findViewById(R.id.closeSearchButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerViewAccounts = findViewById(R.id.recyclerViewAccounts);
        fabAddAccount = findViewById(R.id.fabAddAccount);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        emptyTextView = findViewById(R.id.emptyTextView);
        searchLayout = findViewById(R.id.searchLayout);
        filterLayout = findViewById(R.id.filterLayout);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        spinnerCampusFilter = findViewById(R.id.spinnerCampusFilter);
    }

    /**
     * Thiết lập RecyclerView với layout manager
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAccounts.setLayoutManager(layoutManager);

        // TODO: Sẽ thêm adapter sau khi tạo AccountAdapter
        // accountAdapter = new AccountAdapter(new ArrayList<>(), this);
        // recyclerViewAccounts.setAdapter(accountAdapter);
    }

    /**
     * Thiết lập các bộ lắng nghe sự kiện
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Search button - toggle search bar
        searchButton.setOnClickListener(v -> {
            if (searchLayout.getVisibility() == View.GONE) {
                searchLayout.setVisibility(View.VISIBLE);
                filterLayout.setVisibility(View.VISIBLE);
            } else {
                searchLayout.setVisibility(View.GONE);
                filterLayout.setVisibility(View.GONE);
            }
        });

        // Close search button
        closeSearchButton.setOnClickListener(v -> {
            searchLayout.setVisibility(View.GONE);
            filterLayout.setVisibility(View.GONE);
            searchEditText.setText("");
            // TODO: Clear search filter
        });

        // Clear filter button
        clearFilterButton.setOnClickListener(v -> {
            spinnerRoleFilter.setSelection(0);
            spinnerCampusFilter.setSelection(0);
            // TODO: Clear filters and refresh list
        });

        // Floating Action Button - Add new account
        fabAddAccount.setOnClickListener(v -> {
            // TODO: Navigate to Create Account screen
            // Intent intent = new Intent(AccountListActivity.this, CreateAccountActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Add Account clicked", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Hiển thị/ẩn progress indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewAccounts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Hiển thị/ẩn error message
     */
    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        recyclerViewAccounts.setVisibility(View.GONE);
    }

    /**
     * Hiển thị/ẩn empty state
     */
    private void showEmptyState(boolean show) {
        emptyTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewAccounts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Chuyển về màn hình đăng nhập nếu chưa đăng nhập
     */
    private void goToLoginPage() {
        Intent intent = new Intent(AccountListActivity.this, LoginPage.class);
        startActivity(intent);
        finish();
    }
}