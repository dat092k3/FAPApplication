package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.WizardSubject;

import java.util.ArrayList;
import java.util.List;

public class WizardSubjectAdapter extends RecyclerView.Adapter<WizardSubjectAdapter.ViewHolder> {

    private List<WizardSubject> subjects;
    private OnSubjectActionListener listener;

    public interface OnSubjectActionListener {
        void onRemoveClicked(WizardSubject subject, int position);
    }

    public WizardSubjectAdapter() {
        this.subjects = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wizard_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WizardSubject subject = subjects.get(position);

        String displayName = subject.getSubjectCode() + " - " + subject.getSubjectName();
        holder.tvSubjectName.setText(displayName);

        String teacherText = "Teacher: " + (subject.getTeacherName() != null ? subject.getTeacherName() : "Not assigned");
        holder.tvTeacher.setText(teacherText);

        String scheduleText = "Schedule: " + (subject.getSchedule() != null && !subject.getSchedule().isEmpty()
                ? subject.getSchedule() : "Not set");
        holder.tvSchedule.setText(scheduleText);

        String roomText = "Room: " + (subject.getRoom() != null && !subject.getRoom().isEmpty()
                ? subject.getRoom() : "Not set");
        holder.tvRoom.setText(roomText);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(subject, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public void updateSubjects(List<WizardSubject> newSubjects) {
        this.subjects = new ArrayList<>(newSubjects);
        notifyDataSetChanged();
    }

    public void setOnSubjectActionListener(OnSubjectActionListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvTeacher, tvSchedule, tvRoom;
        Button btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}