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

import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.example.project_btl.Login.SignInactivity;
import com.example.project_btl.home.MainHomeActivity;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.CheckOut.CheckOutActivity;
import com.example.project_btl.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
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
    private Giohang_Adapter adapter;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giohang);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Ánh xạ view
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

        // Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else userId = "guest";

        // Adapter
        adapter = new Giohang_Adapter(items, new Giohang_Adapter.Listener() {
            @Override
            public void onItemsChanged() {
                recalcTotal();
            }

            @Override
            public void onItemRemoved(int position) {
                // (Logic này có thể không cần nếu listener của Firebase đã xử lý)
                // ProductModel removed = items.get(position);
                // db.collection("users").document(userId)
                //         .collection("cartItems")
                //         .document(removed.getId())
                //         .delete();
            }
        });
        recyclerView.setAdapter(adapter);

        // Load giỏ hàng realtime
        loadCartFromFirebase();

        // Chọn tất cả
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (ProductModel it : items) it.setChecked(isChecked);
            adapter.notifyDataSetChanged();
            recalcTotal();
        });

        // Xóa sản phẩm được chọn
        btnDeleteSelected.setOnClickListener(v -> {
            List<ProductModel> toDelete = new ArrayList<>();
            for (ProductModel p : items) {
                if (p.isChecked()) {
                    toDelete.add(p);
                }
            }

            // Xóa trên Firebase
            for (ProductModel p : toDelete) {
                if (p.getId() != null && !p.getId().isEmpty()) {
                    db.collection("users").document(userId)
                            .collection("cartItems")
                            .document(p.getId()) // Dùng ID sản phẩm
                            .delete();
                }
            }
            // Không cần cập nhật adapter, listener sẽ tự động làm
        });

        // Mua hàng → sang form Thanh Toán
        btnBuySelected.setOnClickListener(v -> proceedToCheckout());

        // Áp dụng voucher
        btnApplyCoupon.setOnClickListener(v -> recalcTotal());

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    // (Req 1) - Hàm tải giỏ hàng từ Firebase
    private void loadCartFromFirebase() {
        db.collection("users").document(userId)
                .collection("cartItems")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;
                        items.clear();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                // (Req 1) - Dùng constructor rỗng và setters
                                ProductModel p = new ProductModel();

                                // (Req 2) - ID document chính là ID sản phẩm
                                p.setId(doc.getId());

                                p.setName(doc.getString("name"));
                                p.setPrice(doc.getLong("price"));
                                p.setImageUrl(doc.getString("imageUrl")); // (Req 1) - Lấy URL
                                p.setRating(0f);
                                p.setDescription("");
                                p.setMoreInfor("");

                                // (Req 3) - Lấy số lượng MUA
                                Long qty = doc.getLong("quantity");
                                p.setQuantity(qty != null ? qty.intValue() : 1);

                                p.setSelectedSize(doc.getString("selectedSize"));
                                p.setType(doc.getString("type"));
                                p.setChecked(false); // Mặc định không chọn

                                items.add(p);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        recalcTotal();
                    }
                });
    }

    // (Req 3) - Hàm tính tổng (Đã đúng, không check kho)
    private void recalcTotal() {
        long subtotal = 0;
        for (ProductModel it : items)
            if (it.isChecked()) subtotal += it.getPrice() * it.getQuantity();

        long discount = 0;
        String code = edtCoupon.getText() == null ? "" : edtCoupon.getText().toString().trim();
        if (!TextUtils.isEmpty(code)) {
            if ("GIAM10%".equalsIgnoreCase(code)) discount = Math.round(subtotal * 0.10);
            else if ("GIAM50$".equalsIgnoreCase(code)) discount = Math.min(50, subtotal);
        }
        long total = Math.max(0, subtotal - discount);
        tvSubtotal.setText("Tổng tiền hàng: " + formatVnd(subtotal));
        tvDiscount.setText("Áp dụng Voucher: " + formatVnd(discount));
        tvTotal.setText("Tổng Thanh Toán: " + formatVnd(total));
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "₫";
    }

    // (Req 3) - Chuyển sang thanh toán (Đã đúng, không check kho)
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
        startActivity(intent);
    }
}