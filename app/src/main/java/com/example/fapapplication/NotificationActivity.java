package com.example.fapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.NotificationAdapter;
import com.example.fapapplication.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> list = new ArrayList<>();
    private TextView tvResultCount;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list); // thống nhất với layout XML bạn gửi

        recyclerView = findViewById(R.id.recyclerNotification);
        tvResultCount = findViewById(R.id.tvResultCount);
        edtSearch = findViewById(R.id.edtSearch);

        list.add(new Notification("Thông báo điểm thi kết thúc học phần lần 2 môn KRL312 học kỳ Fall 2025", "7/11/2025",""));
        list.add(new Notification("Kế hoạch Lịch đào tạo đại học hệ chính quy năm 2026", "6/11/2025",""));
        list.add(new Notification("Thông báo điểm thi giữa kỳ học phần KOR411 học kỳ Fall 2025", "5/11/2025",""));
        list.add(new Notification("Thông báo về việc đăng ký mua BHYT năm 2026", "5/11/2025",""));

        adapter = new NotificationAdapter(list, notification -> {
            // Chuyển sang DetailActivity
            Intent intent = new Intent(this, NotificationDetailActivity.class);
            intent.putExtra("title", notification.getTitle());
            intent.putExtra("date", notification.getDate());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tvResultCount.setText("Showing " + list.size() + " results");

        // Thêm lọc tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                tvResultCount.setText("Showing " + adapter.getItemCount() + " results");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
