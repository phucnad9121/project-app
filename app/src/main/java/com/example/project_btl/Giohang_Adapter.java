package com.example.project_btl;

import android.content.Intent;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Giohang_Adapter extends RecyclerView.Adapter<Giohang_Adapter.VH> {
    public interface Listener {
        void onItemsChanged();
        void onItemRemoved(int position);
    }

    private final List<Sp_giohang> items;
    private final Listener listener;

    public Giohang_Adapter(List<Sp_giohang> items, Listener listener) {
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
        Sp_giohang it = items.get(position);

        h.checkbox.setOnCheckedChangeListener(null);
        h.checkbox.setChecked(it.isChecked());
        h.checkbox.setOnCheckedChangeListener((b, c) -> { it.setChecked(c); listener.onItemsChanged(); });

        h.image.setImageResource(it.getImageResId());
        h.name.setText(it.getName());
        h.desc.setText(it.getDescription());
        h.price.setText("Giá: " + formatVnd(it.getPrice()));
        h.quantity.setText("Số lượng: " + it.getQuantity());

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

        h.btnRemove.setOnClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p != RecyclerView.NO_POSITION) {
                items.remove(p);
                notifyItemRemoved(p);
                listener.onItemRemoved(p);
            }
        });

        // Nhấn vào toàn bộ item để mở chi tiết
        View.OnClickListener openDetail = v -> {
            try {
                Intent i = new Intent(v.getContext(), ChiTietSPActivity.class);
                i.putExtra("product_name", it.getName());
                i.putExtra("product_desc", it.getDescription());
                i.putExtra("product_price", it.getPrice());
                i.putExtra("product_image", it.getImageResId());
                v.getContext().startActivity(i);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Không mở được chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        h.itemView.setOnClickListener(openDetail);
        h.infoContainer.setOnClickListener(openDetail);
        h.image.setOnClickListener(openDetail);

        // Nhấn giữ để xoá
        h.infoContainer.setOnLongClickListener(v -> {
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
        CheckBox checkbox; ImageView image; TextView name; TextView desc; TextView price; TextView quantity; View infoContainer;
        ImageButton btnPlus; ImageButton btnMinus; Button btnRemove;
        public VH(@NonNull View v) {
            super(v);
            checkbox = v.findViewById(R.id.checkboxProduct);
            image = v.findViewById(R.id.productImage);
            name = v.findViewById(R.id.productName);
            desc = v.findViewById(R.id.productDesc);
            price = v.findViewById(R.id.productPrice);
            quantity = v.findViewById(R.id.productQuantity);
            infoContainer = v.findViewById(R.id.infoContainer);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnRemove = v.findViewById(R.id.btnRemoveItem);
        }
    }

    private String formatVnd(long v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "đ";
    }
}


