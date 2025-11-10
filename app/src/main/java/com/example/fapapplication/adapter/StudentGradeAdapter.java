package com.example.fapapplication.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.Grade;
import com.example.fapapplication.model.Student;
import com.example.fapapplication.model.StudentGradeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentGradeAdapter extends RecyclerView.Adapter<StudentGradeAdapter.ViewHolder> {

    private List<StudentGradeItem> gradeList;
    private List<StudentGradeItem> gradeListFull;

    public StudentGradeAdapter(List<StudentGradeItem> gradeList) {
        this.gradeList = gradeList;
        this.gradeListFull = new ArrayList<>(gradeList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentGradeItem item = gradeList.get(position);
        Student student = item.getStudent();
        Grade grade = item.getGrade();

        holder.tvStudentName.setText(student.getStudentName());
        holder.tvStudentId.setText(student.getStudentId());

        // Remove old text watchers
        holder.clearTextWatchers();

        // Set existing grades
        setEditTextValue(holder.etPt1, grade.getPt1());
        setEditTextValue(holder.etPt2, grade.getPt2());
        setEditTextValue(holder.etParticipation, grade.getParticipation());
        setEditTextValue(holder.etPe, grade.getPe());
        setEditTextValue(holder.etFe, grade.getFe());

        // Update average
        updateAverage(holder, grade);

        // Add text watchers
        holder.addTextWatcher(holder.etPt1, value -> {
            grade.setPt1(value);
            grade.calculateAverage();
            updateAverage(holder, grade);
        });

        holder.addTextWatcher(holder.etPt2, value -> {
            grade.setPt2(value);
            grade.calculateAverage();
            updateAverage(holder, grade);
        });

        holder.addTextWatcher(holder.etParticipation, value -> {
            grade.setParticipation(value);
            grade.calculateAverage();
            updateAverage(holder, grade);
        });

        holder.addTextWatcher(holder.etPe, value -> {
            grade.setPe(value);
            grade.calculateAverage();
            updateAverage(holder, grade);
        });

        holder.addTextWatcher(holder.etFe, value -> {
            grade.setFe(value);
            grade.calculateAverage();
            updateAverage(holder, grade);
        });
    }

    private void setEditTextValue(EditText editText, Double value) {
        if (value != null) {
            editText.setText(String.valueOf(value));
        } else {
            editText.setText("");
        }
    }

    private void updateAverage(ViewHolder holder, Grade grade) {
        if (grade.getAverage() != null) {
            holder.tvAverage.setText(String.format(Locale.getDefault(), "%.1f", grade.getAverage()));
        } else {
            holder.tvAverage.setText("--");
        }
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    // Search functionality
    public void filter(String query) {
        gradeList.clear();
        if (query.isEmpty()) {
            gradeList.addAll(gradeListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (StudentGradeItem item : gradeListFull) {
                if (item.getStudent().getStudentName().toLowerCase().contains(lowerCaseQuery) ||
                        item.getStudent().getStudentId().toLowerCase().contains(lowerCaseQuery)) {
                    gradeList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<StudentGradeItem> getGradeList() {
        return gradeList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvAverage;
        EditText etPt1, etPt2, etParticipation, etPe, etFe;
        List<TextWatcher> textWatchers = new ArrayList<>();

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvAverage = itemView.findViewById(R.id.tvAverage);
            etPt1 = itemView.findViewById(R.id.etPt1);
            etPt2 = itemView.findViewById(R.id.etPt2);
            etParticipation = itemView.findViewById(R.id.etParticipation);
            etPe = itemView.findViewById(R.id.etPe);
            etFe = itemView.findViewById(R.id.etFe);
        }

        void addTextWatcher(EditText editText, OnGradeChangeListener listener) {
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String text = s.toString().trim();
                    Double value = null;
                    if (!text.isEmpty()) {
                        try {
                            value = Double.parseDouble(text);
                            // Validate range 0-10
                            if (value < 0 || value > 10) {
                                editText.setError("Must be 0-10");
                                return;
                            }
                        } catch (NumberFormatException e) {
                            editText.setError("Invalid number");
                            return;
                        }
                    }
                    listener.onGradeChanged(value);
                }
            };

            textWatchers.add(watcher);
            editText.addTextChangedListener(watcher);
        }

        void clearTextWatchers() {
            for (TextWatcher watcher : textWatchers) {
                etPt1.removeTextChangedListener(watcher);
                etPt2.removeTextChangedListener(watcher);
                etParticipation.removeTextChangedListener(watcher);
                etPe.removeTextChangedListener(watcher);
                etFe.removeTextChangedListener(watcher);
            }
            textWatchers.clear();
        }
    }

    interface OnGradeChangeListener {
        void onGradeChanged(Double value);
    }
}
