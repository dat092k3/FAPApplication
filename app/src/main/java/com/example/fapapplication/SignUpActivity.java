package com.example.fapapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference database;
    private DatabaseReference campusRef;
    private Spinner spinnerCampus;
    private ArrayList<String> campusList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");
        campusRef = FirebaseDatabase.getInstance().getReference("Campus");

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

        spinnerCampus = findViewById(R.id.spinnerCampus);
        loadCampusData(); // ✅ Load campus từ Firebase vào Spinner

        // 👉 Chọn ngày sinh
        tilBirthdate.setEndIconOnClickListener(v -> showDatePicker(etBirthdate));
        etBirthdate.setOnClickListener(v -> showDatePicker(etBirthdate));

        // 👉 Xử lý đăng ký
        signup.setOnClickListener(view -> {
            String fullname = fullName.getText().toString().trim();
            String selectedCampus = spinnerCampus.getSelectedItem() != null ? spinnerCampus.getSelectedItem().toString() : "";
            String em = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirm_password.getText().toString().trim();
            String addr = address.getText().toString().trim();
            String birthdate = etBirthdate.getText().toString().trim();
            String studentIDValue = studentId.getText().toString().trim();

            // Kiểm tra input
            if (fullname.isEmpty() || em.isEmpty() || pass.isEmpty() ||
                    confirmPass.isEmpty() || addr.isEmpty() || birthdate.isEmpty() ||
                    selectedCampus.isEmpty() || studentIDValue.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Creating account...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // đăng ký Firebase Auth
            auth.createUserWithEmailAndPassword(em, pass).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("UID", user.getUid());
                        map.put("StudentId", studentIDValue);
                        map.put("Campus", selectedCampus);
                        map.put("FullName", fullname);
                        map.put("Email", em);
                        map.put("Password", pass);
                        map.put("Address", addr);
                        map.put("Birthdate", birthdate);
                        map.put("CreatedAt", System.currentTimeMillis());

                        database.child(user.getUid()).setValue(map).addOnCompleteListener(saveTask -> {
                            if (saveTask.isSuccessful()) {
                                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginFeidPage.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Sign Up Failed: " +
                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        // 👉 Nút back
        backButton.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginFeidPage.class));
            finish();
        });
    }

    private void loadCampusData() {
        campusRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                campusList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    campusList.add(snapshot.getValue(String.class));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, campusList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCampus.setAdapter(adapter);
            }
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
