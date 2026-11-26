package com.social7.studentlearnanalytics.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.model.Assessment;

import java.util.Locale;

/**
 * Pure-Java parser, validator, and formatter for Quiz grading statuses,
 * student score displays (final vs. provisional), and manual Short Answer grading.
 */
public final class QuizScoreUiHelper {

    public static final String STATUS_PENDING_GRADING = "Pending Grading";
    public static final String STATUS_PARTIALLY_GRADED = "Partially Graded";
    public static final String STATUS_GRADED = "Graded";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_NOT_SUBMITTED = "Not Submitted";

    private QuizScoreUiHelper() {
    }

    public static final class ReviewScoreSummary {
        public final double earnedMarks;
        public final double totalMarks;
        public final @Nullable Double finalScore;
        public final int totalQuestions;
        public final int pendingCount;
        public final int manualGradedCount;
        public final int autoGradedCount;
        public final int unansweredCount;
        public final boolean isProvisional;
        public final String gradingStatus;
        public final boolean correctAnswersVisible;

        public ReviewScoreSummary(
                double earnedMarks,
                double totalMarks,
                @Nullable Double finalScore,
                int totalQuestions,
                int pendingCount,
                int manualGradedCount,
                int autoGradedCount,
                int unansweredCount,
                boolean isProvisional,
                String gradingStatus,
                boolean correctAnswersVisible
        ) {
            this.earnedMarks = earnedMarks;
            this.totalMarks = totalMarks;
            this.finalScore = finalScore;
            this.totalQuestions = totalQuestions;
            this.pendingCount = pendingCount;
            this.manualGradedCount = manualGradedCount;
            this.autoGradedCount = autoGradedCount;
            this.unansweredCount = unansweredCount;
            this.isProvisional = isProvisional;
            this.gradingStatus = gradingStatus;
            this.correctAnswersVisible = correctAnswersVisible;
        }

        @NonNull
        public String formatScoreLine() {
            double scoreVal = (!isProvisional && finalScore != null) ? finalScore : earnedMarks;
            String earnedStr = AssessmentUiHelper.formatMarks(scoreVal);
            String totalStr = AssessmentUiHelper.formatMarks(totalMarks);
            if (isProvisional) {
                return "Score: " + earnedStr + " / " + totalStr + " earned";
            }
            return "Score: " + earnedStr + " / " + totalStr;
        }

        @Nullable
        public String formatProvisionalNote() {
            if (!isProvisional) {
                return null;
            }
            if (STATUS_PARTIALLY_GRADED.equalsIgnoreCase(gradingStatus)) {
                return "Provisional score — some answers still pending teacher grading";
            }
            return "Provisional score — awaiting teacher grading (not final)";
        }
    }

    public static final class MarksValidationResult {
        public final boolean valid;
        public final double marks;
        public final @Nullable String errorMessage;

        private MarksValidationResult(boolean valid, double marks, @Nullable String errorMessage) {
            this.valid = valid;
            this.marks = marks;
            this.errorMessage = errorMessage;
        }

        public static MarksValidationResult ok(double marks) {
            return new MarksValidationResult(true, marks, null);
        }

        public static MarksValidationResult error(String message) {
            return new MarksValidationResult(false, 0.0, message);
        }
    }

    @NonNull
    public static String resolveAttemptGradingStatus(
            @Nullable String rawGradingStatus,
            @Nullable String rawAttemptStatus,
            boolean pendingManual,
            int pendingQuestionsCount,
            int gradedManualCount,
            double manualGradedScore
    ) {
        String attStatus = rawAttemptStatus != null ? rawAttemptStatus.trim().toLowerCase(Locale.US) : "";
        String grStatus = rawGradingStatus != null ? rawGradingStatus.trim().toLowerCase(Locale.US) : "";

        if ("in_progress".equals(attStatus)) {
            return STATUS_IN_PROGRESS;
        }
        if ("not_submitted".equals(grStatus) && attStatus.isEmpty()) {
            return STATUS_NOT_SUBMITTED;
        }
        boolean hasPending = pendingManual
                || pendingQuestionsCount > 0
                || "pending_grading".equals(grStatus)
                || "submitted".equals(attStatus);
        if (hasPending) {
            if (gradedManualCount > 0 || manualGradedScore > 0.0001) {
                return STATUS_PARTIALLY_GRADED;
            }
            return STATUS_PENDING_GRADING;
        }
        if ("graded".equals(grStatus)
                || "auto_graded".equals(grStatus)
                || "completed".equals(attStatus)
                || "graded".equals(attStatus)
                || "timed_out".equals(attStatus)) {
            return STATUS_GRADED;
        }
        return STATUS_NOT_SUBMITTED;
    }

