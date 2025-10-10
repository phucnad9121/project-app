package com.example.project_btl;

import com.example.project_btl.ProductModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderManagerFirebase {

    private static OrderManagerFirebase instance;
    private final FirebaseFirestore db;

    private OrderManagerFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    public static OrderManagerFirebase getInstance() {
        if (instance == null) instance = new OrderManagerFirebase();
        return instance;
    }

    private String getUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    private CollectionReference getOrderRef() {
        String userId = getUserId();
        if (userId == null) return null;
        return db.collection("users").document(userId).collection("orders");
    }

    public void saveOrder(List<ProductModel> products, long total, String payment, String address,
                          OnOrderSavedListener listener) {
        CollectionReference orderRef = getOrderRef();
        if (orderRef == null) {
            if (listener != null) listener.onFailed(new Exception("User chưa đăng nhập"));
            return;
        }

        Map<String, Object> order = new HashMap<>();
        order.put("orderDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        order.put("total", total);
        order.put("paymentMethod", payment);
        order.put("address", address);
        order.put("status", "Đã đặt hàng");

        List<Map<String, Object>> items = new ArrayList<>();
        for (ProductModel p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("quantity", p.getQuantity());
            item.put("size", p.getSelectedSize());
            item.put("image", p.getImage());
            items.add(item);
        }
        order.put("items", items);

        orderRef.add(order)
                .addOnSuccessListener(r -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailed(e);
                });
    }

    public interface OnOrderSavedListener {
        void onSuccess();

        void onFailed(Exception e);
    }
}
