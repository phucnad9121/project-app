package com.example.project_btl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View btnSignIn = findViewById(R.id.btnSignIn);
        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                Intent intent = new Intent(SplashActivity.this, SignInactivity.class);
                startActivity(intent);
            });
        }
    }
}