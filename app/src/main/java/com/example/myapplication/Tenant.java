package com.example.myapplication;

import com.google.firebase.firestore.Exclude;

public class Tenant {
    @Exclude
    private String tenantId;

    private String name;
    private String birthday;
    private String address;
    private String gender;
    private String imageUrl;

    // Required empty constructor for Firestore
    public Tenant() {}

    public Tenant(String tenantId, String name, String birthday, String address, String gender) {
        this.tenantId = tenantId;
        this.name = name;
        this.birthday = birthday;
        this.address = address;
        this.gender = gender;
    }

    // Getters
    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
