package com.example.project_btl;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    private final List<Sp_giohang> items = new ArrayList<>();
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

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);


        // Đặt mặc định chọn giỏ hàng
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) {
                // Đang ở giỏ hàng
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SignInactivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        seedData();
        adapter = new Giohang_Adapter(items, new Giohang_Adapter.Listener() {
            @Override public void onItemsChanged() { recalcTotal(); }
            @Override public void onItemRemoved(int position) { recalcTotal(); }
        });
        recyclerView.setAdapter(adapter);

        checkboxSelectAll.setOnCheckedChangeListener((b, c) -> {
            for (Sp_giohang it : items) it.setChecked(c);
            adapter.notifyDataSetChanged();
            recalcTotal();
        });

        btnDeleteSelected.setOnClickListener(v -> {
            Iterator<Sp_giohang> it = items.iterator();
            while (it.hasNext()) if (it.next().isChecked()) it.remove();
            adapter.notifyDataSetChanged();
            recalcTotal();
        });

        btnBuySelected.setOnClickListener(v -> {
            showCheckoutDialog();
        });

        btnApplyCoupon.setOnClickListener(v -> recalcTotal());

        recalcTotal();
    }

    private void seedData() {
        items.add(new Sp_giohang("1", "Máy xay", "Mô tả 1", 100000, R.drawable.blender1, 1));
        items.add(new Sp_giohang("2", "Máy xay Pro", "Mô tả 2", 200000, R.drawable.blender2, 2));
        items.add(new Sp_giohang("3", "Máy xay Max", "Mô tả 3", 300000, R.drawable.blender3, 1));
    }

    private void recalcTotal() {
        long subtotal = 0;
        for (Sp_giohang it : items) if (it.isChecked()) subtotal += it.getPrice() * it.getQuantity();
        long discount = 0;
        String code = edtCoupon.getText() == null ? "" : edtCoupon.getText().toString().trim();
        if (!TextUtils.isEmpty(code)) {
            if ("GIAM10".equalsIgnoreCase(code)) discount = Math.round(subtotal * 0.10);
            else if ("GIAM50K".equalsIgnoreCase(code)) discount = Math.min(50000, subtotal);
        }
        long total = Math.max(0, subtotal - discount);
        tvSubtotal.setText("Tạm tính: " + formatVnd(subtotal));
        tvDiscount.setText("Giảm: " + formatVnd(discount));
        tvTotal.setText("Tổng: " + formatVnd(total));
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "đ";
    }

    // Hiển thị dialog thanh toán đơn giản
    private void showCheckoutDialog() {
        long currentTotal = 0;
        for (Sp_giohang it : items) if (it.isChecked()) currentTotal += it.getPrice() * it.getQuantity();
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


