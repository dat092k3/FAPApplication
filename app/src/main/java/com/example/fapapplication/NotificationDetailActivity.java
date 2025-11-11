package com.example.fapapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle, tvDetailDate, tvDetailContent;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification); // tên layout bạn vừa tạo

        // Khởi tạo View
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        btnBack = findViewById(R.id.btnBack);

        // Lấy dữ liệu từ Intent
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String content = getIntent().getStringExtra("content");

        // Gán dữ liệu lên layout
        if (title != null) tvDetailTitle.setText(title);
        if (date != null) tvDetailDate.setText("Date: " + date);
        if (content != null && !content.isEmpty()) {
            tvDetailContent.setText(content);
        } else {
            tvDetailContent.setText("No additional content.");
        }

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
