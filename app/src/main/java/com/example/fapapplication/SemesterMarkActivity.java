package com.example.fapapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fapapplication.model.Grade;
import com.google.firebase.firestore.*;
import java.util.*;

public class SemesterMarkActivity extends AppCompatActivity {

    private LinearLayout tabLayout;
    private HorizontalScrollView courseList;
    private FirebaseFirestore db;
    private String currentStudentId = "student001"; // Thay bằng ID thực tế

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_mark);
        courseList = findViewById(R.id.semesterTabs);
        tabLayout = findViewById(R.id.layoutTabs);
        db = FirebaseFirestore.getInstance();

        loadTerms();
    }

    private void loadTerms() {
        db.collection("grades")
                .whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> terms = new TreeSet<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String term = doc.getString("term");
                        if (term != null) terms.add(term);
                    }
                    showTermTabs(new ArrayList<>(terms));
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
        db.collection("grades")
                .whereEqualTo("studentId", currentStudentId)
                .whereEqualTo("term", term)
                .get()
                .addOnSuccessListener(query -> {
                    List<Grade> grades = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Grade grade = doc.toObject(Grade.class);
                        if (grade != null) {
                            grade.calculateAverage();
                            grades.add(grade);
                        }
                    }
                    displayGrades(grades);
                });
    }

    private void displayGrades(List<Grade> grades) {
        courseList.removeAllViews();
        for (Grade grade : grades) {
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