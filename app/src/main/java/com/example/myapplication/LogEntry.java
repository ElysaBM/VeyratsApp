package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogEntry {
    private String tenantName;
    private Date timestampDate;
    private String type;
    private String imageUrl;

    public LogEntry() {}

    public LogEntry(String tenantName, Date timestampDate, String type, String imageUrl) {
        this.tenantName = tenantName;
        this.timestampDate = timestampDate;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    public String getTenantName() {
        return tenantName != null ? tenantName : "Unknown";
    }

    public Date getTimestampDate() {
        return timestampDate != null ? timestampDate : new Date(0);
    }

    public String getFormattedTimestamp() {
        if (timestampDate == null) return "No Timestamp";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(timestampDate);
    }

    public String getType() {
        return type != null ? type : "Unknown";
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public String getEntryExit() {
        if (type == null) return "UNKNOWN";
        switch (type.toLowerCase()) {
            case "entry":
                return "Entry";
            case "exit":
                return "Exit";
            default:
                return type;
        }
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "tenantName='" + tenantName + '\'' +
                ", timestampDate=" + timestampDate +
                ", type='" + type + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
