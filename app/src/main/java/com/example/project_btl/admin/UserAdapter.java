package com.example.project_btl.admin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users; // DSTKHT
    private int selectedPos = RecyclerView.NO_POSITION; // VTitem
    private Context context; // inflate
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onItemClick(User user);
        void onItemLongClick(User user);
    }

    // TR.DLieu
    public UserAdapter(Context context, List<User> users, OnItemInteractionListener listener) {
        this.context = context; // bên trái là biến private, bên phải là biến truyền vào constructor
        this.users = users;
        this.listener = listener;
    }

    // Tạo layout cho từng item trong rv
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v); // tạo ViewHolder để giữ tham chiếu đến các View trong item
    }

    // Đổ dữ liệu
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position); // lấy ra user tương đương
        holder.tvUsername.setText(user.getName()); // gán
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvRole.setText("Role: " + user.getRole());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                notifyItemChanged(selectedPos); // reset lại màu của item
                selectedPos = holder.getBindingAdapterPosition(); // lưu vị trí item đang được click vào selectedPos để đánh dấu là đang được chọn
                notifyItemChanged(selectedPos); // cập nhật lại giao diện item

                // Gọi đến Activity để xử lý việc sửa
                listener.onItemClick(user);
            }
        });


        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                // Gọi đến Activity để xử lý việc xóa
                listener.onItemLongClick(user);
                return true;
            }
            return false;
        });
    }

    // Trả về số item hiển thị trong recyclerview
    @Override
    public int getItemCount() {
        return users.size();
    }

    // Giữ và ánh xạ các view trong từng item của recyclerview
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvRole; // Khai báo các biến để giữ tham chiếu đến các TextView trong layout item_user.xml
        public UserViewHolder(@NonNull View itemView) {
            super(itemView); // itemView: layout của từng dòng (item) trong RecyclerView
            // Giả định id trong item_user.xml là tvUsername, tvEmail, tvRole
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }

    public void addUser(User user) {
        users.add(user); // Thêm user mới vào danh sách dữ liệu (List<User>)
        notifyItemInserted(users.size() - 1); // Báo cho RecyclerView biết có item mới để cập nhật đúng vị trí cuối
    }


    public void updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            // Dùng thuộc tính id để tìm và cập nhật
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser); // Thay user cũ bằng dữ liệu mới
                notifyItemChanged(i); // Cập nhật lại UI tại vị trí đó
                break;
            }
        }
    }

    public void removeUser(User userToRemove) {
        int position = users.indexOf(userToRemove); // Tìm vị trí cần xóa
        if (position > -1) {
            users.remove(position); // Xóa khỏi danh sách dữ liệu
            notifyItemRemoved(position); // Báo cho RecyclerView cập nhật lại UI, chạy animation xoá
        }
    }
}