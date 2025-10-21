package com.example.project_btl.notification;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationManagerFirebase {

    private static NotificationManagerFirebase instance;
    private final FirebaseFirestore db;

    private NotificationManagerFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    public static NotificationManagerFirebase getInstance() {
        if (instance == null) instance = new NotificationManagerFirebase();
        return instance;
    }

    // 🟡 Lấy userId hiện tại từ Firebase Auth
    private String getUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return null;
    }

    // 🟢 Truy cập đến collection thông báo của từng người dùng
    private CollectionReference getNotificationRef() {
        String userId = getUserId();
        if (userId == null) return null;
        return db.collection("users").document(userId).collection("notifications");
    }

    // 🟢 Ghi thông báo mới vào Firestore
    public void addNotification(String message, String type, int iconResId) {
        CollectionReference notiRef = getNotificationRef();
        if (notiRef == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("type", type);
        data.put("icon", iconResId);
        data.put("time", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));

        notiRef.add(data)
                .addOnSuccessListener(ref -> Log.d("Notification", "Thêm thông báo thành công"))
                .addOnFailureListener(e -> Log.e("Notification", "Lỗi thêm thông báo", e));
    }

    // 🟡 Đọc danh sách thông báo của user
    public void loadNotifications(OnNotificationsLoadedListener listener) {
        CollectionReference notiRef = getNotificationRef();
        if (notiRef == null) {
            listener.onLoaded(new ArrayList<>());
            return;
        }

        notiRef.orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<NotificationModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        NotificationModel model = new NotificationModel(
                                doc.getString("message"),
                                doc.getString("time"),
                                doc.contains("icon") ? Math.toIntExact(doc.getLong("icon")) : 0,
                                doc.getId() // Include document ID
                        );
                        list.add(model);
                    }
                    listener.onLoaded(list);
                })
                .addOnFailureListener(e -> listener.onLoaded(new ArrayList<>()));
    }

    // 🟡 Delete notification by document ID
    public void deleteNotification(String documentId, OnNotificationDeletedListener listener) {
        CollectionReference notiRef = getNotificationRef();
        if (notiRef == null) {
            if (listener != null) {
                listener.onDeleted(false, "Người dùng chưa được xác thực");
            }
            return;
        }

        notiRef.document(documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onDeleted(true, "Thông báo đã được xóa thành công");
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onDeleted(false, "Thông báo xoá thất bại: " + e.getMessage());
                    }
                });
    }

    public interface OnNotificationDeletedListener {
        void onDeleted(boolean success, String message);
    }

    public interface OnNotificationsLoadedListener {
        void onLoaded(List<NotificationModel> notifications);
    }
}