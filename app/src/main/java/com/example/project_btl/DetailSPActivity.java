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


import com.bumptech.glide.Glide;
import com.example.project_btl.CheckOut.CheckOutActivity;
import com.example.project_btl.cart.MainActivity_giohang;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class DetailSPActivity extends AppCompatActivity {
    private ImageView productImage;
    private TextView productName, productPrice, tvQuantity, moTa, ttBoSung;
    private RatingBar ratingBar;
    private RadioGroup rgSizeGiay, rgSizeClothes;
    private ImageButton btnGiam, btnTang, btnGioHang, btnBack;
    private MaterialButton btnBuyNow;
    private int quantity = 1; // ✅ (Req 1) Đây là SỐ LƯỢNG MUA, không phải số lượng tồn kho
    private FirebaseFirestore db;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_sp);

        if(getSupportActionBar() != null ){
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "guest";
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

        ProductModel product = (ProductModel) getIntent().getSerializableExtra("product");

        if (product != null) {

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.meme)
                        .error(R.drawable.meme)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.meme);
            }

            productName.setText(product.getName());
            productPrice.setText(String.format("%,d $", product.getPrice()));
            ratingBar.setRating(product.getRating());
            moTa.setText(product.getDescription());
            ttBoSung.setText(product.getMoreInfor());

            // ✅ (Req 1) Luôn set số lượng mua là 1 khi mở
            quantity = 1;
            tvQuantity.setText(String.valueOf(quantity));

            showSizeOptions(product.getType());
        }

        btnBack.setOnClickListener(v -> finish());

        // ✅ (Req 1) Tăng/Giảm SỐ LƯỢNG MUA (biến local 'quantity')
        btnTang.setOnClickListener(v -> {
            quantity++;
            // KHÔNG set product.setQuantity(quantity);
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnGiam.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                // KHÔNG set product.setQuantity(quantity);
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Nút thêm giỏ hàng lên Firebase
        btnGioHang.setOnClickListener(v -> {
            if (product == null) return;
            if (product.getId() == null || product.getId().isEmpty()) {
                Toast.makeText(this, "Lỗi: Sản phẩm không có ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ (Req 2) Lấy size đã chọn
            String size = getSelectedSize();

            // Tạo map để lưu Firestore
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", product.getId());
            cartItem.put("name", product.getName());
            cartItem.put("price", product.getPrice());
            cartItem.put("imageUrl", product.getImageUrl());
            cartItem.put("type", product.getType());

            // ✅ (Req 1) Lưu SỐ LƯỢNG MUA (biến local 'quantity')
            cartItem.put("quantity", quantity);

            // ✅ (Req 2) Lưu SIZE ĐÃ CHỌN (biến local 'size')
            cartItem.put("selectedSize", size);

            // Dùng ID sản phẩm làm ID document trong giỏ hàng
            db.collection("users")
                    .document(userId)
                    .collection("cartItems")
                    .document(product.getId())
                    .set(cartItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DetailSPActivity.this, MainActivity_giohang.class);
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

            // ✅ (Req 2) Lấy size đã chọn
            String size = getSelectedSize();

            // Tạo 1 bản copy của product để gửi đi
            ProductModel productToBuy = new ProductModel();
            productToBuy.setId(product.getId());
            productToBuy.setName(product.getName());
            productToBuy.setPrice(product.getPrice());
            productToBuy.setImageUrl(product.getImageUrl());
            productToBuy.setType(product.getType());
            productToBuy.setDescription(product.getDescription());
            productToBuy.setMoreInfor(product.getMoreInfor());
            productToBuy.setRating(product.getRating());

            // ✅ (Req 1 & 2) Set số lượng MUA và size ĐÃ CHỌN
            productToBuy.setSelectedSize(size);
            productToBuy.setQuantity(quantity);

            ArrayList<ProductModel> selectedItems = new ArrayList<>();
            selectedItems.add(productToBuy);

            Intent intent = new Intent(DetailSPActivity.this, CheckOutActivity.class);
            intent.putExtra("selectedItems", selectedItems);
            startActivity(intent);

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void showSizeOptions(String type) {
        if (type == null) type = "";
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
        return "Chưa chọn size"; // Mặc định cho Vợt
    }
}