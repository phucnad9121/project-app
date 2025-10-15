package com.example.project_btl.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.R;
import com.example.project_btl.notification.NotificationManagerFirebase;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etPassOld, etPassNew, etPassNewAgain;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formchangepassword);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        etPassOld = findViewById(R.id.etPassOld);
        etPassNew = findViewById(R.id.etPassNew);
        etPassNewAgain = findViewById(R.id.etPassNewAgain);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnSave).setOnClickListener(v -> changePassword());
    }
    //Chỗ này là đổi mật khẩu
    private void changePassword() {
        String oldPass = Objects.requireNonNull(etPassOld.getText()).toString().trim();
        String newPass = Objects.requireNonNull(etPassNew.getText()).toString().trim();
        String confirmPass = Objects.requireNonNull(etPassNewAgain.getText()).toString().trim();

        // Kiểm tra dữ liệu mấy gà nhâpj vào có hợp lệ không
        if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "Không tìm thấy người dùng đang đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPass);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    //  Đẩy mật khẩu mới lên
                    currentUser.updatePassword(newPass)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Thay đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                                // 🟢 Gửi thông báo đổi mật khẩu
                                NotificationManagerFirebase.getInstance()
                                        .addNotification("Đổi mật khẩu thành công!", "security", R.drawable.key);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi khi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show());
    }
}