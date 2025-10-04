package com.example.project_btl.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View btnSignIn = findViewById(R.id.btnSignIn);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                Intent intent = new Intent(SplashActivity.this, SignInactivity.class);
                startActivity(intent);
            });
        }
    }
}
