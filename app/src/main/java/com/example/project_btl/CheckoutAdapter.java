package com.example.project_btl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.ProductModel;
import com.example.project_btl.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.VH> {

    private final List<ProductModel> items;

    public CheckoutAdapter(List<ProductModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_checkout_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProductModel p = items.get(position);
        h.name.setText(p.getName());
        h.size.setText("Phân loại: " + p.getSelectedSize());
        h.quantity.setText("Số lượng: " + p.getQuantity());
        h.price.setText(formatVnd(p.getPrice() * p.getQuantity()));
        h.image.setImageResource(p.getImage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, size, quantity, price;

        public VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.imgProduct);
            name = v.findViewById(R.id.tvProductName);
            size = v.findViewById(R.id.tvProductSize);
            quantity = v.findViewById(R.id.tvProductQuantity);
            price = v.findViewById(R.id.tvProductPrice);
        }
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "₫";
    }
}
