package com.example.project_btl.cart;

import com.example.project_btl.ProductModel;

import java.util.ArrayList;
import java.util.List;

/**
 * CartManager dùng để quản lý giỏ hàng trong bộ nhớ (RAM).
 * Đây là Singleton: chỉ tồn tại duy nhất 1 giỏ hàng cho cả ứng dụng.
 *
 * Lưu ý:
 * - Đây chỉ là demo (chưa có database).
 * - Khi tắt app, dữ liệu giỏ hàng sẽ mất.
 * - Sau này nếu có Database (SQLite, Firebase...) thì chỉ cần chỉnh lại bên trong class này.
 */
public class CartManager {

    // Instance duy nhất (Singleton)
    private static CartManager instance;

    private final List<ProductModel> cartList;

    private CartManager() {
        cartList = new ArrayList<>();
    }

    // Hàm trả về instance duy nhất
    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /**
     * Thêm sản phẩm vào giỏ.
     * Nếu sản phẩm đã có trong giỏ (cùng ID và cùng size) -> cộng dồn số lượng.
     */
    public void addToCart(ProductModel product, String size, int quantity) {
        if (product == null || size == null) return;

        boolean found = false;

        for (ProductModel p : cartList) {
            if (p.getId().equals(product.getId()) && size.equals(p.getSelectedSize())) {
                // Nếu đã có -> tăng số lượng
                p.setQuantity(p.getQuantity() + quantity);
                found = true;
                break;
            }
        }

        if (!found) {
            // Nếu chưa có -> tạo bản copy (để tránh ảnh hưởng tới đối tượng gốc)
            ProductModel newItem = new ProductModel(
                    product.getId(),            // id sản phẩm
                    product.getName(),          // tên
                    product.getPrice(),         // giá
                    product.getImage(),         // ảnh
                    product.getRating(),        // rating
                    product.getDescription(),   // mô tả
                    product.getMoreInfor(),     // thông tin thêm
                    quantity,                   // số lượng (mới)
                    size,                       // size đã chọn
                    product.getType()           // loại sản phẩm
            );
            cartList.add(newItem);
        }
    }

    /** Lấy toàn bộ danh sách giỏ hàng */
    public List<ProductModel> getCartList() {
        return cartList;
    }

    /** Xoá 1 sản phẩm trong giỏ */
    public void removeFromCart(ProductModel product) {
        cartList.remove(product);
    }

    /** Xoá toàn bộ giỏ hàng */
    public void clearCart() {
        cartList.clear();
    }

    /** Tính tổng tiền giỏ hàng (chỉ tính các sản phẩm được check) */
    public long getTotalPrice() {
        long total = 0;
        for (ProductModel p : cartList) {
            if (p.isChecked()) {
                total += p.getPrice() * p.getQuantity();
            }
        }
        return total;
    }
}
