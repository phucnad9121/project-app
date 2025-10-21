package com.example.project_btl.notification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.profile.ProfileActivity;
import com.example.project_btl.R;
import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.home.MainHomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Ẩn ActionBar cho gọn
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Khởi tạo RecyclerView
        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Danh sách chứa thông báo
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        // Set delete listener for all users (both admin and regular users)
        adapter.setOnDeleteListener((notification, position) -> {
            showDeleteConfirmationDialog(notification, position);
        });

        // 🔹 Load thông báo thật từ Firestore (theo user đang đăng nhập)
        NotificationManagerFirebase.getInstance().loadNotifications(list -> {
            notificationList.clear();
            notificationList.addAll(list);
            adapter.notifyDataSetChanged();
        });

        // 🔹 Thanh điều hướng dưới cùng
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, MainActivity_giohang.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_notifications) {
                // Nếu đang ở trang này thì không cần reload
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private void showDeleteConfirmationDialog(NotificationModel notification, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteNotification(notification, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNotification(NotificationModel notification, int position) {
        if (notification.getDocumentId() != null) {
            NotificationManagerFirebase.getInstance().deleteNotification(notification.getDocumentId(), (success, message) -> {
                if (success) {
                    // Remove from local list and update UI
                    notificationList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Thông báo đã được xóa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi xóa thông báo: " + message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}