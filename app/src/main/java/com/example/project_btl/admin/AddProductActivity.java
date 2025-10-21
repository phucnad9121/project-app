package com.example.project_btl.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar; // ✅ (Req 3) Thêm import
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
// (Req 1) - Đã xóa import của FirebaseStorage

import java.util.Arrays;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText edtProductName, edtProductPrice, edtProductDescription,
            edtProductMoreInfor, edtImageUrl;
    private Spinner spProductType;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private RatingBar rbProductRating; // ✅ (Req 3) Thêm biến RatingBar

    private ProductModel productToEdit;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();
        setupClickListeners();

        productToEdit = (ProductModel) getIntent().getSerializableExtra("EDIT_PRODUCT");
        if (productToEdit != null) {
            TextView tvTitle = findViewById(R.id.textViewTitle);
            tvTitle.setText("Sửa sản phẩm");
            loadProductData();
        }
    }

    private void initViews() {
        edtProductName = findViewById(R.id.edtProductName);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtProductDescription = findViewById(R.id.edtProductDescription);
        edtProductMoreInfor = findViewById(R.id.edtProductMoreInfor);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        spProductType = findViewById(R.id.spProductType);
        rbProductRating = findViewById(R.id.rbProductRating); // ✅ (Req 3) Ánh xạ RatingBar

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpinner() {
        List<String> categories = Arrays.asList("Vợt", "Giày", "Quần", "Áo");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProductType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProductData() {
        if (productToEdit != null) {
            edtProductName.setText(productToEdit.getName());
            edtProductPrice.setText(String.valueOf(productToEdit.getPrice()));
            edtProductDescription.setText(productToEdit.getDescription());
            edtProductMoreInfor.setText(productToEdit.getMoreInfor());
            edtImageUrl.setText(productToEdit.getImageUrl());
            rbProductRating.setRating(productToEdit.getRating()); // ✅ (Req 3) Load Rating

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spProductType.getAdapter();
            int spinnerPosition = adapter.getPosition(productToEdit.getType());
            spProductType.setSelection(spinnerPosition);
        }
    }

    private void saveProduct() {
        String name = edtProductName.getText().toString().trim();
        String priceStr = edtProductPrice.getText().toString().trim();
        String description = edtProductDescription.getText().toString().trim();
        String moreInfor = edtProductMoreInfor.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();
        String type = spProductType.getSelectedItem().toString();
        float rating = rbProductRating.getRating(); // ✅ (Req 3) Lấy giá trị Rating

        if (name.isEmpty() || priceStr.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền tên, giá và loại sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(this, "Vui lòng nhập URL hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        long price = Long.parseLong(priceStr);

        if (productToEdit != null) {
            // Cập nhật sản phẩm
            updateProduct(name, price, description, moreInfor, imageUrl, type, rating);
        } else {
            // Thêm sản phẩm mới
            addNewProduct(name, price, description, moreInfor, imageUrl, type, rating);
        }
    }

    private void addNewProduct(String name, long price, String description,
                               String moreInfor, String imageUrl, String type, float rating) { // ✅ (Req 3) Thêm rating
        ProductModel product = new ProductModel();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setMoreInfor(moreInfor);
        product.setImageUrl(imageUrl);
        product.setType(type);
        product.setRating(rating); // ✅ (Req 3) Set Rating
        product.setQuantity(9999);
        product.setChecked(true);
        product.setReservedQuantity(0);

        saveProductToFirestore(product);
    }

    private void updateProduct(String name, long price, String description,
                               String moreInfor, String imageUrl, String type, float rating) { // ✅ (Req 3) Thêm rating

        ProductModel product = productToEdit;
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setMoreInfor(moreInfor);
        product.setImageUrl(imageUrl);
        product.setType(type);
        product.setRating(rating); // ✅ (Req 3) Set Rating
        // Không set số lượng

        updateProductInFirestore(product);
    }

    private void saveProductToFirestore(ProductModel product) {
        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    product.setId(docId);
                    db.collection("products").document(docId)
                            .set(product)
                            .addOnSuccessListener(aVoid -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            })
                            .addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Lỗi khi cập nhật ID: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi thêm sản phẩm: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void updateProductInFirestore(ProductModel product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("products").document(product.getId())
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi cập nhật sản phẩm: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                });
    }
}