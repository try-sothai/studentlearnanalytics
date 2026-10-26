package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Notification implements Serializable {
    private String id;
    private String title;
    private String message;
    private String timeAgo;
    private String category;
    private String dateGroup;
    private boolean isRead;

    public Notification(String id, String title, String message, String timeAgo, String category, boolean isRead) {
        this(id, title, message, timeAgo, category, "TODAY", isRead);
    }

    public Notification(String id, String title, String message, String timeAgo, String category, String dateGroup, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timeAgo = timeAgo;
        this.category = category;
        this.dateGroup = dateGroup;
        this.isRead = isRead;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimeAgo() { return timeAgo; }
    public String getCategory() { return category; }
    public String getDateGroup() { return dateGroup != null ? dateGroup : "TODAY"; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
}
