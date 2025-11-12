package com.example.fapapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fapapplication.model.Grade;

import java.util.*;

import com.example.fapapplication.model.SemesterGrade;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SemesterMarkActivity extends AppCompatActivity {

    private LinearLayout tabLayout;
    private HorizontalScrollView courseList;
    private String currentStudentId = "student001"; // Thay bằng ID thực tế
    private FirebaseDatabase database;
    private DatabaseReference dbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_mark);
        courseList = findViewById(R.id.semesterTabs);
        tabLayout = findViewById(R.id.layoutTabs);
        database = FirebaseDatabase.getInstance("https://prm202-4d2da-default-rtdb.asia-southeast1.firebasedatabase.app/");
        dbRef = database.getReference("grades");

        loadTerms();
    }

    private void loadTerms() {
        dbRef = FirebaseDatabase.getInstance(
                "https://prm202-4d2da-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("grades");

        dbRef.orderByChild("studentId").equalTo(currentStudentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Set<String> terms = new TreeSet<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String term = child.child("term").getValue(String.class);
                            if (term != null) terms.add(term);
                        }
                        showTermTabs(new ArrayList<>(terms));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        error.toException().printStackTrace();
                    }
                });
    }


    private void showTermTabs(List<String> terms) {
        tabLayout.removeAllViews();
        for (String term : terms) {
            Button tab = new Button(this);
            tab.setText(term);
            tab.setBackgroundColor(0xFFDDDDDD);
            tab.setOnClickListener(v -> loadGradesForTerm(term));
            tabLayout.addView(tab);
        }
        if (!terms.isEmpty()) {
            loadGradesForTerm(terms.get(0));
        }
    }

    private void loadGradesForTerm(String term) {
        dbRef = FirebaseDatabase.getInstance(
                "https://prm202-4d2da-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("grades");

        dbRef.orderByChild("term").equalTo(term)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<SemesterGrade> grades = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Grade grade = child.getValue(Grade.class);
                            if (grade != null && currentStudentId.equals(grade.getStudentId())) {
                                grade.calculateAverage();
                                grades.add(grade);
                            }
                        }
                        displayGrades(grades);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        error.toException().printStackTrace();
                    }
                });
    }

    private void displayGrades(List<SemesterGrade> grades) {
        courseList.removeAllViews();
        for (SemesterGrade grade : grades) {
            TextView courseView = new TextView(this);
            String status = (grade.getAverage() != null && grade.getAverage() > 0) ? "✅ Passed" : "❌ Not Passed";
            courseView.setText(
                    status + "\n" +
                            grade.getSubject() + "\n" +
                            "Class: " + grade.getClassName() + "\n" +
                            "Average: " + (grade.getAverage() != null ? grade.getAverage() : "N/A")
            );
            courseView.setPadding(0, 24, 0, 24);
            courseView.setTextSize(16);
            courseList.addView(courseView);
        }
    }
}