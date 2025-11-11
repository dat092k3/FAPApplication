package com.example.fapapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.Class;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private int lastPosition = -1;
    private Context context;
    private List<Class> classList;
    private OnClassClickListener clickListener;
    private OnClassLongClickListener longClickListener;

    public interface OnClassClickListener {
        void onClassClick(Class classItem, int position);
    }

    public interface OnClassLongClickListener {
        void onClassLongClick(Class classItem, int position);
    }

    public ClassAdapter(Context context, List<Class> classList) {
        this.context = context;
        this.classList = classList;
    }

    public void setOnClassClickListener(OnClassClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnClassLongClickListener(OnClassLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        Class classItem = classList.get(position);
        holder.bind(classItem, position);

        // Add animation cho items
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    // Update danh sách classes
    public void updateClassList(List<Class> newClassList) {
        this.classList = newClassList;
        notifyDataSetChanged();
    }

    class ClassViewHolder extends RecyclerView.ViewHolder {

        TextView tvClassName, tvSemester, tvDescription, tvStatus, tvCreatedAt;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvSemester = itemView.findViewById(R.id.tvSemester);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onClassClick(classList.get(position), position);
                    }
                }
            });

            // Long-click listener
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        longClickListener.onClassLongClick(classList.get(position), position);
                        return true;
                    }
                }
                return false;
            });
        }

        void bind(Class classItem, int position) {
            // Set class name
            tvClassName.setText(classItem.getClassName());

            // Set semester
            tvSemester.setText(classItem.getSemester());

            // Set description
            if (classItem.getDescription() != null && !classItem.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(classItem.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Set status
            if (classItem.isActive()) {
                tvStatus.setText("Active");
                tvStatus.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.holo_green_light));
            } else {
                tvStatus.setText("Inactive");
                tvStatus.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.darker_gray));
            }

            // Set created date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(classItem.getCreatedAt()));
            tvCreatedAt.setText("Created: " + dateStr);
        }
    }

    // Animation cho list items
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(
                    context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ClassViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}