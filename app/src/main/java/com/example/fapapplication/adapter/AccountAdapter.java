package com.example.fapapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying user accounts in a RecyclerView.
 * Handles click and long-press interactions.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<User> accountList;
    private final Context context;

    // Interface cho sự kiện click
    public interface OnAccountClickListener {
        /**
         * Called when an account item is clicked
         */
        void onAccountClick(User user, int position);
    }

    // Interface cho sự kiện long-click
    public interface OnAccountLongClickListener {
        /**
         * Called when an account item is long-pressed
         * @return true if the event was consumed, false otherwise
         */
        boolean onAccountLongClick(User user, int position);
    }

    private OnAccountClickListener clickListener;
    private OnAccountLongClickListener longClickListener;

    /**
     * Constructor
     *
     * @param context Context của activity
     * @param accountList Danh sách user accounts
     */
    public AccountAdapter(Context context, List<User> accountList) {
        this.context = context;
        this.accountList = accountList != null ? accountList : new ArrayList<>();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        User user = accountList.get(position);

        // Hiển thị thông tin user
        String fullName = user.getFullName() != null ? user.getFullName() : "N/A";
        String email = user.getEmail() != null ? user.getEmail() : "N/A";
        String role = user.getRole() != null ? user.getRole() : "Unknown";
        String campus = user.getCampus() != null ? user.getCampus() : "N/A";

        holder.textViewName.setText(fullName);
        holder.textViewEmail.setText(email);
        holder.textViewRole.setText(role);
        holder.textViewCampus.setText(campus);

        // Hiển thị trạng thái active/inactive
        Boolean isActive = user.getIsActive();
        if (isActive != null && isActive) {
            holder.imageViewStatus.setImageResource(android.R.drawable.presence_online);
            holder.imageViewStatus.setContentDescription("Active");
        } else {
            holder.imageViewStatus.setImageResource(android.R.drawable.presence_offline);
            holder.imageViewStatus.setContentDescription("Inactive");
        }

        // Thiết lập click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAccountClick(user, position);
            }
        });

        // Thiết lập long-press listener
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onAccountLongClick(user, position);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    /**
     * Cập nhật danh sách accounts
     *
     * @param newAccountList Danh sách mới
     */
    public void updateAccountList(List<User> newAccountList) {
        this.accountList = newAccountList != null ? newAccountList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Thiết lập click listener
     */
    public void setOnAccountClickListener(OnAccountClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Thiết lập long-click listener
     */
    public void setOnAccountLongClickListener(OnAccountLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * Lấy user tại vị trí position
     */
    public User getUserAtPosition(int position) {
        if (position >= 0 && position < accountList.size()) {
            return accountList.get(position);
        }
        return null;
    }

    /**
     * ViewHolder class để giữ tham chiếu đến các views của một item
     */
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewName;
        TextView textViewEmail;
        TextView textViewRole;
        TextView textViewCampus;
        ImageView imageViewStatus;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            textViewCampus = itemView.findViewById(R.id.textViewCampus);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
        }
    }
}