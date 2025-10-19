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


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHomeActivity extends AppCompatActivity{
    private ViewPager2 bannerViewPager;
    private androidx.recyclerview.widget.RecyclerView categoryRecyclerView, productRecyclerView;
    //du lieu cho tung catagories
    private Map<String, List<ProductModel>> productByCategory = new HashMap<>();
    private ProductAdapter productAdapter;
    private EditText edtSearch;
    private List<ProductModel> currentProductList = new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_form);

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


        // Xử lý tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim().toLowerCase();
                filterProducts(keyword);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    // Hàm lọc sản phẩm theo tên
    private void filterProducts(String keyword) {
        keyword = keyword.toLowerCase().trim();

        if (keyword.isEmpty()) {
            productAdapter.updateProducts(currentProductList);
            return;
        }

        // Nếu người dùng gõ từ khóa trùng tên danh mục
        for (String category : productByCategory.keySet()) {
            if (category.toLowerCase().contains(keyword)) {
                currentProductList = new ArrayList<>(productByCategory.get(category));
                productAdapter.updateProducts(currentProductList);

                // 🟢 Đánh dấu danh mục đang chọn (đổi màu button)
                categoryAdapter.setSelectedCategory(category);

                return;
            }
        }


        // Nếu không phải danh mục, thì lọc theo tên sản phẩm
        List<ProductModel> filtered = new ArrayList<>();
        String matchedCategory = null;

        for (Map.Entry<String, List<ProductModel>> entry : productByCategory.entrySet()) {
            for (ProductModel p : entry.getValue()) {
                if (p.getName().toLowerCase().contains(keyword)) {
                    filtered.add(p);
                    matchedCategory = entry.getKey(); // lấy danh mục đầu tiên chứa sp
                }
            }
        }

        if (matchedCategory != null) {
            categoryAdapter.setSelectedCategory(matchedCategory);
        }

        productAdapter.updateProducts(filtered);

        // để đổi danh mục được chọn
        if (keyword.toLowerCase().contains("áo")) {
            categoryAdapter.setSelectedCategory("Áo");
        } else if (keyword.toLowerCase().contains("vợt")) {
            categoryAdapter.setSelectedCategory("Vợt");
        } else if (keyword.toLowerCase().contains("giày")) {
            categoryAdapter.setSelectedCategory("Giày");
        } else {
            // Nếu không khớp danh mục nào, bỏ chọn
            categoryAdapter.setSelectedCategory("");
        }
    }

    //  setupProducts()
    private void setupProducts() {
        List<ProductModel> vot = new ArrayList<>();
        vot.add(new ProductModel("vot1", "Victor Ryuga II", 100L, R.drawable.vot_victor_ryuga_ii, 4f,
                "Vợt cầu lông chuyên công", "Tấn công mạnh mẽ, trọng lượng 85g, cán cầm G5", 1, " ", "Vợt"));
        vot.add(new ProductModel("vot2", "Victor Ryuga II Pro", 120L, R.drawable.vot_victor_ryuga_ii_pro, 5f,
                "Vợt cầu lông cao cấp", "Khung carbon siêu nhẹ, cân bằng tốt", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot3", "Lining Halbertec 8000", 79L, R.drawable.vot_lining_halbertec_8000, 3f,
                "Vợt cầu lông tầm trung", "Dành cho người chơi thiên công trình cao", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot4", "Lining Halbertec 9000", 122L, R.drawable.vot_lining_halbertec_9000, 5f,
                "Vợt cầu lông cao cấp", "Khung nhẹ, kiểm soát tốt", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot5", "Victor Brave Sword 12 Pro", 190L, R.drawable.vot_victor_brave_sword_12_pro, 4f,
                "Vợt chuyên nghiệp", "Cân bằng, linh hoạt", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot6", "Yonex Astrox 100", 198L, R.drawable.vot_cau_long_yonex_astrox_100, 4f,
                "Vợt cao cấp", "Công nghệ Aerodynamic", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot7", "Yonex Astrox 99 Pro 2025", 98L, R.drawable.vot_yonex_astrox_99_pro_2025, 4f,
                "Vợt chuyên nghiệp", "Độ bền cao, linh hoạt", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot8", "Yonex Astrox 100VA Tour", 100L, R.drawable.vot_yonex_astrox_100va_tour, 4f,
                "Vợt cầu lông chuyên nghiệp", "Cân bằng tốt, tốc độ cao", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot9", "Yonex Nanoflare 700 Pro", 130L, R.drawable.vot_yonex_nanoflare_700_pro, 5f,
                "Vợt tốc độ cao", "Thiết kế nhẹ, linh hoạt", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot10", "Victor ARS 90K Metallic", 110L, R.drawable.vot_victor_ars_90k_metallic, 3f,
                "Vợt kiểm soát", "khả năng điều cầu tốt", 1, "Chưa Chọn", "Vợt"));
        vot.add(new ProductModel("vot11", "Lining Calibar 600i", 160L, R.drawable.vot_lining_calibar_600i, 5f,
                "Vợt cao cấp", "Khung carbon, kiểm soát tốt", 1, "Chưa Chọn", "Vợt"));

        List<ProductModel> giay = new ArrayList<>();
        giay.add(new ProductModel("giay1", "Lining Ayau 005", 100L, R.drawable.giay_cau_long_lining_ayau005, 3f,
                "Giày cầu lông nam", "Màu xanh dương, size 39-45", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay2", "Lining Ayzu 015", 120L, R.drawable.giay_cau_long_lining_ayzu015, 4f,
                "Giày cầu lông unisex", "Màu trắng, size 38-44", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay3", "Victor A311", 99L, R.drawable.giay_cau_long_victor_a311, 5f,
                "Giày cầu lông nam", "Chống trượt, màu trắng", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay4", "Yonex Aerus X Navy Blue", 198L, R.drawable.giay_cau_long_yonex_aerus_x_navy_blue, 4f,
                "Giày cầu lông cao cấp", "Màu xanh navy, đế cao su", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay5", "Yonex Cascade Accel", 149L, R.drawable.giay_cau_long_yonex_cascade_accel, 3f,
                "Giày cầu lông nam", "Đệm êm, màu xanh dương", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay6", "Yonex Cascade Blue", 110L, R.drawable.giay_cau_long_yonex_cascade_blue, 5f,
                "Giày cầu lông nữ", "Màu xanh, size 36-42", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay7", "Yonex Power Z3", 79L, R.drawable.giay_cau_long_yonex_power_z3, 5f,
                "Giày cầu lông nam", "Màu đỏ, nhẹ, thoáng khí", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay8", "Yonex SHB 65Z3", 130L, R.drawable.giay_cau_long_yonex_shb_65z3, 4f,
                "Giày cầu lông nữ", "Màu xanh, chống trượt", 1, "Chưa Chọn", "Giày"));
        giay.add(new ProductModel("giay9", "Yonex SHB 65Z4", 120L, R.drawable.giay_cau_long_yonex_shb_65z4, 4f,
                "Giày cầu lông unisex", "Màu xám, size 37-44", 1, "Chưa Chọn", "Giày"));

        List<ProductModel> quan = new ArrayList<>();
        quan.add(new ProductModel("quan1", "Felet CM202B Black", 20L, R.drawable.quan_cau_long_felet_cm202b_black, 4f,
                "Quần cầu lông nam", "Màu đen, size S-XL", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan2", "Felet CM202B White", 30L, R.drawable.quan_cau_long_felet_cm202b_white, 5f,
                "Quần cầu lông nữ", "Màu trắng, size S-L", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan3", "Kawasaki SP", 25L, R.drawable.quan_cau_long_kawasaki_sp, 3f,
                "Quần cầu lông unisex", "Màu xanh, chất liệu thoáng", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan4", "Lining 967 Đen", 30L, R.drawable.quan_cau_long_lining_967_den, 4f,
                "Quần cầu lông nam", "Màu đen, co giãn tốt", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan5", "Lining 967 Trắng", 30L, R.drawable.quan_cau_long_lining_967_trang, 5f,
                "Quần cầu lông nữ", "Màu trắng, co giãn tốt", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan6", "Victor 225 Hồng", 60L, R.drawable.quan_cau_long_victor_225_hong, 4f,
                "Quần cầu lông nữ", "Màu hồng, size S-L", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan7", "Victor 901 Trắng Kem", 25L, R.drawable.quan_cau_long_victor_901_trang_kem, 3f,
                "Quần cầu lông nam", "Màu trắng kem, size M-XL", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan8", "Yonex 7049 Xanh Trắng", 40L, R.drawable.quan_cau_long_yonex_7049_xanh_trang, 3f,
                "Quần cầu lông unisex", "Màu xanh trắng, co giãn", 1, "Chưa Chọn", "Quần"));
        quan.add(new ProductModel("quan9", "Yonex TSM2844 Hemlock", 20L, R.drawable.quan_cau_long_yonex_tsm2844_hemlock, 4f,
                "Quần cầu lông nam", "Màu xanh, nhẹ, thoáng khí", 1, "Chưa Chọn", "Quần"));

        List<ProductModel> ao = new ArrayList<>();
        ao.add(new ProductModel("ao1", "Lining 3136 Nam Đen Xanh", 35L, R.drawable.ao_cau_long_lining_3136_nam_den_xanh, 4f,
                "Áo cầu lông nam", "Màu đen xanh, size M-XXL", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao2", "Lining 3175 Trắng", 24L, R.drawable.ao_cau_long_lining_3175_trang, 3f,
                "Áo cầu lông nữ", "Màu trắng, size S-L", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao3", "Lining 6589 Nam Đen Xám", 50L, R.drawable.ao_cau_long_lining_6589_nam_den_xam, 5f,
                "Áo cầu lông unisex", "Màu đen xám, thoáng khí", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao4", "Lining T41 Nam Đen", 25L, R.drawable.ao_cau_long_lining_t41_nam_den, 3f,
                "Áo cầu lông nam", "Màu đen, thấm hút tốt", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao5", "Yonex TPM2969 Patriot Blue", 29L, R.drawable.ao_cau_long_yonex_tpm2969_patrior_blue, 4f,
                "Áo cầu lông nữ", "Màu xanh, thoáng khí", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao6", "Yonex TRM2967 Light Taupe", 52L, R.drawable.ao_cau_long_yonex_trm2967_light_taupe, 3f,
                "Áo cầu lông unisex", "Màu nâu nhạt, thoáng khí", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao7", "Yonex TJM288 Oatmeal", 25L, R.drawable.ao_khoac_cau_long_yonex_tjm288_oatmeal, 5f,
                "Áo khoác cầu lông", "Màu kem, chống gió", 1, "Chưa Chọn", "Áo"));
        ao.add(new ProductModel("ao8", "Kamito Galaxy 1", 29L, R.drawable.ao_kamito_galaxy_1, 5f,
                "Áo cầu lông nam", "Màu xanh dương, thoáng khí", 1, "Chưa Chọn", "Áo"));

        productByCategory.put("Vợt", vot);
        productByCategory.put("Giày", giay);
        productByCategory.put("Quần", quan);
        productByCategory.put("Áo", ao);
    }




}
