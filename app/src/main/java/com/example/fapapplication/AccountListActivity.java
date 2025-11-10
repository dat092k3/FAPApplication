package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity hiển thị danh sách tất cả user accounts trong hệ thống.
 * Cho phép Admin tìm kiếm, xem và quản lý các accounts.
 *
 * Features:
 * - Realtime data synchronization với Firebase
 * - Text search (name, email)
 * - Filter by role (Admin, Teacher, Student)
 * - Filter by campus
 * - Combined filtering
 * - Swipe to refresh
 * - Error handling và retry mechanism
 */
public class AccountListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private EditText searchEditText;
    private Spinner spinnerRoleFilter;
    private Spinner spinnerCampusFilter;
    private ImageButton btnClearFilters;
    private FloatingActionButton fabAddAccount;
    private ImageButton backButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<User> allUsers;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference rolesRef;
    private DatabaseReference campusRef;
    private ValueEventListener usersListener;

    // Filter state
    private String currentSearchQuery = "";
    private String currentRoleFilter = "All Roles";
    private String currentCampusFilter = "All Campuses";

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

        // Khởi tạo Firebase references
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        rolesRef = FirebaseDatabase.getInstance().getReference("Role");
        campusRef = FirebaseDatabase.getInstance().getReference("Campus");

        // Khởi tạo views
        initializeViews();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Load filter options từ Firebase
        loadFilterOptions();

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
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        spinnerCampusFilter = findViewById(R.id.spinnerCampusFilter);
        btnClearFilters = findViewById(R.id.btnClearFilters);
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
            // Navigate to AccountDetailActivity
            Intent intent = new Intent(AccountListActivity.this, AccountDetailActivity.class);
            intent.putExtra("USER_ID", user.getId());
            startActivity(intent);
        });
    }

    /**
     * Load danh sách roles và campuses từ Firebase cho filter dropdowns
     */
    private void loadFilterOptions() {
        // Load roles từ Firebase
        rolesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> roles = new ArrayList<>();
                roles.add("All Roles"); // Default option

                for (DataSnapshot roleSnapshot : dataSnapshot.getChildren()) {
                    String role = roleSnapshot.getValue(String.class);
                    if (role != null && !role.isEmpty()) {
                        roles.add(role);
                    }
                }

                // Thiết lập adapter cho role spinner
                ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                        AccountListActivity.this,
                        android.R.layout.simple_spinner_item,
                        roles
                );
                roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoleFilter.setAdapter(roleAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Nếu lỗi, dùng default roles
                List<String> defaultRoles = Arrays.asList("All Roles", "Admin", "Teacher", "Student");
                ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                        AccountListActivity.this,
                        android.R.layout.simple_spinner_item,
                        defaultRoles
                );
                roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoleFilter.setAdapter(roleAdapter);
            }
        });

        // Load campuses từ Firebase
        campusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> campuses = new ArrayList<>();
                campuses.add("All Campuses"); // Default option

                for (DataSnapshot campusSnapshot : dataSnapshot.getChildren()) {
                    String campus = campusSnapshot.getValue(String.class);
                    if (campus != null && !campus.isEmpty()) {
                        campuses.add(campus);
                    }
                }

                // Thiết lập adapter cho campus spinner
                ArrayAdapter<String> campusAdapter = new ArrayAdapter<>(
                        AccountListActivity.this,
                        android.R.layout.simple_spinner_item,
                        campuses
                );
                campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCampusFilter.setAdapter(campusAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Nếu lỗi, dùng default campuses
                List<String> defaultCampuses = Arrays.asList(
                        "All Campuses",
                        "FU_Hà Nội",
                        "FU_Hồ Chí Minh",
                        "FU_Đà Nẵng",
                        "FU_Quy Nhơn",
                        "FU_Cần Thơ"
                );
                ArrayAdapter<String> campusAdapter = new ArrayAdapter<>(
                        AccountListActivity.this,
                        android.R.layout.simple_spinner_item,
                        defaultCampuses
                );
                campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCampusFilter.setAdapter(campusAdapter);
            }
        });
    }

    /**
     * Thiết lập các listeners cho buttons, search và filters
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
                currentSearchQuery = s.toString();
                applyAllFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Role filter listener
        spinnerRoleFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRoleFilter = parent.getItemAtPosition(position).toString();
                applyAllFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Campus filter listener
        spinnerCampusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCampusFilter = parent.getItemAtPosition(position).toString();
                applyAllFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> clearAllFilters());

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
     * Clear tất cả filters và reset về trạng thái ban đầu
     */
    private void clearAllFilters() {
        // Reset search
        searchEditText.setText("");

        // Reset spinners về "All"
        spinnerRoleFilter.setSelection(0);
        spinnerCampusFilter.setSelection(0);

        // Reset filter state
        currentSearchQuery = "";
        currentRoleFilter = "All Roles";
        currentCampusFilter = "All Campuses";

        // Apply filters (sẽ hiển thị tất cả users)
        applyAllFilters();

        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
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

                // Cập nhật UI với filters hiện tại
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                applyAllFilters();
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
        // Force một lần fetch bằng cách dùng single value event
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

                // Cập nhật UI với filters hiện tại
                applyAllFilters();

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
     * Áp dụng tất cả filters (search + role + campus) lên danh sách users
     * Method này được gọi mỗi khi có thay đổi trong search hoặc filters
     */
    private void applyAllFilters() {
        List<User> filteredList = new ArrayList<>();

        for (User user : allUsers) {
            boolean matchesSearch = matchesSearchQuery(user, currentSearchQuery);
            boolean matchesRole = matchesRoleFilter(user, currentRoleFilter);
            boolean matchesCampus = matchesCampusFilter(user, currentCampusFilter);

            // User phải match tất cả các filters
            if (matchesSearch && matchesRole && matchesCampus) {
                filteredList.add(user);
            }
        }

        // Cập nhật adapter với filtered list
        adapter.updateUsers(filteredList);
        updateEmptyState(filteredList);
    }

    /**
     * Kiểm tra user có match với search query không
     */
    private boolean matchesSearchQuery(User user, String query) {
        if (query.isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Search trong name
        if (user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Search trong email
        if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        return false;
    }

    /**
     * Kiểm tra user có match với role filter không
     */
    private boolean matchesRoleFilter(User user, String roleFilter) {
        if (roleFilter.equals("All Roles")) {
            return true;
        }

        return user.getRole() != null && user.getRole().equals(roleFilter);
    }

    /**
     * Kiểm tra user có match với campus filter không
     */
    private boolean matchesCampusFilter(User user, String campusFilter) {
        if (campusFilter.equals("All Campuses")) {
            return true;
        }

        return user.getCampus() != null && user.getCampus().equals(campusFilter);
    }

    /**
     * Cập nhật trạng thái empty view dựa trên filtered list
     */
    private void updateEmptyState(List<User> filteredList) {
        if (filteredList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            // Tạo message dựa trên filters đang active
            String message = buildEmptyStateMessage();
            emptyTextView.setText(message);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Tạo empty state message dựa trên filters đang active
     */
    private String buildEmptyStateMessage() {
        if (allUsers.isEmpty()) {
            return "No accounts found";
        }

        List<String> activeFilters = new ArrayList<>();

        if (!currentSearchQuery.isEmpty()) {
            activeFilters.add("search: \"" + currentSearchQuery + "\"");
        }

        if (!currentRoleFilter.equals("All Roles")) {
            activeFilters.add("role: " + currentRoleFilter);
        }

        if (!currentCampusFilter.equals("All Campuses")) {
            activeFilters.add("campus: " + currentCampusFilter);
        }

        if (activeFilters.isEmpty()) {
            return "No accounts found";
        }

        return "No matches found for:\n" + String.join(", ", activeFilters);
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