package com.social7.studentlearnanalytics.model;

public class Recommendation {
    private String id;
    private String title;
    private String description;
    private String category;

    public Recommendation(String id, String title, String description, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
}
