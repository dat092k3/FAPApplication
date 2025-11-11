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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---

    // UI Components
    private DrawerLayout drawerLayout; // Layout chính cho menu trượt
    private ImageButton menuButton;
    private CardView cardAccountManagement, cardSubjectManagement,
            cardClassManagement, cardStatistic, cardNotification;

    // Logic & Auth
    private NavHeaderManager navHeaderManager; // Lớp quản lý Nav Header
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo layout này có DrawerLayout và NavigationView
        setContentView(R.layout.activity_admin_dashboard);

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
        setupCardViewListeners(); // Tách riêng logic click CardView
        setupOnBackPressed();
    }

    /**
     * Ánh xạ tất cả các views từ layout vào các biến Java.
     */
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout); // ID của DrawerLayout
        menuButton = findViewById(R.id.menuButton);

        cardAccountManagement = findViewById(R.id.cardAccountManagement);
        cardSubjectManagement = findViewById(R.id.cardSubjectManagement);
        cardClassManagement = findViewById(R.id.cardClassManagement);
        cardStatistic = findViewById(R.id.cardStatistic);
        cardNotification = findViewById(R.id.cardNotification);
    }

    /**
     * Thiết lập menu trượt (Navigation Drawer) và NavHeaderManager.
     */
    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigation_view); // ID của NavigationView

        // 1. Khởi tạo lớp quản lý header, truyền vào các component cần thiết
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
     * Thiết lập sự kiện click cho các CardView.
     */
    private void setupCardViewListeners() {
        cardAccountManagement.setOnClickListener(v -> startActivity(new Intent(this, AccountListActivity.class)));
        cardSubjectManagement.setOnClickListener(v -> startActivity(new Intent(this, SubjectListActivity.class)));
        cardClassManagement.setOnClickListener(v -> startActivity(new Intent(this, ClassListActivity.class)));
//        cardStatistic.setOnClickListener(v -> startActivity(new Intent(this, AdminStatisticActivity.class)));
//        cardNotification.setOnClickListener(v -> startActivity(new Intent(this, AdminNotificationActivity.class)));
    }

    /**
     * Thiết lập cách xử lý sự kiện nhấn nút Back theo chuẩn AndroidX.
     * Ưu tiên đóng menu trượt nếu nó đang mở.
     */
    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Nếu menu đang mở, hãy đóng nó lại
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
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
     * Xử lý đăng xuất. Hàm này được gọi bởi NavHeaderManager.
     */
    private void signOut() {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            showToast("Signed out successfully");
            goToLoginPage();
        });
    }

    // --- CÁC HÀM TIỆN ÍCH (Giữ nguyên) ---

    private void configureGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void goToLoginPage() {
        Intent intent = new Intent(this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
