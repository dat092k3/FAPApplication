package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.fapapplication.adapter.CampusAdapter;
// Import lớp View Binding được tự động tạo ra
import com.example.fapapplication.databinding.ActivityLoginPageBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.List;

public class LoginPage extends AppCompatActivity {

    private static final String TAG = "LoginPage";
    private static final String FPT_EMAIL_SUFFIX = "@fpt.edu.vn";

    // Khai báo đối tượng View Binding
    private ActivityLoginPageBinding binding;

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CampusAdapter campusAdapter;
    private String selectedCampus;

    // Sử dụng ActivityResultLauncher cho việc đăng nhập Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(accountTask);
                } else {
                    Log.w(TAG, "Google Sign In activity was cancelled or failed.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo View Binding
        binding = ActivityLoginPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // Kiểm tra nếu người dùng đã đăng nhập thì chuyển thẳng đến trang chủ
        if (auth.getCurrentUser() != null) {
            goToHomePage();
            return;
        }

        // Tách logic khởi tạo ra các hàm riêng biệt cho dễ đọc
        setupGoogleSignIn();
        setupFeidSignInButton();
        setupCampusRecyclerView();
    }

    /**
     * Cấu hình các dịch vụ liên quan đến Google Sign-In.
     */
    private void setupGoogleSignIn() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        // Sử dụng biểu thức Lambda cho sự kiện click, gọn gàng hơn
        binding.signBtn.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(intent);
        });
    }

    /**
     * Cấu hình cho nút "Sign in with FEID".
     */
    private void setupFeidSignInButton() {
        binding.feIdBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginPage.this, LoginFeidPage.class);
            startActivity(intent);
        });
    }

    /**
     * Cấu hình cho RecyclerView hiển thị danh sách campus.
     */
    private void setupCampusRecyclerView() {
        List<String> campuses = Arrays.asList("FU_Hòa Lạc", "FU_Hồ Chí Minh", "FU_Đà Nẵng", "FU_Cần Thơ", "FU_Quy Nhơn");
        campusAdapter = new CampusAdapter(this, campuses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        binding.campusRecyclerView.setLayoutManager(layoutManager);
        binding.campusRecyclerView.setAdapter(campusAdapter);

        // SnapHelper giúp item tự động căn giữa khi cuộn
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(binding.campusRecyclerView);

        // Tính toán padding để item đầu và cuối có thể cuộn ra giữa màn hình
        int recyclerViewHeight = (int) (150 * getResources().getDisplayMetrics().density);
        int itemHeight = (int) (30 * getResources().getDisplayMetrics().density); // Chiều cao ước tính của một item
        int padding = (recyclerViewHeight / 2) - (itemHeight / 2);
        binding.campusRecyclerView.setPadding(0, padding, 0, padding);


        binding.campusRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Khi người dùng ngừng cuộn, xác định item ở giữa
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateSelectedCampus(snapHelper, layoutManager);
                }
            }
        });

        // Chọn item ở giữa làm giá trị mặc định khi activity được tạo
        binding.campusRecyclerView.post(() -> updateSelectedCampus(snapHelper, layoutManager));
    }

    /**
     * Cập nhật campus được chọn dựa vào item đang ở giữa RecyclerView.
     */
    private void updateSelectedCampus(SnapHelper snapHelper, LinearLayoutManager layoutManager) {
        View centerView = snapHelper.findSnapView(layoutManager);
        if (centerView != null) {
            int position = layoutManager.getPosition(centerView);
            campusAdapter.setSelectedPosition(position);
            selectedCampus = campusAdapter.getSelectedItem();
            Log.d(TAG, "Selected Campus: " + selectedCampus);
        }
    }

    /**
     * Xử lý kết quả trả về từ Google Sign-In.
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Kiểm tra email phải có đuôi @fpt.edu.vn
            if (account != null && account.getEmail() != null && account.getEmail().toLowerCase().endsWith(FPT_EMAIL_SUFFIX)) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                Toast.makeText(this, "Please use an @fpt.edu.vn email.", Toast.LENGTH_LONG).show();
                // Đăng xuất khỏi Google để người dùng có thể chọn tài khoản khác
                googleSignInClient.signOut();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google sign in failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xác thực với Firebase sử dụng token từ Google.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase sign-in successful.");
                        Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                        goToHomePage();
                    } else {
                        Log.e(TAG, "Firebase Authentication Failed.", task.getException());
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Chuyển đến màn hình HomePage và kết thúc màn hình hiện tại.
     */
    private void goToHomePage() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
        finish();
    }
}
