package com.example.fapapplication.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.AttendanceHistoryItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {

    private List<AttendanceHistoryItem> attendanceList;
    private List<AttendanceHistoryItem> attendanceListFull;

    public AttendanceHistoryAdapter(List<AttendanceHistoryItem> attendanceList) {
        this.attendanceList = attendanceList;
        this.attendanceListFull = new ArrayList<>(attendanceList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceHistoryItem item = attendanceList.get(position);

        // Parse date để hiển thị
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(item.getDate());

            if (date != null) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

                holder.tvDay.setText(dayFormat.format(date));
                holder.tvMonth.setText(monthFormat.format(date).toUpperCase());
            }
        } catch (ParseException e) {
            holder.tvDay.setText("--");
            holder.tvMonth.setText("---");
        }

        holder.tvTime.setText(item.getTime());
        holder.tvClassName.setText(item.getClassName());
        holder.tvSubject.setText(item.getSubject());
        holder.tvFullDate.setText(item.getDate());

        // Set status
        if (item.isStatus()) {
            holder.tvStatus.setText("Present");
            holder.tvStatus.setTextColor(Color.WHITE);
            setStatusBackground(holder.tvStatus, "#4CAF50"); // Green
        } else {
            holder.tvStatus.setText("Absent");
            holder.tvStatus.setTextColor(Color.WHITE);
            setStatusBackground(holder.tvStatus, "#F44336"); // Red
        }
    }

    private void setStatusBackground(TextView textView, String colorHex) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(colorHex));
        drawable.setCornerRadius(20f);
        textView.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    // Filter methods
    public void filterAll() {
        attendanceList.clear();
        attendanceList.addAll(attendanceListFull);
        notifyDataSetChanged();
    }

    public void filterPresent() {
        attendanceList.clear();
        for (AttendanceHistoryItem item : attendanceListFull) {
            if (item.isStatus()) {
                attendanceList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void filterAbsent() {
        attendanceList.clear();
        for (AttendanceHistoryItem item : attendanceListFull) {
            if (!item.isStatus()) {
                attendanceList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvMonth, tvTime;
        TextView tvClassName, tvSubject, tvFullDate;
        TextView tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvFullDate = itemView.findViewById(R.id.tvFullDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