    @NonNull
    public static ReviewScoreSummary parseStudentReviewSummary(@Nullable JsonObject reviewData) {
        if (reviewData == null) {
            return new ReviewScoreSummary(0, 0, null, 0, 0, 0, 0, 0, true, STATUS_PENDING_GRADING, false);
        }

        JsonObject result = reviewData.has("result") && reviewData.get("result").isJsonObject()
                ? reviewData.getAsJsonObject("result") : new JsonObject();
        JsonArray questions = reviewData.has("questions") && reviewData.get("questions").isJsonArray()
                ? reviewData.getAsJsonArray("questions") : new JsonArray();

        double earned = 0.0;
        double sumQuestionMarks = 0.0;
        int pendingCount = 0;
        int manualGradedCount = 0;
        int autoGradedCount = 0;
        int unansweredCount = 0;

        for (JsonElement el : questions) {
            if (!el.isJsonObject()) continue;
            JsonObject q = el.getAsJsonObject();
            double qMax = AssessmentUiHelper.optDouble(q, "marks", 0.0);
            sumQuestionMarks += Math.max(0.0, qMax);

            String gStatus = AssessmentUiHelper.optString(q, "grading_status", "").toLowerCase(Locale.US);
            String qType = AssessmentUiHelper.optString(q, "question_type", "").toLowerCase(Locale.US);
            boolean hasMarks = q.has("marks_obtained") && !q.get("marks_obtained").isJsonNull();

            if ("pending_grading".equals(gStatus)
                    || ("short_answer".equals(qType) && !"graded".equals(gStatus) && !"auto_graded".equals(gStatus) && !"unanswered".equals(gStatus) && !hasMarks)) {
                pendingCount++;
            } else if ("unanswered".equals(gStatus)) {
                unansweredCount++;
            } else {
                if ("graded".equals(gStatus)) {
                    manualGradedCount++;
                } else {
                    autoGradedCount++;
                }
                if (hasMarks) {
                    earned += q.get("marks_obtained").getAsDouble();
                }
            }
        }

        double totalMarks = AssessmentUiHelper.optDouble(result, "total_marks", 0.0);
        if (totalMarks <= 0.0) {
            totalMarks = sumQuestionMarks;
        }
        earned = round2(earned);
        totalMarks = round2(totalMarks);

        boolean awaitingManual = AssessmentUiHelper.optBoolean(result, "awaiting_manual_grading", false);
        boolean scoreReleased = AssessmentUiHelper.optBoolean(result, "score_released", false);
        String rawResultGradingStatus = AssessmentUiHelper.optString(result, "grading_status", "");
        String rawResultStatus = AssessmentUiHelper.optString(result, "status", "");
        Double finalScore = (result.has("score") && !result.get("score").isJsonNull())
                ? round2(result.get("score").getAsDouble()) : null;

        boolean isProvisional = pendingCount > 0
                || awaitingManual
                || !scoreReleased
                || "pending_grading".equalsIgnoreCase(rawResultGradingStatus)
                || "submitted".equalsIgnoreCase(rawResultStatus);

        String gradingStatus;
        if (isProvisional) {
            gradingStatus = manualGradedCount > 0 ? STATUS_PARTIALLY_GRADED : STATUS_PENDING_GRADING;
        } else {
            gradingStatus = STATUS_GRADED;
            if (finalScore != null) {
                earned = finalScore;
            }
        }

        boolean correctVisible = AssessmentUiHelper.optBoolean(result, "correct_answers_visible", false);

        return new ReviewScoreSummary(
                earned,
                totalMarks,
                finalScore,
                questions.size(),
                pendingCount,
                manualGradedCount,
                autoGradedCount,
                unansweredCount,
                isProvisional,
                gradingStatus,
                correctVisible
        );
    }

