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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.AssessmentCategoryAdapter;
import com.example.fapapplication.entity.AssessmentCategory;
import com.example.fapapplication.entity.GradeItem;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.repository.SubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateSubjectActivity extends AppCompatActivity {

    // UI Components - Các thành phần giao diện
    private ImageButton backButton;
    private TextView tvTitle, tvTotalWeight, tvWeightError;
    private EditText etSubjectCode, etSubjectName, etDescription;
    private Button btnCancel, btnCreate, btnAddCategory;
    private ProgressBar progressBar;
    private View formContainer, scrollView;
    private RecyclerView recyclerViewAssessments;

    // Data - Dữ liệu
    private SubjectRepository subjectRepository;
    private AssessmentCategoryAdapter assessmentAdapter;
    private List<AssessmentCategory> assessmentCategories;

    // Flags - Các cờ trạng thái
    private boolean isLoading = false;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subject);

        initializeViews();
        setupRepository();
        setupAssessmentRecyclerView();
        setupClickListeners();
        setupTextChangeListeners();
    }

    // Khởi tạo tất cả views
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        tvTitle = findViewById(R.id.tvTitle);
        tvTotalWeight = findViewById(R.id.tvTotalWeight);
        tvWeightError = findViewById(R.id.tvWeightError);

        etSubjectCode = findViewById(R.id.etSubjectCode);
        etSubjectName = findViewById(R.id.etSubjectName);
        etDescription = findViewById(R.id.etDescription);

        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
        scrollView = findViewById(R.id.scrollView);
        recyclerViewAssessments = findViewById(R.id.recyclerViewAssessments);

        assessmentCategories = new ArrayList<>();
    }

    // Khởi tạo repository
    private void setupRepository() {
        subjectRepository = new SubjectRepository();
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

    // Thiết lập các click listeners
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> handleBackPressed());

        btnCancel.setOnClickListener(v -> handleBackPressed());

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

        btnCreate.setOnClickListener(v -> saveSubject());
    }

    // Thiết lập listeners cho text changes
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

        etSubjectCode.addTextChangedListener(textWatcher);
        etSubjectName.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
    }

    // Xóa tất cả error messages
    private void clearErrors() {
        etSubjectCode.setError(null);
        etSubjectName.setError(null);
        etDescription.setError(null);
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

    // Validate toàn bộ form
    private boolean validateForm() {
        boolean isValid = true;

        String code = etSubjectCode.getText().toString().trim();
        String name = etSubjectName.getText().toString().trim();

        // Validate subject code
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

        // Validate subject name
        if (name.isEmpty()) {
            etSubjectName.setError("Tên môn học không được để trống");
            if (isValid) etSubjectName.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            etSubjectName.setError("Tên môn học phải có ít nhất 3 ký tự");
            if (isValid) etSubjectName.requestFocus();
            isValid = false;
        }

        // Validate assessment structure
        if (assessmentCategories.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một danh mục đánh giá", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else {
            // Validate each category
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

                // Validate each grade item
                for (int j = 0; j < category.getGradeItems().size(); j++) {
                    GradeItem item = category.getGradeItems().get(j);

                    if (item.getItemName().trim().isEmpty()) {
                        Toast.makeText(this, "Tên mục đánh giá không được để trống trong danh mục '" + category.getCategoryName() + "'", Toast.LENGTH_SHORT).show();
                        isValid = false;
                        break;
                    }

                    if (item.getWeight() <= 0) {
                        Toast.makeText(this, "Trọng số phải lớn hơn 0 trong danh mục '" + category.getCategoryName() + "'", Toast.LENGTH_SHORT).show();
                        isValid = false;
                        break;
                    }
                }

                if (!isValid) break;
            }
        }

        // Validate total weight
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

    // Lưu subject vào Firebase
    private void saveSubject() {
        hideKeyboard();

        if (!validateForm()) {
            return;
        }

        showSaveLoading(true);

        String code = etSubjectCode.getText().toString().trim();
        String name = etSubjectName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Kiểm tra subject code đã tồn tại chưa
        subjectRepository.isSubjectCodeExists(code, exists -> {
            if (exists) {
                showSaveLoading(false);
                etSubjectCode.setError("Mã môn học đã tồn tại");
                etSubjectCode.requestFocus();
                Toast.makeText(this, "Mã môn học đã được sử dụng", Toast.LENGTH_SHORT).show();
            } else {
                // Tạo subject mới
                Subject newSubject = new Subject(
                        subjectRepository.generateSubjectId(),
                        code,
                        name,
                        description.isEmpty() ? null : description,
                        System.currentTimeMillis(),
                        true
                );

                // Set assessments
                newSubject.setAssessments(assessmentCategories);

                // Lưu vào Firebase
                subjectRepository.createSubject(newSubject, new SubjectRepository.OperationCallback() {
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
        });
    }

    // Hiển thị dialog xác nhận hủy thay đổi
    private void handleBackPressed() {
        if (hasUnsavedChanges) {
            showDiscardChangesDialog();
        } else {
            finish();
        }
    }

    // Dialog xác nhận hủy
    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy thay đổi?")
                .setMessage("Bạn có chắc muốn hủy? Tất cả thay đổi sẽ bị mất.")
                .setPositiveButton("Hủy thay đổi", (dialog, which) -> finish())
                .setNegativeButton("Tiếp tục chỉnh sửa", null)
                .show();
    }

    // Dialog thông báo thành công
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thành công!")
                .setMessage("Môn học đã được tạo thành công.")
                .setPositiveButton("OK", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // Dialog thông báo lỗi
    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage("Không thể tạo môn học: " + errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    // Hiển thị/ẩn loading khi save
    private void showSaveLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            formContainer.setVisibility(View.GONE);
            btnCreate.setEnabled(false);
            btnCancel.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            formContainer.setVisibility(View.VISIBLE);
            btnCreate.setEnabled(true);
            btnCancel.setEnabled(true);
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
        handleBackPressed();
    }
}