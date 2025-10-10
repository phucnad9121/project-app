package com.example.project_btl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.project_btl.cart.MainActivity_giohang;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChiTietSPActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, tvQuantity, moTa, ttBoSung;
    private RatingBar ratingBar;
    private RadioGroup rgSizeGiay, rgSizeClothes;
    private ImageButton btnGiam, btnTang, btnGioHang, btnBack;
    private MaterialButton btnBuyNow;

    private int quantity = 1; // số lượng mặc định = 1
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chitiet_sp);

        if(getSupportActionBar() != null ){
            getSupportActionBar().hide();
        }

        // Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "guest"; // xử lý guest nếu chưa đăng nhập
        }

        // Ánh xạ View
        productImage = findViewById(R.id.ProductImages);
        productName = findViewById(R.id.detailProductName);
        productPrice = findViewById(R.id.detailProductPrice);
        ratingBar = findViewById(R.id.productRating);
        tvQuantity = findViewById(R.id.tvDetailQuantity);
        moTa = findViewById(R.id.MoTa);
        ttBoSung = findViewById(R.id.TTBoSung);
        rgSizeGiay = findViewById(R.id.rgSizeGiay);
        rgSizeClothes = findViewById(R.id.rgSizeClothes);

        btnGiam = findViewById(R.id.btnGiamSoLuong);
        btnTang = findViewById(R.id.btnTangSoLuong);
        btnGioHang = findViewById(R.id.btnGioHang);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnBack = findViewById(R.id.btnBack);
        // Lấy object từ Intent

        ProductModel product = (ProductModel) getIntent().getSerializableExtra("product");

        if (product != null) {
            productImage.setImageResource(product.getImage());
            productName.setText(product.getName());
            productPrice.setText(product.getPrice() + "$");
            ratingBar.setRating(product.getRating());
            moTa.setText(product.getDescription());
            ttBoSung.setText(product.getMoreInfor());

            quantity = product.getQuantity();
            if (quantity <= 0) quantity = 1;
            tvQuantity.setText(String.valueOf(quantity));

            // Hiển thị RadioGroup phù hợp loại sản phẩm
            showSizeOptions(product.getType());
        }

        btnBack.setOnClickListener(v -> finish());

        btnTang.setOnClickListener(v -> {
            quantity++;
            if (product != null) product.setQuantity(quantity);
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnGiam.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                if (product != null) product.setQuantity(quantity);
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Nút thêm giỏ hàng lên Firebase
        btnGioHang.setOnClickListener(v -> {
            if (product == null) return;

            String size = getSelectedSize();
            product.setSelectedSize(size);
            product.setQuantity(quantity);

            // Tạo map để lưu Firestore
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("name", product.getName());
            cartItem.put("price", product.getPrice());
            cartItem.put("quantity", product.getQuantity());
            cartItem.put("selectedSize", product.getSelectedSize());
            cartItem.put("image", product.getImage());
            cartItem.put("type", product.getType());

            // Lưu vào Firestore: collection users -> document userId -> collection cartItems -> document productId
            db.collection("users")
                    .document(userId)
                    .collection("cartItems")
                    .document(product.getId())
                    .set(cartItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        // Mở giỏ hàng
                        Intent intent = new Intent(ChiTietSPActivity.this, MainActivity_giohang.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi thêm giỏ hàng", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        });

        //Bấm buy now sẽ hiển thị luôn form thanh toán
        btnBuyNow.setOnClickListener(v -> {
            if (product == null) return;

            String size = getSelectedSize();
            product.setSelectedSize(size);
            product.setQuantity(quantity);

            // Gửi sản phẩm hiện tại sang CheckOutActivity
            ArrayList<ProductModel> selectedItems = new ArrayList<>();
            selectedItems.add(product);

            Intent intent = new Intent(ChiTietSPActivity.this, CheckOutActivity.class);
            intent.putExtra("selectedItems", selectedItems); // Truyền danh sách có 1 sản phẩm
            startActivity(intent);

            // Hiệu ứng chuyển trang
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void showSizeOptions(String type) {
        if (type.equals("Giày")) {
            rgSizeGiay.setVisibility(View.VISIBLE);
            rgSizeClothes.setVisibility(View.GONE);
        } else if (type.equals("Quần") || type.equals("Áo")) {
            rgSizeGiay.setVisibility(View.GONE);
            rgSizeClothes.setVisibility(View.VISIBLE);
        } else {
            rgSizeGiay.setVisibility(View.GONE);
            rgSizeClothes.setVisibility(View.GONE);
        }
    }

    private String getSelectedSize() {
        if (rgSizeGiay.getVisibility() == View.VISIBLE) {
            int selectedId = rgSizeGiay.getCheckedRadioButtonId();
            if (selectedId == -1) return "Chưa chọn size";
            RadioButton selectedRadio = findViewById(selectedId);
            return selectedRadio.getText().toString();
        } else if (rgSizeClothes.getVisibility() == View.VISIBLE) {
            int selectedId = rgSizeClothes.getCheckedRadioButtonId();
            if (selectedId == -1) return "Chưa chọn size";
            RadioButton selectedRadio = findViewById(selectedId);
            return selectedRadio.getText().toString();
        }
        return "Chưa chọn size";
    }
}