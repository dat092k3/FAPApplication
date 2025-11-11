package com.example.fapapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fapapplication.adapter.ClassDetailPagerAdapter;
import com.example.fapapplication.entity.Class;
import com.example.fapapplication.fragment.ClassBasicInfoFragment;
import com.example.fapapplication.repository.ClassRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ClassDetailActivity extends AppCompatActivity implements ClassBasicInfoFragment.OnBasicInfoChangeListener {

    private ImageButton backButton;
    private TextView tvTitle, tvError;
    private ProgressBar progressBar;
    private LinearLayout tabContainer;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private String classId;
    private Class currentClass;
    private ClassRepository classRepository;
    private ClassDetailPagerAdapter pagerAdapter;

    private boolean hasUnsavedChanges = false;

    private static final String[] TAB_TITLES = {
            "Thông Tin",
            "Môn Học",
            "Sinh Viên"
    };

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
        loadClassData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        tvTitle = findViewById(R.id.tvTitle);
        tvError = findViewById(R.id.tvError);
        progressBar = findViewById(R.id.progressBar);
        tabContainer = findViewById(R.id.tabContainer);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> handleBackPressed());
    }

    private void setupViewPager() {
        pagerAdapter = new ClassDetailPagerAdapter(this, currentClass);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

        ClassBasicInfoFragment basicInfoFragment = pagerAdapter.getBasicInfoFragment();
        if (basicInfoFragment != null) {
            basicInfoFragment.setOnBasicInfoChangeListener(this);
        }
    }

    private void loadClassData() {
        showLoading(true);

        classRepository.getClassById(classId, new ClassRepository.ClassCallback() {
            @Override
            public void onSuccess(Class classObj) {
                showLoading(false);
                currentClass = classObj;
                setupViewPager();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(errorMessage);
            }
        });
    }

    @Override
    public void onDataChanged() {
        hasUnsavedChanges = true;
    }

    @Override
    public void onSaveClicked() {
        saveClass();
    }

    @Override
    public void onCancelClicked() {
        handleBackPressed();
    }

    private void saveClass() {
        ClassBasicInfoFragment basicInfoFragment = pagerAdapter.getBasicInfoFragment();
        if (basicInfoFragment == null) return;

        if (!basicInfoFragment.validateForm()) {
            viewPager.setCurrentItem(0, true);
            return;
        }

        hideKeyboard();
        showSaveLoading(true);

        basicInfoFragment.applyChangesToClass(currentClass);

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
        tabContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showSaveLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tabContainer.setVisibility(show ? View.GONE : View.VISIBLE);

        ClassBasicInfoFragment basicInfoFragment = pagerAdapter.getBasicInfoFragment();
        if (basicInfoFragment != null) {
            basicInfoFragment.setButtonsEnabled(!show);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        tabContainer.setVisibility(View.GONE);
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
    public void onBackPressed() {
        handleBackPressed();
    }
}