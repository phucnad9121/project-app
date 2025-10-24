package com.example.project_btl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.home.BannerAdapter;
import com.example.project_btl.home.CategoryAdapter;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.ProductAdapter;
import com.example.project_btl.ProductModel;
import com.example.project_btl.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminMainHomeActivity extends AppCompatActivity {

    private ViewPager2 bannerViewPager;
    private androidx.recyclerview.widget.RecyclerView categoryRecyclerView, productRecyclerView;
    private EditText edtSearch;

    private List<ProductModel> allProducts = new ArrayList<>();
    private List<ProductModel> currentProductList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private FirebaseFirestore db;

    private String role; // 🟢 lưu role toàn cục để dùng lại

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_admin_form);

        db = FirebaseFirestore.getInstance();

        // 🟢 Lấy role từ Intent
        role = getIntent().getStringExtra("USER_ROLE");

        // Ẩn ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 🟢 Ánh xạ view
        edtSearch = findViewById(R.id.edtSearch);

        // Banner
        bannerViewPager = findViewById(R.id.bannerViewPager);
        List<Integer> banners = new ArrayList<>();
        banners.add(R.drawable.giay_cau_long_lining_ayzu015);
        banners.add(R.drawable.giay_cau_long_lining_ayzu015);
        banners.add(R.drawable.giay_cau_long_lining_ayzu015);
        BannerAdapter bannerAdapter = new BannerAdapter(this, banners);
        bannerViewPager.setAdapter(bannerAdapter);

        // Category
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        List<CategoryModel> categories = new ArrayList<>();
        categories.add(new CategoryModel(R.drawable.ic_badmin, "Vợt"));
        categories.add(new CategoryModel(R.drawable.ic_shoes, "Giày"));
        categories.add(new CategoryModel(R.drawable.ic_jeans, "Quần"));
        categories.add(new CategoryModel(R.drawable.ic_shirt, "Áo"));
        categoryAdapter = new CategoryAdapter(this, categories);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Product list
        productRecyclerView = findViewById(R.id.productRecyclerView);
        productAdapter = new ProductAdapter(this, new ArrayList<>(currentProductList));
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);


        loadAllProductsFromFirestore();

        categoryAdapter.setOnCategoryClickListener(categoryName -> {
            filterProductsByCategory(categoryName);
            edtSearch.setText("");
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProductsByName(s.toString().trim().toLowerCase());
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationAD);
        bottomNavigationView.setSelectedItemId(R.id.nav_admin_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_admin_cart) {
                intent = new Intent(this, MainActivity_giohang.class);
            } else if (id == R.id.nav_admin_notifications) {
                intent = new Intent(this, NotificationsActivity.class);
            } else if (id == R.id.nav_admin_manager) {
                if ("admin".equals(role)) {
                    intent = new Intent(this, AdminActivity.class);
                } else {
                    intent = new Intent(this, ProfileActivity.class);
                }
            }

            if (intent != null) {
                intent.putExtra("USER_ROLE", role);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private void loadAllProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductModel product = document.toObject(ProductModel.class);
                            product.setId(document.getId());
                            allProducts.add(product);
                        }
                        filterProductsByCategory("Vợt");
                        categoryAdapter.setSelectedCategory("Vợt");
                    } else {
                        Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void filterProductsByCategory(String categoryName) {
        currentProductList.clear();
        for (ProductModel p : allProducts) {
            if (p.getType() != null && p.getType().equalsIgnoreCase(categoryName)) {
                currentProductList.add(p);
            }
        }
        productAdapter.updateProducts(currentProductList);
    }


    private void filterProductsByName(String keyword) {
        if (keyword.isEmpty()) {
            productAdapter.updateProducts(currentProductList);
            return;
        }

        List<ProductModel> filtered = new ArrayList<>();
        for (ProductModel p : allProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(keyword)) {
                filtered.add(p);
            }
        }
        productAdapter.updateProducts(filtered);
    }
}
