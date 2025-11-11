package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.WizardStudent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WizardStudentAdapter extends RecyclerView.Adapter<WizardStudentAdapter.ViewHolder> {

    private List<WizardStudent> students;
    private Map<String, String> subjectCodeMap;
    private OnStudentActionListener listener;

    public interface OnStudentActionListener {
        void onRemoveClicked(WizardStudent student, int position);
    }

    public WizardStudentAdapter() {
        this.students = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wizard_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WizardStudent student = students.get(position);

        String displayName = student.getStudentName();
        if (student.getStudentId() != null && !student.getStudentId().isEmpty()) {
            displayName += " (" + student.getStudentId() + ")";
        }
        holder.tvStudentName.setText(displayName);

        StringBuilder subjectsText = new StringBuilder("Subjects: ");
        List<String> assignedSubjectIds = student.getAssignedSubjectIds();
        if (assignedSubjectIds != null && !assignedSubjectIds.isEmpty() && subjectCodeMap != null) {
            List<String> subjectCodes = new ArrayList<>();
            for (String subjectId : assignedSubjectIds) {
                String code = subjectCodeMap.get(subjectId);
                if (code != null) {
                    subjectCodes.add(code);
                }
            }
            if (!subjectCodes.isEmpty()) {
                subjectsText.append(String.join(", ", subjectCodes));
            } else {
                subjectsText.append("None");
            }
        } else {
            subjectsText.append("None");
        }
        holder.tvAssignedSubjects.setText(subjectsText.toString());

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(student, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void updateStudents(List<WizardStudent> newStudents) {
        this.students = new ArrayList<>(newStudents);
        notifyDataSetChanged();
    }

    public void setSubjectCodeMap(Map<String, String> subjectCodeMap) {
        this.subjectCodeMap = subjectCodeMap;
        notifyDataSetChanged();
    }

    public void setOnStudentActionListener(OnStudentActionListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvAssignedSubjects;
        Button btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvAssignedSubjects = itemView.findViewById(R.id.tvAssignedSubjects);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}