package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboard extends AppCompatActivity {

    // Khai báo các biến cho các thành phần UI
    private ImageButton menuButton;
    private BottomNavigationView bottomNavigationView;

    // Khai báo các CardView cho CRUD features
    private CardView cardAccountManagement, cardSubjectManagement, cardClassManagement;

    // Firebase và Google Auth
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- KIỂM TRA ĐĂNG NHẬP ---
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Nếu người dùng chưa đăng nhập, quay về màn hình chính và kết thúc Activity này
            goToMainActivity();
            return;
        }

        // Cấu hình Google Client để có thể đăng xuất
        configureGoogleSignInClient();

        // --- ÁNH XẠ VIEWS TỪ LAYOUT ---
        initializeViews();

        // --- THIẾT LẬP CÁC BỘ LẮNG NGHE SỰ KIỆN (EVENT LISTENERS) ---
        setupClickListeners();
    }

    /**
     * Ánh xạ tất cả các views từ layout vào các biến Java
     */
    private void initializeViews() {
        // Ánh xạ các nút bấm và navigation
        menuButton = findViewById(R.id.menuButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Ánh xạ các CardView cho CRUD features
        cardAccountManagement = findViewById(R.id.cardAccountManagement);
        cardSubjectManagement = findViewById(R.id.cardSubjectManagement);
        cardClassManagement = findViewById(R.id.cardClassManagement);
    }

    /**
     * Thiết lập các bộ lắng nghe sự kiện cho các views
     */
    private void setupClickListeners() {
        // Sự kiện click cho nút Menu
        menuButton.setOnClickListener(view -> showPopupMenu(view));

        // Sự kiện click cho BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showToast("Home clicked");
                return true;
            } else if (itemId == R.id.nav_profile) {
                showToast("Profile clicked");
                return true;
            }
            return false;
        });

        // Thiết lập sự kiện click cho các CardView CRUD
        cardAccountManagement.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AccountListActivity.class);
            startActivity(intent);
        });

        cardSubjectManagement.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, SubjectListActivity.class);
            startActivity(intent);
        });

        cardClassManagement.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, ClassListActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Hiển thị PopupMenu khi người dùng nhấn vào nút menu
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Profile");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Sign Out");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Sign Out".equals(title)) {
                signOut();
                return true;
            } else if ("Profile".equals(title)) {
                showToast("Profile from menu");
                return true;
            } else if ("Settings".equals(title)) {
                showToast("Settings from menu");
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    /**
     * Cấu hình Google Sign-In Client để có thể đăng xuất
     */
    private void configureGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Xử lý đăng xuất khỏi Firebase và Google
     */
    private void signOut() {
        // Đăng xuất khỏi Firebase
        auth.signOut();

        // Đăng xuất khỏi tài khoản Google
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            showToast("Signed out successfully");
            goToMainActivity();
        });
    }

    /**
     * Chuyển về màn hình LoginPage
     */
    private void goToMainActivity() {
        Intent intent = new Intent(AdminDashboard.this, LoginPage.class);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị Toast message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}