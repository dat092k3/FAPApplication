package com.example.fapapplication.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.Class;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClassBasicInfoFragment extends Fragment {

    private EditText etClassName, etSemester, etDescription;
    private SwitchCompat switchActive;
    private TextView tvCreatedAt;
    private Button btnCancel, btnSave;

    private Class currentClass;
    private boolean isLoading = false;
    private OnBasicInfoChangeListener listener;

    public interface OnBasicInfoChangeListener {
        void onDataChanged();
        void onSaveClicked();
        void onCancelClicked();
    }

    public static ClassBasicInfoFragment newInstance(Class classObj) {
        ClassBasicInfoFragment fragment = new ClassBasicInfoFragment();
        fragment.currentClass = classObj;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_basic_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupClickListeners();
        setupTextChangeListeners();

        if (currentClass != null) {
            populateFormFields();
        }
    }

    private void initializeViews(View view) {
        etClassName = view.findViewById(R.id.etClassName);
        etSemester = view.findViewById(R.id.etSemester);
        etDescription = view.findViewById(R.id.etDescription);
        switchActive = view.findViewById(R.id.switchActive);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClicked();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSaveClicked();
            }
        });

        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isLoading && listener != null) {
                listener.onDataChanged();
            }
        });
    }

    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoading && listener != null) {
                    listener.onDataChanged();
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

    private void populateFormFields() {
        if (currentClass == null) return;

        isLoading = true;

        etClassName.setText(currentClass.getClassName());
        etSemester.setText(currentClass.getSemester());
        etDescription.setText(currentClass.getDescription());
        switchActive.setChecked(currentClass.isActive());

        tvCreatedAt.setText(formatTimestamp(currentClass.getCreatedAt()));

        isLoading = false;
    }

    public void updateClassData(Class classObj) {
        this.currentClass = classObj;
        if (getView() != null) {
            populateFormFields();
        }
    }

    public boolean validateForm() {
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

    public void applyChangesToClass(Class classObj) {
        if (classObj == null) return;

        String name = etClassName.getText().toString().trim();
        String semester = etSemester.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        classObj.setClassName(name);
        classObj.setSemester(semester);
        classObj.setDescription(description.isEmpty() ? null : description);
        classObj.setActive(isActive);
    }

    private void clearErrors() {
        etClassName.setError(null);
        etSemester.setError(null);
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("'Ngày tạo:' dd/MM/yyyy 'lúc' HH:mm",
                Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public void setOnBasicInfoChangeListener(OnBasicInfoChangeListener listener) {
        this.listener = listener;
    }

    public void setButtonsEnabled(boolean enabled) {
        if (btnSave != null) btnSave.setEnabled(enabled);
        if (btnCancel != null) btnCancel.setEnabled(enabled);
    }
}