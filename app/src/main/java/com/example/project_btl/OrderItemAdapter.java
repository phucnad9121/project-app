package com.example.project_btl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private List<OrderManagerFirebase.OrderItem> orderItems;

    public OrderItemAdapter(List<OrderManagerFirebase.OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderManagerFirebase.OrderItem item = orderItems.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvItemPrice.setText(formatVnd(item.getPrice()));
        holder.tvItemQuantity.setText("x" + item.getQuantity());

        if (item.getSize() != null && !item.getSize().isEmpty()) {
            holder.tvItemSize.setVisibility(View.VISIBLE);
            holder.tvItemSize.setText("Size: " + item.getSize());
        } else {
            holder.tvItemSize.setVisibility(View.GONE);
        }

        // ======================= PHẦN SỬA LỖI HÌNH ẢNH =======================
        // Ưu tiên tải ảnh từ URL trước
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.ivItemThumbnail.getContext())
                    .load(item.getImageUrl())
                    // Thay R.drawable.meme bằng một ảnh placeholder chung
                    .placeholder(R.drawable.meme)
                    .error(R.drawable.meme) // Ảnh hiển thị khi có lỗi
                    .into(holder.ivItemThumbnail);
        }
        // Nếu không có URL, dùng ảnh local (từ drawable)
        else if (item.getImage() != 0) {
            holder.ivItemThumbnail.setImageResource(item.getImage());
        }
        // Nếu không có cả hai, hiển thị ảnh mặc định
        else {
            holder.ivItemThumbnail.setImageResource(R.drawable.meme);
        }
        // =====================================================================
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemThumbnail;
        TextView tvItemName, tvItemSize, tvItemPrice, tvItemQuantity;

        ViewHolder(View itemView) {
            super(itemView);
            ivItemThumbnail = itemView.findViewById(R.id.ivItemThumbnail);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemSize = itemView.findViewById(R.id.tvItemSize);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
        }
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "$";
    }
}