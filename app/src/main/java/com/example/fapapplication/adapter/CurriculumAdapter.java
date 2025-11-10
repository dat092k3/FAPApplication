package com.example.fapapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.R;
import com.example.fapapplication.model.Curriculum;

import java.util.List;



public class CurriculumAdapter extends RecyclerView.Adapter<CurriculumAdapter.ViewHolder> {
    private List<Curriculum> list;

    public CurriculumAdapter(List<Curriculum> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_curriculum, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Curriculum c = list.get(position);
        holder.tvId.setText(String.valueOf(c.getId()));
        holder.tvCode.setText(c.getSubjectCode());
        holder.tvTerm.setText(String.valueOf(c.getTerm()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvCode, tvTerm;
        ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvTerm = itemView.findViewById(R.id.tvTerm);
        }
    }
}
