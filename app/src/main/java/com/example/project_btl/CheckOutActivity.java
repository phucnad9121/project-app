package com.example.project_btl;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.home.MainHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckOutActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvDiaChi, tvTotal;
    private RecyclerView rcvSanPham;
    private RadioGroup radioThanhToan;
    private Button btnDatHang;
    private List<ProductModel> selectedItems = new ArrayList<>();
    private long totalAmount = 0;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CheckoutAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvTotal = findViewById(R.id.tvTotal);
        rcvSanPham = findViewById(R.id.rcvSanPham);
        radioThanhToan = findViewById(R.id.radioThanhToan);
        btnDatHang = findViewById(R.id.btnDatHang);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        selectedItems = (List<ProductModel>) getIntent().getSerializableExtra("selectedItems");
        if (selectedItems == null) selectedItems = new ArrayList<>();

        adapter = new CheckoutAdapter(selectedItems);
        rcvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rcvSanPham.setAdapter(adapter);

        for (ProductModel p : selectedItems) totalAmount += p.getPrice() * p.getQuantity();
        tvTotal.setText("Tổng thanh toán: " + formatVnd(totalAmount));

        // Tải thông tin người dùng (tên, sđt, địa chỉ) một lần duy nhất
        loadUserData();

        btnDatHang.setOnClickListener(v -> placeOrder());
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            // Hãy chắc chắn tên trường trong Firestore của bạn là "name", "phone", "address"
                            String name = doc.getString("name");
                            String phone = doc.getString("phone");
                            String address = doc.getString("address");

                            if (name != null) tvName.setText(name);
                            if (phone != null) tvPhone.setText(phone);
                            if (address != null) tvDiaChi.setText(address);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CheckOutActivity.this, "Lỗi khi tải thông tin người dùng.", Toast.LENGTH_SHORT).show();
                        Log.w("CheckOutActivity", "Lỗi khi tải thông tin người dùng", e);
                    });
        }
    }

    private void placeOrder() {
        int checkedId = radioThanhToan.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rb = findViewById(checkedId);
        String paymentMethod = rb.getText().toString();
        String address = tvDiaChi.getText().toString();

        OrderManagerFirebase.getInstance().saveOrder(selectedItems, totalAmount, paymentMethod, address,
                new OrderManagerFirebase.OnOrderSavedListener() {
                    @Override
                    public void onSuccess() {
                        for (ProductModel p : selectedItems) {
                            CartManagerFirebase.getInstance().removeFromCart(p.getId());
                        }
                        showSuccessDialog();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(CheckOutActivity.this, "Lưu đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_payment_success, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btnCloseSuccess).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "₫";
    }
}