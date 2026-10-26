package com.social7.studentlearnanalytics.model;

public class Exam {
    private String id;
    private String dateMonth; // e.g. "OCT"
    private String dateDay;   // e.g. "21"
    private String courseName;
    private String details;   // e.g. "Midterm • 9:00 AM • Room B-204"
    private boolean completed;
    private int score;        // e.g. 85
    private int maxScore;     // e.g. 100
    private String scoreText; // e.g. "85.5 / 100"; null when no recorded result exists

    public Exam(String id, String dateMonth, String dateDay, String courseName, String details, boolean completed, int score, int maxScore) {
        this.id = id;
        this.dateMonth = dateMonth;
        this.dateDay = dateDay;
        this.courseName = courseName;
        this.details = details;
        this.completed = completed;
        this.score = score;
        this.maxScore = maxScore;
        this.scoreText = score + " / " + maxScore;
    }

    /** Constructor for API data: scoreText is null when the student has no recorded score. */
    public Exam(String id, String dateMonth, String dateDay, String courseName, String details, boolean completed, String scoreText) {
        this.id = id;
        this.dateMonth = dateMonth;
        this.dateDay = dateDay;
        this.courseName = courseName;
        this.details = details;
        this.completed = completed;
        this.scoreText = scoreText;
    }

    public String getId() { return id; }
    public String getDateMonth() { return dateMonth; }
    public String getDateDay() { return dateDay; }
    public String getCourseName() { return courseName; }
    public String getDetails() { return details; }
    public boolean isCompleted() { return completed; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public String getScoreText() { return scoreText; }
}
