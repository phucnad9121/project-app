package com.example.project_btl;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.example.project_btl.CartManagerFirebase;
import com.example.project_btl.home.MainHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckOutActivity extends AppCompatActivity {

    private TextView tvTenNguoiNhan, tvDiaChi, tvTotal;
    private RecyclerView rcvSanPham;
    private RadioGroup radioThanhToan;
    private Button btnDatHang;

    private List<ProductModel> selectedItems = new ArrayList<>();
    private long totalAmount = 0;

    private FirebaseFirestore db;
    private String userId;
    private CheckoutAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tvTenNguoiNhan = findViewById(R.id.tvTenNguoiNhan);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvTotal = findViewById(R.id.tvTotal);
        rcvSanPham = findViewById(R.id.rcvSanPham);
        radioThanhToan = findViewById(R.id.radioThanhToan);
        btnDatHang = findViewById(R.id.btnDatHang);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy danh sách sản phẩm được chọn từ Intent
        selectedItems = (List<ProductModel>) getIntent().getSerializableExtra("selectedItems");
        if (selectedItems == null) selectedItems = new ArrayList<>();

        adapter = new CheckoutAdapter(selectedItems);
        rcvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rcvSanPham.setAdapter(adapter);

        // Tính tổng tiền
        for (ProductModel p : selectedItems) totalAmount += p.getPrice() * p.getQuantity();
        tvTotal.setText("Tổng thanh toán: " + formatVnd(totalAmount));

        loadAddress();

        btnDatHang.setOnClickListener(v -> placeOrder());
    }

    private void loadAddress() {
        if (userId == null) return;
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String address = doc.getString("address");
                        String name = doc.getString("fullname");
                        if (address != null) tvDiaChi.setText(address);
                        if (name != null) tvTenNguoiNhan.setText(name);
                    }
                });
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
                        // Xóa sản phẩm khỏi giỏ hàng
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
        new AlertDialog.Builder(this)
                .setTitle("Đặt hàng thành công 🎉")
                .setMessage("Cảm ơn bạn đã mua hàng!")
                .setPositiveButton("Về trang chủ", (d, w) -> {
                    startActivity(new Intent(this, MainHomeActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "₫";
    }
}
