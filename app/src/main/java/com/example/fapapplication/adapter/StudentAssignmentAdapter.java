package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentAssignmentAdapter extends RecyclerView.Adapter<StudentAssignmentAdapter.ViewHolder> {

    private List<StudentAssignmentInfo> studentAssignments;
    private OnStudentActionListener listener;

    public static class StudentAssignmentInfo {
        public String userId;
        public String studentName;
        public String studentId;
        public List<String> subjectCodes;
        public long enrolledDate;
        public boolean isActive;

        public StudentAssignmentInfo(String userId, String studentName, String studentId,
                List<String> subjectCodes, long enrolledDate, boolean isActive) {
            this.userId = userId;
            this.studentName = studentName;
            this.studentId = studentId;
            this.subjectCodes = subjectCodes;
            this.enrolledDate = enrolledDate;
            this.isActive = isActive;
        }
    }

    public interface OnStudentActionListener {
        void onViewDetailsClicked(StudentAssignmentInfo studentInfo, int position);
        void onRemoveClicked(StudentAssignmentInfo studentInfo, int position);
    }

    public StudentAssignmentAdapter() {
        this.studentAssignments = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentAssignmentInfo info = studentAssignments.get(position);

        holder.tvStudentName.setText(info.studentName);
        holder.tvStudentId.setText(info.studentId != null ? info.studentId : "N/A");

        if (info.subjectCodes != null && !info.subjectCodes.isEmpty()) {
            StringBuilder subjects = new StringBuilder();
            for (int i = 0; i < info.subjectCodes.size(); i++) {
                subjects.append(info.subjectCodes.get(i));
                if (i < info.subjectCodes.size() - 1) {
                    subjects.append(", ");
                }
            }
            holder.tvEnrolledSubjects.setText(subjects.toString());
        } else {
            holder.tvEnrolledSubjects.setText("No subjects");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("'Enrolled:' dd/MM/yyyy", Locale.getDefault());
        holder.tvEnrolledDate.setText(sdf.format(new Date(info.enrolledDate)));

        if (info.isActive) {
            holder.tvActiveStatus.setText("Active");
            holder.tvActiveStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        } else {
            holder.tvActiveStatus.setText("Inactive");
            holder.tvActiveStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E));
        }

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClicked(info, holder.getAdapterPosition());
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(info, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentAssignments.size();
    }

    public void updateStudentAssignments(List<StudentAssignmentInfo> newAssignments) {
        this.studentAssignments = new ArrayList<>(newAssignments);
        notifyDataSetChanged();
    }

    public void setOnStudentActionListener(OnStudentActionListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvEnrolledSubjects;
        TextView tvEnrolledDate, tvActiveStatus;
        Button btnViewDetails, btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvEnrolledSubjects = itemView.findViewById(R.id.tvEnrolledSubjects);
            tvEnrolledDate = itemView.findViewById(R.id.tvEnrolledDate);
            tvActiveStatus = itemView.findViewById(R.id.tvActiveStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}