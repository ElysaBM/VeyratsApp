package com.example.myapplication;

public class Notification {
    private String imageUrl;
    private String timestamp;

    public Notification(String imageUrl, String timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
