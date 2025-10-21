package com.example.project_btl.cart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // ✅ THÊM IMPORT NÀY
import com.example.project_btl.DetailSPActivity;
import com.example.project_btl.ProductModel;
import com.example.project_btl.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Giohang_Adapter extends RecyclerView.Adapter<Giohang_Adapter.VH> {

    public interface Listener {
        void onItemsChanged();
        void onItemRemoved(int position);
    }

    private final List<ProductModel> items;
    private final Listener listener;

    public Giohang_Adapter(List<ProductModel> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_gh, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProductModel it = items.get(position);

        // Checkbox
        h.checkbox.setOnCheckedChangeListener(null);
        h.checkbox.setChecked(it.isChecked());
        h.checkbox.setOnCheckedChangeListener((b, c) -> {
            it.setChecked(c);
            listener.onItemsChanged();
        });

        // Thông tin sản phẩm
        // ✅ THAY ĐỔI TẠI ĐÂY: DÙNG GLIDE ĐỂ TẢI URL
        if (it.getImageUrl() != null && !it.getImageUrl().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(it.getImageUrl())
                    .placeholder(R.drawable.meme)
                    .error(R.drawable.meme)
                    .into(h.image);
        } else {
            h.image.setImageResource(R.drawable.meme); // Ảnh mặc định
        }
        // h.image.setImageResource(it.getImage()); // ❌ BỎ DÒNG NÀY

        h.name.setText(it.getName());
        h.size.setText("Size: " + it.getSelectedSize());
        h.quantity.setText("SL: " + it.getQuantity());
        h.price.setText("Giá: " + formatVnd(it.getPrice()));

        // Tăng/giảm số lượng (Req 3: Đã bỏ check số lượng)
        h.btnPlus.setOnClickListener(v -> {
            it.setQuantity(it.getQuantity() + 1);
            notifyItemChanged(h.getAdapterPosition());
            listener.onItemsChanged();
        });
        h.btnMinus.setOnClickListener(v -> {
            if (it.getQuantity() > 1) {
                it.setQuantity(it.getQuantity() - 1);
                notifyItemChanged(h.getAdapterPosition());
                listener.onItemsChanged();
            }
        });

        // Xóa sản phẩm
        h.btnRemove.setOnClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p != RecyclerView.NO_POSITION) {
                items.remove(p);
                notifyItemRemoved(p);
                listener.onItemRemoved(p);
            }
        });

        // Nhấn vào item mở chi tiết
        View.OnClickListener openDetail = v -> {
            try {
                Intent i = new Intent(v.getContext(), DetailSPActivity.class);
                i.putExtra("product", it);
                v.getContext().startActivity(i);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Không mở được chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        h.itemView.setOnClickListener(openDetail);
        h.image.setOnClickListener(openDetail);

        // Nhấn giữ để xác nhận xóa
        h.itemView.setOnLongClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return true;
            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Bạn có chắc chắn muốn xóa?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        items.remove(p);
                        notifyItemRemoved(p);
                        listener.onItemRemoved(p);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        ImageView image;
        TextView name,size, price, quantity;
        ImageButton btnPlus, btnMinus;
        Button btnRemove;

        public VH(@NonNull View v) {
            super(v);
            // Các ID này đã khớp với list_item_gh.xml
            checkbox = v.findViewById(R.id.checkboxProduct);
            image = v.findViewById(R.id.productImage);
            name = v.findViewById(R.id.productName);
            size = v.findViewById(R.id.productSize);
            price = v.findViewById(R.id.productPrice);
            quantity = v.findViewById(R.id.productQuantity);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnRemove = v.findViewById(R.id.btnRemoveItem);
        }
    }

    private String formatVnd(long v) {
        // Cập nhật lại định dạng tiền cho đúng (bỏ $ đi)
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "$";
    }
}