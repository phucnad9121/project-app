package com.example.project_btl;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderHistory;
    private TextView tvNoOrders;
    private OrderHistoryAdapter adapter;
    private List<OrderManagerFirebase.OrderData> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        tvNoOrders = findViewById(R.id.tvNoOrders);

        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(orderList);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(adapter);

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        tvNoOrders.setText("Đang tải...");
        tvNoOrders.setVisibility(View.VISIBLE);
        rvOrderHistory.setVisibility(View.GONE);

        OrderManagerFirebase.getInstance().loadOrders(new OrderManagerFirebase.OnOrdersLoadedListener() {
            @Override
            public void onSuccess(List<OrderManagerFirebase.OrderData> orders) {
                runOnUiThread(() -> {
                    orderList.clear();
                    if (orders != null && !orders.isEmpty()) {
                        orderList.addAll(orders);
                        rvOrderHistory.setVisibility(View.VISIBLE);
                        tvNoOrders.setVisibility(View.GONE);
                    } else {
                        rvOrderHistory.setVisibility(View.GONE);
                        tvNoOrders.setVisibility(View.VISIBLE);
                        tvNoOrders.setText("Không có đơn hàng nào");
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderHistoryActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    rvOrderHistory.setVisibility(View.GONE);
                    tvNoOrders.setVisibility(View.VISIBLE);
                    tvNoOrders.setText("Lỗi khi tải dữ liệu");
                });
            }
        });
    }
}