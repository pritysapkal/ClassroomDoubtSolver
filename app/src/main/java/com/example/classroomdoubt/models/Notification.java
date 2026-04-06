package com.example.classroomdoubt.models;

public class Notification {
    private String notifId;
    private String userId;
    private String title;
    private String body;
    private String doubtId;
    private boolean isRead;
    private long createdAt;

    public Notification() {}

    public Notification(String userId, String title, String body, String doubtId) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.doubtId = doubtId;
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getDoubtId() { return doubtId; }
    public void setDoubtId(String doubtId) { this.doubtId = doubtId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}