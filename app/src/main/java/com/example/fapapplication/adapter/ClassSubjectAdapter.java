package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.ClassSubject;
import com.example.fapapplication.entity.Subject;
import com.example.fapapplication.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSubjectAdapter extends RecyclerView.Adapter<ClassSubjectAdapter.ViewHolder> {

    private List<ClassSubject> classSubjects;
    private Map<String, Subject> subjectMap;
    private Map<String, User> teacherMap;
    private OnClassSubjectActionListener listener;

    public interface OnClassSubjectActionListener {
        void onEditClicked(ClassSubject classSubject, int position);
        void onDeleteClicked(ClassSubject classSubject, int position);
    }

    public ClassSubjectAdapter() {
        this.classSubjects = new ArrayList<>();
        this.subjectMap = new HashMap<>();
        this.teacherMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassSubject classSubject = classSubjects.get(position);

        Subject subject = subjectMap.get(classSubject.getSubjectId());
        User teacher = teacherMap.get(classSubject.getTeacherId());

        if (subject != null) {
            holder.tvSubjectCode.setText(subject.getSubjectCode());
            holder.tvSubjectName.setText(subject.getSubjectName());
        } else {
            holder.tvSubjectCode.setText(classSubject.getSubjectId());
            holder.tvSubjectName.setText("Loading...");
        }

        if (teacher != null) {
            holder.tvTeacherName.setText(teacher.getFullName());
        } else {
            holder.tvTeacherName.setText("No teacher assigned");
        }

        holder.tvSchedule.setText(classSubject.getSchedule() != null ?
                classSubject.getSchedule() : "No schedule");
        holder.tvRoom.setText(classSubject.getRoom() != null ?
                classSubject.getRoom() : "No room");

        if (classSubject.isActive()) {
            holder.tvActiveStatus.setText("Active");
            holder.tvActiveStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        } else {
            holder.tvActiveStatus.setText("Inactive");
            holder.tvActiveStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClicked(classSubject, holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(classSubject, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return classSubjects.size();
    }

    public void updateClassSubjects(List<ClassSubject> newClassSubjects) {
        this.classSubjects = new ArrayList<>(newClassSubjects);
        notifyDataSetChanged();
    }

    public void updateSubjectMap(Map<String, Subject> subjects) {
        this.subjectMap = subjects;
        notifyDataSetChanged();
    }

    public void updateTeacherMap(Map<String, User> teachers) {
        this.teacherMap = teachers;
        notifyDataSetChanged();
    }

    public void setOnClassSubjectActionListener(OnClassSubjectActionListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectCode, tvSubjectName, tvTeacherName;
        TextView tvSchedule, tvRoom, tvActiveStatus;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvActiveStatus = itemView.findViewById(R.id.tvActiveStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}