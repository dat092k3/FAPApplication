package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu; // Dùng để tạo menu khi nhấn nút menu
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fapapplication.activity.CurriculumActivity;
import com.example.fapapplication.activity.StudentAttendanceActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePage extends AppCompatActivity {

    // Khai báo các biến cho các thành phần UI
    private ImageButton menuButton;
    private BottomNavigationView bottomNavigationView;

    // Khai báo các CardView để bắt sự kiện click
    private CardView cardNotification, cardAppStatus, cardTimetable, cardExamSchedule, cardSemesterSchedule;
    private CardView cardAttendanceReport, cardMarkReport, cardCurriculumReport;

    // Firebase và Google Auth
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

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

    private void initializeViews() {
        // Ánh xạ các nút bấm và navigation
        menuButton = findViewById(R.id.menuButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Ánh xạ các CardView (thêm ID cho chúng trong file XML nếu bạn chưa có)
        // Lưu ý: Tôi sẽ giả định bạn đã thêm các ID sau vào các CardView tương ứng
        // Ví dụ: android:id="@+id/cardNotification" cho CardView đầu tiên
        // Nếu bạn chưa thêm ID, code sẽ báo lỗi. Hãy quay lại file XML và thêm chúng.
        // cardNotification = findViewById(R.id.cardNotification);
        // cardAppStatus = findViewById(R.id.cardAppStatus);
        // ... (làm tương tự cho các card khác)
        cardAttendanceReport = findViewById(R.id.cardAttendanceReport);
        cardMarkReport = findViewById(R.id.cardMarkReport);
        cardCurriculumReport = findViewById(R.id.cardCurriculumReport);
    }

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
                // Ví dụ: Chuyển sang trang Profile
                // startActivity(new Intent(HomePage.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                showToast("Settings clicked");
                return true;
            }
            return false;
        });

        // Thiết lập sự kiện click cho các CardView (sau khi đã thêm ID và ánh xạ)
        /*
        cardNotification.setOnClickListener(v -> showToast("Notification Card Clicked"));
        cardAppStatus.setOnClickListener(v -> showToast("Application Status Card Clicked"));
        */
        if (cardAttendanceReport != null) {
            cardAttendanceReport.setOnClickListener(v ->
                    startActivity(new Intent(HomePage.this, StudentAttendanceActivity.class))
            );
        }
        if (cardMarkReport != null) {
            cardMarkReport.setOnClickListener(v ->
                    showToast("Mark Report clicked")
            );
        }
        if (cardCurriculumReport != null) {
            cardCurriculumReport.setOnClickListener(v ->
                    startActivity(new Intent(HomePage.this, CurriculumActivity.class))
            );
        }
    }

    // Hiển thị một PopupMenu khi người dùng nhấn vào nút menu
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        // Tạo menu động, bạn cũng có thể inflate từ một file XML
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

    private void configureGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Hàm xử lý đăng xuất
    private void signOut() {
        // Đăng xuất khỏi Firebase
        auth.signOut();

        // Đăng xuất khỏi tài khoản Google
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            showToast("Signed out successfully");
            goToMainActivity();
        });
    }

    // Hàm tiện ích để chuyển về MainActivity
    private void goToMainActivity() {
        Intent intent = new Intent(HomePage.this, LoginPage.class);
        startActivity(intent);
        finish(); // Đóng HomePage lại
    }

    // Hàm tiện ích để hiển thị Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
