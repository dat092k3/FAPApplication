package com.example.fapapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminStatisticActivity extends AppCompatActivity {

    // UI Components
    private TextView totalUsersText, activeUsersText;
    private PieChart passRateChart;
    private LineChart activityChart;
    private SwitchCompat notificationSwitch, timetableSwitch, examScheduleSwitch, applicationStatusSwitch;
    private ImageButton backButton, refreshButton;

    // Firebase
    private DatabaseReference databaseReference;
    private DatabaseReference featureFlagsRef;
    private DatabaseReference statsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        featureFlagsRef = databaseReference.child("featureFlags");
        statsRef = databaseReference.child("statistics");

        // Initialize UI
        initializeViews();
        setupListeners();

        // Load data
        loadStatistics();
        setupRealtimeListeners();
    }

    private void initializeViews() {
        // Statistics
        totalUsersText = findViewById(R.id.totalUsersText);
        activeUsersText = findViewById(R.id.activeUsersText);

        // Charts
        passRateChart = findViewById(R.id.passRateChart);
        activityChart = findViewById(R.id.activityChart);

        // Feature Flags
        notificationSwitch = findViewById(R.id.notificationSwitch);
        timetableSwitch = findViewById(R.id.timetableSwitch);
        examScheduleSwitch = findViewById(R.id.examScheduleSwitch);
        applicationStatusSwitch = findViewById(R.id.applicationStatusSwitch);

        // Buttons
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);

        // Setup Charts
        setupCharts();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        refreshButton.setOnClickListener(v -> {
            loadStatistics();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
        });

        // Feature Flag Switches
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateFeatureFlag("notificationEnabled", isChecked));

        timetableSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateFeatureFlag("timetableEnabled", isChecked));

        examScheduleSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateFeatureFlag("examScheduleEnabled", isChecked));

        applicationStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateFeatureFlag("applicationStatusEnabled", isChecked));
    }

    private void setupCharts() {
        // Setup Pie Chart
        passRateChart.setUsePercentValues(true);
        passRateChart.getDescription().setEnabled(false);
        passRateChart.setExtraOffsets(5, 10, 5, 5);
        passRateChart.setDragDecelerationFrictionCoef(0.95f);
        passRateChart.setDrawHoleEnabled(true);
        passRateChart.setHoleColor(Color.WHITE);
        passRateChart.setTransparentCircleRadius(61f);
        passRateChart.setDrawEntryLabels(false);

        Legend pieLegend = passRateChart.getLegend();
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieLegend.setDrawInside(false);

        // Setup Line Chart
        activityChart.getDescription().setEnabled(false);
        activityChart.setDrawGridBackground(false);
        activityChart.setDrawBorders(false);
        activityChart.getAxisRight().setEnabled(false);

        XAxis xAxis = activityChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        Legend lineLegend = activityChart.getLegend();
        lineLegend.setEnabled(false);
    }

    private void loadFeatureFlags() {
        featureFlagsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    notificationSwitch.setChecked(
                            snapshot.child("notificationEnabled").getValue(Boolean.class) != null ?
                                    snapshot.child("notificationEnabled").getValue(Boolean.class) : true
                    );
                    timetableSwitch.setChecked(
                            snapshot.child("timetableEnabled").getValue(Boolean.class) != null ?
                                    snapshot.child("timetableEnabled").getValue(Boolean.class) : true
                    );
                    examScheduleSwitch.setChecked(
                            snapshot.child("examScheduleEnabled").getValue(Boolean.class) != null ?
                                    snapshot.child("examScheduleEnabled").getValue(Boolean.class) : true
                    );
                    applicationStatusSwitch.setChecked(
                            snapshot.child("applicationStatusEnabled").getValue(Boolean.class) != null ?
                                    snapshot.child("applicationStatusEnabled").getValue(Boolean.class) : true
                    );
                } else {
                    // Initialize with default values
                    initializeDefaultFeatureFlags();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStatisticActivity.this,
                        "Error loading feature flags", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeDefaultFeatureFlags() {
        Map<String, Object> defaultFlags = new HashMap<>();
        defaultFlags.put("notificationEnabled", true);
        defaultFlags.put("timetableEnabled", true);
        defaultFlags.put("examScheduleEnabled", true);
        defaultFlags.put("applicationStatusEnabled", true);

        featureFlagsRef.setValue(defaultFlags);
    }

    private void updateFeatureFlag(String flagName, boolean value) {
        featureFlagsRef.child(flagName).setValue(value)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Feature updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating feature", Toast.LENGTH_SHORT).show());
    }

    private void loadStatistics() {
        // Load user statistics
        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long totalUsers = snapshot.child("totalUsers").getValue(Long.class);
                    Long activeUsers = snapshot.child("activeUsers").getValue(Long.class);
                    Long passCount = snapshot.child("passCount").getValue(Long.class);
                    Long notPassCount = snapshot.child("notPassCount").getValue(Long.class);

                    totalUsersText.setText(totalUsers != null ? String.valueOf(totalUsers) : "0");
                    activeUsersText.setText(activeUsers != null ? String.valueOf(activeUsers) : "0");

                    // Update Pass Rate Chart
                    updatePassRateChart(
                            passCount != null ? passCount : 0,
                            notPassCount != null ? notPassCount : 0
                    );

                    // Load activity data
                    loadActivityData(snapshot.child("activity"));
                } else {
                    // Initialize with demo data
                    initializeDemoData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminStatisticActivity.this,
                        "Error loading statistics", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassRateChart(long passCount, long notPassCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (passCount > 0) {
            entries.add(new PieEntry(passCount, "Pass"));
        }
        if (notPassCount > 0) {
            entries.add(new PieEntry(notPassCount, "Not Pass"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(76, 175, 80));  // Green for Pass
        colors.add(Color.rgb(244, 67, 54));  // Red for Not Pass
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        passRateChart.setData(data);
        passRateChart.invalidate();
    }

    private void loadActivityData(DataSnapshot activitySnapshot) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        if (activitySnapshot.exists()) {
            int index = 0;
            for (DataSnapshot daySnapshot : activitySnapshot.getChildren()) {
                String day = daySnapshot.getKey();
                Long value = daySnapshot.getValue(Long.class);

                if (day != null && value != null) {
                    entries.add(new Entry(index, value));
                    labels.add(day);
                    index++;
                }
            }
        }

        if (entries.isEmpty()) {
            // Demo data
            labels.add("Mon");
            labels.add("Tue");
            labels.add("Wed");
            labels.add("Thu");
            labels.add("Fri");
            labels.add("Sat");
            labels.add("Sun");

            entries.add(new Entry(0, 45));
            entries.add(new Entry(1, 52));
            entries.add(new Entry(2, 48));
            entries.add(new Entry(3, 65));
            entries.add(new Entry(4, 58));
            entries.add(new Entry(5, 35));
            entries.add(new Entry(6, 28));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Active Users");
        dataSet.setColor(Color.rgb(255, 193, 7));
        dataSet.setCircleColor(Color.rgb(255, 193, 7));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(255, 193, 7));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        activityChart.setData(lineData);

        XAxis xAxis = activityChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);

        activityChart.invalidate();
    }

    private void setupRealtimeListeners() {
        // Real-time listener for statistics
        statsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long totalUsers = snapshot.child("totalUsers").getValue(Long.class);
                    Long activeUsers = snapshot.child("activeUsers").getValue(Long.class);

                    if (totalUsers != null) {
                        totalUsersText.setText(String.valueOf(totalUsers));
                    }
                    if (activeUsers != null) {
                        activeUsersText.setText(String.valueOf(activeUsers));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void initializeDemoData() {
        // Initialize demo statistics
        Map<String, Object> demoStats = new HashMap<>();
        demoStats.put("totalUsers", 150);
        demoStats.put("activeUsers", 48);
        demoStats.put("passCount", 120);
        demoStats.put("notPassCount", 30);

        // Demo activity data
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("Mon", 45);
        activityData.put("Tue", 52);
        activityData.put("Wed", 48);
        activityData.put("Thu", 65);
        activityData.put("Fri", 58);
        activityData.put("Sat", 35);
        activityData.put("Sun", 28);

        demoStats.put("activity", activityData);

        statsRef.setValue(demoStats)
                .addOnSuccessListener(aVoid -> loadStatistics());
    }
}