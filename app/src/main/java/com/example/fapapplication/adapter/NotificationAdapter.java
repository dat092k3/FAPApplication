package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> implements Filterable {

    private List<Notification> list;
    private List<Notification> listFull; // bản đầy đủ để filter
    private OnItemClickListener listener;

    public NotificationAdapter(List<Notification> list, OnItemClickListener listener) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
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
        Notification item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDate.setText("Date: " + item.getDate());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return notificationFilter;
    }

    private Filter notificationFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Notification> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                for (Notification n : listFull) {
                    if (n.getTitle().toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                            n.getDate().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(n);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvDate = itemView.findViewById(R.id.tvNotificationDate);
        }
    }
}
