package com.example.fapapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter để hiển thị danh sách user accounts trong RecyclerView.
 * Hỗ trợ click listeners và cập nhật dữ liệu.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private Context context;
    private List<User> userList;
    private OnAccountClickListener clickListener;

    /**
     * Constructor cho AccountAdapter
     *
     * @param context Context của activity
     * @param userList Danh sách users cần hiển thị
     */
    public AccountAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList != null ? userList : new ArrayList<>();
    }

    /**
     * Interface để xử lý click events
     */
    public interface OnAccountClickListener {
        void onAccountClick(User user, int position);
    }

    /**
     * Thiết lập click listener
     *
     * @param listener Click listener
     */
    public void setOnAccountClickListener(OnAccountClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        User user = userList.get(position);

        // Thiết lập avatar text (chữ cái đầu của tên)
        String avatarText = "?";
        if (user.getName() != null && !user.getName().isEmpty()) {
            avatarText = user.getName().substring(0, 1).toUpperCase();
        }
        holder.avatarTextView.setText(avatarText);

        // Thiết lập màu avatar dựa trên role
        String role = user.getRole();
        if ("Admin".equals(role)) {
            holder.avatarTextView.setBackgroundColor(Color.parseColor("#F44336")); // Red
        } else if ("Teacher".equals(role)) {
            holder.avatarTextView.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else if ("Student".equals(role)) {
            holder.avatarTextView.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
        } else {
            holder.avatarTextView.setBackgroundColor(Color.parseColor("#757575")); // Gray
        }

        // Thiết lập thông tin user
        holder.nameTextView.setText(user.getName() != null ? user.getName() : "No Name");
        holder.emailTextView.setText(user.getEmail() != null ? user.getEmail() : "No Email");
        holder.roleTextView.setText(user.getRole() != null ? user.getRole() : "No Role");

        // Hiển thị campus nếu có
        if (user.getCampus() != null && !user.getCampus().isEmpty()) {
            holder.campusTextView.setVisibility(View.VISIBLE);
            holder.campusTextView.setText(user.getCampus());
        } else {
            holder.campusTextView.setVisibility(View.GONE);
        }

        // Thiết lập click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAccountClick(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Cập nhật danh sách users và refresh RecyclerView
     *
     * @param newUserList Danh sách users mới
     */
    public void updateUsers(List<User> newUserList) {
        this.userList = newUserList != null ? newUserList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class chứa các views của từng item
     */
    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView avatarTextView;
        TextView nameTextView;
        TextView emailTextView;
        TextView roleTextView;
        TextView campusTextView;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarTextView = itemView.findViewById(R.id.avatarTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            campusTextView = itemView.findViewById(R.id.campusTextView);
        }
    }
}