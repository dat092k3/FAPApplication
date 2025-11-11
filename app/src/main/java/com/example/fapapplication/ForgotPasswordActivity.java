package com.example.fapapplication;    import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    // --- UI Components ---
    private EditText etEmail;
    private MaterialButton btnSendLink;
    private ProgressBar progressBar;
    private ImageButton backButton;

    // --- Firebase ---
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // --- Khởi tạo ---
        initializeViews();
        auth = FirebaseAuth.getInstance();

        // --- Thiết lập sự kiện click ---
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        // Sự kiện cho nút gửi link
        btnSendLink.setOnClickListener(v -> sendPasswordResetEmail());

        // Sự kiện cho nút back
        backButton.setOnClickListener(v -> finish()); // Đóng activity hiện tại và quay về
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        // --- Kiểm tra dữ liệu đầu vào ---
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please provide a valid email");
            etEmail.requestFocus();
            return;
        }

        // --- Hiển thị ProgressBar và gọi Firebase ---
        showLoading(true);

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            showLoading(false); // Ẩn ProgressBar sau khi có kết quả
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                // (Tùy chọn) Tự động quay về trang Login sau vài giây
                // finish();
            } else {
                // Hiển thị lỗi từ Firebase
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Hiển thị hoặc ẩn ProgressBar và bật/tắt nút bấm.
     * @param isLoading True để hiển thị loading, False để ẩn.
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendLink.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendLink.setEnabled(true);
        }
    }
}
    