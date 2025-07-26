package com.example.myapplication;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static Cloudinary cloudinary;

    public static synchronized Cloudinary getInstance() {
        if (cloudinary == null) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dfbqvnhnd");
            config.put("api_key", "869747567378725");
            config.put("api_secret", "4Cr5UsfY6-qx0QvEMRkoz1VpHV8");
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
}
