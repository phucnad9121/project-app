package com.example.project_btl.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Intent;

import com.example.project_btl.CategoryModel;
import com.example.project_btl.cart.MainActivity_giohang;
import com.example.project_btl.notification.NotificationsActivity;
import com.example.project_btl.ProductAdapter;
import com.example.project_btl.ProductModel;
import com.example.project_btl.R;
import com.example.project_btl.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHomeActivity extends AppCompatActivity{
    private ViewPager2 bannerViewPager;
    private androidx.recyclerview.widget.RecyclerView categoryRecyclerView, productRecyclerView;

    // (Req 2) - Dữ liệu sẽ được load từ Firebase
    private List<ProductModel> allProducts = new ArrayList<>(); // Tải tất cả SP về để tìm kiếm
    private List<ProductModel> currentProductList = new ArrayList<>(); // SP đang hiển thị (sau khi lọc)
    private ProductAdapter productAdapter;
    private EditText edtSearch;
    private CategoryAdapter categoryAdapter;

    private FirebaseFirestore db; // (Req 2) - Add Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_form);

        db = FirebaseFirestore.getInstance(); // (Req 2) - Init Firebase

        //tim kiem
        edtSearch = findViewById(R.id.edtSearch);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Mặc định chọn Home
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, MainActivity_giohang.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });



        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        // Banner
        bannerViewPager = findViewById(R.id.bannerViewPager);
        List<Integer> banners = new ArrayList<>();
        banners.add(R.drawable.giay_cau_long_lining_ayzu015);
        banners.add(R.drawable.giay_cau_long_yonex_shb_65z4);
        banners.add(R.drawable.giay_cau_long_victor_a311);
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

        // (Req 2) - Gọi hàm khởi tạo dữ liệu sản phẩm
        // setupProducts(); // (Req 2) - Bỏ dữ liệu cứng

        // Hiển thị mặc định danh sách Vợt
        productRecyclerView = findViewById(R.id.productRecyclerView);
        productAdapter = new ProductAdapter(this, new ArrayList<>(currentProductList)); // (Req 2) - Dùng list rỗng
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);

        // (Req 2 & 4) - Tải tất cả sản phẩm từ Firestore
        loadAllProductsFromFirestore();

        // (Req 2 & 4) - Bắt sự kiện click category
        categoryAdapter.setOnCategoryClickListener(categoryName -> {
            // (Req 4) - Lọc danh sách sản phẩm đã tải
            filterProductsByCategory(categoryName);
            // (Req 2) - Xóa text tìm kiếm khi chọn category
            edtSearch.setText("");
        });


        // Xử lý tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim().toLowerCase();
                filterProductsByName(keyword); // (Req 2) - Lọc trên danh sách đã tải
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // (Req 2) - Hàm tải TẤT CẢ sản phẩm từ Firestore
    private void loadAllProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductModel product = document.toObject(ProductModel.class);
                            product.setId(document.getId()); // (Req 2) - Gán ID
                            allProducts.add(product);
                        }
                        Log.d("MainHomeActivity", "Loaded " + allProducts.size() + " products.");
                        // (Req 2) - Hiển thị danh mục "Vợt" làm mặc định
                        filterProductsByCategory("Vợt");
                        categoryAdapter.setSelectedCategory("Vợt");
                    } else {
                        Log.w("MainHomeActivity", "Error getting documents.", task.getException());
                        Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // (Req 4) - Hàm lọc sản phẩm theo danh mục (từ danh sách allProducts)
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


    // (Req 2) - Hàm lọc sản phẩm theo tên (từ danh sách allProducts)
    private void filterProductsByName(String keyword) {
        keyword = keyword.toLowerCase().trim();
        List<ProductModel> filtered = new ArrayList<>();

        if (keyword.isEmpty()) {
            // (Req 2) - Nếu xóa hết text, quay lại danh sách hiện tại (theo category)
            productAdapter.updateProducts(currentProductList);
            return;
        }

        // (Req 2) - Lọc từ danh sách TẤT CẢ sản phẩm
        String matchedCategory = null;
        for (ProductModel p : allProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(keyword)) {
                filtered.add(p);
                if (matchedCategory == null) {
                    matchedCategory = p.getType(); // (Req 4) - Tự động chọn category
                }
            }
        }

        if (matchedCategory != null) {
            categoryAdapter.setSelectedCategory(matchedCategory);
        } else {
            categoryAdapter.setSelectedCategory(null); // Bỏ chọn
        }

        productAdapter.updateProducts(filtered);
    }

    // (Req 2) - Xóa toàn bộ hàm setupProducts()

}