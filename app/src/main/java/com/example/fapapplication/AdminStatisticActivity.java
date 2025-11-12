package com.example.fapapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminStatisticActivity extends AppCompatActivity {

    // UI Components
    private TextView totalUsersText, totalStudentsText, totalTeachersText;
    private TextView totalSubjectsText, totalClassesText, activeUsersText;
    private PieChart studentTeacherRatioChart;
    private LineChart userGrowthChart;
    private BarChart classSizeChart;
    private ImageButton backButton, refreshButton;

    // Firebase References
    private DatabaseReference usersRef;
    private DatabaseReference subjectsRef;
    private DatabaseReference classesRef;
    private DatabaseReference ucsRef;
    private DatabaseReference classSubjectsRef;

    // Data counters
    private int studentCount = 0;
    private int teacherCount = 0;
    private int subjectCount = 0;
    private int classCount = 0;
    private Map<String, Integer> newUsersByDate = new HashMap<>();
    private Map<String, Integer> classSizes = new HashMap<>();
    private Map<String, String> classIdToName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistic);

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        subjectsRef = FirebaseDatabase.getInstance().getReference("Subjects");
        classesRef = FirebaseDatabase.getInstance().getReference("Classes");
        ucsRef = FirebaseDatabase.getInstance().getReference("UserClassSubjects");
        classSubjectsRef = FirebaseDatabase.getInstance().getReference("ClassSubjects");

        // Initialize UI
        initializeViews();
        setupListeners();
        setupCharts();

        // Load ALL real data
        loadAllStatistics();
    }

    private void initializeViews() {
        // Statistics texts
        totalUsersText = findViewById(R.id.totalUsersText);
        totalStudentsText = findViewById(R.id.totalStudentsText);
        totalTeachersText = findViewById(R.id.totalTeachersText);
        totalSubjectsText = findViewById(R.id.totalSubjectsText);
        totalClassesText = findViewById(R.id.totalClassesText);
        activeUsersText = findViewById(R.id.activeUsersText);

        // Charts
        studentTeacherRatioChart = findViewById(R.id.studentTeacherRatioChart);
        userGrowthChart = findViewById(R.id.userGrowthChart);
        classSizeChart = findViewById(R.id.classSizeChart);

        // Buttons
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        refreshButton.setOnClickListener(v -> {
            resetCounters();
            loadAllStatistics();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCharts() {
        // Setup Pie Chart (Student/Teacher Ratio)
        studentTeacherRatioChart.setUsePercentValues(true);
        studentTeacherRatioChart.getDescription().setEnabled(false);
        studentTeacherRatioChart.setDrawHoleEnabled(true);
        studentTeacherRatioChart.setHoleColor(Color.WHITE);
        studentTeacherRatioChart.setTransparentCircleRadius(61f);
        studentTeacherRatioChart.setDrawEntryLabels(false);

        Legend pieLegend = studentTeacherRatioChart.getLegend();
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        // Setup Line Chart (User Growth)
        userGrowthChart.getDescription().setEnabled(false);
        userGrowthChart.setDrawGridBackground(false);
        userGrowthChart.getAxisRight().setEnabled(false);

        XAxis lineXAxis = userGrowthChart.getXAxis();
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXAxis.setDrawGridLines(false);

        // Setup Bar Chart (Class Sizes)
        classSizeChart.getDescription().setEnabled(false);
        classSizeChart.setDrawGridBackground(false);
        classSizeChart.getAxisRight().setEnabled(false);
        classSizeChart.setFitBars(true);

        XAxis barXAxis = classSizeChart.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setGranularity(1f);
        barXAxis.setDrawGridLines(false);
    }

    private void resetCounters() {
        studentCount = 0;
        teacherCount = 0;
        subjectCount = 0;
        classCount = 0;
        newUsersByDate.clear();
        classSizes.clear();
        classIdToName.clear();
    }

    private void loadAllStatistics() {
        // Load theo thứ tự: Classes → Users → Subjects → UCS
        loadClasses();
    }

    /**
     * 1. LOAD CLASSES - Lấy className mapping
     */
    private void loadClasses() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classCount = 0;
                classIdToName.clear();

                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.child("ClassId").getValue(String.class);
                    String className = classSnapshot.child("ClassName").getValue(String.class);

                    if (classId != null && className != null) {
                        classIdToName.put(classId, className);
                        classCount++;
                    }
                }

                totalClassesText.setText(String.valueOf(classCount));

                // Next: Load Users
                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStatisticActivity.this,
                        "Error loading classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 2. LOAD USERS - Count Students/Teachers, User Growth, Active Users
     */
    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentCount = 0;
                teacherCount = 0;
                newUsersByDate.clear();

                Set<String> activeUserIds = new HashSet<>();
                long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
                List<String> last30Days = getLast30Days();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        // Count by role
                        String role = userSnapshot.child("Role").getValue(String.class);

                        if ("Student".equalsIgnoreCase(role)) {
                            studentCount++;
                        } else if ("Teacher".equalsIgnoreCase(role)) {
                            teacherCount++;
                        }

                        // User growth (last 30 days)
                        Long createdAt = userSnapshot.child("CreatedAt").getValue(Long.class);
                        if (createdAt != null) {
                            String dateStr = getDateString(createdAt);
                            if (last30Days.contains(dateStr)) {
                                newUsersByDate.put(dateStr,
                                        newUsersByDate.getOrDefault(dateStr, 0) + 1);
                            }

                            // Active users (created in last 7 days)
                            if (createdAt >= sevenDaysAgo) {
                                String userId = userSnapshot.child("UserId").getValue(String.class);
                                if (userId != null) {
                                    activeUserIds.add(userId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Update UI
                int totalUsers = studentCount + teacherCount;
                totalUsersText.setText(String.valueOf(totalUsers));
                totalStudentsText.setText(String.valueOf(studentCount));
                totalTeachersText.setText(String.valueOf(teacherCount));
                activeUsersText.setText(String.valueOf(activeUserIds.size()));

                // Update charts
                updateStudentTeacherRatioChart();
                updateUserGrowthChart();

                // Next: Load Subjects
                loadSubjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStatisticActivity.this,
                        "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 3. LOAD SUBJECTS
     */
    private void loadSubjects() {
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectCount = (int) snapshot.getChildrenCount();
                totalSubjectsText.setText(String.valueOf(subjectCount));

                // Next: Load UCS for class sizes
                loadUserClassSubjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStatisticActivity.this,
                        "Error loading subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 4. LOAD USER CLASS SUBJECTS - Calculate class sizes
     */
    private void loadUserClassSubjects() {
        // First load ClassSubjects to get class mapping
        classSubjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot classSubjectsSnapshot) {
                Map<String, String> csToClass = new HashMap<>();

                for (DataSnapshot cs : classSubjectsSnapshot.getChildren()) {
                    String csId = cs.child("ClassSubjectId").getValue(String.class);
                    String classId = cs.child("ClassId").getValue(String.class);

                    if (csId != null && classId != null) {
                        csToClass.put(csId, classId);
                    }
                }

                // Now load UCS and count students per class
                ucsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        classSizes.clear();

                        for (DataSnapshot ucs : snapshot.getChildren()) {
                            String role = ucs.child("Role").getValue(String.class);
                            String classSubjectId = ucs.child("ClassSubjectId").getValue(String.class);

                            // Only count students
                            if ("Student".equalsIgnoreCase(role) && classSubjectId != null) {
                                String classId = csToClass.get(classSubjectId);
                                if (classId != null) {
                                    String className = classIdToName.get(classId);
                                    if (className != null) {
                                        classSizes.put(className,
                                                classSizes.getOrDefault(className, 0) + 1);
                                    }
                                }
                            }
                        }

                        // Update class size chart
                        updateClassSizeChart();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminStatisticActivity.this,
                                "Error loading enrollments", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStatisticActivity.this,
                        "Error loading class subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * UPDATE STUDENT/TEACHER RATIO CHART
     */
    private void updateStudentTeacherRatioChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (studentCount > 0) {
            entries.add(new PieEntry(studentCount, "Students"));
        }
        if (teacherCount > 0) {
            entries.add(new PieEntry(teacherCount, "Teachers"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(33, 150, 243));  // Blue for Students
        colors.add(Color.rgb(156, 39, 176));  // Purple for Teachers
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        studentTeacherRatioChart.setData(data);
        studentTeacherRatioChart.invalidate();
    }

    /**
     * UPDATE USER GROWTH CHART (Last 30 days)
     */
    private void updateUserGrowthChart() {
        List<String> last30Days = getLast30Days();
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < last30Days.size(); i++) {
            String date = last30Days.get(i);
            int count = newUsersByDate.getOrDefault(date, 0);

            entries.add(new Entry(i, count));
            labels.add(getDayLabel(date, i));
        }

        LineDataSet dataSet = new LineDataSet(entries, "New Users");
        dataSet.setColor(Color.rgb(76, 175, 80));
        dataSet.setCircleColor(Color.rgb(76, 175, 80));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(76, 175, 80));
        dataSet.setFillAlpha(30);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        userGrowthChart.setData(lineData);

        XAxis xAxis = userGrowthChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(6);
        xAxis.setGranularity(5f);

        userGrowthChart.invalidate();
    }

    /**
     * UPDATE CLASS SIZE CHART
     */
    private void updateClassSizeChart() {
        if (classSizes.isEmpty()) {
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : classSizes.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Students per Class");
        dataSet.setColor(Color.rgb(255, 152, 0));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        classSizeChart.setData(barData);

        XAxis xAxis = classSizeChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45);

        classSizeChart.invalidate();
    }

    // UTILITY METHODS
    private List<String> getLast30Days() {
        List<String> days = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int i = 29; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L));
            days.add(sdf.format(calendar.getTime()));
        }
        return days;
    }

    private String getDateString(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(timestamp);
    }

    private String getDayLabel(String dateStr, int index) {
        // Show every 5th day
        if (index % 5 == 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat labelFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                return labelFormat.format(sdf.parse(dateStr));
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}