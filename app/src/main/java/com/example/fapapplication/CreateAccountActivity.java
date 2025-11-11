package com.example.fapapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import com.example.fapapplication.repository.UserRepository;
import com.example.fapapplication.utils.ValidationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * CreateAccountActivity cho phép Admin tạo account mới cho Teacher hoặc Student.
 *
 * Features:
 * - Form nhập đầy đủ thông tin user
 * - Chọn role (Teacher/Student only, không có Admin)
 * - Chọn campus từ Firebase
 * - Validation đầy đủ
 * - Tạo account trong Firebase Auth và lưu data vào Realtime Database
 */
public class CreateAccountActivity extends AppCompatActivity {

    // UI Components
    private ImageButton backButton;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    // Form fields
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etStudentId;
    private EditText etBirthdate;
    private EditText etAddress;
    private RadioGroup radioGroupRole;
    private RadioButton radioTeacher;
    private RadioButton radioStudent;
    private Spinner spinnerCampus;

    // Error indicators
    private TextView tvFullNameError;
    private TextView tvEmailError;
    private TextView tvPasswordError;
    private TextView tvConfirmPasswordError;
    private TextView tvStudentIdError;
    private TextView tvBirthdateError;
    private TextView tvRoleError;
    private TextView tvCampusError;

    // Action buttons
    private Button btnCancel;
    private Button btnCreate;

    // Firebase
    private DatabaseReference campusRef;
    private UserRepository userRepository;

    // Data
    private List<String> campusList;
    private ArrayAdapter<String> campusAdapter;
    private Calendar selectedCalendar;

    // State management for account creation process
    private boolean isCreatingAccount = false;
    private static final String STATE_IS_CREATING = "state_is_creating";
    private static final String STATE_BIRTHDATE = "state_birthdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initializeViews();
        setupClickListeners();
        loadCampusData();

