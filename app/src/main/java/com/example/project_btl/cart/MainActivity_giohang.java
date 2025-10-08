package com.example.project_btl.cart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.ProductModel;
import com.example.project_btl.profile.ProfileActivity;
import com.example.project_btl.R;
import com.example.project_btl.home.MainHomeActivity;
import com.example.project_btl.notification.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity_giohang extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CheckBox checkboxSelectAll;
    private Button btnDeleteSelected, btnBuySelected, btnApplyCoupon;
    private EditText edtCoupon;
    private TextView tvSubtotal, tvDiscount, tvTotal;

    private final List<ProductModel> items = new ArrayList<>();
    private Giohang_Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giohang);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnBuySelected = findViewById(R.id.btnBuySelected);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        edtCoupon = findViewById(R.id.edtCoupon);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotal = findViewById(R.id.tvTotal);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) return true;
            else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1️⃣ Lấy sản phẩm mới thêm từ ChiTietSPActivity và thêm vào cart manager
        ProductModel newProduct = (ProductModel) getIntent().getSerializableExtra("cart_product");
        if (newProduct != null) {
            CartManager.getInstance().addToCart(newProduct, newProduct.getSelectedSize(), newProduct.getQuantity());
        }
        // 2️⃣ Đồng bộ items với CartManager
        items.clear();
        items.addAll(CartManager.getInstance().getCartList());

        // 3️⃣ Khởi tạo adapter
        adapter = new Giohang_Adapter(items, new Giohang_Adapter.Listener() {
            @Override
            public void onItemsChanged() { recalcTotal(); }
            @Override
            public void onItemRemoved(int position) {
                ProductModel removed = items.get(position);
                CartManager.getInstance().removeFromCart(removed); // xóa khỏi CartManager
                items.remove(position); // Xóa khỏi list local
                adapter.notifyItemRemoved(position);
                recalcTotal();
            }
        });
        recyclerView.setAdapter(adapter);

        // Checkbox chọn tất cả
        checkboxSelectAll.setOnCheckedChangeListener((b, c) -> {
            for (ProductModel it : items) it.setChecked(c);
            adapter.notifyDataSetChanged();
            recalcTotal();
        });

        btnDeleteSelected.setOnClickListener(v -> {
            Iterator<ProductModel> it = items.iterator();
            while (it.hasNext()) {
                ProductModel p = it.next();
                if (p.isChecked()) {
                    CartManager.getInstance().removeFromCart(p); // xóa khỏi CartManager
                    it.remove(); // xóa khỏi list local
                }
            }
            adapter.notifyDataSetChanged();
            recalcTotal();
        });

        btnBuySelected.setOnClickListener(v -> showCheckoutDialog());
        btnApplyCoupon.setOnClickListener(v -> recalcTotal());

        recalcTotal();
    }

    private void recalcTotal() {
        long subtotal = 0;
        for (ProductModel it : items) if (it.isChecked()) subtotal += it.getPrice() * it.getQuantity();
        long discount = 0;
        String code = edtCoupon.getText() == null ? "" : edtCoupon.getText().toString().trim();
        if (!TextUtils.isEmpty(code)) {
            if ("GIAM10%".equalsIgnoreCase(code)) discount = Math.round(subtotal * 0.10);
            else if ("GIAM50$".equalsIgnoreCase(code)) discount = Math.min(50, subtotal);
        }
        long total = Math.max(0, subtotal - discount);
        tvSubtotal.setText("Tổng tiền hàng: " + formatVnd(subtotal));
        tvDiscount.setText("Áp dụng Vocher giảm giá: " + formatVnd(discount));
        tvTotal.setText("Tổng Thanh Toán: " + formatVnd(total));
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "$";
    }

    private void showCheckoutDialog() {
        long currentTotal = 0;
        for (ProductModel it : items) if (it.isChecked()) currentTotal += it.getPrice() * it.getQuantity();
        if (currentTotal == 0) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkout, null);
        TextView tvDialogTotal = dialogView.findViewById(R.id.tvDialogTotal);
        RadioGroup rg = dialogView.findViewById(R.id.rgPayment);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmPayment);

        tvDialogTotal.setText("Tổng thanh toán: " + formatVnd(currentTotal));

        android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnConfirm.setOnClickListener(v -> {
            int checkedId = rg.getCheckedRadioButtonId();
            if (checkedId == View.NO_ID) {
                Toast.makeText(this, "Chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            dlg.dismiss();
            showSuccessDialog();
        });

        dlg.show();
    }

    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_success, null);
        Button btnClose = dialogView.findViewById(R.id.btnCloseSuccess);
        android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        btnClose.setOnClickListener(v -> dlg.dismiss());
        dlg.show();
    }
}
