package com.social7.studentlearnanalytics.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.model.Assessment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Pure-Java parser & formatter for SLAS Universal Reopen, Retake & Late Submission API contracts.
 * Android never independently grants permissions or overrides server deadlines.
 */
public final class ReopenPermissionHelper {

    public static final String STATUS_NOT_SUBMITTED = "Not Submitted";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_LATE_SUBMITTED = "Late Submitted";
    public static final String STATUS_REOPENED = "Reopened";
    public static final String STATUS_EXTENDED = "Extended";
    public static final String STATUS_EXPIRED = "Expired";

    private ReopenPermissionHelper() {
    }

    @NonNull
    public static String normalizeAssessmentType(@Nullable String raw) {
        if (raw == null) {
            return "assignment";
        }
        String norm = raw.trim().toLowerCase(Locale.US).replace('-', '_').replace(' ', '_');
        if ("quiz".equals(norm) || "quizzes".equals(norm)) {
            return "quiz";
        }
        if ("homework".equals(norm)) {
            return "homework";
        }
        return "assignment";
    }

    public static final class ReopenStatusModel {
        public final String assessmentType;
        public final int assessmentId;
        public final @Nullable Integer classId;
        public final int studentId;
        public final @Nullable String originalDeadline;
        public final @Nullable String extendedDeadline;
        public final @Nullable String effectiveDeadline;
        public final String studentSubmissionStatus;
        public final String reopenStatus;
        public final String windowState;
        public final @Nullable Integer originalAttemptLimit;
        public final int additionalAttempts;
        public final @Nullable Integer effectiveAttemptLimit;
        public final int usedAttempts;
        public final @Nullable Integer remainingAttempts;
        public final boolean allowSubmission;
        public final boolean allowLateSubmission;
        public final boolean allowResubmission;
        public final boolean canSubmit;
        public final boolean canResubmit;
        public final boolean canStart;
        public final boolean canContinue;
        public final boolean hasActiveOverride;
        public final @Nullable String blockedReason;

        public ReopenStatusModel(
                String assessmentType,
                int assessmentId,
                @Nullable Integer classId,
                int studentId,
                @Nullable String originalDeadline,
                @Nullable String extendedDeadline,
                @Nullable String effectiveDeadline,
                String studentSubmissionStatus,
                String reopenStatus,
                String windowState,
                @Nullable Integer originalAttemptLimit,
                int additionalAttempts,
                @Nullable Integer effectiveAttemptLimit,
                int usedAttempts,
                @Nullable Integer remainingAttempts,
                boolean allowSubmission,
                boolean allowLateSubmission,
                boolean allowResubmission,
                boolean canSubmit,
                boolean canResubmit,
                boolean canStart,
                boolean canContinue,
                boolean hasActiveOverride,
                @Nullable String blockedReason
        ) {
            this.assessmentType = assessmentType;
            this.assessmentId = assessmentId;
            this.classId = classId;
            this.studentId = studentId;
            this.originalDeadline = originalDeadline;
            this.extendedDeadline = extendedDeadline;
            this.effectiveDeadline = effectiveDeadline;
            this.studentSubmissionStatus = studentSubmissionStatus;
            this.reopenStatus = reopenStatus;
            this.windowState = windowState;
            this.originalAttemptLimit = originalAttemptLimit;
            this.additionalAttempts = additionalAttempts;
            this.effectiveAttemptLimit = effectiveAttemptLimit;
            this.usedAttempts = usedAttempts;
            this.remainingAttempts = remainingAttempts;
            this.allowSubmission = allowSubmission;
            this.allowLateSubmission = allowLateSubmission;
            this.allowResubmission = allowResubmission;
            this.canSubmit = canSubmit;
            this.canResubmit = canResubmit;
            this.canStart = canStart;
            this.canContinue = canContinue;
            this.hasActiveOverride = hasActiveOverride;
            this.blockedReason = blockedReason;
        }
    }

