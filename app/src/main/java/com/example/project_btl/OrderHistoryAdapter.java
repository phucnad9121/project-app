package com.example.project_btl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private List<OrderManagerFirebase.OrderData> orders;

    public OrderHistoryAdapter(List<OrderManagerFirebase.OrderData> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderManagerFirebase.OrderData order = orders.get(position);

        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
        holder.tvOrderStatus.setText(order.getStatus());
        holder.tvTotalAmount.setText("Tổng: " + formatVnd(order.getTotal()));

        if (order.getItems() != null) {
            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getItems());
            holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.rvOrderItems.getContext()));
            holder.rvOrderItems.setAdapter(itemAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderDate, tvOrderStatus, tvTotalAmount;
        RecyclerView rvOrderItems;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
        }
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "₫";
    }
}