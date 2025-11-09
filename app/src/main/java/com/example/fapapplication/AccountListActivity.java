package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountListActivity extends AppCompatActivity {

    private ImageButton backButton;
    private FirebaseAuth auth;

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

        // Thiết lập click listeners
        setupClickListeners();
    }

    /**
     * Ánh xạ các views từ layout
     */
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Thiết lập các bộ lắng nghe sự kiện
     */
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });
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