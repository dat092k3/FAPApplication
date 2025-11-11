package com.example.fapapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private Context context;
    private List<Subject> subjectList;
    private List<Subject> subjectListFull; // Backup list for filtering
    private OnSubjectClickListener clickListener;
    private OnSubjectLongClickListener longClickListener;

    /**
     * Constructor cho SubjectAdapter
     * Constructor for SubjectAdapter
     *
     * @param context Context của activity
     * @param subjectList Danh sách subjects cần hiển thị
     */
    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList != null ? subjectList : new ArrayList<>();
        this.subjectListFull = new ArrayList<>(this.subjectList);
    }

    /**
     * Interface để xử lý click events
     * Interface for handling click events
     */
    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject, int position);
    }

    /**
     * Interface để xử lý long-click events
     * Interface for handling long-click events
     */
    public interface OnSubjectLongClickListener {
        boolean onSubjectLongClick(Subject subject, int position);
    }

    /**
     * Thiết lập click listener
     * Set click listener
     *
     * @param listener Click listener
     */
    public void setOnSubjectClickListener(OnSubjectClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Thiết lập long-click listener
     * Set long-click listener
     *
     * @param listener Long-click listener
     */
    public void setOnSubjectLongClickListener(OnSubjectLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        // Hiển thị subject code badge (lấy 3-4 chữ cái đầu)
        // Display subject code badge (first 3-4 letters)
        String codeBadge = subject.getSubjectCode();
        if (codeBadge != null && codeBadge.length() > 4) {
            codeBadge = codeBadge.substring(0, 4);
        }
        holder.tvSubjectCodeBadge.setText(codeBadge != null ? codeBadge.toUpperCase() : "???");

        // Thiết lập màu badge (xanh dương cho tất cả subjects)
        // Set badge color (blue for all subjects)
        holder.tvSubjectCodeBadge.setBackgroundColor(Color.parseColor("#2196F3"));

        // Hiển thị subject name và code
        // Display subject name and code
        holder.tvSubjectName.setText(subject.getSubjectName() != null ? subject.getSubjectName() : "No Name");
        holder.tvSubjectCode.setText(subject.getSubjectCode() != null ? subject.getSubjectCode() : "N/A");

        // Hiển thị assessment info (số lượng categories)
        // Display assessment info (number of categories)
        int assessmentCount = 0;
        if (subject.getAssessments() != null) {
            assessmentCount = subject.getAssessments().size();
        }

        String assessmentText;
        if (assessmentCount == 0) {
            assessmentText = "No assessments configured";
        } else if (assessmentCount == 1) {
            assessmentText = "1 assessment category";
        } else {
            assessmentText = assessmentCount + " assessment categories";
        }
        holder.tvAssessmentInfo.setText(assessmentText);

        // Hiển thị trạng thái active/inactive
        // Display active/inactive status
        if (subject.isActive()) {
            holder.ivActiveStatus.setColorFilter(Color.parseColor("#4CAF50")); // Green
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.ivActiveStatus.setColorFilter(Color.parseColor("#F44336")); // Red
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
        }

        // Hiển thị ngày tạo
        // Display created date
        String createdDate = formatTimestamp(subject.getCreatedAt());
        holder.tvCreatedAt.setText("Created: " + createdDate);

        // Thiết lập click listener
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSubjectClick(subject, position);
            }
        });

        // Thiết lập long-click listener
        // Set long-click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onSubjectLongClick(subject, position);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    /**
     * Cập nhật danh sách subjects và refresh RecyclerView
     * Update the subject list and refresh the RecyclerView
     *
     * @param newSubjectList Danh sách subjects mới
     */
    public void updateSubjectList(List<Subject> newSubjectList) {
        this.subjectList = newSubjectList != null ? newSubjectList : new ArrayList<>();
        this.subjectListFull = new ArrayList<>(this.subjectList);
        notifyDataSetChanged();
    }

    /**
     * Lọc danh sách subjects dựa trên query string
     * Filter the subject list based on query string
     *
     * @param query Search query (subject code or name)
     */
    public void filter(String query) {
        List<Subject> filteredList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Không có query, hiển thị tất cả
            // No query, show all
            filteredList.addAll(subjectListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();

            for (Subject subject : subjectListFull) {
                // Tìm kiếm trong subject code và name
                // Search in subject code and name
                boolean matchesCode = subject.getSubjectCode() != null &&
                        subject.getSubjectCode().toLowerCase().contains(lowerCaseQuery);
                boolean matchesName = subject.getSubjectName() != null &&
                        subject.getSubjectName().toLowerCase().contains(lowerCaseQuery);

                if (matchesCode || matchesName) {
                    filteredList.add(subject);
                }
            }
        }

        subjectList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * Lọc theo trạng thái active/inactive
     * Filter by active/inactive status
     *
     * @param filterType "All", "Active", or "Inactive"
     */
    public void filterByStatus(String filterType) {
        List<Subject> filteredList = new ArrayList<>();

        if (filterType == null || filterType.equals("All")) {
            filteredList.addAll(subjectListFull);
        } else if (filterType.equals("Active")) {
            for (Subject subject : subjectListFull) {
                if (subject.isActive()) {
                    filteredList.add(subject);
                }
            }
        } else if (filterType.equals("Inactive")) {
            for (Subject subject : subjectListFull) {
                if (!subject.isActive()) {
                    filteredList.add(subject);
                }
            }
        }

        subjectList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * Format timestamp thành date string
     * Format timestamp to date string
     *
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "Unknown";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * ViewHolder cho Subject items
     * ViewHolder for Subject items
     */
    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectCodeBadge;
        TextView tvSubjectName;
        TextView tvSubjectCode;
        TextView tvAssessmentInfo;
        ImageView ivActiveStatus;
        TextView tvStatus;
        TextView tvCreatedAt;

        /**
         * Constructor cho SubjectViewHolder
         * Constructor for SubjectViewHolder
         *
         * @param itemView View của list item
         */
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectCodeBadge = itemView.findViewById(R.id.tvSubjectCodeBadge);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode);
            tvAssessmentInfo = itemView.findViewById(R.id.tvAssessmentInfo);
            ivActiveStatus = itemView.findViewById(R.id.ivActiveStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}