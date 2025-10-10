package com.example.project_btl;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        cartItem.put("image", product.getImage());
        cartItem.put("type", product.getType());

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

    public void updateQuantity(ProductModel product) {
        CollectionReference cartRef = getCartRef();
        if (cartRef == null) return;

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("quantity", product.getQuantity());

        cartRef.document(product.getId())
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void loadCartItems(OnCartLoadedListener listener) {
        CollectionReference cartRef = getCartRef();
        if (cartRef == null) {
            listener.onLoaded(new ArrayList<>());
            return;
        }

        cartRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ProductModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ProductModel p = doc.toObject(ProductModel.class);
                        if (p != null) list.add(p);
                    }
                    listener.onLoaded(list);
                })
                .addOnFailureListener(e -> listener.onLoaded(new ArrayList<>()));
    }

    public interface OnCartLoadedListener {
        void onLoaded(List<ProductModel> items);
    }
}
