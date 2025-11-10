package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fapapplication.adapter.AccountAdapter;
import com.example.fapapplication.entity.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách tất cả user accounts trong hệ thống.
 * Cho phép Admin tìm kiếm, xem và quản lý các accounts.
 *
 * Features:
 * - Realtime data synchronization với Firebase
 * - Search và filter
 * - Swipe to refresh
 * - Error handling và retry mechanism
 */
public class AccountListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private EditText searchEditText;
    private FloatingActionButton fabAddAccount;
    private ImageButton backButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<User> allUsers;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);

        // Kiểm tra authentication
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Khởi tạo views
        initializeViews();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập listeners
        setupListeners();

        // Load dữ liệu từ Firebase
        loadUsersFromFirebase();
    }

    /**
     * Khởi tạo tất cả các views
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewAccounts);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        searchEditText = findViewById(R.id.searchEditText);
        fabAddAccount = findViewById(R.id.fabAddAccount);
        backButton = findViewById(R.id.backButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        allUsers = new ArrayList<>();
    }

    /**
     * Thiết lập RecyclerView với adapter và layout manager
     */
    private void setupRecyclerView() {
        adapter = new AccountAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Thiết lập click listener cho items
        adapter.setOnAccountClickListener((user, position) -> {
            // TODO: Navigate to detail screen
            Toast.makeText(this, "Clicked: " + user.getFullName(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Thiết lập các listeners cho buttons, search và swipe refresh
     */
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // FAB để thêm account mới
        fabAddAccount.setOnClickListener(v -> {
            // TODO: Navigate to Create Account screen
            Toast.makeText(this, "Add Account clicked", Toast.LENGTH_SHORT).show();
        });

        // Search listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Swipe to refresh listener
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Thiết lập màu cho refresh indicator
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_dark),
                getResources().getColor(android.R.color.holo_orange_dark),
                getResources().getColor(android.R.color.holo_green_dark)
        );
    }

    /**
     * Load danh sách users từ Firebase Realtime Database
     * Sử dụng ValueEventListener để nhận updates realtime
     */
    private void loadUsersFromFirebase() {
        // Hiển thị loading nếu chưa có data
        if (allUsers.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }

        // Tạo listener để đọc dữ liệu từ Firebase
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsers.clear();

                // Parse từng user từ Firebase
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        User user = new User();

                        // Đọc từng field từ Firebase
                        user.setId(userSnapshot.child("UID").getValue(String.class));
                        user.setName(userSnapshot.child("FullName").getValue(String.class));
                        user.setEmail(userSnapshot.child("Email").getValue(String.class));
                        user.setRole(userSnapshot.child("Role").getValue(String.class));
                        user.setCampus(userSnapshot.child("Campus").getValue(String.class));
                        user.setBirthdate(userSnapshot.child("Birthdate").getValue(String.class));

                        // Chỉ thêm user nếu có đủ thông tin cần thiết
                        if (user.getId() != null && user.getName() != null) {
                            allUsers.add(user);
                        }
                    } catch (Exception e) {
                        // Bỏ qua user nếu có lỗi parse
                        e.printStackTrace();
                    }
                }

                // Cập nhật UI
                updateUI();

                // Tắt loading và refresh indicators
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi khi load data
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                showError("Error loading accounts: " + databaseError.getMessage());
            }
        };

        // Attach listener to Firebase reference
        usersRef.addValueEventListener(usersListener);
    }

    /**
     * Refresh lại data từ Firebase
     * Method này được gọi khi user swipe down
     */
    private void refreshData() {
        // Clear search để hiển thị tất cả data
        searchEditText.setText("");

        // Vì đang dùng ValueEventListener (realtime), data sẽ tự động update
        // Nên chỉ cần force một lần fetch bằng cách dùng single value event
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsers.clear();

                // Parse từng user từ Firebase
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        User user = new User();

                        user.setId(userSnapshot.child("UID").getValue(String.class));
                        user.setName(userSnapshot.child("FullName").getValue(String.class));
                        user.setEmail(userSnapshot.child("Email").getValue(String.class));
                        user.setRole(userSnapshot.child("Role").getValue(String.class));
                        user.setCampus(userSnapshot.child("Campus").getValue(String.class));
                        user.setBirthdate(userSnapshot.child("Birthdate").getValue(String.class));

                        if (user.getId() != null && user.getName() != null) {
                            allUsers.add(user);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Cập nhật UI
                updateUI();

                // TẮT refresh indicator
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TẮT refresh indicator ngay cả khi có lỗi
                swipeRefreshLayout.setRefreshing(false);
                showError("Error refreshing: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Cập nhật UI dựa trên data hiện tại
     */
    private void updateUI() {
        if (allUsers.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("No accounts found");
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Apply current search filter nếu có
            String currentSearch = searchEditText.getText().toString();
            if (!currentSearch.isEmpty()) {
                filterUsers(currentSearch);
            } else {
                adapter.updateUsers(allUsers);
            }
        }
    }

    /**
     * Lọc danh sách users theo search query
     * Search trong name, email, role và campus
     */
    private void filterUsers(String query) {
        if (query.isEmpty()) {
            adapter.updateUsers(allUsers);
            updateEmptyState(allUsers.isEmpty());
            return;
        }

        List<User> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (User user : allUsers) {
            boolean matchFound = false;

            // Search trong name
            if (user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) {
                matchFound = true;
            }

            // Search trong email
            if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) {
                matchFound = true;
            }

            // Search trong role
            if (user.getRole() != null && user.getRole().toLowerCase().contains(lowerQuery)) {
                matchFound = true;
            }

            // Search trong campus
            if (user.getCampus() != null && user.getCampus().toLowerCase().contains(lowerQuery)) {
                matchFound = true;
            }

            if (matchFound) {
                filteredList.add(user);
            }
        }

        adapter.updateUsers(filteredList);
        updateEmptyState(filteredList.isEmpty());

        if (filteredList.isEmpty()) {
            emptyTextView.setText("No matches found for \"" + query + "\"");
        }
    }

    /**
     * Cập nhật trạng thái empty view
     */
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hiển thị error message cho user
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("Unable to load accounts.\nPlease check your connection and try again.");
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Cleanup khi Activity bị destroy
     * Quan trọng: Remove Firebase listener để tránh memory leaks
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove Firebase listener để tránh memory leak
        if (usersRef != null && usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }
}