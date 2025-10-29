package com.example.fapapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fapapplication.R; // Thay bằng package của bạn
import java.util.List;

public class CampusAdapter extends RecyclerView.Adapter<CampusAdapter.CampusViewHolder> {

    private final List<String> campusList;
    private int selectedPosition = -1; // Vị trí của item đang được chọn
    private final Context context; // Thêm dòng này

    public CampusAdapter(Context context, List<String> campusList) { // Sửa lại dòng này
        this.context = context; // Thêm dòng này
        this.campusList = campusList;
    }

    @NonNull
    @Override
    public CampusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campus, parent, false);
        return new CampusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampusViewHolder holder, int position) {
        String campusName = campusList.get(position);
        holder.campusTextView.setText(campusName);

        // Thay đổi giao diện của item được chọn
        if (position == selectedPosition) {
            holder.campusTextView.setTextColor(Color.parseColor("#333333"));
            holder.campusTextView.setTypeface(null, Typeface.BOLD);
            holder.campusTextView.setScaleX(1.1f); // Phóng to một chút
            holder.campusTextView.setScaleY(1.1f);
        } else {
            holder.campusTextView.setTextColor(Color.parseColor("#8D8D8D"));
            holder.campusTextView.setTypeface(null, Typeface.NORMAL);
            holder.campusTextView.setScaleX(1.0f);
            holder.campusTextView.setScaleY(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return campusList.size();
    }

    // Phương thức để cập nhật item được chọn
    public void setSelectedPosition(int position) {
        int oldSelected = selectedPosition;
        selectedPosition = position;
        // Thông báo cho adapter vẽ lại item cũ và item mới
        notifyItemChanged(oldSelected);
        notifyItemChanged(selectedPosition);
    }

    public String getSelectedItem() {
        if (selectedPosition != -1) {
            return campusList.get(selectedPosition);
        }
        return null;
    }

    // Lớp ViewHolder để giữ tham chiếu đến các view của một item
    public static class CampusViewHolder extends RecyclerView.ViewHolder {
        TextView campusTextView;

        public CampusViewHolder(@NonNull View itemView) {
            super(itemView);
            campusTextView = itemView.findViewById(R.id.campusTextView);
        }
    }
}