    @NonNull
    public static ReopenStatusModel parseStatusPayload(@Nullable JsonObject obj) {
        if (obj == null) {
            return new ReopenStatusModel(
                    "assignment", 0, null, 0,
                    null, null, null,
                    STATUS_NOT_SUBMITTED, STATUS_NOT_SUBMITTED, "open",
                    1, 0, 1, 0, 1,
                    true, false, false, false, false, false, false, false, null
            );
        }

        String rawType = optNullableString(obj, "assessment_type");
        if (rawType == null) {
            rawType = optNullableString(obj, "assignment_type");
        }
        String type = normalizeAssessmentType(rawType);
        int assessmentId = optInt(obj, "assessment_id", optInt(obj, "id", 0));
        Integer classId = optNullableInt(obj, "class_id");
        int studentId = optInt(obj, "student_id", 0);

        String originalDeadline = optNullableString(obj, "original_deadline");
        if (originalDeadline == null) {
            originalDeadline = "quiz".equals(type)
                    ? optNullableString(obj, "end_at")
                    : optNullableString(obj, "due_date");
        }
        String extendedDeadline = optNullableString(obj, "extended_deadline");
        String effectiveDeadline = optNullableString(obj, "effective_deadline");
        if (effectiveDeadline == null) {
            effectiveDeadline = extendedDeadline != null ? extendedDeadline
                    : (optNullableString(obj, "effective_end_at") != null ? optNullableString(obj, "effective_end_at") : originalDeadline);
        }

        String submissionStatus = optNullableString(obj, "student_submission_status");
        if (submissionStatus == null) {
            submissionStatus = STATUS_NOT_SUBMITTED;
        }
        String reopenStatus = optNullableString(obj, "reopen_status");
        if (reopenStatus == null) {
            reopenStatus = submissionStatus;
        }
        String windowState = optNullableString(obj, "window_state");
        if (windowState == null) {
            windowState = "open";
        }

        Integer origLimit = optNullableInt(obj, "original_attempt_limit");
        if (origLimit == null && obj.has("max_attempts")) {
            origLimit = optNullableInt(obj, "max_attempts");
        }
        int additional = optInt(obj, "additional_attempts", optInt(obj, "extra_attempts", 0));
        Integer effectiveLimit = optNullableInt(obj, "effective_attempt_limit");
        if (effectiveLimit == null && origLimit != null) {
            effectiveLimit = origLimit + Math.max(0, additional);
        }
        int used = optInt(obj, "used_attempts", optInt(obj, "attempts_used", 0));
        Integer remaining = optNullableInt(obj, "remaining_attempts");
        if (remaining == null) {
            remaining = optNullableInt(obj, "attempts_remaining");
        }
        if (remaining == null && effectiveLimit != null) {
            remaining = Math.max(0, effectiveLimit - used);
        }

        boolean allowSubmission = optBoolean(obj, "allow_submission", true);
        boolean allowLate = optBoolean(obj, "allow_late_submission", false);
        boolean allowResubmit = optBoolean(obj, "allow_resubmission", false);
        boolean canStart = optBoolean(obj, "can_start", false);
        boolean canContinue = optBoolean(obj, "can_continue", false);
        boolean canResubmit = optBoolean(obj, "can_resubmit", false);
        boolean canSubmit = obj.has("can_submit")
                ? optBoolean(obj, "can_submit", false)
                : (canStart || canContinue || canResubmit);
        boolean hasOverride = optBoolean(obj, "has_active_override", obj.has("override") && obj.get("override").isJsonObject());
        String blockedReason = optNullableString(obj, "blocked_reason");

        return new ReopenStatusModel(
                type,
                assessmentId,
                classId,
                studentId,
                originalDeadline,
                extendedDeadline,
                effectiveDeadline,
                submissionStatus,
                reopenStatus,
                windowState,
                origLimit,
                additional,
                effectiveLimit,
                used,
                remaining,
                allowSubmission,
                allowLate,
                allowResubmit,
                canSubmit,
                canResubmit,
                canStart,
                canContinue,
                hasOverride,
                blockedReason
        );
    }

