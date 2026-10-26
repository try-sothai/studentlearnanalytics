package com.social7.studentlearnanalytics.model;

public class Assessment {
    private String id;
    private String title;
    private String date;
    private int score;
    private int totalScore;
    private String type;
    private String semester;
    private String courseName;
    private boolean isSubmitted;
    private boolean isGraded;

    public Assessment(String id, String title, String date, int score, int totalScore, String type) {
        this(id, title, date, score, totalScore, type, null, null, true, true);
    }

    public Assessment(String id, String title, String date, int score, int totalScore, String type,
                      String semester, String courseName, boolean isSubmitted, boolean isGraded) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.score = score;
        this.totalScore = totalScore;
        this.type = type;
        this.semester = semester;
        this.courseName = courseName;
        this.isSubmitted = isSubmitted;
        this.isGraded = isGraded;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getScore() { return score; }
    public int getTotalScore() { return totalScore; }
    public String getType() { return type; }
    public String getSemester() { return semester; }
    public String getCourseName() { return courseName; }
    public boolean isSubmitted() { return isSubmitted; }
    public boolean isGraded() { return isGraded; }

    public String getFormattedScore() {
        if (isGraded) {
            return score + " / " + totalScore;
        } else if (isSubmitted) {
            return "Submitted";
        } else {
            return "Not Submitted";
        }
    }
}
