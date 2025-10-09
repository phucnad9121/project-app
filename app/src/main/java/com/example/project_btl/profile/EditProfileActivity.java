package com.example.project_btl.profile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etUsername, etPhone, etEmail, etAddress;
    private Spinner spGender;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();  //Dùng để tạo tài khoản mật khẩu,......
        firestore = FirebaseFirestore.getInstance();//Tạo,lưu dữ, đọc , câpj nhâpj liệu người dùng


        //  Ánh xạ view
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        spGender = findViewById(R.id.spGender);

        //  Setup Spinner giới tính
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Male","Female","Other"});
        spGender.setAdapter(genderAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        //  Lấy thông tin tu8 FIREBASE
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            loadUserData();
        }

        // Lưu thay đổi
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validate()) {
                saveUserData();
            }
        });
    }

    //  Lấy dữ liệu bằng FIRESTORE
    private void loadUserData() {
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy từng trường trong document
                        etName.setText(documentSnapshot.getString("name"));
                        etUsername.setText(documentSnapshot.getString("username"));
                        etPhone.setText(documentSnapshot.getString("phone"));
                        etEmail.setText(documentSnapshot.getString("email"));
                        etAddress.setText(documentSnapshot.getString("address"));

                        String gender = documentSnapshot.getString("gender");
                        if (gender != null) {
                            switch (gender) {
                                case "Male": spGender.setSelection(0); break;
                                case "Female": spGender.setSelection(1); break;
                                default: spGender.setSelection(2); break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Cái này là đẩy dữ liệu lên data
    private void saveUserData() {
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String address = Objects.requireNonNull(etAddress.getText()).toString().trim();
        String gender = spGender.getSelectedItem().toString();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("username", username);
        updatedData.put("phone", phone);
        updatedData.put("email", email);
        updatedData.put("address", address);
        updatedData.put("gender", gender);

        firestore.collection("users").document(userId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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