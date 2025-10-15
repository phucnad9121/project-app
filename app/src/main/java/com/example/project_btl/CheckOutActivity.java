package com.example.project_btl;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

    private TextView tvName, tvPhone, tvDiaChi;
    private TextView tvTongTienHang, tvGiamGia, tvTongThanhToan, tvTotal;
    private RecyclerView rcvSanPham;
    private RadioGroup radioThanhToan;
    private Button btnDatHang;
    private ImageButton btnBack;
    private List<ProductModel> checkoutList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CheckoutAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Ánh xạ các View từ layout
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        rcvSanPham = findViewById(R.id.rcvSanPham);
        radioThanhToan = findViewById(R.id.radioThanhToan);
        btnDatHang = findViewById(R.id.btnDatHang);
        btnBack = findViewById(R.id.btnBack);

        tvTongTienHang = findViewById(R.id.tvTongTienHang);
        tvGiamGia = findViewById(R.id.tvGiamGia);
        tvTongThanhToan = findViewById(R.id.tvTongThanhToan);
        tvTotal = findViewById(R.id.tvTotal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkoutList = (ArrayList<ProductModel>) getIntent().getSerializableExtra("selectedItems");
        if (checkoutList == null || checkoutList.isEmpty()) {
            Log.e("CheckOutActivity", "Danh sách thanh toán rỗng hoặc null.");
            Toast.makeText(this, "Không có sản phẩm để thanh toán.", Toast.LENGTH_SHORT).show();
            checkoutList = new ArrayList<>();
        }

        adapter = new CheckoutAdapter(checkoutList);
        rcvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rcvSanPham.setAdapter(adapter);

        updateTotalAmount();
        loadUserData();

        btnDatHang.setOnClickListener(v -> placeOrder());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateTotalAmount() {
        double subtotal = 0;
        if (checkoutList != null) {
            for (ProductModel item : checkoutList) {
                if (item != null) {
                    subtotal += item.getPrice() * item.getQuantity();
                }
            }
        }
        Log.d("CheckOutActivity", "Tổng tiền hàng đã tính: " + subtotal);

        double discount = 0;
        double totalAmount = subtotal - discount;

        tvTongTienHang.setText(formatVnd(subtotal));
        tvGiamGia.setText("- " + formatVnd(discount));
        tvTongThanhToan.setText(formatVnd(totalAmount));
        tvTotal.setText("Tổng tiền: " + formatVnd(totalAmount));
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
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

        double totalAmountDouble = 0;
        for(ProductModel p : checkoutList) {
            totalAmountDouble += p.getPrice() * p.getQuantity();
        }

        // *** ĐÂY LÀ CHỖ SỬA LỖI ***
        // Chuyển đổi (ép kiểu) totalAmount từ double sang long trước khi gọi hàm
        long totalAmountLong = (long) totalAmountDouble;

        OrderManagerFirebase.getInstance().saveOrder(checkoutList, totalAmountLong, paymentMethod, address,
                new OrderManagerFirebase.OnOrderSavedListener() {
                    @Override
                    public void onSuccess() {
                        for (ProductModel p : checkoutList) {
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

        view.findViewById(R.id.btnCloseSuccess).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private String formatVnd(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}