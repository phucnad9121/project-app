package com.example.project_btl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.Login.SignInactivity;
import com.example.project_btl.profile.OrderHistoryActivity;
import com.example.project_btl.R;
import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.home.MainHomeActivity;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.profile.ChangePasswordActivity;
import com.example.project_btl.profile.EditProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private View rowEditProfile, rowNotification, rowAddress, rowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
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
                Intent intent = new Intent(this, AdminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // <-- Flag này
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        setupRow((View) findViewById(R.id.rowManagerAccount),
                R.drawable.ic_bell, "Manager Account");

        setupRow((View) findViewById(R.id.rowManagerProduce),
                R.drawable.ic_orders_history, "Manager Produce");


        findViewById(R.id.rowManagerAccount).setOnClickListener(v ->
                startActivity(new Intent(this, MainAccountManagenment.class)));

        // click Edit Profile -> mở màn 2
        findViewById(R.id.rowManagerProduce).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));



        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            // Xóa session hoặc token ở đây nếu có

            Intent intent = new Intent(AdminActivity.this, SignInactivity.class);
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