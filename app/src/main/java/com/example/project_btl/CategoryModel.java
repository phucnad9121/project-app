package com.example.project_btl;

public class CategoryModel {
    private int icon;
    private String name;

    public CategoryModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() { return icon; }
    public String getName() { return name; }
}
