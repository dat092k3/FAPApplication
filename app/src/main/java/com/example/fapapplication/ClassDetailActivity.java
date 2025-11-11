package com.example.fapapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fapapplication.entity.Class;
import com.example.fapapplication.repository.ClassRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClassDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView tvTitle, tvCreatedAt, tvError;
    private EditText etClassName, etSemester, etDescription;
    private SwitchCompat switchActive;
    private Button btnCancel, btnSave;
    private ProgressBar progressBar;
    private View formContainer;
    private ScrollView scrollView;

    private String classId;
    private Class currentClass;
    private ClassRepository classRepository;

    private boolean isLoading = false;
    private boolean hasUnsavedChanges = false;

    private static final String KEY_CLASS_NAME = "class_name";
    private static final String KEY_SEMESTER = "semester";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_HAS_CHANGES = "has_changes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        classId = getIntent().getStringExtra("CLASS_ID");
        if (classId == null) {
            Toast.makeText(this, "Invalid class ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        classRepository = ClassRepository.getInstance();

        initializeViews();
        setupClickListeners();
        setupTextChangeListeners();
        loadClassData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        tvTitle = findViewById(R.id.tvTitle);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvError = findViewById(R.id.tvError);

        etClassName = findViewById(R.id.etClassName);
        etSemester = findViewById(R.id.etSemester);
        etDescription = findViewById(R.id.etDescription);

        switchActive = findViewById(R.id.switchActive);

        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
        scrollView = findViewById(R.id.scrollView);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> handleBackPressed());

        btnCancel.setOnClickListener(v -> handleBackPressed());

        btnSave.setOnClickListener(v -> saveClass());

        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isLoading) {
                hasUnsavedChanges = true;

                if (!isChecked && currentClass != null && currentClass.isActive()) {
                    showDeactivateConfirmationDialog();
                }
            }
        });
    }

    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoading) {
                    hasUnsavedChanges = true;
                    clearErrors();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etClassName.addTextChangedListener(textWatcher);
        etSemester.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
    }

    private void clearErrors() {
        etClassName.setError(null);
        etSemester.setError(null);
        tvError.setVisibility(View.GONE);
    }

    private void loadClassData() {
        showLoading(true);

        classRepository.getClassById(classId, new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(Class classObj) {
                showLoading(false);
                currentClass = classObj;
                populateFormFields();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(errorMessage);
            }
        });
    }

    private void populateFormFields() {
        if (currentClass == null) return;

        isLoading = true;

        etClassName.setText(currentClass.getClassName());
        etSemester.setText(currentClass.getSemester());
        etDescription.setText(currentClass.getDescription());
        switchActive.setChecked(currentClass.isActive());

        tvCreatedAt.setText(formatTimestamp(currentClass.getCreatedAt()));

        isLoading = false;
        hasUnsavedChanges = false;
    }

    private boolean validateForm() {
        boolean isValid = true;

        String name = etClassName.getText().toString().trim();
        String semester = etSemester.getText().toString().trim();

        if (name.isEmpty()) {
            etClassName.setError("Tên lớp không được để trống");
            etClassName.requestFocus();
            isValid = false;
        } else if (name.length() < 2) {
            etClassName.setError("Tên lớp phải có ít nhất 2 ký tự");
            etClassName.requestFocus();
            isValid = false;
        }

        if (semester.isEmpty()) {
            etSemester.setError("Học kỳ không được để trống");
            if (isValid) etSemester.requestFocus();
            isValid = false;
        } else if (semester.length() < 3) {
            etSemester.setError("Học kỳ phải có ít nhất 3 ký tự");
            if (isValid) etSemester.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void saveClass() {
        hideKeyboard();

        if (!validateForm()) {
            return;
        }

        showSaveLoading(true);

        String name = etClassName.getText().toString().trim();
        String semester = etSemester.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        currentClass.setClassName(name);
        currentClass.setSemester(semester);
        currentClass.setDescription(description.isEmpty() ? null : description);
        currentClass.setActive(isActive);

        classRepository.updateClass(currentClass, new ClassRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                showSaveLoading(false);
                hasUnsavedChanges = false;
                showSuccessDialog();
            }

            @Override
            public void onError(String errorMessage) {
                showSaveLoading(false);
                showErrorDialog(errorMessage);
            }
        });
    }

    private void showDeactivateConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Vô hiệu hóa lớp học?")
                .setMessage("Bạn có chắc muốn vô hiệu hóa lớp học này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {

                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    isLoading = true;
                    switchActive.setChecked(true);
                    isLoading = false;
                    hasUnsavedChanges = false;
                })
                .show();
    }

    private void handleBackPressed() {
        if (hasUnsavedChanges) {
            showDiscardChangesDialog();
        } else {
            finish();
        }
    }

    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy thay đổi?")
                .setMessage("Bạn có chắc muốn hủy? Tất cả thay đổi sẽ bị mất.")
                .setPositiveButton("Hủy thay đổi", (dialog, which) -> finish())
                .setNegativeButton("Tiếp tục chỉnh sửa", null)
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thành công!")
                .setMessage("Lớp học đã được cập nhật thành công.")
                .setPositiveButton("OK", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage("Không thể cập nhật lớp học: " + errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        formContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showSaveLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            formContainer.setVisibility(View.GONE);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            formContainer.setVisibility(View.VISIBLE);
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        formContainer.setVisibility(View.GONE);
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("'Ngày tạo:' dd/MM/yyyy 'lúc' HH:mm",
                Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_CLASS_NAME, etClassName.getText().toString());
        outState.putString(KEY_SEMESTER, etSemester.getText().toString());
        outState.putString(KEY_DESCRIPTION, etDescription.getText().toString());
        outState.putBoolean(KEY_IS_ACTIVE, switchActive.isChecked());
        outState.putBoolean(KEY_HAS_CHANGES, hasUnsavedChanges);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            isLoading = true;

            etClassName.setText(savedInstanceState.getString(KEY_CLASS_NAME, ""));
            etSemester.setText(savedInstanceState.getString(KEY_SEMESTER, ""));
            etDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION, ""));
            switchActive.setChecked(savedInstanceState.getBoolean(KEY_IS_ACTIVE, true));
            hasUnsavedChanges = savedInstanceState.getBoolean(KEY_HAS_CHANGES, false);

            isLoading = false;
        }
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }
}