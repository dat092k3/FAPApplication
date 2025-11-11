package com.example.fapapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
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

    // Action buttons
    private Button btnCancel;
    private Button btnCreate;

    // Firebase
    private DatabaseReference campusRef;

    // Data
    private List<String> campusList;
    private ArrayAdapter<String> campusAdapter;
    private Calendar selectedCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initializeViews();
        setupClickListeners();
        loadCampusData();
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

        // Create button - sẽ implement sau
        btnCreate.setOnClickListener(v -> {
            // TODO: Will implement in next subtask
            Toast.makeText(this, "Create functionality will be implemented next", Toast.LENGTH_SHORT).show();
        });
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

    @Override
    public void onBackPressed() {
        // TODO: Will add unsaved changes check in later subtask
        super.onBackPressed();
    }
}