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
import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHomeActivity extends AppCompatActivity{
    private ViewPager2 bannerViewPager;
    private androidx.recyclerview.widget.RecyclerView categoryRecyclerView, productRecyclerView;
    //du lieuj cho tung câtgories
    private Map<String, List<ProductModel>> productByCategory = new HashMap<>();
    private ProductAdapter productAdapter;

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
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, NotificationsActivity.class));
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
        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categories);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Products

        // Gọi hàm khởi tạo dữ liệu sản phẩm
        setupProducts();

        // Hiển thị mặc định danh sách Vợt
        productRecyclerView = findViewById(R.id.productRecyclerView);
        productAdapter = new ProductAdapter(this, new ArrayList<>(productByCategory.get("Vợt")));
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);

        // Bắt sự kiện click category
        categoryAdapter.setOnCategoryClickListener(categoryName -> {
            List<ProductModel> filteredList = productByCategory.get(categoryName);
            if (filteredList != null) {
                productAdapter.updateProducts(filteredList);
            }
        });
    }

    //  setupProducts()
    private void setupProducts() {
        List<ProductModel> vot = new ArrayList<>();
        vot.add(new ProductModel(R.drawable.vot_victor_ryuga_ii, "Victor ryuga ii", "$100", 4));
        vot.add(new ProductModel(R.drawable.vot_victor_ryuga_ii_pro, "Victor ryuga ii pro", "$120", 5));
        vot.add(new ProductModel(R.drawable.vot_lining_halbertec_8000, "Lining Halbertec 8000", "$79", 3));
        vot.add(new ProductModel(R.drawable.vot_lining_halbertec_9000, "Lining Halbertec 9000", "$122", 5));
        vot.add(new ProductModel(R.drawable.vot_victor_brave_sword_12_pro, "victor_brave_sword_12_pro", "$190", 4));
        vot.add(new ProductModel(R.drawable.vot_cau_long_yonex_astrox_100, "yonex_astrox_100", "$198", 4));
        vot.add(new ProductModel(R.drawable.vot_yonex_astrox_99_pro_2025, "yonex_astrox_99_pro_2025", "$98", 4));
        vot.add(new ProductModel(R.drawable.vot_yonex_astrox_100va_tour, "yonex_astrox_100va_tour", "$100", 4));
        vot.add(new ProductModel(R.drawable.vot_yonex_nanoflare_700_pro, "yonex_nanoflare_700_pro", "$130", 5));
        vot.add(new ProductModel(R.drawable.vot_victor_ars_90k_metallic, "victor_ars_90k_metallic", "$110", 3));
        vot.add(new ProductModel(R.drawable.vot_lining_calibar_600i, "lining_calibar_600i", "$160", 5));

        List<ProductModel> giay = new ArrayList<>();
        giay.add(new ProductModel(R.drawable.giay_cau_long_lining_ayau005, "Lining Ayau 005", "$100", 3));
        giay.add(new ProductModel(R.drawable.giay_cau_long_lining_ayzu015, "Lining Ayzu015", "$120", 4));
        giay.add(new ProductModel(R.drawable.giay_cau_long_victor_a311, "Victor A311", "$99", 5));
        giay.add(new ProductModel(R.drawable.giay_cau_long_yonex_aerus_x_navy_blue, "Yonex Aerus X Navy Blue", "$198", 4));
        giay.add(new ProductModel(R.drawable.giay_cau_long_yonex_cascade_accel, "Yonex Cascade Accel", "$149", 3));
        giay.add(new ProductModel(R.drawable.giay_cau_long_yonex_cascade_blue, "Yonex Cascade Blue", "$110", 5));
        giay.add(new ProductModel(R.drawable.giay_cau_long_yonex_power_z3, "Yonex Power z3", "$79", 5));
        giay.add(new ProductModel(R.drawable.giay_cau_long_yonex_shb_65z3, "Yonex Shb 65z3", "$130", 4));
        giay.add(new ProductModel(R.drawable.giay_cau_long_yonex_shb_65z4, "Yonex Shb 65z4", "$120", 4));

        List<ProductModel> quan = new ArrayList<>();
        quan.add(new ProductModel(R.drawable.quan_cau_long_felet_cm202b_black, "felet_cm202b_black", "$20", 4));
        quan.add(new ProductModel(R.drawable.quan_cau_long_felet_cm202b_white, "felet_cm202b_white", "$30", 5));
        quan.add(new ProductModel(R.drawable.quan_cau_long_kawasaki_sp, "kawasaki_sp", "$25", 3));
        quan.add(new ProductModel(R.drawable.quan_cau_long_lining_967_den, "lining_967_den", "$30", 4));
        quan.add(new ProductModel(R.drawable.quan_cau_long_lining_967_trang, "lining_967_trang", "$30", 5));
        quan.add(new ProductModel(R.drawable.quan_cau_long_victor_225_hong, "victor_225_hong", "$60", 4));
        quan.add(new ProductModel(R.drawable.quan_cau_long_victor_901_trang_kem, "victor_901_trang_kem", "$25", 3));
        quan.add(new ProductModel(R.drawable.quan_cau_long_yonex_7049_xanh_trang, "yonex_7049_xanh_trang", "$40", 3));
        quan.add(new ProductModel(R.drawable.quan_cau_long_yonex_tsm2844_hemlock, "yonex_tsm2844_hemlock", "$20", 4));

        List<ProductModel> ao = new ArrayList<>();
        ao.add(new ProductModel(R.drawable.ao_cau_long_lining_3136_nam_den_xanh, "lining_3136_nam_den_xanh", "$35", 4));
        ao.add(new ProductModel(R.drawable.ao_cau_long_lining_3175_trang, "lining_3175_trang", "$24", 3));
        ao.add(new ProductModel(R.drawable.ao_cau_long_lining_6589_nam_den_xam, "lining_6589_nam_den_xam", "$50", 5));
        ao.add(new ProductModel(R.drawable.ao_cau_long_lining_t41_nam_den, "lining_t41_nam_den", "$25", 3));
        ao.add(new ProductModel(R.drawable.ao_cau_long_yonex_tpm2969_patrior_blue, "yonex_tpm2969_patrior_blue", "$29", 4));
        ao.add(new ProductModel(R.drawable.ao_cau_long_yonex_trm2967_light_taupe, "yonex_trm2967_light_taupe", "$52", 3));
        ao.add(new ProductModel(R.drawable.ao_khoac_cau_long_yonex_tjm288_oatmeal, "yonex_tjm288_oatmeal", "$25", 5));
        ao.add(new ProductModel(R.drawable.ao_kamito_galaxy_1, "Kamito Galaxy 1", "$29", 5));

        productByCategory.put("Vợt", vot);
        productByCategory.put("Giày", giay);
        productByCategory.put("Quần", quan);
        productByCategory.put("Áo", ao);
    }


}
