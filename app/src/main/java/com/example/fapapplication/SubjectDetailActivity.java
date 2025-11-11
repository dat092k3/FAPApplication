package com.example.fapapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.AssessmentCategoryAdapter;
import com.example.fapapplication.entity.AssessmentCategory;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.repository.SubjectRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubjectDetailActivity extends AppCompatActivity {

    // UI Components - Các thành phần giao diện
    private ImageButton backButton;
    private TextView tvTitle, tvCreatedAt, tvError, tvTotalWeight, tvWeightError;
    private EditText etSubjectCode, etSubjectName, etDescription;
    private SwitchCompat switchActive;
    private Button btnCancel, btnSave, btnAddCategory;
    private ProgressBar progressBar;
    private View formContainer, scrollView;
    private RecyclerView recyclerViewAssessments;

    // Data - Dữ liệu
    private String subjectId;
    private Subject currentSubject;
    private SubjectRepository subjectRepository;
    private AssessmentCategoryAdapter assessmentAdapter;
    private List<AssessmentCategory> assessmentCategories;

    // Flags - Các cờ trạng thái
    private boolean isLoading = false;
    private boolean hasUnsavedChanges = false;

    // State keys - Keys để lưu state
    private static final String KEY_SUBJECT_CODE = "subject_code";
    private static final String KEY_SUBJECT_NAME = "subject_name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_HAS_CHANGES = "has_changes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        subjectId = getIntent().getStringExtra("SUBJECT_ID");
        if (subjectId == null || subjectId.isEmpty()) {
            Toast.makeText(this, "ID môn học không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        subjectRepository = new SubjectRepository();
        assessmentCategories = new ArrayList<>();

        initializeViews();
        setupClickListeners();
        setupAssessmentRecyclerView();
        loadSubjectData();
    }

    // Khởi tạo tất cả views
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        tvTitle = findViewById(R.id.tvTitle);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvError = findViewById(R.id.tvError);
        tvTotalWeight = findViewById(R.id.tvTotalWeight);
        tvWeightError = findViewById(R.id.tvWeightError);

        etSubjectCode = findViewById(R.id.etSubjectCode);
        etSubjectName = findViewById(R.id.etSubjectName);
        etDescription = findViewById(R.id.etDescription);

        switchActive = findViewById(R.id.switchActive);

        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
        scrollView = findViewById(R.id.scrollView);

        recyclerViewAssessments = findViewById(R.id.recyclerViewAssessments);
    }

    // Thiết lập các click listeners
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        btnCancel.setOnClickListener(v -> {
            if (hasUnsavedChanges) {
                showDiscardChangesDialog();
            } else {
                finish();
            }
        });

        btnSave.setOnClickListener(v -> saveSubject());

        btnAddCategory.setOnClickListener(v -> {
            AssessmentCategory newCategory = new AssessmentCategory("", new ArrayList<>());
            assessmentCategories.add(newCategory);
            assessmentAdapter.notifyItemInserted(assessmentCategories.size() - 1);
            hasUnsavedChanges = true;
            calculateTotalWeight();

            // Scroll to bottom để hiển thị category mới
            recyclerViewAssessments.postDelayed(() ->
                            recyclerViewAssessments.smoothScrollToPosition(assessmentCategories.size() - 1),
                    100
            );
        });

        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoading) {
                return;
            }

            if (!isChecked && currentSubject != null && currentSubject.isActive()) {
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận vô hiệu hóa")
                        .setMessage("Bạn có chắc muốn vô hiệu hóa môn học này? Môn học sẽ không hiển thị trong danh sách hoạt động.")
                        .setPositiveButton("Vô hiệu hóa", (dialog, which) -> {
                            hasUnsavedChanges = true;
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            buttonView.setChecked(true);
                        })
                        .show();
            } else {
                hasUnsavedChanges = true;
            }
        });

        setupTextChangeListeners();
    }

    // Thiết lập listeners cho text changes
    private void setupTextChangeListeners() {
        etSubjectCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoading) {
                    hasUnsavedChanges = true;
                    etSubjectCode.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSubjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoading) {
                    hasUnsavedChanges = true;
                    etSubjectName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoading) {
                    hasUnsavedChanges = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Thiết lập RecyclerView cho assessment structure
    private void setupAssessmentRecyclerView() {
        assessmentAdapter = new AssessmentCategoryAdapter(assessmentCategories);
        recyclerViewAssessments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAssessments.setAdapter(assessmentAdapter);

        assessmentAdapter.setOnCategoryChangeListener(new AssessmentCategoryAdapter.OnCategoryChangeListener() {
            @Override
            public void onCategoryRemoved(int position) {
                assessmentCategories.remove(position);
                assessmentAdapter.notifyItemRemoved(position);
                hasUnsavedChanges = true;
                calculateTotalWeight();
            }

            @Override
            public void onCategoryChanged() {
                hasUnsavedChanges = true;
                calculateTotalWeight();
            }
        });
    }

    // Load dữ liệu subject từ Firebase
    private void loadSubjectData() {
        showLoading(true);

        subjectRepository.getSubjectById(subjectId, new SubjectRepository.SubjectCallback() {
            @Override
            public void onSuccess(Subject subject) {
                currentSubject = subject;
                runOnUiThread(() -> {
                    populateFormFields();
                    showLoading(false);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showError(errorMessage);
                    showLoading(false);
                });
            }
        });
    }

    // Điền dữ liệu vào form
    private void populateFormFields() {
        isLoading = true;

        etSubjectCode.setText(currentSubject.getSubjectCode());
        etSubjectName.setText(currentSubject.getSubjectName());
        etDescription.setText(currentSubject.getDescription() != null ? currentSubject.getDescription() : "");
        switchActive.setChecked(currentSubject.isActive());

        if (currentSubject.getCreatedAt() > 0) {
            tvCreatedAt.setText("Ngày tạo: " + formatTimestamp(currentSubject.getCreatedAt()));
        } else {
            tvCreatedAt.setText("Ngày tạo: Không rõ");
        }

        assessmentCategories.clear();
        if (currentSubject.getAssessments() != null && !currentSubject.getAssessments().isEmpty()) {
            assessmentCategories.addAll(currentSubject.getAssessments());
        }
        assessmentAdapter.notifyDataSetChanged();
        calculateTotalWeight();

        isLoading = false;
        hasUnsavedChanges = false;
    }

    // Tính tổng trọng số và kiểm tra
    private void calculateTotalWeight() {
        double totalWeight = 0;
        for (AssessmentCategory category : assessmentCategories) {
            totalWeight += category.getTotalWeight();
        }

        tvTotalWeight.setText(String.format(Locale.getDefault(), "%.1f%%", totalWeight));

        if (Math.abs(totalWeight - 100.0) > 0.1 && totalWeight > 0) {
            tvWeightError.setVisibility(View.VISIBLE);
            tvTotalWeight.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvWeightError.setVisibility(View.GONE);
            tvTotalWeight.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }


    // Lưu subject vào Firebase
    private void saveSubject() {
        hideKeyboard();

        if (!validateForm()) {
            return;
        }

        double totalWeight = 0;
        for (AssessmentCategory category : assessmentCategories) {
            totalWeight += category.getTotalWeight();
        }

        if (Math.abs(totalWeight - 100.0) > 0.1) {
            return;
        }

        String code = etSubjectCode.getText().toString().trim();
        String name = etSubjectName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        currentSubject.setSubjectCode(code);
        currentSubject.setSubjectName(name);
        currentSubject.setDescription(desc.isEmpty() ? null : desc);
        currentSubject.setActive(isActive);
        currentSubject.setAssessments(new ArrayList<>(assessmentCategories));

        showSaveLoading(true);

        subjectRepository.updateSubject(currentSubject, new SubjectRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showSaveLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showSaveLoading(false);
                    showErrorDialog(errorMessage);
                });
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("Môn học đã được cập nhật thành công!")
                .setPositiveButton("OK", (dialog, which) -> {
                    hasUnsavedChanges = false;
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage("Không thể lưu môn học: " + errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    // Validate toàn bộ form
    private boolean validateForm() {
        boolean isValid = true;

        String code = etSubjectCode.getText().toString().trim();
        String name = etSubjectName.getText().toString().trim();

        if (code.isEmpty()) {
            etSubjectCode.setError("Mã môn học không được để trống");
            etSubjectCode.requestFocus();
            isValid = false;
        } else if (code.length() < 3) {
            etSubjectCode.setError("Mã môn học phải có ít nhất 3 ký tự");
            etSubjectCode.requestFocus();
            isValid = false;
        } else if (!code.matches("^[A-Z0-9]+$")) {
            etSubjectCode.setError("Mã môn học chỉ được chứa chữ in hoa và số");
            etSubjectCode.requestFocus();
            isValid = false;
        }

        if (name.isEmpty()) {
            etSubjectName.setError("Tên môn học không được để trống");
            if (isValid) etSubjectName.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            etSubjectName.setError("Tên môn học phải có ít nhất 3 ký tự");
            if (isValid) etSubjectName.requestFocus();
            isValid = false;
        }

        if (assessmentCategories.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một danh mục đánh giá", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else {
            for (int i = 0; i < assessmentCategories.size(); i++) {
                AssessmentCategory category = assessmentCategories.get(i);

                if (category.getCategoryName().trim().isEmpty()) {
                    Toast.makeText(this, "Danh mục thứ " + (i + 1) + " không được để trống", Toast.LENGTH_SHORT).show();
                    isValid = false;
                    break;
                }

                if (category.getGradeItems().isEmpty()) {
                    Toast.makeText(this, "Danh mục '" + category.getCategoryName() + "' phải có ít nhất một mục đánh giá", Toast.LENGTH_SHORT).show();
                    isValid = false;
                    break;
                }

                for (int j = 0; j < category.getGradeItems().size(); j++) {
                    if (category.getGradeItems().get(j).getItemName().trim().isEmpty()) {
                        Toast.makeText(this, "Tên mục đánh giá không được để trống trong danh mục '" + category.getCategoryName() + "'", Toast.LENGTH_SHORT).show();
                        isValid = false;
                        break;
                    }

                    if (category.getGradeItems().get(j).getWeight() <= 0) {
                        Toast.makeText(this, "Trọng số phải lớn hơn 0 trong danh mục '" + category.getCategoryName() + "'", Toast.LENGTH_SHORT).show();
                        isValid = false;
                        break;
                    }
                }

                if (!isValid) break;
            }
        }

        double totalWeight = 0;
        for (AssessmentCategory category : assessmentCategories) {
            totalWeight += category.getTotalWeight();
        }

        if (Math.abs(totalWeight - 100.0) > 0.1) {
            Toast.makeText(this, "Tổng trọng số phải bằng 100% (hiện tại: " + String.format(Locale.getDefault(), "%.1f%%", totalWeight) + ")", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }

    // Hiển thị dialog xác nhận hủy thay đổi
    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy thay đổi?")
                .setMessage("Bạn có thay đổi chưa lưu. Bạn có chắc muốn hủy?")
                .setPositiveButton("Hủy thay đổi", (dialog, which) -> finish())
                .setNegativeButton("Tiếp tục chỉnh sửa", null)
                .show();
    }

    // Hiển thị/ẩn loading indicator
    private void showLoading(boolean show) {
        isLoading = show;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    // Hiển thị/ẩn loading khi save
    private void showSaveLoading(boolean show) {
        btnSave.setEnabled(!show);
        btnCancel.setEnabled(!show);
        btnSave.setText(show ? "Đang lưu..." : "Lưu");
        btnSave.setAlpha(show ? 0.5f : 1.0f);
    }

    // Hiển thị thông báo lỗi
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    // Format timestamp thành string
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_SUBJECT_CODE, etSubjectCode.getText().toString());
        outState.putString(KEY_SUBJECT_NAME, etSubjectName.getText().toString());
        outState.putString(KEY_DESCRIPTION, etDescription.getText().toString());
        outState.putBoolean(KEY_IS_ACTIVE, switchActive.isChecked());
        outState.putBoolean(KEY_HAS_CHANGES, hasUnsavedChanges);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            isLoading = true;

            etSubjectCode.setText(savedInstanceState.getString(KEY_SUBJECT_CODE, ""));
            etSubjectName.setText(savedInstanceState.getString(KEY_SUBJECT_NAME, ""));
            etDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION, ""));
            switchActive.setChecked(savedInstanceState.getBoolean(KEY_IS_ACTIVE, true));
            hasUnsavedChanges = savedInstanceState.getBoolean(KEY_HAS_CHANGES, false);

            isLoading = false;
        }
    }

    // Ẩn bàn phím
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}