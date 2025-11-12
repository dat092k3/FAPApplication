package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.Schedule;
import com.example.fapapplication.dto.ScheduleDTO;
import com.example.fapapplication.entity.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminScheduleAdapter extends RecyclerView.Adapter<AdminScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleDTO> scheduleDTOs;
    private List<TimeSlot> timeSlots;
    private OnScheduleActionListener listener;

    public interface OnScheduleActionListener {
        void onEdit(ScheduleDTO scheduleDTO, int position);
        void onDelete(ScheduleDTO scheduleDTO, int position);
    }

    public AdminScheduleAdapter(List<ScheduleDTO> scheduleDTOs, List<TimeSlot> timeSlots,
                           OnScheduleActionListener listener) {
        this.scheduleDTOs = scheduleDTOs;
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleDTO dto = scheduleDTOs.get(position);
        holder.bind(dto, position);
    }

    @Override
    public int getItemCount() {
        return scheduleDTOs.size();
    }

    public void updateList(List<ScheduleDTO> newList) {
        this.scheduleDTOs = newList;
        notifyDataSetChanged();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, timeText, weekBadge, classText, subjectText, teacherText, roomText;
        Button editButton, deleteButton;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            weekBadge = itemView.findViewById(R.id.weekBadge);
            classText = itemView.findViewById(R.id.classText);
            subjectText = itemView.findViewById(R.id.subjectText);
            teacherText = itemView.findViewById(R.id.teacherText);
            roomText = itemView.findViewById(R.id.roomText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(ScheduleDTO dto, int position) {
            Schedule schedule = dto.getSchedule();

            // Format date
            dateText.setText(formatDate(schedule.getDate()));

            // Get time slot info
            TimeSlot slot = findTimeSlot(schedule.getSlotId());
            if (slot != null) {
                timeText.setText("Slot " + slot.getSlotNumber() + ": " + slot.getTimeRange());
            }

            weekBadge.setText("Week " + schedule.getWeek());
            classText.setText(dto.getClassName());
            subjectText.setText(dto.getSubjectName());
            teacherText.setText(dto.getTeacherName());
            roomText.setText(schedule.getRoom());

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(dto, position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(dto, position);
                }
            });
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateStr;
            }
        }

        private TimeSlot findTimeSlot(String slotId) {
            for (TimeSlot slot : timeSlots) {
                if (slot.getId().equals(slotId)) {
                    return slot;
                }
            }
            return null;
        }
    }
}