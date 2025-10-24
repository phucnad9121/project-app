package com.example.project_btl.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project_btl.AdminActivity;
import com.example.project_btl.CategoryModel;
import com.example.project_btl.ProductAdapter;
import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainHomeActivity extends AppCompatActivity {

    private ViewPager2 bannerViewPager;
    private androidx.recyclerview.widget.RecyclerView categoryRecyclerView, productRecyclerView;

    private List<ProductModel> allProducts = new ArrayList<>();
    private List<ProductModel> currentProductList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private EditText edtSearch;
    private CategoryAdapter categoryAdapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_form);

        db = FirebaseFirestore.getInstance();

        edtSearch = findViewById(R.id.edtSearch);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        final String role = getIntent().getStringExtra("USER_ROLE") != null
                ? getIntent().getStringExtra("USER_ROLE")
                : "user";

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_cart) {
                Intent intent = new Intent(this, MainActivity_giohang.class);
                intent.putExtra("USER_ROLE", role);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_notifications) {
                Intent intent = new Intent(this, NotificationsActivity.class);
                intent.putExtra("USER_ROLE", role);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent;
                if ("admin".equals(role)) {
                    intent = new Intent(this, AdminActivity.class);
                } else {
                    intent = new Intent(this, ProfileActivity.class);
                }
                intent.putExtra("USER_ROLE", role);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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

        // Products
        productRecyclerView = findViewById(R.id.productRecyclerView);
        productAdapter = new ProductAdapter(this, new ArrayList<>(currentProductList));
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);

        loadAllProductsFromFirestore();

        categoryAdapter.setOnCategoryClickListener(categoryName -> {
            filterProductsByCategory(categoryName);
            edtSearch.setText("");
        });

        // Tìm kiếm sản phẩm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim().toLowerCase();
                filterProductsByName(keyword);
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
                        Log.d("MainHomeActivity", "Loaded " + allProducts.size() + " products.");
                        filterProductsByCategory("Vợt");
                        categoryAdapter.setSelectedCategory("Vợt");
                    } else {
                        Log.w("MainHomeActivity", "Error getting documents.", task.getException());
                        Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterProductsByCategory(String categoryName) {
        currentProductList.clear();
        if (categoryName == null || categoryName.isEmpty()) {
            currentProductList.addAll(allProducts);
        } else {
            for (ProductModel p : allProducts) {
                if (p.getType() != null && p.getType().equalsIgnoreCase(categoryName)) {
                    currentProductList.add(p);
                }
            }
        }
        productAdapter.updateProducts(currentProductList);
        Log.d("MainHomeActivity", "Filtered " + currentProductList.size() + " products for category: " + categoryName);
    }

    private void filterProductsByName(String keyword) {
        keyword = keyword.toLowerCase().trim();
        List<ProductModel> filtered = new ArrayList<>();

        if (keyword.isEmpty()) {
            productAdapter.updateProducts(currentProductList);
            return;
        }

        String matchedCategory = null;
        for (ProductModel p : allProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(keyword)) {
                filtered.add(p);
                if (matchedCategory == null) {
                    matchedCategory = p.getType();
                }
            }
        }

        if (matchedCategory != null) {
            categoryAdapter.setSelectedCategory(matchedCategory);
        } else {
            categoryAdapter.setSelectedCategory(null);
        }

        productAdapter.updateProducts(filtered);
    }
}
