package com.example.fapapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fapapplication.entity.User;
import com.example.fapapplication.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity để xem và chỉnh sửa thông tin chi tiết của user account
 */
public class AccountDetailActivity extends AppCompatActivity {

    // UI Components
    private ImageButton backButton;
    private TextView tvTitle, tvUserAvatar, tvUserId, tvCreatedAt, tvAccountStatus, tvError;
    private EditText etFullName, etEmail, etStudentId, etBirthdate, etAddress;
    private Spinner spinnerRole, spinnerCampus;
    private SwitchCompat switchAccountStatus;
    private Button btnCancel, btnSave;
    private ProgressBar progressBar;
    private View formContainer;

    // Data
    private String userId;
    private User currentUser;
    private UserRepository userRepository;

    // Filter data từ Firebase
    private List<String> roleList = new ArrayList<>();
    private List<String> campusList = new ArrayList<>();

    // Flags
    private boolean isLoading = false;
    private boolean hasUnsavedChanges = false;
    private boolean isValidatingForm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        // Kiểm tra authentication
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy userId từ Intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        userRepository = new UserRepository();
        initializeViews();
        setupClickListeners();
        setupValidationListeners();
        loadFilterData();
        loadUserData();
    }

    /**
     * Khởi tạo tất cả views
     */
    private void initializeViews() {
        try {
            backButton = findViewById(R.id.backButton);
            tvTitle = findViewById(R.id.tvTitle);
            tvUserAvatar = findViewById(R.id.tvUserAvatar);
            tvUserId = findViewById(R.id.tvUserId);
            tvCreatedAt = findViewById(R.id.tvCreatedAt);
            tvAccountStatus = findViewById(R.id.tvAccountStatus);
            tvError = findViewById(R.id.tvError);

            etFullName = findViewById(R.id.etFullName);
            etEmail = findViewById(R.id.etEmail);
            etStudentId = findViewById(R.id.etStudentId);
            etBirthdate = findViewById(R.id.etBirthdate);
            etAddress = findViewById(R.id.etAddress);

            spinnerRole = findViewById(R.id.spinnerRole);
            spinnerCampus = findViewById(R.id.spinnerCampus);
            switchAccountStatus = findViewById(R.id.switchAccountStatus);

            btnCancel = findViewById(R.id.btnCancel);
            btnSave = findViewById(R.id.btnSave);

            progressBar = findViewById(R.id.progressBar);
            formContainer = findViewById(R.id.formContainer);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Setup tất cả click listeners
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Birthdate picker
        etBirthdate.setOnClickListener(v -> showDatePicker());

        // Account status switch - với confirmation cho deactivation
        switchAccountStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Nếu đang loading, bỏ qua
            if (isLoading) {
                return;
            }

            // Nếu user đang tắt account, hiển thị confirmation dialog
            if (!isChecked && currentUser != null && currentUser.isActive()) {
                // User đang cố gắng deactivate account, hiển thị warning
                showDeactivationConfirmation();
            } else {
                // Activation hoặc không có thay đổi
                updateAccountStatusUI(isChecked);
                hasUnsavedChanges = true;
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog();
            } else {
                finish();
            }
        });

        // Save button - validate và save
        btnSave.setOnClickListener(v -> {
            if (validateAllFields()) {
                saveUserData();
            }
        });

        // Track changes
        setupChangeTracking();
    }

    /**
     * Track thay đổi trong các EditText
     */
    private void setupChangeTracking() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (!hasFocus && !isLoading) {
                hasUnsavedChanges = true;
            }
        };

        etFullName.setOnFocusChangeListener(focusListener);
        etEmail.setOnFocusChangeListener(focusListener);
        etStudentId.setOnFocusChangeListener(focusListener);
        etAddress.setOnFocusChangeListener(focusListener);
    }

    /**
     * Setup validation listeners để clear errors khi user nhập liệu
     */
    private void setupValidationListeners() {
        // Clear error khi user bắt đầu nhập
        etFullName.addTextChangedListener(new ValidationTextWatcher(etFullName));
        etEmail.addTextChangedListener(new ValidationTextWatcher(etEmail));
        etStudentId.addTextChangedListener(new ValidationTextWatcher(etStudentId));
        etBirthdate.addTextChangedListener(new ValidationTextWatcher(etBirthdate));

        // Clear error khi chọn spinner
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isLoading) {
                    clearSpinnerError();
                    hasUnsavedChanges = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCampus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isLoading) {
                    clearSpinnerError();
                    hasUnsavedChanges = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * TextWatcher để clear error khi user nhập
     */
    private class ValidationTextWatcher implements TextWatcher {
        private EditText editText;

        public ValidationTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isLoading && !isValidatingForm) {
                clearFieldError(editText);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validate toàn bộ form trước khi save
     */
    private boolean validateAllFields() {
        isValidatingForm = true;
        boolean isValid = true;

        // Clear tất cả errors trước
        clearAllErrors();

        // Validate từng field
        if (!validateFullName()) isValid = false;
        if (!validateEmail()) isValid = false;
        if (!validateRole()) isValid = false;
        if (!validateCampus()) isValid = false;
        if (!validateStudentId()) isValid = false;
        if (!validateBirthdate()) isValid = false;

        isValidatingForm = false;

        // Hiển thị message nếu có lỗi
        if (!isValid) {
            Toast.makeText(this, "Please fix all errors before saving", Toast.LENGTH_LONG).show();
            // Focus vào field đầu tiên có lỗi
            focusFirstError();
        }

        return isValid;
    }

    /**
     * Validate Full Name
     */
    private boolean validateFullName() {
        String fullName = etFullName.getText().toString().trim();

        if (fullName.isEmpty()) {
            showFieldError(etFullName, "Full name is required");
            return false;
        }

        if (fullName.length() < 2) {
            showFieldError(etFullName, "Full name must be at least 2 characters");
            return false;
        }

        if (fullName.length() > 100) {
            showFieldError(etFullName, "Full name is too long (max 100 characters)");
            return false;
        }

        // Kiểm tra có chứa số không
        if (fullName.matches(".*\\d.*")) {
            showFieldError(etFullName, "Full name cannot contain numbers");
            return false;
        }

        return true;
    }

    /**
     * Validate Email
     */
    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showFieldError(etEmail, "Email is required");
            return false;
        }

        if (!isValidEmail(email)) {
            showFieldError(etEmail, "Please enter a valid email address");
            return false;
        }

        return true;
    }

    /**
     * Validate Role
     */
    private boolean validateRole() {
        String selectedRole = spinnerRole.getSelectedItem().toString();

        if (selectedRole.equals("Select Role")) {
            showSpinnerError("Please select a role");
            return false;
        }

        return true;
    }

    /**
     * Validate Campus
     */
    private boolean validateCampus() {
        String selectedCampus = spinnerCampus.getSelectedItem().toString();

        if (selectedCampus.equals("Select Campus")) {
            showSpinnerError("Please select a campus");
            return false;
        }

        return true;
    }

    /**
     * Validate Student ID (bắt buộc cho Student/Teacher)
     */
    private boolean validateStudentId() {
        String selectedRole = spinnerRole.getSelectedItem().toString();
        String studentId = etStudentId.getText().toString().trim();

        // Chỉ bắt buộc với Student và Teacher
        if ((selectedRole.equals("Student") || selectedRole.equals("Teacher")) && studentId.isEmpty()) {
            showFieldError(etStudentId, selectedRole + " ID is required");
            return false;
        }

        // Nếu có nhập, kiểm tra format
        if (!studentId.isEmpty() && studentId.length() < 3) {
            showFieldError(etStudentId, "ID must be at least 3 characters");
            return false;
        }

        return true;
    }

    /**
     * Validate Birthdate (optional nhưng nếu có thì phải đúng format)
     */
    private boolean validateBirthdate() {
        String birthdate = etBirthdate.getText().toString().trim();

        // Optional field
        if (birthdate.isEmpty()) {
            return true;
        }

        // Kiểm tra format DD/MM/YYYY
        if (!isValidDateFormat(birthdate)) {
            showFieldError(etBirthdate, "Invalid date format (DD/MM/YYYY)");
            return false;
        }

        // Kiểm tra tuổi hợp lệ (10-100 tuổi)
        if (!isValidAge(birthdate)) {
            showFieldError(etBirthdate, "Age must be between 10 and 100 years");
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra email format
     */
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Kiểm tra date format DD/MM/YYYY
     */
    private boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Kiểm tra tuổi hợp lệ (10-100)
     */
    private boolean isValidAge(String birthdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(sdf.parse(birthdate));

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

            if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }

            return age >= 10 && age <= 100;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== ERROR HANDLING METHODS ====================

    /**
     * Hiển thị error cho EditText
     */
    private void showFieldError(EditText field, String error) {
        field.setError(error);
        field.requestFocus();
    }

    /**
     * Clear error cho EditText
     */
    private void clearFieldError(EditText field) {
        field.setError(null);
    }

    /**
     * Hiển thị error cho Spinner (dùng Toast)
     */
    private void showSpinnerError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clear spinner error
     */
    private void clearSpinnerError() {
        // Spinner không có setError, chỉ cần clear toast
    }

    /**
     * Clear tất cả errors
     */
    private void clearAllErrors() {
        clearFieldError(etFullName);
        clearFieldError(etEmail);
        clearFieldError(etStudentId);
        clearFieldError(etBirthdate);
        clearFieldError(etAddress);
        tvError.setVisibility(View.GONE);
    }

    /**
     * Focus vào field đầu tiên có lỗi
     */
    private void focusFirstError() {
        if (etFullName.getError() != null) {
            etFullName.requestFocus();
        } else if (etEmail.getError() != null) {
            etEmail.requestFocus();
        } else if (etStudentId.getError() != null) {
            etStudentId.requestFocus();
        } else if (etBirthdate.getError() != null) {
            etBirthdate.requestFocus();
        }
    }

    // ==================== DATA LOADING METHODS ====================

    /**
     * Load roles và campuses từ Firebase
     */
    private void loadFilterData() {
        DatabaseReference rolesRef = FirebaseDatabase.getInstance().getReference("Role");
        DatabaseReference campusRef = FirebaseDatabase.getInstance().getReference("Campus");

        // Load roles
        rolesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roleList.clear();
                roleList.add("Select Role");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String role = snapshot.getValue(String.class);
                    if (role != null && !role.isEmpty()) {
                        roleList.add(role);
                    }
                }
                setupRoleSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                roleList = Arrays.asList("Select Role", "Admin", "Teacher", "Student");
                setupRoleSpinner();
            }
        });

        // Load campuses
        campusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                campusList.clear();
                campusList.add("Select Campus");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String campus = snapshot.getValue(String.class);
                    if (campus != null && !campus.isEmpty()) {
                        campusList.add(campus);
                    }
                }
                setupCampusSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                campusList = Arrays.asList("Select Campus", "FU_Hà Nội", "FU_Hồ Chí Minh",
                        "FU_Đà Nẵng", "FU_Quy Nhơn", "FU_Cần Thơ");
                setupCampusSpinner();
            }
        });
    }

    /**
     * Setup Role spinner
     */
    private void setupRoleSpinner() {
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, roleList
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRole.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup Campus spinner
     */
    private void setupCampusSpinner() {
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, campusList
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCampus.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load user data từ Firebase
     */
    private void loadUserData() {
        isLoading = true;
        showLoading(true);

        userRepository.getUserByUid(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onSuccess(User user) {
                isLoading = false;
                showLoading(false);
                if (user != null) {
                    currentUser = user;
                    populateFormFields(user);
                } else {
                    showError("User not found");
                }
            }

            @Override
            public void onError(String errorMessage) {
                isLoading = false;
                showLoading(false);
                showError("Failed to load user: " + errorMessage);
            }
        });
    }

    /**
     * Điền dữ liệu user vào form
     */
    private void populateFormFields(User user) {
        try {
            // Avatar với initials
            String initials = getInitials(user.getName());
            tvUserAvatar.setText(initials);

            // User info
            tvUserId.setText("ID: " + user.getId());
            tvCreatedAt.setText("Created: " + formatTimestamp(user.getCreatedAt()));

            // Form fields
            etFullName.setText(user.getName());
            etEmail.setText(user.getEmail());
            etStudentId.setText(user.getStudentId());
            etBirthdate.setText(user.getBirthdate());
            etAddress.setText(user.getAddress());

            // Role spinner
            if (user.getRole() != null) {
                int rolePosition = roleList.indexOf(user.getRole());
                if (rolePosition >= 0) {
                    spinnerRole.setSelection(rolePosition);
                }
            }

            // Campus spinner
            if (user.getCampus() != null) {
                int campusPosition = campusList.indexOf(user.getCampus());
                if (campusPosition >= 0) {
                    spinnerCampus.setSelection(campusPosition);
                }
            }

            // Account status
            boolean isActive = user.isActive();
            switchAccountStatus.setChecked(isActive);
            tvAccountStatus.setText(isActive ? "Active" : "Inactive");
            tvAccountStatus.setTextColor(getResources().getColor(
                    isActive ? R.color.success_green : android.R.color.holo_red_dark
            ));

            // Reset flag sau khi load xong
            hasUnsavedChanges = false;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error displaying user data");
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Lấy initials từ tên
     */
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    /**
     * Format timestamp thành date string
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "Unknown";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(timestamp);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Hiển thị date picker
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Parse current date nếu có
        String currentDate = etBirthdate.getText().toString();
        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");
                if (parts.length == 3) {
                    calendar.set(Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[0]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            dayOfMonth, month + 1, year);
                    etBirthdate.setText(date);
                    hasUnsavedChanges = true;
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Hiển thị/ẩn loading
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        formContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    /**
     * Hiển thị error message
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        formContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Dialog xác nhận khi có unsaved changes
     */
    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Discard them?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Lưu thông tin user vào Firebase
     */
    private void saveUserData() {
        // Hiển thị loading
        showSaveLoading(true);

        // Tạo User object từ form data
        User updatedUser = collectFormData();

        // Kiểm tra xem email có thay đổi không
        boolean emailChanged = !updatedUser.getEmail().equals(currentUser.getEmail());

        if (emailChanged) {
            // Nếu email thay đổi, cần update cả Firebase Auth
            showEmailChangeConfirmation(updatedUser);
        } else {
            // Chỉ update Realtime Database
            updateUserInDatabase(updatedUser);
        }
    }

    /**
     * Thu thập dữ liệu từ form vào User object
     */
    private User collectFormData() {
        User user = new User();
        user.setId(userId);
        user.setUid(userId);
        user.setName(etFullName.getText().toString().trim());
        user.setFullName(etFullName.getText().toString().trim());
        user.setEmail(etEmail.getText().toString().trim());
        user.setRole(spinnerRole.getSelectedItem().toString());
        user.setCampus(spinnerCampus.getSelectedItem().toString());
        user.setStudentId(etStudentId.getText().toString().trim());
        user.setBirthdate(etBirthdate.getText().toString().trim());
        user.setAddress(etAddress.getText().toString().trim());
        user.setIsActive(switchAccountStatus.isChecked());
        user.setCreatedAt(currentUser.getCreatedAt()); // Giữ nguyên createdAt

        return user;
    }

    /**
     * Update user vào Firebase Realtime Database
     */
    private void updateUserInDatabase(User updatedUser) {
        userRepository.updateUser(updatedUser, new UserRepository.OnUserOperationListener() {
            @Override
            public void onSuccess() {
                showSaveLoading(false);
                currentUser = updatedUser;
                hasUnsavedChanges = false;
                showSuccessDialog();
            }

            @Override
            public void onError(String errorMessage) {
                showSaveLoading(false);
                showSaveError("Failed to save: " + errorMessage);
            }
        });
    }

    /**
     * Hiển thị dialog xác nhận thay đổi email
     */
    private void showEmailChangeConfirmation(User updatedUser) {
        showSaveLoading(false);

        new AlertDialog.Builder(this)
                .setTitle("Change Email Address")
                .setMessage("Changing email address will also update your login credentials. " +
                        "You will need to use the new email for future logins.\n\n" +
                        "Are you sure you want to continue?")
                .setPositiveButton("Yes, Change Email", (dialog, which) -> {
                    showSaveLoading(true);
                    updateEmailAndDatabase(updatedUser);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled, do nothing
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Update cả email trong Firebase Auth và Realtime Database
     */
    private void updateEmailAndDatabase(User updatedUser) {
        String newEmail = updatedUser.getEmail();

        // Lấy current Firebase user
        com.google.firebase.auth.FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            showSaveLoading(false);
            showSaveError("Authentication error. Please login again.");
            return;
        }

        // Update email trong Firebase Auth trước
        firebaseUser.updateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    // Email updated thành công trong Auth, giờ update Database
                    updateUserInDatabase(updatedUser);
                })
                .addOnFailureListener(e -> {
                    showSaveLoading(false);
                    String errorMessage = "Failed to update email: ";

                    // Parse error message
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("requires-recent-login")) {
                            errorMessage += "Please logout and login again before changing email.";
                        } else if (e.getMessage().contains("email-already-in-use")) {
                            errorMessage += "This email is already in use by another account.";
                        } else if (e.getMessage().contains("invalid-email")) {
                            errorMessage += "Invalid email format.";
                        } else {
                            errorMessage += e.getMessage();
                        }
                    } else {
                        errorMessage += "Unknown error occurred.";
                    }

                    showSaveError(errorMessage);
                });
    }

    /**
     * Hiển thị dialog thành công
     */
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("User information has been updated successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Pass result back với flag để refresh list
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Hiển thị error khi save
     */
    private void showSaveError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Hiển thị/ẩn loading khi save
     */
    private void showSaveLoading(boolean show) {
        if (show) {
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            btnSave.setText("Saving...");
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
            btnSave.setText("Save Changes");
            progressBar.setVisibility(View.GONE);
        }
    }


    /**
     * Update UI khi account status thay đổi
     */
    private void updateAccountStatusUI(boolean isActive) {
        tvAccountStatus.setText(isActive ? "Active" : "Inactive");
        tvAccountStatus.setTextColor(getResources().getColor(
                isActive ? R.color.success_green : android.R.color.holo_red_dark
        ));
    }

    /**
     * Hiển thị dialog xác nhận khi deactivate account
     */
    private void showDeactivationConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate Account")
                .setMessage("Are you sure you want to deactivate this account?\n\n" +
                        "Deactivated accounts will not be able to log in until reactivated.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Deactivate", (dialog, which) -> {
                    // User confirmed deactivation
                    switchAccountStatus.setChecked(false);
                    updateAccountStatusUI(false);
                    hasUnsavedChanges = true;
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled, revert switch
                    switchAccountStatus.setChecked(true);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }
}