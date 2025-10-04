package com.example.project_btl.notification;

public class NotificationModel {
    private String message;
    private String time;
    private int icon;

    public NotificationModel(String message, String time, int icon) {
        this.message = message;
        this.time = time;
        this.icon = icon;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public int getIcon() {
        return icon;
    }
}
