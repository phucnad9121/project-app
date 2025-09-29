package com.example.project_btl;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class testlogin extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button btnLoginTab, btnSignupTab;
        ViewFlipper viewFlipper;

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        btnLoginTab = findViewById(R.id.btnLoginTab);
        btnSignupTab = findViewById(R.id.btnSignupTab);
        viewFlipper = findViewById(R.id.viewFlipper);

        btnLoginTab.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(0); // Hiện form login
            btnLoginTab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6200EE"))); // tím
            btnSignupTab.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        });

        btnSignupTab.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(1); // Hiện form sign up
            btnSignupTab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6200EE")));
            btnLoginTab.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        });
    }

}
