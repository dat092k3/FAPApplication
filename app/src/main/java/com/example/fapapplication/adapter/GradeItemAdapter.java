package com.example.fapapplication.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.GradeItem;

import java.util.List;

public class GradeItemAdapter extends RecyclerView.Adapter<GradeItemAdapter.GradeItemViewHolder> {

    private List<GradeItem> gradeItems;
    private OnGradeItemChangeListener changeListener;

    public interface OnGradeItemChangeListener {
        void onItemRemoved(int position);
        void onItemChanged();
    }

    public GradeItemAdapter(List<GradeItem> gradeItems) {
        this.gradeItems = gradeItems;
    }

    public void setOnGradeItemChangeListener(OnGradeItemChangeListener listener) {
        this.changeListener = listener;
    }

    @NonNull
    @Override
    public GradeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade_item, parent, false);
        return new GradeItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeItemViewHolder holder, int position) {
        GradeItem item = gradeItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return gradeItems.size();
    }

    class GradeItemViewHolder extends RecyclerView.ViewHolder {
        EditText etItemName;
        EditText etWeight;
        ImageButton btnRemoveItem;

        public GradeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            etItemName = itemView.findViewById(R.id.etItemName);
            etWeight = itemView.findViewById(R.id.etWeight);
            btnRemoveItem = itemView.findViewById(R.id.btnRemoveItem);
        }

        void bind(GradeItem item, int position) {
            etItemName.removeTextChangedListener((TextWatcher) etItemName.getTag());
            etWeight.removeTextChangedListener((TextWatcher) etWeight.getTag());

            etItemName.setText(item.getItemName());
            etWeight.setText(String.valueOf(item.getWeight()));

            TextWatcher nameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setItemName(s.toString());
                    if (changeListener != null) {
                        changeListener.onItemChanged();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            TextWatcher weightWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        double weight = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        item.setWeight(weight);
                        if (changeListener != null) {
                            changeListener.onItemChanged();
                        }
                    } catch (NumberFormatException e) {
                        item.setWeight(0);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            etItemName.addTextChangedListener(nameWatcher);
            etWeight.addTextChangedListener(weightWatcher);

            etItemName.setTag(nameWatcher);
            etWeight.setTag(weightWatcher);

            // Auto-clear "0" hoặc "0.0" khi focus vào weight field
            etWeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    String currentText = etWeight.getText().toString().trim();
                    // Nếu là "0" hoặc "0.0" thì xóa để user nhập dễ hơn
                    if (currentText.equals("0") || currentText.equals("0.0")) {
                        etWeight.setText("");
                    }
                    // Select all text để user có thể ghi đè luôn
                    etWeight.selectAll();
                } else {
                    // Khi mất focus, nếu rỗng thì set lại về 0
                    String currentText = etWeight.getText().toString().trim();
                    if (currentText.isEmpty()) {
                        etWeight.setText("0.0");
                    }
                }
            });

            btnRemoveItem.setOnClickListener(v -> {
                if (changeListener != null) {
                    changeListener.onItemRemoved(getAdapterPosition());
                }
            });
        }
    }
}