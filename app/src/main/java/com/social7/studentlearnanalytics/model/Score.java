package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Score implements Serializable {
    private String id;
    private String title;
    private String date;
    private int score;
    private int maxScore;
    private String type; // "Assignment", "Quiz", "Exam", "Attendance"

    public Score(String id, String title, String date, int score, int maxScore, String type) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.score = score;
        this.maxScore = maxScore;
        this.type = type;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public String getType() { return type; }

    public void setScore(int score) { this.score = score; }
}
