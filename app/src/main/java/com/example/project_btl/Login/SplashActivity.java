package com.example.project_btl.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.R;

public class SplashActivity extends AppCompatActivity {

    TextView txtRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View btnSignIn = findViewById(R.id.btnSignIn);
        txtRegister = findViewById(R.id.txtRegister);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                Intent intent = new Intent(SplashActivity.this, SignInactivity.class);
                startActivity(intent);
            });
        }
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, SignInactivity.class);

            // Truyền "extra" để LoginActivity biết mở tab Đăng ký
            intent.putExtra("openSignup", true);
            startActivity(intent);
        });
    }
}
