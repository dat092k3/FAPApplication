package com.example.fapapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.AdminScheduleAdapter;
import com.example.fapapplication.entity.ClassSubject;
import com.example.fapapplication.entity.Schedule;
import com.example.fapapplication.dto.ScheduleDTO;
import com.example.fapapplication.entity.TimeSlot;
import com.example.fapapplication.entity.UserClassSubject;
import com.example.fapapplication.utils.ScheduleValidator;
import com.example.fapapplication.utils.ValidationResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminScheduleActivity extends AppCompatActivity
        implements AdminScheduleAdapter.OnScheduleActionListener {

    // UI Components
    private RecyclerView recyclerView;
    private AdminScheduleAdapter adapter;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private ImageButton backButton, addButton;

    // Filter components
    private Spinner weekSpinner, classSpinner;
    private Button datePickerButton, applyFilterButton, applyTemplateButton;
    private String selectedDate = "";
    private int selectedWeek = 0;
    private String selectedClass = "All";

    // Data - ĐỔI SANG DTO
    private List<ScheduleDTO> allScheduleDTOs;
    private List<ScheduleDTO> filteredScheduleDTOs;
    private List<TimeSlot> timeSlots;
    private List<UserClassSubject> ucsList;

    // Cache data để JOIN
    private Map<String, com.example.fapapplication.entity.Class> classesMap;
    private Map<String, com.example.fapapplication.entity.Subject> subjectsMap;
    private Map<String, ClassSubject> classSubjectsMap;
    private Map<String, com.example.fapapplication.entity.User> usersMap;

    // Firebase
    private DatabaseReference schedulesRef;
    private DatabaseReference timeSlotsRef;
    private DatabaseReference ucsRef;
    private DatabaseReference classesRef;
    private DatabaseReference subjectsRef;
    private DatabaseReference classSubjectsRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);

        // Initialize Firebase
        schedulesRef = FirebaseDatabase.getInstance().getReference("Schedules");
        timeSlotsRef = FirebaseDatabase.getInstance().getReference("TimeSlots");
        ucsRef = FirebaseDatabase.getInstance().getReference("UserClassSubjects");
        classesRef = FirebaseDatabase.getInstance().getReference("Classes");
        subjectsRef = FirebaseDatabase.getInstance().getReference("Subjects");
        classSubjectsRef = FirebaseDatabase.getInstance().getReference("ClassSubjects");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize data lists
        allScheduleDTOs = new ArrayList<>();
        filteredScheduleDTOs = new ArrayList<>();
        timeSlots = new ArrayList<>();
        ucsList = new ArrayList<>();

        classesMap = new HashMap<>();
        subjectsMap = new HashMap<>();
        classSubjectsMap = new HashMap<>();
        usersMap = new HashMap<>();

        // Initialize views
        initializeViews();
        setupRecyclerView();

        // Load data theo thứ tự
        loadAllReferenceData();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.scheduleRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
        addButton = findViewById(R.id.addButton);

        weekSpinner = findViewById(R.id.weekSpinner);
        classSpinner = findViewById(R.id.classSpinner);
        datePickerButton = findViewById(R.id.datePickerButton);
        applyFilterButton = findViewById(R.id.applyFilterButton);
        applyTemplateButton = findViewById(R.id.applyTemplateButton);

        backButton.setOnClickListener(v -> finish());
        addButton.setOnClickListener(v -> showAddEditDialog(null, -1));
        datePickerButton.setOnClickListener(v -> showDatePicker());
        applyFilterButton.setOnClickListener(v -> applyFilters());
        applyTemplateButton.setOnClickListener(v -> showApplyTemplateDialog());

        // Setup Week Spinner (0 = All, 1-10)
        List<String> weeks = new ArrayList<>();
        weeks.add("All Weeks");
        for (int i = 1; i <= 10; i++) {
            weeks.add("Week " + i);
        }
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, weeks);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner.setAdapter(weekAdapter);
    }

    private void setupRecyclerView() {
        adapter = new AdminScheduleAdapter(filteredScheduleDTOs, timeSlots, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * LOAD ALL REFERENCE DATA FIRST
     */
    private void loadAllReferenceData() {
        showLoading(true);

        // Load theo thứ tự: TimeSlots → Classes → Subjects → ClassSubjects → Users → UCS → Schedules
        loadTimeSlots();
    }

    private void loadTimeSlots() {
        timeSlotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timeSlots.clear();

                if (!snapshot.exists()) {
                    initializeDefaultTimeSlots();
                } else {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        TimeSlot slot = TimeSlot.fromFirebaseSnapshot(ds);
                        if (slot != null) {
                            timeSlots.add(slot);
                        }
                    }
                }

                // Next: Load Classes
                loadClasses();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading time slots", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeDefaultTimeSlots() {
        timeSlots.add(new TimeSlot("slot1", "7:30 - 9:50", 1));
        timeSlots.add(new TimeSlot("slot2", "10:00 - 12:20", 2));
        timeSlots.add(new TimeSlot("slot3", "12:50 - 15:10", 3));
        timeSlots.add(new TimeSlot("slot4", "15:20 - 17:40", 4));

        for (TimeSlot slot : timeSlots) {
            timeSlotsRef.child(slot.getId()).setValue(slot.toFirebaseMap());
        }
    }

    private void loadClasses() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classesMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.example.fapapplication.entity.Class classObj =
                            com.example.fapapplication.entity.Class.fromFirebaseSnapshot(ds);
                    if (classObj != null) {
                        classesMap.put(classObj.getId(), classObj);
                    }
                }

                // Next: Load Subjects
                loadSubjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubjects() {
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectsMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.example.fapapplication.entity.Subject subject =
                            com.example.fapapplication.entity.Subject.fromFirebaseSnapshot(ds);
                    if (subject != null) {
                        subjectsMap.put(subject.getId(), subject);
                    }
                }

                // Next: Load ClassSubjects
                loadClassSubjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClassSubjects() {
        classSubjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classSubjectsMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ClassSubject cs = ClassSubject.fromFirebaseSnapshot(ds);
                    if (cs != null) {
                        classSubjectsMap.put(cs.getId(), cs);
                    }
                }

                // Next: Load Users
                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading class subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.example.fapapplication.entity.User user =
                            com.example.fapapplication.entity.User.fromFirebaseSnapshot(ds);
                    if (user != null) {
                        usersMap.put(user.getId(), user);
                    }
                }

                // Next: Load UserClassSubjects
                loadUserClassSubjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserClassSubjects() {
        ucsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ucsList.clear();
                List<String> classNames = new ArrayList<>();
                classNames.add("All");

                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserClassSubject ucs = UserClassSubject.fromFirebaseSnapshot(ds);
                    if (ucs != null) {
                        ucsList.add(ucs);

                        // Get class name
                        ClassSubject cs = classSubjectsMap.get(ucs.getClassSubjectId());
                        if (cs != null) {
                            com.example.fapapplication.entity.Class classObj = classesMap.get(cs.getClassId());
                            if (classObj != null && !classNames.contains(classObj.getClassName())) {
                                classNames.add(classObj.getClassName());
                            }
                        }
                    }
                }

                // Setup Class Spinner
                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(
                        AdminScheduleActivity.this,
                        android.R.layout.simple_spinner_item,
                        classNames
                );
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classSpinner.setAdapter(classAdapter);

                // Finally: Load Schedules
                loadSchedules();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading class subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * LOAD SCHEDULES AND BUILD DTOs
     */
    private void loadSchedules() {
        schedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allScheduleDTOs.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Schedule schedule = Schedule.fromFirebaseSnapshot(ds);
                    if (schedule != null) {
                        // BUILD DTO with JOIN
                        ScheduleDTO dto = buildScheduleDTO(schedule);
                        if (dto != null) {
                            allScheduleDTOs.add(dto);
                        }
                    }
                }

                applyFilters();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminScheduleActivity.this,
                        "Error loading schedules", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * BUILD ScheduleDTO with JOIN logic
     */
    private ScheduleDTO buildScheduleDTO(Schedule schedule) {
        // Get UserClassSubject
        UserClassSubject ucs = null;
        for (UserClassSubject u : ucsList) {
            if (u.getId().equals(schedule.getUserClassSubjectId())) {
                ucs = u;
                break;
            }
        }

        if (ucs == null) return null;

        // Get ClassSubject
        ClassSubject cs = classSubjectsMap.get(ucs.getClassSubjectId());
        if (cs == null) return null;

        // Get Class
        com.example.fapapplication.entity.Class classObj = classesMap.get(cs.getClassId());
        if (classObj == null) return null;

        // Get Subject
        com.example.fapapplication.entity.Subject subject = subjectsMap.get(cs.getSubjectId());
        if (subject == null) return null;

        // Get Teacher
        com.example.fapapplication.entity.User teacher = usersMap.get(ucs.getUserId());
        String teacherName = teacher != null ? teacher.getFullName() : "Unknown";

        return new ScheduleDTO(
                schedule,
                classObj.getClassName(),
                subject.getSubjectName(),
                teacherName
        );
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());

                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    datePickerButton.setText(displayFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void applyFilters() {
        filteredScheduleDTOs.clear();

        for (ScheduleDTO dto : allScheduleDTOs) {
            Schedule schedule = dto.getSchedule();

            boolean matchWeek = selectedWeek == 0 || schedule.getWeek() == selectedWeek;
            boolean matchDate = selectedDate.isEmpty() || schedule.getDate().equals(selectedDate);
            boolean matchClass = selectedClass.equals("All") ||
                    dto.getClassName().equals(selectedClass);

            if (matchWeek && matchDate && matchClass) {
                filteredScheduleDTOs.add(dto);
            }
        }

        // Sort by date, then by slot
        Collections.sort(filteredScheduleDTOs, new Comparator<ScheduleDTO>() {
            @Override
            public int compare(ScheduleDTO d1, ScheduleDTO d2) {
                int dateCompare = d1.getSchedule().getDate().compareTo(d2.getSchedule().getDate());
                if (dateCompare != 0) return dateCompare;
                return d1.getSchedule().getSlotId().compareTo(d2.getSchedule().getSlotId());
            }
        });

        adapter.updateList(filteredScheduleDTOs);
        updateEmptyState();
    }

    private void showAddEditDialog(ScheduleDTO scheduleDTO, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_schedule, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        Spinner weekSpinnerDialog = dialogView.findViewById(R.id.weekSpinnerDialog);
        Button datePickerButtonDialog = dialogView.findViewById(R.id.datePickerButtonDialog);
        Spinner slotSpinner = dialogView.findViewById(R.id.slotSpinner);
        Spinner ucsSpinner = dialogView.findViewById(R.id.ucsSpinner);
        TextInputEditText roomInput = dialogView.findViewById(R.id.roomInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // Setup Week Spinner
        List<String> weeks = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            weeks.add("Week " + i);
        }
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, weeks);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinnerDialog.setAdapter(weekAdapter);

        // Setup Slot Spinner
        List<String> slotNames = new ArrayList<>();
        for (TimeSlot slot : timeSlots) {
            slotNames.add("Slot " + slot.getSlotNumber() + ": " + slot.getTimeRange());
        }
        ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, slotNames);
        slotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        slotSpinner.setAdapter(slotAdapter);

        // Setup UCS Spinner - Hiển thị ClassName - SubjectName (TeacherName)
        List<String> ucsNames = new ArrayList<>();
        for (UserClassSubject ucs : ucsList) {
            ClassSubject cs = classSubjectsMap.get(ucs.getClassSubjectId());
            if (cs != null) {
                com.example.fapapplication.entity.Class classObj = classesMap.get(cs.getClassId());
                com.example.fapapplication.entity.Subject subject = subjectsMap.get(cs.getSubjectId());
                com.example.fapapplication.entity.User teacher = usersMap.get(ucs.getUserId());

                String displayName = classObj.getClassName() + " - " +
                        subject.getSubjectName() + " (" +
                        teacher.getFullName() + ")";
                ucsNames.add(displayName);
            }
        }
        ArrayAdapter<String> ucsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ucsNames);
        ucsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ucsSpinner.setAdapter(ucsAdapter);

        // Date picker for dialog
        final String[] dialogSelectedDate = {""};
        datePickerButtonDialog.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dialogSelectedDate[0] = sdf.format(calendar.getTime());

                        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        datePickerButtonDialog.setText(displayFormat.format(calendar.getTime()));

                        // Calculate week number
                        int week = getWeekNumber(calendar);
                        weekSpinnerDialog.setSelection(week - 1);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        });

        boolean isEditing = scheduleDTO != null;
        dialogTitle.setText(isEditing ? "Edit Schedule" : "Add Schedule");

        if (isEditing) {
            Schedule schedule = scheduleDTO.getSchedule();
            weekSpinnerDialog.setSelection(schedule.getWeek() - 1);
            dialogSelectedDate[0] = schedule.getDate();
            datePickerButtonDialog.setText(schedule.getDate());

            // Find and select slot
            for (int i = 0; i < timeSlots.size(); i++) {
                if (timeSlots.get(i).getId().equals(schedule.getSlotId())) {
                    slotSpinner.setSelection(i);
                    break;
                }
            }

            // Find and select UCS
            for (int i = 0; i < ucsList.size(); i++) {
                if (ucsList.get(i).getId().equals(schedule.getUserClassSubjectId())) {
                    ucsSpinner.setSelection(i);
                    break;
                }
            }

            roomInput.setText(schedule.getRoom());
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            int weekPos = weekSpinnerDialog.getSelectedItemPosition();
            int week = weekPos + 1;
            String date = dialogSelectedDate[0];
            int slotPos = slotSpinner.getSelectedItemPosition();
            int ucsPos = ucsSpinner.getSelectedItemPosition();
            String room = roomInput.getText().toString().trim();

            if (date.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (room.isEmpty()) {
                roomInput.setError("Room is required");
                return;
            }

            TimeSlot selectedSlot = timeSlots.get(slotPos);
            UserClassSubject selectedUcs = ucsList.get(ucsPos);

            if (isEditing) {
                updateSchedule(scheduleDTO.getSchedule().getId(), week, date,
                        selectedSlot.getId(), selectedUcs.getId(), room, position);
            } else {
                createSchedule(week, date, selectedSlot.getId(), selectedUcs.getId(), room);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void createSchedule(int week, String date, String slotId,
                                String ucsId, String room) {
        String id = Schedule.generateId();

        Schedule newSchedule = new Schedule(
                id, ucsId, date, week, slotId, room, false,
                System.currentTimeMillis(), true
        );

        // Build DTO để validate
        ScheduleDTO newDTO = buildScheduleDTO(newSchedule);
        if (newDTO == null) {
            Toast.makeText(this, "Error building schedule data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate
        ValidationResult result = ScheduleValidator.validate(newDTO, allScheduleDTOs);
        if (!result.isValid()) {
            new AlertDialog.Builder(this)
                    .setTitle("Validation Error")
                    .setMessage(result.getErrorMessage())
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Save to Firebase
        schedulesRef.child(id).setValue(newSchedule.toFirebaseMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Schedule created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating schedule", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateSchedule(String id, int week, String date, String slotId,
                                String ucsId, String room, int position) {
        Schedule updatedSchedule = new Schedule(
                id, ucsId, date, week, slotId, room, false,
                System.currentTimeMillis(), true
        );

        // Build DTO để validate
        ScheduleDTO updatedDTO = buildScheduleDTO(updatedSchedule);
        if (updatedDTO == null) {
            Toast.makeText(this, "Error building schedule data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate
        ValidationResult result = ScheduleValidator.validate(updatedDTO, allScheduleDTOs);
        if (!result.isValid()) {
            new AlertDialog.Builder(this)
                    .setTitle("Validation Error")
                    .setMessage(result.getErrorMessage())
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        schedulesRef.child(id).setValue(updatedSchedule.toFirebaseMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Schedule updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating schedule", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSchedule(String id, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    schedulesRef.child(id).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error deleting schedule", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Template, utility methods...
    private void showApplyTemplateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Apply Week 1 Template");
        builder.setMessage("This will copy all Week 1 schedules to Weeks 2-10. Continue?");

        builder.setPositiveButton("Apply", (dialog, which) -> {
            applyWeek1Template();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void applyWeek1Template() {
        // Similar logic as before, but using ScheduleDTO
        // Copy schedules from week 1 to weeks 2-10
        Toast.makeText(this, "Template feature - implement similar to original", Toast.LENGTH_SHORT).show();
    }

    private int getWeekNumber(Calendar calendar) {
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        return ((weekOfYear - 1) % 10) + 1;
    }

    private void updateEmptyState() {
        if (filteredScheduleDTOs.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(ScheduleDTO scheduleDTO, int position) {
        showAddEditDialog(scheduleDTO, position);
    }

    @Override
    public void onDelete(ScheduleDTO scheduleDTO, int position) {
        deleteSchedule(scheduleDTO.getSchedule().getId(), position);
    }
}