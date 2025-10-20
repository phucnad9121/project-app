package com.example.project_btl.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {
    private EditText edtProductName, edtProductPrice, edtProductDescription, 
            edtProductMoreInfor, edtProductQuantity;
    private Spinner spProductType;
    private ImageView ivProductImage;
    private Button btnChooseImage, btnSave, btnCancel;
    private ImageButton btnBack;
    
    private Uri imageUri;
    private ProductModel productToEdit;
    
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        initViews();
        setupSpinner();
        setupClickListeners();
        
        // Kiểm tra xem có sản phẩm để sửa không
        productToEdit = (ProductModel) getIntent().getSerializableExtra("EDIT_PRODUCT");
        if (productToEdit != null) {
            loadProductData();
        }
    }

    private void initViews() {
        edtProductName = findViewById(R.id.edtProductName);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtProductDescription = findViewById(R.id.edtProductDescription);
        edtProductMoreInfor = findViewById(R.id.edtProductMoreInfor);
        edtProductQuantity = findViewById(R.id.edtProductQuantity);
        spProductType = findViewById(R.id.spProductType);
        
        ivProductImage = findViewById(R.id.ivProductImage);
        btnChooseImage = findViewById(R.id.btnChooseImage);
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
        btnChooseImage.setOnClickListener(v -> openImageChooser());
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProductImage.setImageURI(imageUri);
        }
    }

    private void loadProductData() {
        if (productToEdit != null) {
            edtProductName.setText(productToEdit.getName());
            edtProductPrice.setText(String.valueOf(productToEdit.getPrice()));
            edtProductDescription.setText(productToEdit.getDescription());
            edtProductMoreInfor.setText(productToEdit.getMoreInfor());
            edtProductQuantity.setText(String.valueOf(productToEdit.getQuantity()));
            
            // Thiết lập loại sản phẩm trong spinner
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
        String quantityStr = edtProductQuantity.getText().toString().trim();
        String type = spProductType.getSelectedItem().toString();

        if (name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long price = Long.parseLong(priceStr);
        int quantity = Integer.parseInt(quantityStr);

        if (productToEdit != null) {
            // Cập nhật sản phẩm
            updateProduct(name, price, description, moreInfor, quantity, type);
        } else {
            // Thêm sản phẩm mới
            addNewProduct(name, price, description, moreInfor, quantity, type);
        }
    }

    private void addNewProduct(String name, long price, String description, 
                              String moreInfor, int quantity, String type) {
        ProductModel product = new ProductModel();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setMoreInfor(moreInfor);
        product.setQuantity(quantity);
        product.setType(type);
        product.setChecked(true); // Mặc định còn hàng
        product.setReservedQuantity(0); // Mặc định không có đơn đặt giữ

        // Upload image to Firebase Storage nếu có
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("product_images/" + System.currentTimeMillis() + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    runOnUiThread(() -> {
                                        product.setImageUrl(uri.toString());
                                        saveProductToFirestore(product);
                                    });
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Nếu upload thất bại, vẫn lưu sản phẩm mà không có ảnh
                        runOnUiThread(() -> {
                            saveProductToFirestore(product);
                        });
                    });
        } else {
            // Không có ảnh mới, lưu sản phẩm với ảnh mặc định
            runOnUiThread(() -> {
                saveProductToFirestore(product);
            });
        }
    }

    private void updateProduct(String name, long price, String description, 
                              String moreInfor, int quantity, String type) {
        // Cập nhật sản phẩm trong Firestore
        ProductModel product = productToEdit;
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setMoreInfor(moreInfor);
        product.setQuantity(quantity);
        product.setType(type);

        if (imageUri != null) {
            // Upload ảnh mới nếu có
            StorageReference imageRef = storageRef.child("product_images/" + System.currentTimeMillis() + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    runOnUiThread(() -> {
                                        product.setImageUrl(uri.toString());
                                        updateProductInFirestore(product);
                                    });
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Nếu upload ảnh mới thất bại, giữ nguyên ảnh cũ
                        runOnUiThread(() -> {
                            updateProductInFirestore(product);
                        });
                    });
        } else {
            // Không có ảnh mới, cập nhật sản phẩm mà không thay đổi ảnh
            runOnUiThread(() -> {
                updateProductInFirestore(product);
            });
        }
    }

    private void saveProductToFirestore(ProductModel product) {
        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                        finish();
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