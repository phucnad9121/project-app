package com.example.project_btl;

public class CategoryModel {
    private int icon;
    private String name;

    public CategoryModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    // Nếu bạn muốn dùng thêm getIconResId thì viết như này
    public int getIconResId() {
        return icon;
    }

    // setter (nếu cần)
    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }
}


