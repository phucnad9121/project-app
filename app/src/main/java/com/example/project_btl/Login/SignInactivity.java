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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.CreateDatabase;
import com.example.project_btl.R;
import com.example.project_btl.home.MainHomeActivity;

public class SignInactivity extends AppCompatActivity {

    private Button btnLoginTab, btnSignupTab, btnSignIn, btnSignUp, btnRecoverPassword;
    private ViewFlipper viewFlipper;
    private EditText edtUsernameLG, edtPasswordLG;
    private EditText edtNameRG, edtUserNameRG, edtPasswordRG, edtEmailRG;
    private EditText edtForgotUser, edtForgotName, edtForgotEmail;
    private CheckBox chkRemember;
    private TextView tvForgot, txtBackToLogin;

    private CreateDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_register);

        // Ẩn ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Khởi tạo DB
        db = new CreateDatabase(this);
        db.getWritableDatabase();

        // Ánh xạ View
        btnLoginTab = findViewById(R.id.btnLoginTab);
        btnSignupTab = findViewById(R.id.btnSignupTab);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);

        edtUsernameLG = findViewById(R.id.edtUsernameLG);
        edtPasswordLG = findViewById(R.id.edtPasswordLG);

        edtNameRG = findViewById(R.id.edtNameRG);
        edtUserNameRG = findViewById(R.id.edtUserNameRG);
        edtPasswordRG = findViewById(R.id.edtPasswordRG);
        edtEmailRG = findViewById(R.id.edtEmailRG);

        edtForgotUser = findViewById(R.id.edtForgotUser);
        edtForgotName = findViewById(R.id.edtForgotName);
        edtForgotEmail = findViewById(R.id.edtForgotEmail);

        chkRemember = findViewById(R.id.chkRemember);
        tvForgot = findViewById(R.id.tvForgot);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);

        viewFlipper = findViewById(R.id.viewFlipper);

        // Load dữ liệu nhớ mật khẩu
        loadRememberedUser();

        // Mặc định hiển thị Login
        viewFlipper.setDisplayedChild(0);
        highlightLoginTab();

        // Chuyển tab Login
        btnLoginTab.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(0);
            highlightLoginTab();
        });

        // Chuyển tab Signup
        btnSignupTab.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(1);
            highlightSignupTab();
        });

        // Chuyển tab Forgot Password
        tvForgot.setOnClickListener(v -> viewFlipper.setDisplayedChild(2));
        txtBackToLogin.setOnClickListener(v -> viewFlipper.setDisplayedChild(0));

        // Xử lý đăng nhập
        btnSignIn.setOnClickListener(v -> login());

        // Xử lý đăng ký
        btnSignUp.setOnClickListener(v -> signup());

        // Xử lý recover password
        btnRecoverPassword.setOnClickListener(v -> recoverPassword());

        // Nếu mở Signup từ Intent
        boolean openSignup = getIntent().getBooleanExtra("openSignup", false);
        if (openSignup) {
            btnSignupTab.performClick();
        }
    }

    // ===================== METHODS =====================

    private void login() {
        String user = edtUsernameLG.getText().toString().trim();
        String pass = edtPasswordLG.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.checkUser(user, pass)) {
            // Lưu nhớ mật khẩu nếu checkbox tích
            SharedPreferences.Editor editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit();
            if (chkRemember.isChecked()) {
                editor.putString("username", user);
                editor.putString("password", pass);
            } else {
                editor.clear();
            }
            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignInactivity.this, MainHomeActivity.class);
            intent.putExtra("username", user);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }

    private void signup() {
        String name = edtNameRG.getText().toString().trim();
        String user = edtUserNameRG.getText().toString().trim();
        String pass = edtPasswordRG.getText().toString().trim();
        String email = edtEmailRG.getText().toString().trim();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = db.insertUser(user, pass, name, email);
        if (success) {
            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            btnLoginTab.performClick(); // quay về Login
        } else {
            Toast.makeText(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
        }
    }

    private void recoverPassword() {
        String user = edtForgotUser.getText().toString().trim();
        String name = edtForgotName.getText().toString().trim();
        String email = edtForgotEmail.getText().toString().trim();

        if (user.isEmpty() || name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = db.getPassword(user, name, email);
        if (password != null) {
            // Hiển thị mật khẩu trong AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle("Mật khẩu của bạn")
                    .setMessage("Mật khẩu: " + password)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            viewFlipper.setDisplayedChild(0); // quay về Login
        } else {
            Toast.makeText(this, "Thông tin không đúng, không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRememberedUser() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String savedUser = prefs.getString("username", "");
        String savedPass = prefs.getString("password", "");
        if (!savedUser.isEmpty() && !savedPass.isEmpty()) {
            edtUsernameLG.setText(savedUser);
            edtPasswordLG.setText(savedPass);
            chkRemember.setChecked(true);
        }
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
