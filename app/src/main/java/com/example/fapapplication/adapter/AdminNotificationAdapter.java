package com.example.fapapplication.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.Notification;

import java.util.List;

public class AdminNotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Notification notification);
        void onDelete(Notification notification);
    }

    public NotificationAdapter(List<Notification> notificationList, OnItemClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification noti = notificationList.get(position);
        holder.title.setText(noti.getTitle());
        holder.message.setText(Html.fromHtml(noti.getMessage())); // Giữ format HTML
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(noti));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(noti));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.titleInput);
            message = view.findViewById(R.id.messageEditor);
            btnEdit = view.findViewById(R.id.);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}

