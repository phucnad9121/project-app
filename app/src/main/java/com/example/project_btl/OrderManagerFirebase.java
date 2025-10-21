package com.example.project_btl;

import android.util.Log;

import com.example.project_btl.notification.NotificationManagerFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.*;

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
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return null;
    }

    private CollectionReference getOrderRef() {
        String userId = getUserId();
        if (userId == null) return null;
        return db.collection("users").document(userId).collection("orders");
    }

    // ✅ ĐÃ CHỈNH: Trả về thông tin OrderData sau khi lưu
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
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("quantity", p.getQuantity());
            item.put("size", p.getSelectedSize());
            item.put("image", p.getImage());
            item.put("imageUrl", p.getImageUrl());
            items.add(item);
        }
        order.put("items", items);

        // 🔹 Lưu đơn hàng vào Firestore
        orderRef.add(order)
                .addOnSuccessListener((DocumentReference ref) -> {
                    String id = ref.getId();
                    OrderData orderData = new OrderData(
                            id,
                            (String) order.get("orderDate"),
                            total,
                            payment,
                            address,
                            "Đã đặt hàng",
                            convertToOrderItems(items)
                    );
                    // 🟢 Gửi thông báo đặt hàng
                    NotificationManagerFirebase.getInstance()
                            .addNotification("Đơn hàng #" + id + " đã được đặt thành công!", "order", R.drawable.ic_shopping_cart);

                    if (listener != null) listener.onSuccess(orderData); // ✅ Trả về dữ liệu đơn hàng
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailed(e);
                });
    }

    private List<OrderItem> convertToOrderItems(List<Map<String, Object>> itemsData) {
        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> itemData : itemsData) {
            OrderItem item = new OrderItem();
            item.setName((String) itemData.get("name"));
            item.setPrice(toLong(itemData.get("price")));
            item.setQuantity(toInt(itemData.get("quantity")));
            item.setSize((String) itemData.get("size"));
            item.setImage(toInt(itemData.get("image")));
            item.setImageUrl((String) itemData.get("imageUrl"));
            items.add(item);
        }
        return items;
    }

    public void loadOrders(OnOrdersLoadedListener listener) {
        CollectionReference orderRef = getOrderRef();
        if (orderRef == null) {
            if (listener != null) listener.onFailed(new Exception("Người dùng chưa đăng nhập"));
            return;
        }

        orderRef.orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<OrderData> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        try {
                            String id = doc.getId();
                            String orderDate = doc.getString("orderDate");
                            String paymentMethod = doc.getString("paymentMethod");
                            String status = doc.getString("status");
                            String address = doc.getString("address");
                            long total = toLong(doc.get("total"));

                            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) doc.get("items");
                            List<OrderItem> items = convertToOrderItems(itemsData);
                            orders.add(new OrderData(id, orderDate, total, paymentMethod, address, status, items));
                        } catch (Exception e) {
                            Log.e("OrderManager", "Lỗi phân tích đơn hàng: " + doc.getId(), e);
                        }
                    }
                    if (listener != null) listener.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailed(e);
                });
    }

    private long toLong(Object obj) { if (obj instanceof Number) return ((Number) obj).longValue(); return 0; }
    private int toInt(Object obj) { if (obj instanceof Number) return ((Number) obj).intValue(); return 0; }

    // ✅ ĐÃ SỬA: Interface nhận OrderData khi lưu thành công
    public interface OnOrderSavedListener {
        void onSuccess(OrderData orderData);
        void onFailed(Exception e);
    }

    public interface OnOrdersLoadedListener {
        void onSuccess(List<OrderData> orders);
        void onFailed(Exception e);
    }

    public static class OrderData {
        private String id, orderDate, paymentMethod, address, status;
        private long total;
        private List<OrderItem> items;

        public OrderData(String id, String orderDate, long total, String paymentMethod, String address, String status, List<OrderItem> items) {
            this.id = id;
            this.orderDate = orderDate;
            this.total = total;
            this.paymentMethod = paymentMethod;
            this.address = address;
            this.status = status;
            this.items = items;
        }

        public String getId() { return id; }
        public String getOrderDate() { return orderDate; }
        public String getStatus() { return status; }
        public long getTotal() { return total; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getAddress() { return address; }
        public List<OrderItem> getItems() { return items; }
    }

    public static class OrderItem {
        private String name, size, imageUrl;
        private long price;
        private int quantity, image;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getPrice() { return price; }
        public void setPrice(long price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public int getImage() { return image; }
        public void setImage(int image) { this.image = image; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}