    @NonNull
    public static Assessment parseStudentQuizItem(@NonNull JsonObject obj, @Nullable String overrideDateText) {
        String id = AssessmentUiHelper.optString(obj, "id", "");
        String title = AssessmentUiHelper.optString(obj, "title", "Quiz");
        String startAt = AssessmentUiHelper.optString(obj, "start_at", "");
        String endAt = AssessmentUiHelper.optString(obj, "end_at", "");
        String effectiveEndAt = AssessmentUiHelper.optString(obj, "effective_end_at", endAt);
        if (!effectiveEndAt.isEmpty()) {
            endAt = effectiveEndAt;
        }
        String rawDate = !endAt.isEmpty() ? endAt : (!startAt.isEmpty() ? startAt : AssessmentUiHelper.optString(obj, "created_at", ""));
        String dateStr = overrideDateText != null
                ? overrideDateText
                : ((!endAt.isEmpty() ? "Closes: " : "") + AssessmentUiHelper.formatDateShort(rawDate));

        double totalMarks = AssessmentUiHelper.optDouble(obj, "total_marks", 0.0);
        if (totalMarks <= 0.0) {
            totalMarks = AssessmentUiHelper.optDouble(obj, "total_score", 0.0);
        }
        int questionsCount = AssessmentUiHelper.optInt(obj, "questions_count", 0);
        int durationMins = AssessmentUiHelper.optInt(obj, "duration_minutes", 0);
        String rawStatus = AssessmentUiHelper.optString(obj, "status", "published");
        String windowState = AssessmentUiHelper.optString(obj, "window_state", "");
        int attemptsUsed = AssessmentUiHelper.optInt(obj, "attempts_used", 0);
        boolean hasMaxCap = obj.has("max_attempts") && !obj.get("max_attempts").isJsonNull();
        int maxAttempts = hasMaxCap ? AssessmentUiHelper.optInt(obj, "max_attempts", 1) : 0;
        boolean canStart = AssessmentUiHelper.optBoolean(obj, "can_start", false);
        boolean canContinue = AssessmentUiHelper.optBoolean(obj, "can_continue", false);
        boolean hasPendingGrading = AssessmentUiHelper.optBoolean(obj, "has_pending_grading", false);
        boolean hasEffectiveScore = obj.has("effective_score") && !obj.get("effective_score").isJsonNull();
        double effectiveScoreVal = hasEffectiveScore ? AssessmentUiHelper.optDouble(obj, "effective_score", 0.0) : 0.0;

        boolean inProgress = canContinue;
        boolean isSubmitted = attemptsUsed > 0;
        boolean isGraded = false;
        boolean attemptPending = false;
        double latestSubmittedScore = 0.0;
        boolean hasLatestSubmittedScore = false;
        double latestAttemptSnapshotTotal = 0.0;
        double latestAutoScore = 0.0;
        boolean hasLatestAutoScore = false;
        double latestManualScore = 0.0;
        String latestSubmittedAttemptId = null;
        String latestSubmittedStatus = "";
        String latestSubmittedGradingStatus = "";
        boolean latestIsLate = false;

        if (obj.has("attempts") && obj.get("attempts").isJsonArray()) {
            JsonArray atts = obj.getAsJsonArray("attempts");
            for (JsonElement attEl : atts) {
                if (!attEl.isJsonObject()) continue;
                JsonObject att = attEl.getAsJsonObject();
                boolean isReset = AssessmentUiHelper.optBoolean(att, "is_reset", false);
                if (isReset) continue;

                String st = AssessmentUiHelper.optString(att, "status", "");
                String gr = AssessmentUiHelper.optString(att, "grading_status", "");
                if ("in_progress".equalsIgnoreCase(st)) {
                    inProgress = true;
                    continue;
                }

                // First non-reset submitted/completed attempt (array is ordered desc by attempt_number)
                isSubmitted = true;
                if (latestSubmittedAttemptId == null) {
                    latestSubmittedAttemptId = AssessmentUiHelper.optString(att, "id", null);
                    latestSubmittedStatus = st;
                    latestSubmittedGradingStatus = gr;
                    latestIsLate = AssessmentUiHelper.optBoolean(att, "is_late", false);
                    latestAttemptSnapshotTotal = AssessmentUiHelper.optDouble(att, "total_marks_snapshot", 0.0);

                    if (att.has("auto_graded_score") && !att.get("auto_graded_score").isJsonNull()) {
                        latestAutoScore = att.get("auto_graded_score").getAsDouble();
                        hasLatestAutoScore = true;
                    }
                    if (att.has("manual_graded_score") && !att.get("manual_graded_score").isJsonNull()) {
                        latestManualScore = att.get("manual_graded_score").getAsDouble();
                    }

                    if ("pending_grading".equalsIgnoreCase(gr) || "submitted".equalsIgnoreCase(st)) {
                        attemptPending = true;
                    } else if (("completed".equalsIgnoreCase(st)
                            || "timed_out".equalsIgnoreCase(st)
                            || "graded".equalsIgnoreCase(st))
                            && att.has("score") && !att.get("score").isJsonNull()) {
                        latestSubmittedScore = att.get("score").getAsDouble();
                        hasLatestSubmittedScore = true;
                        isGraded = true;
                    }
                }
            }
        }

        if (latestAttemptSnapshotTotal > 0.0) {
            totalMarks = latestAttemptSnapshotTotal;
        }
        if (totalMarks <= 0.0) {
            totalMarks = 100.0;
        }

        boolean pendingFinal = hasPendingGrading || attemptPending;
        double exactScoreVal = 0.0;
        if (hasEffectiveScore && !pendingFinal) {
            exactScoreVal = effectiveScoreVal;
            isGraded = true;
        } else if (!pendingFinal && hasLatestSubmittedScore) {
            exactScoreVal = latestSubmittedScore;
            isGraded = true;
        } else if (pendingFinal) {
            exactScoreVal = 0.0;
            isGraded = false;
        }

        boolean isPartiallyGraded = pendingFinal && latestManualScore > 0.0001;
        Double provisionalEarned = null;
        if (pendingFinal && (hasLatestAutoScore || latestManualScore > 0.0001)) {
            provisionalEarned = round2(latestAutoScore + latestManualScore);
        }

        boolean isClosed = "closed".equalsIgnoreCase(windowState)
                || (windowState.isEmpty() && ("closed".equalsIgnoreCase(rawStatus) || (!endAt.isEmpty() && AssessmentUiHelper.isPast(endAt))));
        boolean isUpcoming = "upcoming".equalsIgnoreCase(windowState)
                || (windowState.isEmpty() && !startAt.isEmpty() && AssessmentUiHelper.isFuture(startAt));
        boolean isLateOpen = "late_open".equalsIgnoreCase(windowState);

        String attemptInfo = hasMaxCap ? (attemptsUsed + "/" + maxAttempts) : (attemptsUsed > 0 ? attemptsUsed + "/∞" : "");
        String gradingStatusLabel = null;
        String statusLabel;
        String actionLabel;

        if (inProgress && !isClosed) {
            statusLabel = STATUS_IN_PROGRESS;
            actionLabel = "Continue Quiz";
        } else if (pendingFinal) {
            gradingStatusLabel = isPartiallyGraded ? STATUS_PARTIALLY_GRADED : STATUS_PENDING_GRADING;
            statusLabel = gradingStatusLabel;
            actionLabel = canStart ? "Retake Quiz" : "Review Answers";
        } else if (isGraded) {
            gradingStatusLabel = STATUS_GRADED;
            statusLabel = attemptsUsed > 1 && !attemptInfo.isEmpty() ? "Graded (" + attemptInfo + ")" : STATUS_GRADED;
            actionLabel = canStart ? "Retake Quiz" : "Review Answers";
        } else if (isSubmitted) {
            gradingStatusLabel = STATUS_PENDING_GRADING;
            statusLabel = STATUS_PENDING_GRADING;
            actionLabel = canStart ? "Retake Quiz" : "Review Answers";
        } else if (isClosed) {
            statusLabel = "Closed";
            actionLabel = "View Details";
        } else if (isUpcoming) {
            statusLabel = "Upcoming";
            actionLabel = "View Details";
        } else if (isLateOpen) {
            statusLabel = "Late Window Open";
            actionLabel = "Start Quiz";
        } else {
            statusLabel = "Available";
            actionLabel = attemptsUsed > 0 ? "Retake Quiz" : "Start Quiz";
        }

        String courseCode = "";
        String courseName = "";
        if (obj.has("course") && obj.get("course").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("course");
            courseCode = AssessmentUiHelper.optString(c, "code", AssessmentUiHelper.optString(c, "course_code", ""));
            courseName = AssessmentUiHelper.optString(c, "name", AssessmentUiHelper.optString(c, "course_name", ""));
        }

        String className = "";
        String semesterName = "";
        String academicYearName = "";
        if (obj.has("academic_context") && obj.get("academic_context").isJsonObject()) {
            JsonObject ctx = obj.getAsJsonObject("academic_context");
            className = AssessmentUiHelper.optString(ctx, "class_name", "");
            semesterName = AssessmentUiHelper.optString(ctx, "semester_name", "");
            academicYearName = AssessmentUiHelper.optString(ctx, "academic_year_name", "");
        }
        if (semesterName.isEmpty() && obj.has("semester") && obj.get("semester").isJsonObject()) {
            JsonObject semObj = obj.getAsJsonObject("semester");
            semesterName = AssessmentUiHelper.optString(semObj, "name",
                    AssessmentUiHelper.optString(semObj, "semester_name", ""));
        } else if (semesterName.isEmpty() && obj.has("semester_id") && !obj.get("semester_id").isJsonNull()) {
            semesterName = "Semester " + obj.get("semester_id").getAsString();
        }

        Assessment item = new Assessment(
                id,
                title,
                dateStr,
                (int) Math.round(exactScoreVal),
                (int) Math.round(totalMarks),
                "quiz",
                semesterName,
                courseName,
                isSubmitted,
                isGraded
        );
        item.setCourseCode(courseCode);
        item.setClassName(className);
        item.setAcademicYearName(academicYearName);
        item.setRawStatus(rawStatus);
        item.setQuestionsCount(questionsCount);
        item.setDurationMinutes(durationMins);
        item.setInProgress(inProgress);
        item.setExactScore(isGraded ? round2(exactScoreVal) : null);
        item.setExactTotalScore(round2(totalMarks));
        item.setProvisionalEarnedScore(provisionalEarned);
        item.setPendingGrading(pendingFinal);
        item.setPartiallyGraded(isPartiallyGraded);
        item.setGradingStatusLabel(gradingStatusLabel);
        item.setLatestAttemptId(latestSubmittedAttemptId);
        item.setReviewAllowed(isSubmitted && latestSubmittedAttemptId != null && !latestSubmittedAttemptId.isEmpty());

        String serverSubStatus = AssessmentUiHelper.optString(obj, "student_submission_status", "");
        if (serverSubStatus.isEmpty()) {
            if (isSubmitted) {
                serverSubStatus = latestIsLate ? "Late Submitted" : "Submitted";
            } else if (inProgress) {
                serverSubStatus = STATUS_IN_PROGRESS;
            } else {
                serverSubStatus = STATUS_NOT_SUBMITTED;
            }
        }
        item.setSubmissionStatusLabel(serverSubStatus);

        ReopenPermissionHelper.applyToAssessment(item, obj);
        item.setAssessmentApiType("quiz");

        String srvReopen = item.getReopenStatus();
        if (item.getExtendedDeadline() != null && !item.getExtendedDeadline().isEmpty() && overrideDateText == null) {
            String updatedDate = "Extended: " + AssessmentUiHelper.formatDateShort(item.getExtendedDeadline());
            if (item.getRemainingAttempts() != null && item.getOriginalAttemptLimit() != null) {
                updatedDate += " • " + item.getRemainingAttempts() + " left";
            }
            item.setDate(updatedDate);
        }

        // Preserve explicit grading status instead of letting reopen_status="Submitted" clobber Pending/Partially/Graded
        if (inProgress && !isClosed) {
            item.setStatusLabel(STATUS_IN_PROGRESS);
            item.setActionLabel("Continue Quiz");
        } else if (pendingFinal) {
            item.setStatusLabel(gradingStatusLabel);
            item.setActionLabel(canStart ? "Retake Quiz" : "Review Answers");
        } else if (isGraded) {
            if ("Reopened".equalsIgnoreCase(srvReopen) || "Extended".equalsIgnoreCase(srvReopen)) {
                item.setStatusLabel(srvReopen);
            } else {
                item.setStatusLabel(statusLabel);
            }
            item.setActionLabel(canStart ? "Retake Quiz" : "Review Answers");
        } else if (srvReopen != null && !srvReopen.isEmpty() && !"Not Submitted".equalsIgnoreCase(srvReopen)) {
            item.setStatusLabel(srvReopen);
            item.setActionLabel(canStart ? (attemptsUsed > 0 ? "Retake Quiz" : "Start Quiz")
                    : (isSubmitted ? "Review Answers" : "View Details"));
        } else {
            item.setStatusLabel(statusLabel);
            item.setActionLabel(actionLabel);
        }

        return item;
    }