    public static void applyToAssessment(@Nullable Assessment a, @Nullable JsonObject obj) {
        if (a == null || obj == null) {
            return;
        }
        ReopenStatusModel m = parseStatusPayload(obj);
        a.setAssessmentApiType(m.assessmentType);
        a.setReopenStatus(m.reopenStatus);
        a.setStudentSubmissionStatus(m.studentSubmissionStatus);
        a.setOriginalDeadline(m.originalDeadline);
        a.setExtendedDeadline(m.extendedDeadline);
        a.setEffectiveDeadline(m.effectiveDeadline);
        a.setOriginalAttemptLimit(m.originalAttemptLimit);
        a.setAdditionalAttempts(m.additionalAttempts);
        a.setUsedAttempts(m.usedAttempts);
        a.setRemainingAttempts(m.remainingAttempts);
        a.setAllowSubmission(m.allowSubmission);
        a.setAllowLateSubmission(m.allowLateSubmission);
        a.setAllowResubmission(m.allowResubmission);
        a.setCanSubmit(m.canSubmit);
        a.setCanResubmit(m.canResubmit);
        a.setHasActiveOverride(m.hasActiveOverride);
        a.setBlockedReason(m.blockedReason);
        if (m.reopenStatus != null && !m.reopenStatus.isEmpty()) {
            a.setStatusLabel(m.reopenStatus);
        }
    }

    @NonNull
    public static String resolveBadgeColorHex(@Nullable String reopenStatus) {
        if (reopenStatus == null) {
            return "#5B3DF5";
        }
        String norm = reopenStatus.trim().toLowerCase(Locale.US);
        if (norm.contains("reopen")) {
            return "#7C3AED";
        }
        if (norm.contains("extend")) {
            return "#0284C7";
        }
        if (norm.contains("expir") || norm.contains("overdue")) {
            return "#DC2626";
        }
        if (norm.contains("late")) {
            return "#EA580C";
        }
        if (norm.contains("not submitted") || norm.contains("not_submitted")) {
            return "#64748B";
        }
        if (norm.contains("progress")) {
            return "#D97706";
        }
        if (norm.contains("submitted")) {
            return "#2563EB";
        }
        if (norm.contains("graded") || norm.contains("completed")) {
            return "#10B981";
        }
        return "#5B3DF5";
    }

    @NonNull
    public static String formatAttemptSummary(
            @Nullable Integer used,
            @Nullable Integer limit,
            @Nullable Integer remaining
    ) {
        int u = used != null ? Math.max(0, used) : 0;
        if (limit == null || limit <= 0) {
            return String.format(Locale.US, "%d / Unlimited", u);
        }
        int rem = remaining != null ? Math.max(0, remaining) : Math.max(0, limit - u);
        return String.format(Locale.US, "%d / %d used (%d left)", u, limit, rem);
    }

    @NonNull
    public static String formatDeadlineSummary(
            @Nullable String originalDeadline,
            @Nullable String extendedDeadline
    ) {
        String origFmt = formatIsoShort(originalDeadline);
        String extFmt = formatIsoShort(extendedDeadline);
        if (!extFmt.isEmpty() && !origFmt.isEmpty()) {
            return "Original: " + origFmt + " • Extended: " + extFmt;
        }
        if (!extFmt.isEmpty()) {
            return "Extended: " + extFmt;
        }
        if (!origFmt.isEmpty()) {
            return "Due: " + origFmt;
        }
        return "No deadline";
    }

    @NonNull
    public static String formatIsoShort(@Nullable String raw) {
        if (raw == null || raw.trim().isEmpty() || "null".equalsIgnoreCase(raw.trim())) {
            return "";
        }
        String trimmed = raw.trim();
        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(p, Locale.US);
                if (p.endsWith("'Z'")) {
                    parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date d = parser.parse(trimmed);
                if (d != null) {
                    return new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(d);
                }
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }

    @Nullable
    private static String optNullableString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return null;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return null;
        String v = el.getAsString();
        return v == null || v.trim().isEmpty() ? null : v.trim();
    }

    private static int optInt(JsonObject obj, String key, int fallback) {
        Integer v = optNullableInt(obj, key);
        return v != null ? v : fallback;
    }

    @Nullable
    private static Integer optNullableInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return null;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return null;
        try {
            return el.getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean optBoolean(JsonObject obj, String key, boolean fallback) {
        if (obj == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            if (el.getAsJsonPrimitive().isNumber()) {
                return el.getAsInt() != 0;
            }
            return el.getAsBoolean();
        } catch (Exception e) {
            return fallback;
        }
    }
}
