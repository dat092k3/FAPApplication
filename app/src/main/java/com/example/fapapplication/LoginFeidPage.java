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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LoginFeidPage extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email, password;
    private Spinner spinnerCampus;
    private Button btnLogin;
    private ImageView backButton;
    private DatabaseReference campusRef, userRef;
    private ArrayList<String> campusList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_feid_page);

        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        spinnerCampus = findViewById(R.id.spinnerCampus);
        btnLogin = findViewById(R.id.btnLogin);
        backButton = findViewById(R.id.backButton);
        TextView signup = findViewById(R.id.tvNoAccount);

        auth = FirebaseAuth.getInstance();
        campusRef = FirebaseDatabase.getInstance().getReference("Campus");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        loadCampusData();

        btnLogin.setOnClickListener(view -> loginUser());

        signup.setOnClickListener(v -> {
            startActivity(new Intent(LoginFeidPage.this, SignUpActivity.class));
            finish();
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginFeidPage.this, LoginPage.class));
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

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        campusList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCampus.setAdapter(adapter);
            }
        });
    }

    private void loginUser() {
        String em = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String selectedCampus = spinnerCampus.getSelectedItem().toString();

        if (em.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Signing in...");
        dialog.setCancelable(false);
        dialog.show();

        auth.signInWithEmailAndPassword(em, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            userRef.child(user.getUid()).get().addOnCompleteListener(dataTask -> {
                                dialog.dismiss();
                                if (dataTask.isSuccessful()) {
                                    String campusInDB = dataTask.getResult().child("Campus").getValue(String.class);

                                    if (!selectedCampus.equals(campusInDB)) {
                                        auth.signOut();
                                        Toast.makeText(this, "Wrong campus selected!", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Intent i = new Intent(LoginFeidPage.this, HomePage.class);
                                    i.putExtra("User UID", user.getUid());
                                    startActivity(i);
                                    finish();
                                }
                            });
                        }
                    } else {
                        dialog.dismiss();
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
