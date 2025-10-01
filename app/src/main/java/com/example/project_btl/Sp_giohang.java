package com.example.project_btl;

import java.io.Serializable;

public class Sp_giohang implements Serializable {
    private final String id;
    private String name;
    private String description;
    private long price;
    private int imageResId;
    private int quantity;
    private boolean checked;

    public Sp_giohang(String id, String name, String description, long price, int imageResId, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.quantity = Math.max(1, quantity);
        this.checked = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = Math.max(0, price); }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(1, quantity); }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}


