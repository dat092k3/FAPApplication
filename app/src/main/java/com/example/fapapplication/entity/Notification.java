package com.example.fapapplication.entity;

public class Notification {
    private String id;
    private String title;
    private String message;
    private long createTime;

    public Notification() { }

    public Notification(String id, String title, String message, long createTime) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createTime = createTime;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getCreateTime() { return createTime; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}

