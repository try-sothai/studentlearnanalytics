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

    // Extended metadata for Exam Detail modal & rich cards
    private String title;
    private String courseCode;
    private String className;
    private String semesterName;
    private String examType;
    private String statusLabel;
    private String rawStatus;
    private String venue;
    private Integer durationMinutes;
    private String examDateFormatted;
    private String description;
    private String grade;
    private String remarks;

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public String getRawStatus() { return rawStatus; }
    public void setRawStatus(String rawStatus) { this.rawStatus = rawStatus; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getExamDateFormatted() { return examDateFormatted; }
    public void setExamDateFormatted(String examDateFormatted) { this.examDateFormatted = examDateFormatted; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
