package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.AccountAdapter;
import com.example.fapapplication.entity.User;
import com.example.fapapplication.repository.UserRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AccountListActivity extends AppCompatActivity {

    private static final String TAG = "AccountListActivity";

    // UI Components
    private ImageButton backButton;
    private FloatingActionButton fabAddAccount;
    private RecyclerView recyclerViewAccounts;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;

    // Firebase và Repository
    private FirebaseAuth auth;
    private UserRepository userRepository;

    // Adapter và Data
    private AccountAdapter accountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_account_list);

            // Kiểm tra đăng nhập
            auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not logged in, redirecting to login");
                goToLoginPage();
                return;
            }

            // Khởi tạo repository
            userRepository = new UserRepository();

            // Ánh xạ views
            initializeViews();

            // Thiết lập RecyclerView
            setupRecyclerView();

            // Thiết lập click listeners
            setupClickListeners();

            // Load data từ Firebase Realtime Database
            loadAccounts();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Ánh xạ các views từ layout
     */
    private void initializeViews() {
        try {
            backButton = findViewById(R.id.backButton);
            fabAddAccount = findViewById(R.id.fabAddAccount);
            recyclerViewAccounts = findViewById(R.id.recyclerViewAccounts);
            progressBar = findViewById(R.id.progressBar);
            errorTextView = findViewById(R.id.errorTextView);
            emptyTextView = findViewById(R.id.emptyTextView);

            // Kiểm tra null views
            if (backButton == null || fabAddAccount == null || recyclerViewAccounts == null ||
                    progressBar == null || errorTextView == null || emptyTextView == null) {
                throw new IllegalStateException("One or more views are null");
            }

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    /**
     * Thiết lập RecyclerView với adapter
     */
    private void setupRecyclerView() {
        try {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerViewAccounts.setLayoutManager(layoutManager);

            // Khởi tạo adapter với empty list
            accountAdapter = new AccountAdapter(this, new java.util.ArrayList<>());
            recyclerViewAccounts.setAdapter(accountAdapter);

            // Thiết lập click listener cho adapter
            accountAdapter.setOnAccountClickListener((user, position) -> {
                try {
                    // TODO: Navigate to Account Detail screen
                    Toast.makeText(this, "Clicked: " + (user != null ? user.getFullName() : "Unknown"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error handling account click", e);
                }
            });

            // Thiết lập long-click listener cho adapter
            accountAdapter.setOnAccountLongClickListener((user, position) -> {
                try {
                    // TODO: Show options menu
                    Toast.makeText(this, "Long clicked: " + (user != null ? user.getFullName() : "Unknown"), Toast.LENGTH_SHORT).show();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error handling account long-click", e);
                    return false;
                }
            });

            Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            throw e;
        }
    }

    /**
     * Thiết lập các bộ lắng nghe sự kiện
     */
    private void setupClickListeners() {
        try {
            // Back button
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }

            // Floating Action Button - Add new account
            if (fabAddAccount != null) {
                fabAddAccount.setOnClickListener(v -> {
                    try {
                        // TODO: Navigate to Create Account screen
                        Toast.makeText(this, "Add Account clicked", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling FAB click", e);
                    }
                });
            }

            Log.d(TAG, "Click listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    /**
     * Load danh sách accounts từ Firebase Realtime Database
     */
    private void loadAccounts() {
        if (userRepository == null) {
            Log.e(TAG, "UserRepository is null");
            showError("Repository not initialized");
            return;
        }

        showLoading(true);
        hideError();

        Log.d(TAG, "Starting to load accounts from Firebase");

        userRepository.getAllUsers(new UserRepository.OnUsersLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                try {
                    Log.d(TAG, "Accounts loaded successfully, count: " + (users != null ? users.size() : 0));
                    showLoading(false);

                    if (users != null && !users.isEmpty()) {
                        accountAdapter.updateAccountList(users);
                        updateEmptyState();
                        Log.d(TAG, "Adapter updated with " + users.size() + " users");
                    } else {
                        accountAdapter.updateAccountList(null);
                        updateEmptyState();
                        Log.d(TAG, "No users found");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing loaded users", e);
                    showLoading(false);
                    showError("Error displaying accounts: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading users: " + errorMessage);
                showLoading(false);
                showError(errorMessage != null ? errorMessage : "Failed to load accounts");
            }
        });
    }

    /**
     * Hiển thị/ẩn progress indicator
     */
    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (recyclerViewAccounts != null) {
                recyclerViewAccounts.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing/hiding loading", e);
        }
    }

    /**
     * Hiển thị/ẩn error message
     */
    private void showError(String message) {
        try {
            if (errorTextView != null) {
                errorTextView.setText(message != null ? message : "Unknown error");
                errorTextView.setVisibility(View.VISIBLE);
            }
            if (recyclerViewAccounts != null) {
                recyclerViewAccounts.setVisibility(View.GONE);
            }
            if (emptyTextView != null) {
                emptyTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message", e);
        }
    }

    /**
     * Ẩn error message
     */
    private void hideError() {
        try {
            if (errorTextView != null) {
                errorTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding error message", e);
        }
    }

    /**
     * Cập nhật empty state
     */
    private void updateEmptyState() {
        try {
            boolean isEmpty = accountAdapter == null || accountAdapter.getItemCount() == 0;
            if (emptyTextView != null) {
                emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
            if (recyclerViewAccounts != null) {
                recyclerViewAccounts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state", e);
        }
    }

    /**
     * Chuyển về màn hình đăng nhập nếu chưa đăng nhập
     */
    private void goToLoginPage() {
        try {
            Intent intent = new Intent(AccountListActivity.this, LoginPage.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data khi quay lại màn hình để cập nhật thay đổi
        if (userRepository != null) {
            loadAccounts();
        }
    }
}