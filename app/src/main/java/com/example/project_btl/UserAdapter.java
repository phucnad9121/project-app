package com.example.project_btl;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private int selectedPos = RecyclerView.NO_POSITION;
    private Context context;
    private OnItemInteractionListener listener; // Biến listener để giao tiếp

    // --- BẮT ĐẦU SỬA ĐỔI 1: Tạo Interface ---
    public interface OnItemInteractionListener {
        void onItemClick(User user);
        void onItemLongClick(User user);
    }
    // --- KẾT THÚC SỬA ĐỔI 1 ---

    // --- BẮT ĐẦU SỬA ĐỔI 2: Cập nhật Constructor ---
    public UserAdapter(Context context, List<User> users, OnItemInteractionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }
    // --- KẾT THÚC SỬA ĐỔI 2 ---

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getName()); // Hiển thị tên đầy đủ sẽ đẹp hơn
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvRole.setText("Role: " + user.getRole());

        // Giữ lại logic tô màu khi chọn
        holder.itemView.setBackgroundColor(selectedPos == position ? Color.LTGRAY : Color.TRANSPARENT);

        // --- BẮT ĐẦU SỬA ĐỔI 3: Gán sự kiện ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Cập nhật vị trí được chọn và tô màu
                notifyItemChanged(selectedPos);
                selectedPos = holder.getAdapterPosition();
                notifyItemChanged(selectedPos);

                // Gọi đến Activity để xử lý việc sửa
                listener.onItemClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                // Gọi đến Activity để xử lý việc xóa
                listener.onItemLongClick(user);
                return true; // Đánh dấu sự kiện đã được xử lý
            }
            return false;
        });
        // --- KẾT THÚC SỬA ĐỔI 3 ---
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // ViewHolder giữ nguyên
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvRole;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Giả định id trong item_user.xml là tvUsername, tvEmail, tvRole
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }

    // --- BẮT ĐẦU SỬA ĐỔI 4: Thêm các phương thức quản lý dữ liệu ---
    public void addUser(User user) {
        users.add(user);
        notifyItemInserted(users.size() - 1);
    }

    public void updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            // Dùng một thuộc tính không đổi như username để tìm và cập nhật
            if (users.get(i).getUsername().equals(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeUser(User userToRemove) {
        int position = users.indexOf(userToRemove);
        if (position > -1) {
            users.remove(position);
            notifyItemRemoved(position);
        }
    }
    // --- KẾT THÚC SỬA ĐỔI 4 ---
}