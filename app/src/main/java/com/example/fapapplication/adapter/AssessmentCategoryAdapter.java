package com.example.fapapplication.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.entity.AssessmentCategory;
import com.example.fapapplication.entity.GradeItem;

import java.util.List;

public class AssessmentCategoryAdapter extends RecyclerView.Adapter<AssessmentCategoryAdapter.CategoryViewHolder> {

    private List<AssessmentCategory> categories;
    private OnCategoryChangeListener changeListener;

    public interface OnCategoryChangeListener {
        void onCategoryRemoved(int position);
        void onCategoryChanged();
    }

    public AssessmentCategoryAdapter(List<AssessmentCategory> categories) {
        this.categories = categories;
    }

    public void setOnCategoryChangeListener(OnCategoryChangeListener listener) {
        this.changeListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assessment_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        AssessmentCategory category = categories.get(position);
        holder.bind(category, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        EditText etCategoryName;
        ImageButton btnRemoveCategory;
        RecyclerView recyclerViewGradeItems;
        Button btnAddGradeItem;
        GradeItemAdapter gradeItemAdapter;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            etCategoryName = itemView.findViewById(R.id.etCategoryName);
            btnRemoveCategory = itemView.findViewById(R.id.btnRemoveCategory);
            recyclerViewGradeItems = itemView.findViewById(R.id.recyclerViewGradeItems);
            btnAddGradeItem = itemView.findViewById(R.id.btnAddGradeItem);

            recyclerViewGradeItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        void bind(AssessmentCategory category, int position) {
            etCategoryName.removeTextChangedListener((TextWatcher) etCategoryName.getTag());

            etCategoryName.setText(category.getCategoryName());

            TextWatcher nameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    category.setCategoryName(s.toString());
                    if (changeListener != null) {
                        changeListener.onCategoryChanged();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            etCategoryName.addTextChangedListener(nameWatcher);
            etCategoryName.setTag(nameWatcher);

            gradeItemAdapter = new GradeItemAdapter(category.getGradeItems());
            recyclerViewGradeItems.setAdapter(gradeItemAdapter);

            gradeItemAdapter.setOnGradeItemChangeListener(new GradeItemAdapter.OnGradeItemChangeListener() {
                @Override
                public void onItemRemoved(int itemPosition) {
                    category.getGradeItems().remove(itemPosition);
                    gradeItemAdapter.notifyItemRemoved(itemPosition);
                    if (changeListener != null) {
                        changeListener.onCategoryChanged();
                    }
                }

                @Override
                public void onItemChanged() {
                    if (changeListener != null) {
                        changeListener.onCategoryChanged();
                    }
                }
            });

            btnAddGradeItem.setOnClickListener(v -> {
                GradeItem newItem = new GradeItem("", 0.0);
                category.getGradeItems().add(newItem);
                gradeItemAdapter.notifyItemInserted(category.getGradeItems().size() - 1);
                if (changeListener != null) {
                    changeListener.onCategoryChanged();
                }
            });

            btnRemoveCategory.setOnClickListener(v -> {
                if (changeListener != null) {
                    changeListener.onCategoryRemoved(getAdapterPosition());
                }
            });
        }
    }
}