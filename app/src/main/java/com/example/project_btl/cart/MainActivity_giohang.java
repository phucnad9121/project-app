package com.example.project_btl.cart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.AdminActivity;
import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.example.project_btl.CheckOut.CheckOutActivity;
import com.example.project_btl.home.MainHomeActivity;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity_giohang extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CheckBox checkboxSelectAll;
    private Button btnDeleteSelected, btnBuySelected, btnApplyCoupon;
    private EditText edtCoupon;
    private TextView tvSubtotal, tvDiscount, tvTotal;
    private List<ProductModel> items = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private String role;
    private Giohang_Adapter adapter;
    private CartManagerFirebase cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giohang);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerViewProducts);
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnBuySelected = findViewById(R.id.btnBuySelected);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        edtCoupon = findViewById(R.id.edtCoupon);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotal = findViewById(R.id.tvTotal);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        // them khoi tao
        cartManager = CartManagerFirebase.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        role = getIntent().getStringExtra("USER_ROLE");
        if (role == null) role = "user";

        adapter = new Giohang_Adapter(items, new Giohang_Adapter.Listener() {
            @Override
            public void onItemsChanged() {
                recalcTotal();
            }

            @Override
            public void onItemRemoved(int position) {
                if (position >= 0 && position < items.size()) {
                    String productId = items.get(position).getId();
                    cartManager.removeFromCart(productId);
                }
            }

            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                // Code mới: Lấy ID và gọi CartManager để cập nhật
                if (position >= 0 && position < items.size()) {
                    String productId = items.get(position).getId();
                    cartManager.updateQuantity(productId, newQuantity);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        loadCartFromFirebase();

        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (ProductModel it : items) it.setChecked(isChecked);
            adapter.notifyDataSetChanged();
            recalcTotal();
        });

        btnDeleteSelected.setOnClickListener(v -> {
            List<ProductModel> toDelete = new ArrayList<>();
            for (ProductModel p : items) {
                if (p.isChecked()) {
                    toDelete.add(p);
                }
            }

            for (ProductModel p : toDelete) {
                if (p.getId() != null && !p.getId().isEmpty()) {
                    cartManager.removeFromCart(p.getId());
                }
            }
        });

        btnBuySelected.setOnClickListener(v -> proceedToCheckout());
        btnApplyCoupon.setOnClickListener(v -> recalcTotal());

        // menu , chuyen tab menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, MainHomeActivity.class);
            } else if (id == R.id.nav_notifications) {
                intent = new Intent(this, NotificationsActivity.class);
            } else if (id == R.id.nav_profile) {
                if ("admin".equals(role)) {
                    intent = new Intent(this, AdminActivity.class);
                } else {
                    intent = new Intent(this, ProfileActivity.class);
                }
            } else if (id == R.id.nav_cart) {
                return true;
            }

            if (intent != null) {
                intent.putExtra("USER_ROLE", role);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }

    private void loadCartFromFirebase() {
        db.collection("users").document(userId)
                .collection("cartItems")
                .addSnapshotListener((@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) -> {
                    if (error != null) return;
                    items.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ProductModel p = new ProductModel();
                            p.setId(doc.getId());
                            p.setName(doc.getString("name"));
                            p.setPrice(doc.getLong("price"));
                            p.setImageUrl(doc.getString("imageUrl"));
                            p.setRating(0f);
                            p.setDescription("");
                            p.setMoreInfor("");
                            Long qty = doc.getLong("quantity");
                            p.setQuantity(qty != null ? qty.intValue() : 1);
                            p.setSelectedSize(doc.getString("selectedSize"));
                            p.setType(doc.getString("type"));
                            p.setChecked(false);
                            items.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    recalcTotal();
                });
    }

    private void recalcTotal() {
        long subtotal = 0;
        for (ProductModel it : items)
            if (it.isChecked()) subtotal += it.getPrice() * it.getQuantity();

        long discount = 0;
        String code = edtCoupon.getText() == null ? "" : edtCoupon.getText().toString().trim();
        if (!TextUtils.isEmpty(code)) {
            if ("GIAM10%".equalsIgnoreCase(code)) discount = Math.round(subtotal * 0.10);
            else if ("GIAM50000".equalsIgnoreCase(code)) discount = Math.min(50000, subtotal);
        }
        long total = Math.max(0, subtotal - discount);
        tvSubtotal.setText("Tổng tiền hàng: " + formatVnd(subtotal));
        tvDiscount.setText("Áp dụng Voucher: " + formatVnd(discount));
        tvTotal.setText("Tổng Thanh Toán: " + formatVnd(total));
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "₫";
    }

    private void proceedToCheckout() {
        ArrayList<ProductModel> selectedItems = new ArrayList<>();
        for (ProductModel p : items) {
            if (p.isChecked()) {
                selectedItems.add(p);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CheckOutActivity.class);
        intent.putExtra("selectedItems", selectedItems);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }
}
