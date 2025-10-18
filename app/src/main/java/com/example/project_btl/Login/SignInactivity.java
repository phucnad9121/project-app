package com.example.project_btl.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.AdminMainHomeActivity;
import com.example.project_btl.R;
import com.example.project_btl.home.MainHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInactivity extends AppCompatActivity {

    private Button btnLoginTab, btnSignupTab, btnSignIn, btnSignUp, btnRecoverPassword;
    private ViewFlipper viewFlipper;
    private EditText edtEmailLG, edtPasswordLG;
    private EditText edtNameRG, edtEmailRG, edtPasswordRG;
    private EditText edtForgotEmail;
    private CheckBox chkRemember;
    private TextView tvForgot, txtBackToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_register);

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Ánh xạ view
        btnLoginTab = findViewById(R.id.btnLoginTab);
        btnSignupTab = findViewById(R.id.btnSignupTab);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);

        edtEmailLG = findViewById(R.id.edtUsernameLG);
        edtPasswordLG = findViewById(R.id.edtPasswordLG);

        edtNameRG = findViewById(R.id.edtNameRG);
        edtEmailRG = findViewById(R.id.edtEmailRG);
        edtPasswordRG = findViewById(R.id.edtPasswordRG);

        edtForgotEmail = findViewById(R.id.edtForgotEmail);
        chkRemember = findViewById(R.id.chkRemember);
        tvForgot = findViewById(R.id.tvForgot);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        viewFlipper = findViewById(R.id.viewFlipper);

        // Load thông tin đã lưu nếu có
        loadRememberedUser();

        viewFlipper.setDisplayedChild(0);
        highlightLoginTab();

        // Chuyển tab
        btnLoginTab.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(0);
            highlightLoginTab();
        });
        btnSignupTab.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(1);
            highlightSignupTab();
        });

        boolean openSignup = getIntent().getBooleanExtra("openSignup", false);
        if (openSignup) {
            btnSignupTab.performClick(); // Tự động click vào tab Đăng ký
        }

        tvForgot.setOnClickListener(v -> viewFlipper.setDisplayedChild(2));
        txtBackToLogin.setOnClickListener(v -> viewFlipper.setDisplayedChild(0));

        // Xử lý nút
        btnSignIn.setOnClickListener(v -> login());
        btnSignUp.setOnClickListener(v -> signup());
        btnRecoverPassword.setOnClickListener(v -> recoverPassword());
    }

    // ================= METHODS =================

    // Lưu mật khẩu nếu checked
    private void saveLogin(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void clearLogin() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void loadRememberedUser() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember", false);
        if(remember) {
            edtEmailLG.setText(prefs.getString("email", ""));
            edtPasswordLG.setText(prefs.getString("password", ""));
            chkRemember.setChecked(true);
        }
    }

    private void login() {
        String email = edtEmailLG.getText().toString().trim();
        String pass = edtPasswordLG.getText().toString().trim();

        if(email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if(chkRemember.isChecked()) saveLogin(email, pass);
                        else clearLogin();

                        // --- THAY ĐỔI TỪ ĐÂY ---
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            // Truy vấn Firestore để lấy thông tin role
                            firestore.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String userRole = documentSnapshot.getString("role");
                                            if ("admin".equals(userRole)) {
                                                // Nếu là admin, chuyển sang màn hình Admin (bạn cần tạo Activity này)
                                                Toast.makeText(this, "Đăng nhập với quyền Admin thành công", Toast.LENGTH_SHORT).show();
                                                // Ví dụ: startActivity(new Intent(SignInactivity.this, AdminDashboardActivity.class));
                                                // Tạm thời vẫn vào Home để demo
                                                Intent intent = new Intent(SignInactivity.this, AdminMainHomeActivity.class);
                                                intent.putExtra("USER_ROLE", userRole); // Truyền vai trò "admin"
                                                startActivity(intent);
                                            } else {
                                                // Nếu là user thường, chuyển sang màn hình Home
                                                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignInactivity.this, MainHomeActivity.class);
                                                intent.putExtra("USER_ROLE", userRole); // Truyền vai trò "user"
                                                startActivity(intent);
                                            }
                                            finish(); // Đóng màn hình đăng nhập
                                        } else {
                                            // Không tìm thấy thông tin user trong Firestore (ít xảy ra nếu đăng ký đúng)
                                            Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Lỗi khi đọc Firestore
                                        Toast.makeText(this, "Lỗi khi kiểm tra quyền: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            // Lỗi không lấy được user sau khi đăng nhập (hiếm khi xảy ra)
                            Toast.makeText(this, "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
                        }

//                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(SignInactivity.this, MainHomeActivity.class));
//                        finish();

                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signup() {
        String name = edtNameRG.getText().toString().trim();
        String email = edtEmailRG.getText().toString().trim();
        String pass = edtPasswordRG.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if(user != null) {
                            String uid = user.getUid();
                            Map<String,Object> data = new HashMap<>();
                            data.put("name", name);
                            data.put("email", email);
                            data.put("role", "user"); // Mặc định là user

                            firestore.collection("users").document(uid)
                                    .set(data)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Lưu dữ liệu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void recoverPassword() {
        String email = edtForgotEmail.getText().toString().trim();
        if(email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Email reset mật khẩu đã được gửi", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gửi email thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void highlightLoginTab() {
        btnLoginTab.setBackgroundTintList(getColorStateList(R.color.darkBlue));
        btnSignupTab.setBackgroundTintList(getColorStateList(R.color.darkGrey));
    }

    private void highlightSignupTab() {
        btnSignupTab.setBackgroundTintList(getColorStateList(R.color.darkBlue));
        btnLoginTab.setBackgroundTintList(getColorStateList(R.color.darkGrey));
    }
}
