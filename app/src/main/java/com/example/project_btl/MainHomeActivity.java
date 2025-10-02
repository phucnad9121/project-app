package com.example.project_btl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
public class MainHomeActivity extends AppCompatActivity{
    private ViewPager2 bannerViewPager;
    private androidx.recyclerview.widget.RecyclerView categoryRecyclerView, productRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_form);

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
                startActivity(new Intent(this, SignInactivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SplashActivity.class));
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
        categories.add(new CategoryModel(R.drawable.giay_cau_long_yonex_cascade_accel, "Giày"));
        categories.add(new CategoryModel(R.drawable.lining_halbertec_9000, "Vợt"));
        categories.add(new CategoryModel(R.drawable.profile, "Quần"));
        categories.add(new CategoryModel(R.drawable.profile, "Áo"));
        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categories);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Products
        productRecyclerView = findViewById(R.id.productRecyclerView);
        List<ProductModel> products = new ArrayList<>();
        products.add(new ProductModel(R.drawable.lining_calibar_600i, "Lining Calibar 600i", "$120",3));
        products.add(new ProductModel(R.drawable.victor_ars_90k_metallic, "Victor ars 90k metallic", "$120",5));
        products.add(new ProductModel(R.drawable.victor_ryuga_ii, "Victor ryuga ii", "$100",4));
        products.add(new ProductModel(R.drawable.victor_ryuga_ii_pro, "Victor ryuga ii pro", "$160",5));
        ProductAdapter productAdapter = new ProductAdapter(this, products);
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);
    }




}