    public static void applyReviewSummaryToAssessment(@Nullable Assessment item, @Nullable ReviewScoreSummary summary) {
        if (item == null || summary == null) {
            return;
        }
        if (summary.totalMarks > 0.0) {
            item.setExactTotalScore(summary.totalMarks);
            item.setTotalScore((int) Math.round(summary.totalMarks));
        }

        if (summary.isProvisional) {
            item.setPendingGrading(true);
            item.setPartiallyGraded(STATUS_PARTIALLY_GRADED.equalsIgnoreCase(summary.gradingStatus));
            item.setGraded(false);
            item.setProvisionalEarnedScore(summary.earnedMarks);
            item.setGradingStatusLabel(summary.gradingStatus);
            if (!item.isInProgress()) {
                item.setStatusLabel(summary.gradingStatus);
            }
        } else {
            double finalVal = summary.finalScore != null ? summary.finalScore : summary.earnedMarks;
            item.setPendingGrading(false);
            item.setPartiallyGraded(false);
            item.setGraded(true);
            item.setSubmitted(true);
            item.setExactScore(round2(finalVal));
            item.setScore((int) Math.round(finalVal));
            item.setProvisionalEarnedScore(null);
            item.setGradingStatusLabel(STATUS_GRADED);
            if (!"Reopened".equalsIgnoreCase(item.getReopenStatus())
                    && !"Extended".equalsIgnoreCase(item.getReopenStatus())) {
                item.setStatusLabel(STATUS_GRADED);
            }
        }
    }

