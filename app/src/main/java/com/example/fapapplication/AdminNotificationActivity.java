package com.example.fapapplication;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fapapplication.adapter.AdminNotificationAdapter;
import com.example.fapapplication.adapter.NotificationAdapter;
import com.example.fapapplication.entity.Notification;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class AdminNotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private FloatingActionButton btnAdd;
    private AdminNotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification);

        rvNotifications = findViewById(R.id.notificationRecycler);
        btnAdd = findViewById(R.id.addNotificationBtn);
        dbRef = FirebaseDatabase.getInstance().getReference("Notifications");

        loadNotifications();

        adapter = new AdminNotificationAdapter(notificationList, new AdminNotificationAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Notification notification) {
                showAddEditDialog(notification);
            }

            @Override
            public void onDelete(Notification notification) {
                dbRef.child(notification.getId()).removeValue();
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddEditDialog(null)); // null = thêm mới

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        dbRef.orderByChild("createTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Notification n = child.getValue(Notification.class);
                    notificationList.add(0, n); // thêm lên đầu
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showAddEditDialog(@Nullable Notification noti) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_notification, null);
        builder.setView(view);

        EditText edtTitle = view.findViewById(R.id.titleInput);
        RichEditor editor = view.findViewById(R.id.messageEditor);
        Button bold = view.findViewById(R.id.boldBtn);
        Button italic = view.findViewById(R.id.italicBtn);
        Button undo = view.findViewById(R.id.undoBtn);

        bold.setOnClickListener(v -> editor.setBold());
        italic.setOnClickListener(v -> editor.setItalic());
        undo.setOnClickListener(v -> editor.undo());

        if (noti != null) {
            builder.setTitle("Cập nhật thông báo");
            edtTitle.setText(noti.getTitle());
            editor.setHtml(noti.getMessage());
        } else {
            builder.setTitle("Thêm mới thông báo");
        }


        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String title = edtTitle.getText().toString();
            String msg = Html.toHtml(new SpannableStringBuilder(editor.getHtml()));

            if (title.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            if (noti == null) { // thêm mới
                String id = dbRef.push().getKey();
                Notification newNoti = new Notification(id, title, msg, System.currentTimeMillis());
                dbRef.child(id).setValue(newNoti)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Thêm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else { // cập nhật
                noti.setTitle(title);
                noti.setMessage(msg);
                noti.setCreateTime(System.currentTimeMillis()); // cập nhật lại thời gian
                dbRef.child(noti.getId()).setValue(noti)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Huỷ", null);
        builder.show();
    }
}

