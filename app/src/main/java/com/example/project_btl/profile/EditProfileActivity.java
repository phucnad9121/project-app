package com.example.project_btl.profile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.R;
import com.example.project_btl.notification.NotificationManagerFirebase;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        spGender = findViewById(R.id.spGender);

        // Khởi tạo spinner giới tính
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Male","Female","Other"});
        spGender.setAdapter(genderAdapter);

        // Tìm nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Lấy thông tin user hiện tại
        // Lấy user hiện đang đăng nhập (nếu có)
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            loadUserData(); // tải thông tin của user từ Firestore và điền vào các ô text trong form.
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validate()) {
                saveUserData(); // lưu lên firestore
            }
        });
    }

    private void loadUserData() {
        // Lấy tham chiếu đến tài liệu người dùng trong Firestore
        DocumentReference docRef = firestore.collection("users").document(userId); // truy cập tới users (nơi lưu thông tin tất cả)
        docRef.get() // gửi yêu cầu đọc dữ liệu đến Firestore, lấy thông tin của document đó.
                // documentSnapshot: bản sao của document vừa đọc được
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // gán dữ liệu từ firestore lên giao diện
                        etName.setText(documentSnapshot.getString("name"));
                        etUsername.setText(documentSnapshot.getString("username"));
                        etPhone.setText(documentSnapshot.getString("phone"));
                        etEmail.setText(documentSnapshot.getString("email"));
                        etAddress.setText(documentSnapshot.getString("address"));

                        // gán giá trị giới tính
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
                // Xử lí lỗi
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserData() {
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String address = Objects.requireNonNull(etAddress.getText()).toString().trim();
        String gender = spGender.getSelectedItem().toString();

        // Gom dữ liệu thành 1 map dạng key-value rồi gửi lên firestore (key: trường mới, value: gtri vừa nhập)
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("username", username);
        updatedData.put("phone", phone);
        updatedData.put("email", email);
        updatedData.put("address", address);
        updatedData.put("gender", gender);

        // Cập nhật dữ liệu lên firestore
        firestore.collection("users").document(userId)
                .update(updatedData)
                // được gọi khi cập nhật thành công
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Gửi thông báo hồ sơ đã cập nhật thành công
                    NotificationManagerFirebase.getInstance().addNotification("Cập nhật hồ sơ thành công!", "profile", R.drawable.user);

                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // thông báo khi người dùng cập nhật hồ sơ
    private void addProfileUpdateNotification() {
        if (userId == null) return;

        Map<String, Object> noti = new HashMap<>();
        // ghi nội dung thông báo
        noti.put("message", "Cập nhật hồ sơ thành công");
        // ghi thời gian gửi thông báo
        noti.put("time", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        // ghi biểu tượng gửi thông báo
        noti.put("icon", "profile_update");

        // thêm thông báo vào firestore
        firestore.collection("users")
                // chọn tài liệu người dùng hiện tại
                .document(userId)
                .collection("notifications")
                // thêm 1 doc mới vào noti
                .add(noti)
                .addOnSuccessListener(doc -> {})
                .addOnFailureListener(e -> {});
    }

    // kiểm tra dữ liệu người dùng nhập
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