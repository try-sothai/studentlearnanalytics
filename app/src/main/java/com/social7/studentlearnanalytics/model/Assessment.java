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
    private Integer yearLevel;
    private String yearLevelCode;
    private String statusLabel;
    private String rawStatus;
    private String actionLabel;
    private int durationMinutes;
    private int questionsCount;
    private String instructions;
    private boolean isOverdue;
    private boolean inProgress;

    // Universal Reopen, Retake & Late Submission server-truth fields
    private String assessmentApiType = "assignment";
    private String reopenStatus;
    private String studentSubmissionStatus;
    private String originalDeadline;
    private String extendedDeadline;
    private String effectiveDeadline;
    private Integer originalAttemptLimit;
    private int additionalAttempts;
    private int usedAttempts;
    private Integer remainingAttempts;
    private boolean allowSubmission = true;
    private boolean allowLateSubmission;
    private boolean allowResubmission;
    private boolean canSubmit = true;
    private boolean canResubmit;
    private boolean hasActiveOverride;
    private String blockedReason;

    // Quiz scoring & grading workflow fields
    private Double exactScore;
    private Double exactTotalScore;
    private Double provisionalEarnedScore;
    private boolean pendingGrading;
    private boolean partiallyGraded;
    private String gradingStatusLabel;
    private String submissionStatusLabel;
    private String latestAttemptId;
    private boolean reviewAllowed;

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
    public void setDate(String date) { this.date = date; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public String getType() { return type; }
    public String getSemester() { return semester; }
    public String getCourseName() { return courseName; }
    public boolean isSubmitted() { return isSubmitted; }
    public void setSubmitted(boolean submitted) { isSubmitted = submitted; }
    public boolean isGraded() { return isGraded; }
    public void setGraded(boolean graded) { isGraded = graded; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYearName() { return academicYearName; }
    public void setAcademicYearName(String academicYearName) { this.academicYearName = academicYearName; }

    public Integer getYearLevel() { return yearLevel; }
    public void setYearLevel(Object rawYearLevel) {
        this.yearLevel = com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(rawYearLevel);
        String c = com.social7.studentlearnanalytics.utils.YearLevelHelper.toCode(this.yearLevel);
        if (c != null) this.yearLevelCode = c;
    }

    public String getYearLevelCode() {
        if (yearLevelCode != null && !yearLevelCode.isEmpty()) return yearLevelCode;
        String c = com.social7.studentlearnanalytics.utils.YearLevelHelper.toCode(yearLevel);
        return c != null ? c : "";
    }
    public void setYearLevelCode(String yearLevelCode) { this.yearLevelCode = yearLevelCode; }

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

    public String getAssessmentApiType() { return assessmentApiType; }
    public void setAssessmentApiType(String assessmentApiType) { this.assessmentApiType = assessmentApiType; }

    public String getReopenStatus() { return reopenStatus; }
    public void setReopenStatus(String reopenStatus) { this.reopenStatus = reopenStatus; }

    public String getStudentSubmissionStatus() { return studentSubmissionStatus; }
    public void setStudentSubmissionStatus(String studentSubmissionStatus) { this.studentSubmissionStatus = studentSubmissionStatus; }

    public String getOriginalDeadline() { return originalDeadline; }
    public void setOriginalDeadline(String originalDeadline) { this.originalDeadline = originalDeadline; }

    public String getExtendedDeadline() { return extendedDeadline; }
    public void setExtendedDeadline(String extendedDeadline) { this.extendedDeadline = extendedDeadline; }

    public String getEffectiveDeadline() { return effectiveDeadline; }
    public void setEffectiveDeadline(String effectiveDeadline) { this.effectiveDeadline = effectiveDeadline; }

    public Integer getOriginalAttemptLimit() { return originalAttemptLimit; }
    public void setOriginalAttemptLimit(Integer originalAttemptLimit) { this.originalAttemptLimit = originalAttemptLimit; }

    public int getAdditionalAttempts() { return additionalAttempts; }
    public void setAdditionalAttempts(int additionalAttempts) { this.additionalAttempts = additionalAttempts; }

    public int getUsedAttempts() { return usedAttempts; }
    public void setUsedAttempts(int usedAttempts) { this.usedAttempts = usedAttempts; }

    public Integer getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(Integer remainingAttempts) { this.remainingAttempts = remainingAttempts; }

    public boolean isAllowSubmission() { return allowSubmission; }
    public void setAllowSubmission(boolean allowSubmission) { this.allowSubmission = allowSubmission; }

    public boolean isAllowLateSubmission() { return allowLateSubmission; }
    public void setAllowLateSubmission(boolean allowLateSubmission) { this.allowLateSubmission = allowLateSubmission; }

    public boolean isAllowResubmission() { return allowResubmission; }
    public void setAllowResubmission(boolean allowResubmission) { this.allowResubmission = allowResubmission; }

    public boolean isCanSubmit() { return canSubmit; }
    public void setCanSubmit(boolean canSubmit) { this.canSubmit = canSubmit; }

    public boolean isCanResubmit() { return canResubmit; }
    public void setCanResubmit(boolean canResubmit) { this.canResubmit = canResubmit; }

    public boolean isHasActiveOverride() { return hasActiveOverride; }
    public void setHasActiveOverride(boolean hasActiveOverride) { this.hasActiveOverride = hasActiveOverride; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }

    public Double getExactScore() { return exactScore; }
    public void setExactScore(Double exactScore) { this.exactScore = exactScore; }

    public Double getExactTotalScore() { return exactTotalScore; }
    public void setExactTotalScore(Double exactTotalScore) { this.exactTotalScore = exactTotalScore; }

    public Double getProvisionalEarnedScore() { return provisionalEarnedScore; }
    public void setProvisionalEarnedScore(Double provisionalEarnedScore) { this.provisionalEarnedScore = provisionalEarnedScore; }

    public boolean isPendingGrading() { return pendingGrading; }
    public void setPendingGrading(boolean pendingGrading) { this.pendingGrading = pendingGrading; }

    public boolean isPartiallyGraded() { return partiallyGraded; }
    public void setPartiallyGraded(boolean partiallyGraded) { this.partiallyGraded = partiallyGraded; }

    public String getGradingStatusLabel() { return gradingStatusLabel; }
    public void setGradingStatusLabel(String gradingStatusLabel) { this.gradingStatusLabel = gradingStatusLabel; }

    public String getSubmissionStatusLabel() { return submissionStatusLabel; }
    public void setSubmissionStatusLabel(String submissionStatusLabel) { this.submissionStatusLabel = submissionStatusLabel; }

    public String getLatestAttemptId() { return latestAttemptId; }
    public void setLatestAttemptId(String latestAttemptId) { this.latestAttemptId = latestAttemptId; }

    public boolean isReviewAllowed() { return reviewAllowed; }
    public void setReviewAllowed(boolean reviewAllowed) { this.reviewAllowed = reviewAllowed; }

    public String getFormattedScore() {
        if (isGraded) {
            if (exactScore != null && exactTotalScore != null) {
                return com.social7.studentlearnanalytics.utils.AssessmentUiHelper.formatMarks(exactScore)
                        + " / "
                        + com.social7.studentlearnanalytics.utils.AssessmentUiHelper.formatMarks(exactTotalScore);
            }
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
