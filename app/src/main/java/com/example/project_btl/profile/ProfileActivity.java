package com.example.project_btl.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.Login.SignInactivity;
import com.example.project_btl.R;
import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.home.MainHomeActivity;
import com.example.project_btl.notification.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private View rowEditProfile, rowNotification, rowAddress, rowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Đặt mặc định chọn profile
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

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
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        setupRow((View) findViewById(R.id.rowEditProfile),
                R.drawable.ic_bell, "Edit Profile");

        setupRow((View) findViewById(R.id.rowHistory),
                R.drawable.ic_bell, "Order History");

        setupRow((View) findViewById(R.id.rowPassword),
                R.drawable.ic_lock, "Change Password");

        findViewById(R.id.rowPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        // click Edit Profile -> mở màn 2
        findViewById(R.id.rowEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            // Xóa session hoặc token ở đây nếu có

            Intent intent = new Intent(ProfileActivity.this, SignInactivity.class);
            // Xóa tất cả activity cũ để không quay lại bằng nút back
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupRow(View row, int iconRes, String title) {
        ImageView iv = row.findViewById(R.id.ivIcon);
        TextView tv = row.findViewById(R.id.tvTitle);
        iv.setImageResource(iconRes);
        tv.setText(title);
    }

}