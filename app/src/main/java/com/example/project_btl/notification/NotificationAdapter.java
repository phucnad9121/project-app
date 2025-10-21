package com.example.project_btl.notification;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_btl.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel> notifications;

    public NotificationAdapter(List<NotificationModel> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NotificationModel notification = notifications.get(position);
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTime());

        int iconId = notification.getIcon();

        try {
            // Thử gán ảnh
            holder.imgIcon.setImageResource(iconId);
        } catch (Exception e) {
            holder.imgIcon.setImageResource(R.drawable.ic_bell);
        }
        // Code OnClickListener bạn thêm từ lần trước vẫn giữ nguyên
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Context context = holder.itemView.getContext();
                android.content.Intent intent = new android.content.Intent(context, com.example.project_btl.profile.OrderHistoryActivity.class);
                context.startActivity(intent);
            }
        });

        // Set long click listener for all users (admin and regular users)
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConfirmation(notification, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private void showDeleteConfirmation(NotificationModel notification, int position) {
        // This method will be called to confirm deletion - implementation will be handled by the activity
        if (onDeleteListener != null && notification.getDocumentId() != null) {
            onDeleteListener.onDeleteRequested(notification, position);
        }
    }

    // Interface to handle delete requests
    public interface OnDeleteListener {
        void onDeleteRequested(NotificationModel notification, int position);
    }

    private OnDeleteListener onDeleteListener;

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView imgIcon;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}