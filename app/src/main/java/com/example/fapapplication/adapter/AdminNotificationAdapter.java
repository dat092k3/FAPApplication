package com.example.fapapplication.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Notification notification);
        void onDelete(Notification notification);
    }

    public AdminNotificationAdapter(List<Notification> notificationList, OnItemClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification noti = notificationList.get(position);
        holder.title.setText(noti.getTitle());
        holder.message.setText(Html.fromHtml(noti.getMessage(), HtmlCompat.FROM_HTML_MODE_LEGACY)); // Giữ format HTML
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.createTime.setText(String.valueOf(noti.getCreateTime()));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(noti));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(noti));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, createTime;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txtTitle);
            message = view.findViewById(R.id.txtMessage);
            createTime = view.findViewById(R.id.createTime);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}

