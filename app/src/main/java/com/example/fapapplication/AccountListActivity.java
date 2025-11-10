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
 */
public class AccountListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private EditText searchEditText;
    private FloatingActionButton fabAddAccount;
    private ImageButton backButton;

    private List<User> allUsers;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);

        // Kiểm tra authentication
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
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
     * Thiết lập các listeners cho buttons và search
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
    }

    /**
     * Load danh sách users từ Firebase Realtime Database
     */
    private void loadUsersFromFirebase() {
        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        // Đọc dữ liệu từ Firebase
        usersRef.addValueEventListener(new ValueEventListener() {
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

                        allUsers.add(user);
                    } catch (Exception e) {
                        // Bỏ qua user nếu có lỗi parse
                    }
                }

                // Cập nhật UI
                progressBar.setVisibility(View.GONE);

                if (allUsers.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.updateUsers(allUsers);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Error loading accounts");
                Toast.makeText(AccountListActivity.this,
                        "Error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lọc danh sách users theo search query
     */
    private void filterUsers(String query) {
        if (query.isEmpty()) {
            adapter.updateUsers(allUsers);
            return;
        }

        List<User> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (User user : allUsers) {
            if ((user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) ||
                    (user.getRole() != null && user.getRole().toLowerCase().contains(lowerQuery)) ||
                    (user.getCampus() != null && user.getCampus().toLowerCase().contains(lowerQuery))) {
                filteredList.add(user);
            }
        }

        adapter.updateUsers(filteredList);

        if (filteredList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("No matches found");
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
    }
}