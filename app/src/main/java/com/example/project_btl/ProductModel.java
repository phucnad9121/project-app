package com.example.project_btl;

import java.io.Serializable;

public class ProductModel implements Serializable {
    private String id;          // ID sản phẩm (dùng cho SQL)
    private String name;
    private String selectedSize;
    private Long price;
    private int image;
    private String description;
    private String moreInfor;
    private float rating;
    private int quantity;
    private boolean checked;    // để dùng trong giỏ hàng
    private String type;
    // constructor
    public ProductModel(String id, String name, Long price, int image, float rating,
                        String description, String moreInfor, int quantity, String selectedSize, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.rating = rating;
        this.description = description;
        this.moreInfor = moreInfor;
        this.quantity = Math.max(1, quantity);
        this.selectedSize = selectedSize;
        this.checked = false;
        this.type = type;
    }

    // getter & setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public int getImage() { return image; }
    public void setImage(int image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMoreInfor() { return moreInfor; }
    public void setMoreInfor(String moreInfor) { this.moreInfor = moreInfor; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(1, quantity); }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
