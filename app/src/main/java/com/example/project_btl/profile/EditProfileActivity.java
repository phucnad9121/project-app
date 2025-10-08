package com.example.project_btl.profile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etUsername, etPhone, etEmail;
    private Spinner spGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();


        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        spGender = findViewById(R.id.spGender);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Male","Female","Other"});
        spGender.setAdapter(genderAdapter);
        spGender.setSelection(0);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validate()) {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean validate() {
        if (Objects.requireNonNull(etName.getText()).toString().trim().isEmpty()) {
            etName.setError("Required"); return false;
        }
        if (Objects.requireNonNull(etEmail.getText()).toString().trim().isEmpty()) {
            etEmail.setError("Required"); return false;
        }
        return true;
    }



}