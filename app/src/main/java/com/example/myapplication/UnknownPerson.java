package com.example.myapplication;

public class UnknownPerson {
    private String imageUrl;
    private String timestamp; // This will store the time when the unknown person was detected

    // Default constructor required for Firebase
    public UnknownPerson() {
    }

    // Constructor to initialize values
    public UnknownPerson(String imageUrl, String timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getter for imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    // Setter for imageUrl (needed by Firebase)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter for timestamp
    public String getTimestamp() {
        return timestamp;
    }

    // Setter for timestamp (needed by Firebase)
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
