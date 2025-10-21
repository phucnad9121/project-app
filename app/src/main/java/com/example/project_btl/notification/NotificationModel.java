package com.example.project_btl.notification;

public class NotificationModel {
    private String message;
    private String time;
    private int icon;
    private String documentId;

    public NotificationModel(String message, String time, int icon) {
        this.message = message;
        this.time = time;
        this.icon = icon;
    }

    public NotificationModel(String message, String time, int icon, String documentId) {
        this.message = message;
        this.time = time;
        this.icon = icon;
        this.documentId = documentId;
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
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
