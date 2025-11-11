package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fapapplication.activity.SelectSubjectClassActivity;
import com.example.fapapplication.activity.SelectSubjectClassAttendanceActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeacherDashboard extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---

    // UI Components
    private DrawerLayout drawerLayout; // Layout chính cho menu trượt
    private ImageButton menuButton;
    private BottomNavigationView bottomNavigationView;
    private CardView cardCheckAttendance, cardGrades;

    // Logic & Auth
    private NavHeaderManager navHeaderManager; // Lớp quản lý Nav Header
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo layout này có DrawerLayout và NavigationView
        setContentView(R.layout.activity_teacher_dashboard);

        // --- KIỂM TRA ĐĂNG NHẬP ---
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            goToLoginPage();
            return;
        }

        // --- KHỞI TẠO VÀ CÀI ĐẶT ---
        initializeViews();
        configureGoogleSignInClient();
        setupNavigationDrawer(); // Hàm chính để cài đặt menu trượt
        setupClickListeners();   // Hàm này chỉ cài đặt cho các view khác
        setupOnBackPressed();
    }

    /**
     * Ánh xạ tất cả các views từ layout vào các biến Java.
     */
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout); // ID của DrawerLayout
        menuButton = findViewById(R.id.menuButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        cardCheckAttendance = findViewById(R.id.cardCheckAttendance);
        cardGrades = findViewById(R.id.cardGrades);
    }

    /**
     * Thiết lập menu trượt (Navigation Drawer) và NavHeaderManager.
     * Hàm này cũng sẽ xử lý sự kiện click cho menuButton.
     */
    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigation_view); // ID của NavigationView

        // 1. Khởi tạo lớp quản lý header
        navHeaderManager = new NavHeaderManager(this, drawerLayout, navigationView);

        // 2. Yêu cầu manager cập nhật thông tin người dùng lên header
        navHeaderManager.loadAndDisplayUserData();

        // 3. Đăng ký lắng nghe sự kiện logout từ manager
        navHeaderManager.setOnLogoutClickListener(this::signOut);

        // 4. Gán sự kiện cho nút menu để MỞ menu trượt
        menuButton.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * Thiết lập các sự kiện click cho các view KHÔNG phải là menu button.
     */
    private void setupClickListeners() {
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
            // Không cần điều kiện thứ 3 cho nav_profile, nó sẽ không bao giờ được gọi
            return false;
        });

        // Thiết lập sự kiện click cho các CardView
        cardCheckAttendance.setOnClickListener(v ->
                startActivity(new Intent(this, SelectSubjectClassAttendanceActivity.class))
        );
        cardGrades.setOnClickListener(v ->
                startActivity(new Intent(this, SelectSubjectClassActivity.class))
        );
    }

    /**
     * Thiết lập cách xử lý nút Back: ưu tiên đóng menu trượt nếu nó đang mở.
     */
    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Nếu menu đang mở, hãy đóng nó lại
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Nếu không, thực hiện hành vi back mặc định (thoát Activity)
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Cấu hình Google Sign-In Client để có thể đăng xuất.
     */
    private void configureGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Xử lý đăng xuất. Hàm này được gọi bởi NavHeaderManager.
     */
    private void signOut() {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            showToast("Signed out successfully");
            goToLoginPage();
        });
    }

    /**
     * Chuyển về màn hình LoginPage và xóa các activity cũ.
     */
    private void goToLoginPage() {
        Intent intent = new Intent(this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị một Toast message ngắn.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
