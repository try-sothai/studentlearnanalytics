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

    // Extended real-API fields (nullable/defaults for backward compatibility)
    private String courseCode;
    private String className;
    private String academicYearName;
    private String statusLabel;
    private String rawStatus;
    private String actionLabel;
    private int durationMinutes;
    private int questionsCount;
    private String instructions;
    private boolean isOverdue;
    private boolean inProgress;

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

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYearName() { return academicYearName; }
    public void setAcademicYearName(String academicYearName) { this.academicYearName = academicYearName; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public String getRawStatus() { return rawStatus; }
    public void setRawStatus(String rawStatus) { this.rawStatus = rawStatus; }

    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isOverdue() { return isOverdue; }
    public void setOverdue(boolean overdue) { isOverdue = overdue; }

    public boolean isInProgress() { return inProgress; }
    public void setInProgress(boolean inProgress) { this.inProgress = inProgress; }

    public String getFormattedScore() {
        if (isGraded) {
            return score + " / " + totalScore;
        } else if (statusLabel != null && !statusLabel.isEmpty()) {
            return statusLabel;
        } else if (inProgress) {
            return "In Progress";
        } else if (isSubmitted) {
            return "Submitted";
        } else if (isOverdue) {
            return "Overdue";
        } else {
            return "Available";
        }
    }
}
