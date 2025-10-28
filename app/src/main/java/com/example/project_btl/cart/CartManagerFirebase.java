package com.example.project_btl.cart;

import com.example.project_btl.ProductModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
public class CartManagerFirebase {
    private static CartManagerFirebase instance;
    private FirebaseFirestore db;
    private CartManagerFirebase() {
        db = FirebaseFirestore.getInstance();
    }
    public static CartManagerFirebase getInstance() {
        if (instance == null) {
            instance = new CartManagerFirebase();
        }
        return instance;
    }

    private String getUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else return null;
    }

    private CollectionReference getCartRef() {
        String userId = getUserId();
        if (userId == null) return null;
        return db.collection("users")
                .document(userId)
                .collection("cartItems");
    }

    public void addToCart(ProductModel product) {
        CollectionReference cartRef = getCartRef();
        if (cartRef == null) return;

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("name", product.getName());
        cartItem.put("price", product.getPrice());
        cartItem.put("quantity", product.getQuantity());
        cartItem.put("selectedSize", product.getSelectedSize());
        cartItem.put("type", product.getType());
        cartItem.put("imageUrl", product.getImageUrl());

        cartRef.document(product.getId())
                .set(cartItem)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void removeFromCart(String productId) {
        CollectionReference cartRef = getCartRef();
        if (cartRef == null) return;

        cartRef.document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void updateQuantity(String productId, int newQuantity) {
        CollectionReference cartRef = getCartRef();
        if (cartRef == null) return;

        cartRef.document(productId)
                .update("quantity", newQuantity) // Chỉ cập nhật trường số lượng
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(Throwable::printStackTrace);
    }
}