    @NonNull
    public static String formatQuestionType(@Nullable String rawType) {
        if (rawType == null) return "Multiple Choice";
        String norm = rawType.trim().toLowerCase(Locale.US);
        if ("short_answer".equals(norm)) return "Short Answer";
        if ("true_false".equals(norm)) return "True / False";
        return "Multiple Choice";
    }

    @NonNull
    public static String formatQuestionGradingStatus(@Nullable String rawStatus, boolean isPending) {
        if (isPending || "pending_grading".equalsIgnoreCase(rawStatus)) {
            return STATUS_PENDING_GRADING;
        }
        if ("unanswered".equalsIgnoreCase(rawStatus)) {
            return "Unanswered";
        }
        if ("graded".equalsIgnoreCase(rawStatus) || "auto_graded".equalsIgnoreCase(rawStatus)) {
            return STATUS_GRADED;
        }
        return STATUS_PENDING_GRADING;
    }

    @NonNull
    public static MarksValidationResult validateMarksInput(@Nullable String rawInput, double maxMarks) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return MarksValidationResult.error("Please enter marks.");
        }
        double parsed;
        try {
            parsed = Double.parseDouble(rawInput.trim());
        } catch (NumberFormatException e) {
            return MarksValidationResult.error("Enter a valid numeric mark.");
        }
        if (Double.isNaN(parsed) || Double.isInfinite(parsed)) {
            return MarksValidationResult.error("Enter a valid numeric mark.");
        }
        if (parsed < 0.0) {
            return MarksValidationResult.error("Marks cannot be negative.");
        }
        if (maxMarks >= 0.0 && parsed > maxMarks + 0.0001) {
            return MarksValidationResult.error("Marks cannot exceed maximum marks (" + AssessmentUiHelper.formatMarks(maxMarks) + ").");
        }
        return MarksValidationResult.ok(round2(parsed));
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
