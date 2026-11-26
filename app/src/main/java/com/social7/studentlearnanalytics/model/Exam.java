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
    private String startTime;
    private String endTime;
    private Double rawScore;
    private Double percentage;
    private String resultStatus;
    private boolean published;

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
    public void setScore(int score) { this.score = score; }
    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }
    public String getScoreText() { return scoreText; }
    public void setScoreText(String scoreText) { this.scoreText = scoreText; }

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

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Double getRawScore() { return rawScore; }
    public void setRawScore(Double rawScore) { this.rawScore = rawScore; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public static String calculateTimeRange(String startTime, String endTime, Integer durationMinutes) {
        String s = normalizeTimeStr(startTime);
        String e = normalizeTimeStr(endTime);
        if (s.isEmpty()) {
            return "";
        }
        if (e.isEmpty() && durationMinutes != null && durationMinutes > 0) {
            e = addMinutesToTime(s, durationMinutes);
        }
        return e.isEmpty() ? s : (s + " – " + e);
    }

    public static String addMinutesToTime(String startTime, int durationMinutes) {
        String s = normalizeTimeStr(startTime);
        if (s.length() != 5 || durationMinutes <= 0) return "";
        try {
            String[] parts = s.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) return "";
            int totalMins = (h * 60 + m + durationMinutes) % (24 * 60);
            return String.format(java.util.Locale.US, "%02d:%02d", totalMins / 60, totalMins % 60);
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String normalizeTimeStr(String time) {
        if (time == null) return "";
        String trimmed = time.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return "";
        if (trimmed.length() >= 5 && trimmed.charAt(2) == ':') {
            return trimmed.substring(0, 5);
        }
        return trimmed;
    }
}
