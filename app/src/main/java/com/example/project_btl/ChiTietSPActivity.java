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

import com.example.project_btl.cart.CartManager;
import com.example.project_btl.cart.MainActivity_giohang;
import com.google.android.material.button.MaterialButton;

public class ChiTietSPActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, tvQuantity, moTa, ttBoSung;
    private RatingBar ratingBar;
    private RadioGroup rgSizeGiay, rgSizeClothes;
    private ImageButton btnGiam, btnTang, btnGioHang, btnBack;
    private MaterialButton btnBuyNow;

    private int quantity = 1; // số lượng mặc định = 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chitiet_sp); // đúng tên layout bạn đã gửi
        if(getSupportActionBar() != null ){
            getSupportActionBar().hide();
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
            // lấy số lượng ban đầu từ model
            quantity = product.getQuantity();
            if (quantity <= 0) quantity = 1;
            tvQuantity.setText(String.valueOf(quantity));

            // Hiển thị RadioGroup phù hợp loại sản phẩm
            showSizeOptions(product.getType());
        }

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Xử lý tăng/giảm số lượng
        btnTang.setOnClickListener(v -> {
            quantity++;
            product.setQuantity(quantity);
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnGiam.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                product.setQuantity(quantity);
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Nút thêm giỏ hàng
        btnGioHang.setOnClickListener(v -> {
            String size = getSelectedSize();
            product.setSelectedSize(size); // lưu size vào model
            product.setQuantity(quantity); // lưu số lượng vào model
            // Thêm vào CartManager
            CartManager.getInstance().addToCart(product, size, quantity);
            Intent intent = new Intent(ChiTietSPActivity.this, MainActivity_giohang.class);
            startActivity(intent);
        });

        // Nút mua ngay
        btnBuyNow.setOnClickListener(v -> {
            String size = getSelectedSize();
            Toast.makeText(this,
                    "Mua ngay: " + product.getName() +
                            " - Size " + size + " - SL: " + quantity,
                    Toast.LENGTH_LONG).show();
            // Chuyển sang màn hình thanh toán
        });

    }
    // Hiển thị RadioGroup theo type sản phẩm
    private void showSizeOptions(String type) {
        if (type.equals("Giày")) {
            rgSizeGiay.setVisibility(View.VISIBLE);
            rgSizeClothes.setVisibility(View.GONE);
        } else if (type.equals("Quần") || type.equals("Áo")) {
            rgSizeGiay.setVisibility(View.GONE);
            rgSizeClothes.setVisibility(View.VISIBLE);
        } else { // Vợt hoặc loại khác
            rgSizeGiay.setVisibility(View.GONE);
            rgSizeClothes.setVisibility(View.GONE);
        }
    }


    //  lấy size được chọn trong RadioGroup
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
