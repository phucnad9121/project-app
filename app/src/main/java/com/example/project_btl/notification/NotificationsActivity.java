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

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Tạo dữ liệu giả lập
        notificationList = new ArrayList<>();
        notificationList.add(new NotificationModel("Đơn hàng #1234 đã được xác nhận", "5 phút trước", R.drawable.cart_icon));
        notificationList.add(new NotificationModel("Giảm giá 20% cho giày Yonex", "1 giờ trước", R.drawable.notification));
        notificationList.add(new NotificationModel("Bạn quên mua 2 sản phẩm trong giỏ hàng", "Hôm qua", R.drawable.notification));

        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Đặt mặc định chọn profile
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
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
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