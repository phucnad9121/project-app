package com.example.project_btl.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_btl.ProductModel;
import com.example.project_btl.R;

import java.util.List;

public class ProductManagementAdapter extends RecyclerView.Adapter<ProductManagementAdapter.ProductViewHolder> {
    private Context context;
    private List<ProductModel> productList;
    
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(ProductModel product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(ProductModel product);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public ProductManagementAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void removeProduct(ProductModel product) {
        int position = productList.indexOf(product);
        if (position != -1) {
            productList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductType, tvProductStatus;
        private Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductType = itemView.findViewById(R.id.tvProductType);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && editClickListener != null) {
                    editClickListener.onEditClick(productList.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(productList.get(position));
                }
            });
        }

        public void bind(ProductModel product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format("%,d đ", product.getPrice()));
            tvProductQuantity.setText("Số lượng: " + product.getQuantity());
            tvProductType.setText("Loại: " + product.getType());
            
            // Hiển thị trạng thái sản phẩm
            if (product.isChecked()) {
                tvProductStatus.setText("Còn hàng");
                tvProductStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvProductStatus.setText("Hết hàng");
                tvProductStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }

            // Load ảnh sản phẩm nếu có
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.meme)
                        .error(R.drawable.meme)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(product.getImage()); // Sử dụng ảnh mặc định từ resource
            }
        }
    }
}