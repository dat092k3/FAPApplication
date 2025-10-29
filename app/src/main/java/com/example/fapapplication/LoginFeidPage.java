package com.example.fapapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFeidPage extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText email, password;
    private Spinner spinnerCampus;
    private Button btnLogin;
    private ImageView backButton;
    private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_feid_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        spinnerCampus = findViewById(R.id.spinnerCampus);
        btnLogin = findViewById(R.id.btnLogin);
        backButton = findViewById(R.id.backButton);
        signup = findViewById(R.id.tvNoAccount);

        setupCampusSpinner();

        btnLogin.setOnClickListener(view -> loginUser());
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginPage.class));
            finish();
        });
        signup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });
    }

    private void setupCampusSpinner() {
        String[] campuses = new String[]{"FU-Hòa Lạc", "FU-Hồ Chí Minh", "FU-Đà Nẵng", "FU-Cần Thơ", "FU-Quy Nhơn"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, campuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampus.setAdapter(adapter);
    }

    private void loginUser() {
        String em = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String selectedCampus = spinnerCampus.getSelectedItem().toString();

        if (em.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang đăng nhập...");
        pd.setCancelable(false);
        pd.show();

        auth.signInWithEmailAndPassword(em, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        db.collection("Users").document(user.getUid()).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    pd.dismiss();
                                    if (documentSnapshot.exists()) {
                                        String campusInDB = documentSnapshot.getString("Campus");
                                        if (campusInDB != null && campusInDB.equals(selectedCampus)) {
                                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(this, HomePage.class);
                                            i.putExtra("User UID", user.getUid());
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Sai campus. Vui lòng chọn đúng campus đã đăng ký!", Toast.LENGTH_LONG).show();
                                            auth.signOut();
                                        }
                                    } else {
                                        Toast.makeText(this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
