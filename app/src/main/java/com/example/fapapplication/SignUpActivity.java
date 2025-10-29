package com.example.fapapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        EditText studentId = findViewById(R.id.etStudentId);
        EditText fullName = findViewById(R.id.etFullName);
        EditText email = findViewById(R.id.etEmail);
        EditText password = findViewById(R.id.etPassword);
        EditText confirm_password = findViewById(R.id.etConfirmPassword);
        EditText address = findViewById(R.id.etAddress);
        EditText etBirthdate = findViewById(R.id.etBirthdate);
        Button signup = findViewById(R.id.btnSignUp);
        ImageView backButton = findViewById(R.id.backButton);
        TextInputLayout tilBirthdate = findViewById(R.id.tilBirthdate);

        // Cho phép chọn ngày sinh
        tilBirthdate.setEndIconOnClickListener(v -> showDatePicker(etBirthdate));
        etBirthdate.setOnClickListener(v -> showDatePicker(etBirthdate));

        // 🔹 Nút đăng ký
        signup.setOnClickListener(view -> {
            String fullname = fullName.getText().toString().trim();
            String em = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirm_password.getText().toString().trim();
            String addr = address.getText().toString().trim();
            String birthdate = etBirthdate.getText().toString().trim();

            // Kiểm tra các trường
            if (fullname.isEmpty() || em.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || addr.isEmpty() || birthdate.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                confirm_password.setError("Password does not match");
                return;
            }

            if (pass.length() < 6) {
                password.setError("Password must be at least 6 characters");
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
            progressDialog.setMessage("Creating account...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // 🔹 Tạo tài khoản Firebase
            auth.createUserWithEmailAndPassword(em, pass)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // 🔹 Lưu thông tin người dùng vào Realtime Database
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("UID", user.getUid());
                                map.put("StudentId", studentId.getText().toString());
                                map.put("FullName", fullName.getText().toString());
                                map.put("Email", em);
                                map.put("Password", pass);
                                map.put("Address", addr);
                                map.put("Birthdate", birthdate);
                                map.put("CreatedAt", System.currentTimeMillis());

                                database.child(user.getUid()).setValue(map)
                                        .addOnCompleteListener(saveTask -> {
                                            if (saveTask.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent(SignUpActivity.this, LoginFeidPage.class);
                                                startActivity(i);
                                                finish();
                                            } else {
                                                Toast.makeText(SignUpActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 🔹 Nút quay lại
        backButton.setOnClickListener(view -> {
            Intent i = new Intent(SignUpActivity.this, LoginFeidPage.class);
            startActivity(i);
            finish();
        });
    }

    private void showDatePicker(EditText etBirthdate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etBirthdate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Không cho chọn ngày tương lai
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Mở menu chọn năm ngay khi mở dialog
        datePickerDialog.setOnShowListener(dialog -> {
            try {
                datePickerDialog.getDatePicker().findViewById(
                        getResources().getIdentifier("date_picker_header_year", "id", "android")
                ).performClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        datePickerDialog.show();
    }
}
