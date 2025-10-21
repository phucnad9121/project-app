package com.example.project_btl;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // (Req 1) - Import Glide

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> productList;

    public ProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        // (Req 1) - Dùng Glide để load ảnh từ URL
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.meme) // Ảnh chờ
                    .error(R.drawable.meme) // Ảnh lỗi
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.meme); // Ảnh mặc định
        }
        // holder.image.setImageResource(product.getImage()); // (Req 1) - Bỏ dòng này

        holder.name.setText(product.getName());
        holder.price.setText(String.format("%,d đ", product.getPrice())); // (Fix) - Định dạng tiền

        // Click vào item thì mở ChiTietSPActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailSPActivity.class);
            // truyền nguyên object product qua intent
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }


    public void updateProducts(List<ProductModel> newProducts) {
        this.productList.clear();
        this.productList.addAll(newProducts);
        notifyDataSetChanged();
    }


    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
        }
    }

}