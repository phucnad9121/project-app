package com.example.project_btl;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;

public class ChiTietSPActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chitiet_sp);

        String name = getIntent().getStringExtra("product_name");
        String desc = getIntent().getStringExtra("product_desc");
        int price = (int) getIntent().getLongExtra("product_price", 0);
        int img = getIntent().getIntExtra("product_image", R.drawable.vot_yonex_astrox_100va_tour);

        ViewPager2 pager = findViewById(R.id.viewPagerProductImages);
        ProductImageAdapter adapter = new ProductImageAdapter(Arrays.asList(
                img,
                R.drawable.vot_yonex_astrox_99_pro_2025,
                R.drawable.vot_yonex_nanoflare_700_pro
        ));
        pager.setAdapter(adapter);

        TextView tvName = findViewById(R.id.detailProductName);
        TextView tvPrice = findViewById(R.id.detailProductPrice);
        if (tvName != null && name != null) tvName.setText(name);
        if (tvPrice != null) tvPrice.setText("Giá: " + price + "đ");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}



