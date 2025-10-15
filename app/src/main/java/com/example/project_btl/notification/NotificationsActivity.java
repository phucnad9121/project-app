package com.example.project_btl.notification;

import android.content.Intent;
import android.os.Bundle;
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
}
