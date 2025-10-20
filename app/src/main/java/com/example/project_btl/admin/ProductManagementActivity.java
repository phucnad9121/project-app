package com.example.project_btl.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.example.project_btl.home.MainHomeActivity;
import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.AdminActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private RecyclerView rvProductList;
    private ProductManagementAdapter productAdapter;
    private List<ProductModel> productList;
    private Button btnAddNewProduct;
    
    private FirebaseFirestore db;
    
    // Biến để theo dõi trạng thái load dữ liệu
    private boolean isLoadingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();
        initViews();
        setupBottomNavigation();
        loadProductData();
        setupClickListeners();
    }

    private void initViews() {
        rvProductList = findViewById(R.id.rvProductList);
        btnAddNewProduct = findViewById(R.id.btnAddNewProduct);
        
        productList = new ArrayList<>();
        productAdapter = new ProductManagementAdapter(this, productList);
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        rvProductList.setAdapter(productAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Đặt mặc định chọn profile
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, MainActivity_giohang.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, AdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private void loadProductData() {
        // Tránh việc load dữ liệu nhiều lần đồng thời
        if (isLoadingData) {
            return;
        }
        
        isLoadingData = true;
        
        // Tạo danh sách tạm để chứa sản phẩm mới
        List<ProductModel> newProductList = new ArrayList<>();
        
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    runOnUiThread(() -> {
                        isLoadingData = false; // Reset trạng thái sau khi hoàn thành
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Convert Firestore document to ProductModel
                                String id = document.getId();
                                String name = document.getString("name");
                                Long price = document.getLong("price");
                                String description = document.getString("description");
                                String moreInfor = document.getString("moreInfor");
                                Long quantityLong = document.getLong("quantity");
                                int quantity = quantityLong != null ? quantityLong.intValue() : 0;
                                String selectedSize = document.getString("selectedSize");
                                String type = document.getString("type");
                                String imageUrl = document.getString("imageUrl");
                                Boolean available = document.getBoolean("available");
                                
                                ProductModel product = new ProductModel(
                                        id, name, price, R.drawable.meme, 0f, 
                                        description, moreInfor, quantity, 
                                        selectedSize != null ? selectedSize : "Chưa Chọn", type
                                );
                                
                                // Get reserved quantity, default to 0 if not present
                                Long reservedQuantityLong = document.getLong("reservedQuantity");
                                int reservedQuantity = reservedQuantityLong != null ? reservedQuantityLong.intValue() : 0;
                                product.setReservedQuantity(reservedQuantity);
                                
                                // Set imageUrl if available
                                if (imageUrl != null) {
                                    product.setImageUrl(imageUrl);
                                }
                                
                                // Set available status
                                if (available != null) {
                                    product.setChecked(available);
                                }
                                
                                newProductList.add(product);
                            }
                            
                            // Thay thế toàn bộ danh sách cũ bằng danh sách mới
                            productList.clear();
                            productList.addAll(newProductList);
                            productAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Lỗi khi tải dữ liệu sản phẩm: " + task.getException().getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }

    private void setupClickListeners() {
        btnAddNewProduct.setOnClickListener(v -> {
            // Ngăn chặn click đa lần
            v.setEnabled(false);
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
            // Kích hoạt lại sau một thời gian ngắn
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        // Set up edit and delete listeners in adapter
        productAdapter.setOnEditClickListener(product -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            intent.putExtra("EDIT_PRODUCT", product);
            startActivity(intent);
        });

        productAdapter.setOnDeleteClickListener(product -> {
            deleteProduct(product);
        });
    }

    private void deleteProduct(ProductModel product) {
        // Delete from Firestore
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    runOnUiThread(() -> {
                        // Remove from adapter and update UI
                        productAdapter.removeProduct(product);
                        Toast.makeText(this, "Xóa sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from add/edit activity
        // Sử dụng Handler để tránh việc gọi quá nhanh
        new android.os.Handler().postDelayed(() -> {
            loadProductData();
        }, 200); // Delay nhẹ 200ms để tránh gọi quá nhanh
    }
    
    public void refreshProductList() {
        loadProductData();
    }
}