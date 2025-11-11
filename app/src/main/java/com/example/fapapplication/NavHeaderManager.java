package com.example.fapapplication;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NavHeaderManager {

    private static final String TAG = "NavHeaderManager";

    // UI & Context
    private final Context context;
    private final DrawerLayout drawerLayout;
    private final ImageView userAvatar;
    private final TextView userName;
    private final TextView userEmail;
    private final TextView userBalance;
    private final LinearLayout logoutButton;

    // Firebase
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // Callback
    public interface OnLogoutClickListener {
        void onLogoutClicked();
    }
    private OnLogoutClickListener logoutClickListener;

    public NavHeaderManager(Context context, DrawerLayout drawerLayout, NavigationView navigationView) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        View headerView = navigationView.getHeaderView(0);
        userAvatar = headerView.findViewById(R.id.userAvatar);
        userName = headerView.findViewById(R.id.userName);
        userEmail = headerView.findViewById(R.id.userEmail);
        userBalance = headerView.findViewById(R.id.userBalance);
        logoutButton = headerView.findViewById(R.id.logoutButton);

        setupLogoutButton();
    }

    public void setOnLogoutClickListener(OnLogoutClickListener listener) {
        this.logoutClickListener = listener;
    }

    /**
     * Hàm chính: Tự tải dữ liệu người dùng từ Firebase Auth và Firestore, sau đó hiển thị.
     * Activity chỉ cần gọi hàm này.
     */
    public void loadAndDisplayUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Cannot load user data, user is not logged in.");
            return;
        }

        // Lấy UID của người dùng để truy vấn database
        String uid = currentUser.getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        // Bắt đầu lấy dữ liệu từ Firestore
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            String fullnameFromDb = null;
            if (documentSnapshot.exists()) {
                // Nếu document tồn tại, đọc trường "fullname"
                fullnameFromDb = documentSnapshot.getString("FullName");
                Log.d(TAG, "Successfully fetched FullName: " + fullnameFromDb);
            } else {
                Log.w(TAG, "User document does not exist in Firestore for UID: " + uid);
            }
            // Gọi hàm hiển thị với fullname vừa lấy được (có thể là null)
            displayUserInfo(currentUser, fullnameFromDb);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load user data from Firestore", e);
            Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show();
            // Nếu có lỗi, vẫn hiển thị thông tin cơ bản để UI không bị trống
            displayUserInfo(currentUser, null);
        });
    }

    /**
     * Hàm nội bộ, chịu trách nhiệm CẬP NHẬT GIAO DIỆN với dữ liệu đã có.
     */
    private void displayUserInfo(FirebaseUser currentUser, String fullname) {
        // Cập nhật Email và Ảnh đại diện
        userEmail.setText(currentUser.getEmail());
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(context).load(currentUser.getPhotoUrl()).circleCrop().into(userAvatar);
        } else {
            userAvatar.setImageResource(R.drawable.logofap);
        }

        // --- LOGIC HIỂN THỊ TÊN ĐÃ ĐƯỢC TỐI ƯU ---
        // 1. Ưu tiên hiển thị fullname từ database
        if (fullname != null && !fullname.isEmpty()) {
            userName.setText(fullname);
        } else {
            // 2. Nếu không có, dùng tên từ Google/Facebook làm phương án dự phòng
            String displayNameFromAuth = currentUser.getDisplayName();
            if (displayNameFromAuth != null && !displayNameFromAuth.isEmpty()) {
                userName.setText(displayNameFromAuth);
            }
        }
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            if (logoutClickListener != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
                logoutClickListener.onLogoutClicked();
            }
        });
    }
}
