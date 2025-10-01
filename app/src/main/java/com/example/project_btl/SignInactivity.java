package com.example.project_btl;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignInactivity extends AppCompatActivity {
    private Button btnLoginTab, btnSignupTab, btnSignIn;
    private ViewFlipper viewFlipper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // dùng layout đúng với file XML bạn gửi (signin_register.xml)
        setContentView(R.layout.signin_register);
        if (getSupportActionBar() != null) {

            // Ẩn ActionBar (nếu có)
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }


            // ánh xạ view
            btnLoginTab = findViewById(R.id.btnLoginTab);
            btnSignupTab = findViewById(R.id.btnSignupTab);
            btnSignIn = findViewById(R.id.btnSignIn);
            viewFlipper = findViewById(R.id.viewFlipper);

            if (btnSignIn != null) {
                btnSignIn.setOnClickListener(v -> {
                    Intent intent = new Intent(SignInactivity.this, MainHomeActivity.class);
                    startActivity(intent);
                });
            }

            // mặc định hiển thị Login
            viewFlipper.setDisplayedChild(0);
            highlightLoginTab();

            // sự kiện khi bấm "Đăng nhập"
            btnLoginTab.setOnClickListener(v -> {
                viewFlipper.setDisplayedChild(0); // Hiện form login
                highlightLoginTab();
            });

            // sự kiện khi bấm "Đăng ký"
            btnSignupTab.setOnClickListener(v -> {
                viewFlipper.setDisplayedChild(1); // Hiện form sign up
                highlightSignupTab();
            });
        }
    }

        private void highlightLoginTab() {
            btnLoginTab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D47A1"))); // xanh đậm
            btnSignupTab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#757575"))); // xám
        }

        private void highlightSignupTab() {
            btnSignupTab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D47A1"))); // xanh đậm
            btnLoginTab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#757575"))); // xám
        }

    }