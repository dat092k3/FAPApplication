package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AccountListActivity extends AppCompatActivity {

    // UI Components
    private ImageButton backButton;
    private FloatingActionButton fabAddAccount;
    private RecyclerView recyclerViewAccounts;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;

    // Firebase
    private FirebaseAuth auth;

    // Adapter và Data
    private AccountAdapter accountAdapter;
    private List<User> accountList;

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

        // Khởi tạo danh sách
        accountList = new ArrayList<>();

        // Ánh xạ views
        initializeViews();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập click listeners
        setupClickListeners();
    }

    /**
     * Ánh xạ các views từ layout
     */
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        fabAddAccount = findViewById(R.id.fabAddAccount);
        recyclerViewAccounts = findViewById(R.id.recyclerViewAccounts);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        emptyTextView = findViewById(R.id.emptyTextView);
    }

    /**
     * Thiết lập RecyclerView với adapter
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAccounts.setLayoutManager(layoutManager);

        accountAdapter = new AccountAdapter(this, accountList);
        recyclerViewAccounts.setAdapter(accountAdapter);

        // Thiết lập click listener cho adapter
        accountAdapter.setOnAccountClickListener((user, position) -> {
            // TODO: Navigate to Account Detail screen
            Toast.makeText(this, "Clicked: " + user.getFullName(), Toast.LENGTH_SHORT).show();
        });

        // Thiết lập long-click listener cho adapter
        accountAdapter.setOnAccountLongClickListener((user, position) -> {
            // TODO: Show options menu
            Toast.makeText(this, "Long clicked: " + user.getFullName(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    /**
     * Thiết lập các bộ lắng nghe sự kiện
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Floating Action Button - Add new account
        fabAddAccount.setOnClickListener(v -> {
            // TODO: Navigate to Create Account screen
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
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * Ẩn error message
     */
    private void hideError() {
        errorTextView.setVisibility(View.GONE);
    }

    /**
     * Cập nhật empty state
     */
    private void updateEmptyState() {
        boolean isEmpty = accountAdapter.getItemCount() == 0;
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewAccounts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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