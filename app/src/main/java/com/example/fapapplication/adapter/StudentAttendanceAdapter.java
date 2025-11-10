package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.Student;
import com.example.fapapplication.model.StudentAttendanceItem;

import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {

    private List<StudentAttendanceItem> studentList;
    private List<StudentAttendanceItem> studentListFull; // Cho search
    private OnAttendanceChangeListener listener;

    public interface OnAttendanceChangeListener {
        void onAttendanceChanged();
    }

    public StudentAttendanceAdapter(List<StudentAttendanceItem> studentList, OnAttendanceChangeListener listener) {
        this.studentList = studentList;
        this.studentListFull = new ArrayList<>(studentList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentAttendanceItem item = studentList.get(position);
        Student student = item.getStudent();

        holder.tvStudentName.setText(student.getStudentName());
        holder.tvStudentId.setText(student.getStudentId());

        // Update button states
        updateButtonStates(holder, item.isPresent());

        // Present button click
        holder.btnPresent.setOnClickListener(v -> {
            item.setPresent(true);
            updateButtonStates(holder, true);
            if (listener != null) {
                listener.onAttendanceChanged();
            }
        });

        // Absent button click
        holder.btnAbsent.setOnClickListener(v -> {
            item.setPresent(false);
            updateButtonStates(holder, false);
            if (listener != null) {
                listener.onAttendanceChanged();
            }
        });
    }

    private void updateButtonStates(ViewHolder holder, boolean isPresent) {
        if (isPresent) {
            // Present is active
            holder.btnPresent.setBackgroundTintList(
                    ContextCompat.getColorStateList(holder.itemView.getContext(), android.R.color.holo_green_dark)
            );
            holder.btnPresent.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white)
            );

            // Absent is inactive
            holder.btnAbsent.setBackgroundTintList(
                    ContextCompat.getColorStateList(holder.itemView.getContext(), android.R.color.darker_gray)
            );
            holder.btnAbsent.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray)
            );
        } else {
            // Absent is active
            holder.btnAbsent.setBackgroundTintList(
                    ContextCompat.getColorStateList(holder.itemView.getContext(), android.R.color.holo_red_dark)
            );
            holder.btnAbsent.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white)
            );

            // Present is inactive
            holder.btnPresent.setBackgroundTintList(
                    ContextCompat.getColorStateList(holder.itemView.getContext(), android.R.color.darker_gray)
            );
            holder.btnPresent.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray)
            );
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // Search functionality
    public void filter(String query) {
        studentList.clear();
        if (query.isEmpty()) {
            studentList.addAll(studentListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (StudentAttendanceItem item : studentListFull) {
                if (item.getStudent().getStudentName().toLowerCase().contains(lowerCaseQuery) ||
                        item.getStudent().getStudentId().toLowerCase().contains(lowerCaseQuery)) {
                    studentList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Mark all present/absent
    public void markAll(boolean isPresent) {
        for (StudentAttendanceItem item : studentList) {
            item.setPresent(isPresent);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onAttendanceChanged();
        }
    }


    // Get current list
    public List<StudentAttendanceItem> getStudentList() {
        return studentList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId;
        Button btnPresent, btnAbsent;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            btnPresent = itemView.findViewById(R.id.btnPresent);
            btnAbsent = itemView.findViewById(R.id.btnAbsent);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
