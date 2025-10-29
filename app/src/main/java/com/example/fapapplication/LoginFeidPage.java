package com.example.fapapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFeidPage extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email, password;
    private Spinner spinnerCampus;
    private Button btnLogin;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_feid_page);

        // Ánh xạ view
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        spinnerCampus = findViewById(R.id.spinnerCampus);
        btnLogin = findViewById(R.id.btnLogin);
        backButton = findViewById(R.id.backButton);
        TextView signup = findViewById(R.id.tvNoAccount);

        // Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Nếu người dùng đã đăng nhập thì chuyển thẳng vào HomePage
        if (user != null) {
            Intent i = new Intent(LoginFeidPage.this, HomePage.class);
            i.putExtra("User UID", user.getUid());
            startActivity(i);
            finish();
        }

        // Xử lý nút đăng nhập
        btnLogin.setOnClickListener(view -> {
            String em = email.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (em.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginFeidPage.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(LoginFeidPage.this);
            progressDialog.setMessage("Signing in...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            auth.signInWithEmailAndPassword(em, pass)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user1 = auth.getCurrentUser();
                            Intent i = new Intent(LoginFeidPage.this, HomePage.class);
                            i.putExtra("User UID", user1.getUid());
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(LoginFeidPage.this,
                                    "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Thiết lập spinner campus
        setupCampusSpinner();

        // Chuyển sang trang đăng ký
        signup.setOnClickListener(view -> {
            Intent i = new Intent(LoginFeidPage.this, SignUpActivity.class);
            startActivity(i);
            finish();
        });

        // Nút quay lại
        backButton.setOnClickListener(view -> {
            Intent i = new Intent(LoginFeidPage.this, LoginPage.class);
            startActivity(i);
            finish();
        });
    }

    // Hàm khởi tạo danh sách campus
    private void setupCampusSpinner() {
        String[] campuses = new String[]{
                "FU-Hòa Lạc",
                "FU-Hồ Chí Minh",
                "FU-Đà Nẵng",
                "FU-Cần Thơ",
                "FU-Quy Nhơn"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                campuses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampus.setAdapter(adapter);
    }
}