        // Initialize UserRepository
        userRepository = new UserRepository();
    }

    /**
     * Khởi tạo tất cả views
     */
    private void initializeViews() {
        // Header
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);

        // Form fields
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etStudentId = findViewById(R.id.etStudentId);
        etBirthdate = findViewById(R.id.etBirthdate);
        etAddress = findViewById(R.id.etAddress);

        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioTeacher = findViewById(R.id.radioTeacher);
        radioStudent = findViewById(R.id.radioStudent);

        spinnerCampus = findViewById(R.id.spinnerCampus);

        // Buttons
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);

        // Initialize calendar
        selectedCalendar = Calendar.getInstance();

        // Setup campus spinner
        campusList = new ArrayList<>();
        campusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, campusList);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampus.setAdapter(campusAdapter);
    }

    /**
     * Setup click listeners cho các UI components
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Birthdate picker
        etBirthdate.setOnClickListener(v -> showDatePicker());

        // Cancel button
        btnCancel.setOnClickListener(v -> onBackPressed());

        // Create button - validate và create account
        btnCreate.setOnClickListener(v -> {
            if (validateAllFields()) {
                showConfirmationDialog();
            }
        });

        // Setup real-time validation listeners
        setupValidationListeners();
    }

    /**
     * Load danh sách campus từ Firebase
     */
    private void loadCampusData() {
        campusRef = FirebaseDatabase.getInstance().getReference("Campus");

        campusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                campusList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String campus = snapshot.getValue(String.class);
                    if (campus != null && !campus.isEmpty()) {
                        campusList.add(campus);
                    }
                }

                campusAdapter.notifyDataSetChanged();

                // Set default selection if available
                if (!campusList.isEmpty()) {
                    spinnerCampus.setSelection(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CreateAccountActivity.this,
                        "Error loading campus data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiển thị DatePicker để chọn ngày sinh
     */
    private void showDatePicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etBirthdate.setText(sdf.format(selectedCalendar.getTime()));
                },
                year, month, day
        );

        // Set max date to today (không thể chọn ngày trong tương lai)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set min date to 100 years ago (giới hạn tuổi hợp lý)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    /**
     * Setup real-time validation listeners cho các input fields
     */
    private void setupValidationListeners() {
        // Full Name validation
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFullName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Email validation
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password validation
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
                // Also validate confirm password if it has text
                if (!etConfirmPassword.getText().toString().isEmpty()) {
                    validateConfirmPassword();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm Password validation
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Student ID validation (chỉ khi role là Student)
        etStudentId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (radioStudent.isChecked()) {
                    validateStudentId();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Role selection listener - toggle Student ID requirement
        radioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioStudent) {
                etStudentId.setEnabled(true);
                etStudentId.setAlpha(1.0f);
            } else {
                etStudentId.setEnabled(false);
                etStudentId.setAlpha(0.5f);
                etStudentId.setError(null); // Clear error when disabled
            }
        });
    }

    /**
     * Validate tất cả fields trước khi submit
     *
     * @return true nếu tất cả fields hợp lệ, false nếu có lỗi
     */
    private boolean validateAllFields() {
        boolean isValid = true;

        // Validate từng field
        if (!validateFullName()) isValid = false;
        if (!validateEmail()) isValid = false;
        if (!validatePassword()) isValid = false;
        if (!validateConfirmPassword()) isValid = false;
        if (!validateBirthdate()) isValid = false;
        if (!validateAddress()) isValid = false;
        if (!validateRole()) isValid = false;
        if (!validateCampus()) isValid = false;

        // Validate Student ID nếu role là Student
        if (radioStudent.isChecked()) {
            if (!validateStudentId()) isValid = false;
        }

        // Scroll to first error field nếu có lỗi
        if (!isValid) {
            scrollView.smoothScrollTo(0, 0);
            Toast.makeText(this, "Vui lòng kiểm tra lại các trường thông tin.", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    /**
     * Validate Full Name field
     */
    private boolean validateFullName() {
        String fullName = etFullName.getText().toString().trim();

        if (!ValidationUtils.isValidFullName(fullName)) {
            etFullName.setError(ValidationUtils.getFullNameErrorMessage());
            return false;
        }

        etFullName.setError(null);
        return true;
    }

    /**
     * Validate Email field
     */
    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(ValidationUtils.getEmailErrorMessage());
            return false;
        }

        etEmail.setError(null);
        return true;
    }

    /**
     * Validate Password field
     */
    private boolean validatePassword() {
        String password = etPassword.getText().toString();

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError(ValidationUtils.getPasswordErrorMessage());
            return false;
        }

        etPassword.setError(null);
        return true;
    }

    /**
     * Validate Confirm Password field
     */
    private boolean validateConfirmPassword() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            etConfirmPassword.setError(ValidationUtils.getPasswordMismatchErrorMessage());
            return false;
        }

        etConfirmPassword.setError(null);
        return true;
    }

    /**
     * Validate Student ID field (chỉ khi role là Student)
     */
    private boolean validateStudentId() {
        String studentId = etStudentId.getText().toString().trim();

        if (!ValidationUtils.isValidStudentId(studentId)) {
            etStudentId.setError(ValidationUtils.getStudentIdErrorMessage());
            return false;
        }

        etStudentId.setError(null);
        return true;
    }

    /**
     * Validate Birthdate field
     */
    private boolean validateBirthdate() {
        String birthdate = etBirthdate.getText().toString().trim();

        if (!ValidationUtils.isValidBirthdate(birthdate)) {
            etBirthdate.setError(ValidationUtils.getBirthdateErrorMessage());
            return false;
        }

        etBirthdate.setError(null);
        return true;
    }

    /**
     * Validate Address field
     */
    private boolean validateAddress() {
        String address = etAddress.getText().toString().trim();

        if (!ValidationUtils.isNotEmpty(address)) {
            etAddress.setError(ValidationUtils.getRequiredFieldErrorMessage());
            return false;
        }

        etAddress.setError(null);
        return true;
    }

    /**
     * Validate Role selection
     */
    private boolean validateRole() {
        if (radioGroupRole.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn Role (Teacher hoặc Student).", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validate Campus selection
     */
    private boolean validateCampus() {
        if (spinnerCampus.getSelectedItem() == null || campusList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn Campus.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Hiển thị confirmation dialog trước khi tạo account
     */
    private void showConfirmationDialog() {
        String role = radioStudent.isChecked() ? "Student" : "Teacher";
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String campus = spinnerCampus.getSelectedItem().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận tạo tài khoản");
        builder.setMessage(
                "Bạn có chắc chắn muốn tạo tài khoản?\n\n" +
                        "Họ tên: " + fullName + "\n" +
                        "Email: " + email + "\n" +
                        "Role: " + role + "\n" +
                        "Campus: " + campus
        );
        builder.setPositiveButton("Tạo tài khoản", (dialog, which) -> createAccount());
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Tạo account mới trong Firebase Authentication và Realtime Database
     */
    private void createAccount() {
        // Đánh dấu đang trong quá trình tạo account
        isCreatingAccount = true;

        // Hiển thị loading state
        setLoading(true);

        // Lấy thông tin từ form
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String fullName = etFullName.getText().toString().trim();
        String role = radioStudent.isChecked() ? "Student" : "Teacher";
        String studentId = etStudentId.getText().toString().trim();
        String campus = spinnerCampus.getSelectedItem().toString();
        String address = etAddress.getText().toString().trim();
        String birthdate = etBirthdate.getText().toString().trim();

        // Gọi UserRepository để tạo user
        userRepository.createUserWithAuth(
                email,
                password,
                fullName,
                role,
                studentId,
                campus,
                address,
                birthdate,
                com.google.firebase.auth.FirebaseAuth.getInstance(),
                // Auth success listener
                new UserRepository.OnUserOperationListener() {
                    @Override
                    public void onSuccess() {
                        // This is called when Auth is successful
                        // Don't show success yet, wait for DB success
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Auth failed
                        setLoading(false);
                        showErrorDialog("Lỗi tạo tài khoản", errorMessage);
                    }
                },
                // Database success listener
                new UserRepository.OnUserOperationListener() {
                    @Override
                    public void onSuccess() {
                        // Cả Auth và Database đều thành công
                        isCreatingAccount = false;
                        setLoading(false);
                        showSuccessDialog();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Lỗi xảy ra, reset state
                        isCreatingAccount = false;
                        setLoading(false);
                        showErrorDialog("Lỗi tạo tài khoản", errorMessage);
                    }
                }
        );
    }

    /**
     * Set loading state (show/hide progress bar and disable/enable UI)
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        scrollView.setAlpha(isLoading ? 0.5f : 1.0f);

        // Disable all input fields and buttons
        etFullName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
        etStudentId.setEnabled(!isLoading);
        etBirthdate.setEnabled(!isLoading);
        etAddress.setEnabled(!isLoading);
        radioTeacher.setEnabled(!isLoading);
        radioStudent.setEnabled(!isLoading);
        spinnerCampus.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        btnCreate.setEnabled(!isLoading);
        backButton.setEnabled(!isLoading);
    }

    /**
     * Hiển thị dialog khi tạo account thành công
     */
    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thành công");
        builder.setMessage("Tài khoản đã được tạo thành công!");
        builder.setPositiveButton("OK", (dialog, which) -> {
            setResult(RESULT_OK); // Notify AccountListActivity to refresh
            finish();
        });
        builder.setCancelable(false); // Không cho dismiss bằng cách bấm ra ngoài
        builder.show();
    }

    /**
     * Hiển thị dialog khi có lỗi
     */
    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    /**
     * Lưu trạng thái của Activity khi có configuration change (e.g., rotation)
     * Saves the activity state when configuration changes occur
     *
     * @param outState Bundle to save state data
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Lưu trạng thái đang tạo account
        // Save account creation state
        outState.putBoolean(STATE_IS_CREATING, isCreatingAccount);

        // Lưu ngày sinh đã chọn (nếu có)
        // Save selected birthdate if exists
        if (selectedCalendar != null) {
            outState.putLong(STATE_BIRTHDATE, selectedCalendar.getTimeInMillis());
        }
    }

    /**
     * Khôi phục trạng thái của Activity sau configuration change
     * Restores the activity state after configuration changes
     *
     * @param savedInstanceState Bundle containing saved state data
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Khôi phục trạng thái đang tạo account
        // Restore account creation state
        isCreatingAccount = savedInstanceState.getBoolean(STATE_IS_CREATING, false);
        if (isCreatingAccount) {
            setLoading(true);
        }

        // Khôi phục ngày sinh đã chọn
        // Restore selected birthdate
        long birthdateMillis = savedInstanceState.getLong(STATE_BIRTHDATE, -1);
        if (birthdateMillis != -1) {
            selectedCalendar.setTimeInMillis(birthdateMillis);
        }
    }

    @Override
    public void onBackPressed() {
        // Không cho phép back nếu đang loading
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Vui lòng đợi quá trình tạo tài khoản hoàn tất.", Toast.LENGTH_SHORT).show();
            return;
        }

        super.onBackPressed();
    }
}