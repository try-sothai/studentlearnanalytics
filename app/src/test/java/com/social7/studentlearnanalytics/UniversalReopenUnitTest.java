package com.social7.studentlearnanalytics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.utils.ReopenPermissionHelper;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class UniversalReopenUnitTest {

    @Test
    public void normalizeAssessmentType_handlesVariantsUniformly() {
        assertEquals("quiz", ReopenPermissionHelper.normalizeAssessmentType("quiz"));
        assertEquals("quiz", ReopenPermissionHelper.normalizeAssessmentType("Quizzes"));
        assertEquals("homework", ReopenPermissionHelper.normalizeAssessmentType("homework"));
        assertEquals("homework", ReopenPermissionHelper.normalizeAssessmentType(" HOMEWORK "));
        assertEquals("assignment", ReopenPermissionHelper.normalizeAssessmentType("assignment"));
        assertEquals("assignment", ReopenPermissionHelper.normalizeAssessmentType("file_upload"));
        assertEquals("assignment", ReopenPermissionHelper.normalizeAssessmentType("project"));
        assertEquals("assignment", ReopenPermissionHelper.normalizeAssessmentType("lab"));
        assertEquals("assignment", ReopenPermissionHelper.normalizeAssessmentType(null));
        assertEquals("assignment", ReopenPermissionHelper.normalizeAssessmentType(""));
    }

    @Test
    public void resolveBadgeColorHex_distinguishesAllSevenStatuses() {
        String[] statuses = new String[]{
                ReopenPermissionHelper.STATUS_NOT_SUBMITTED,
                ReopenPermissionHelper.STATUS_IN_PROGRESS,
                ReopenPermissionHelper.STATUS_SUBMITTED,
                ReopenPermissionHelper.STATUS_LATE_SUBMITTED,
                ReopenPermissionHelper.STATUS_REOPENED,
                ReopenPermissionHelper.STATUS_EXTENDED,
                ReopenPermissionHelper.STATUS_EXPIRED
        };
        Set<String> distinctColors = new HashSet<>();
        for (String status : statuses) {
            String hex = ReopenPermissionHelper.resolveBadgeColorHex(status);
            assertNotNull(hex);
            assertTrue("Expected hex color for " + status, hex.startsWith("#") && hex.length() == 7);
            distinctColors.add(hex);
        }
        assertEquals("All 7 reopen statuses must map to distinct badge colors", 7, distinctColors.size());
    }

    @Test
    public void parseStatusPayloadAndApplyToAssessment_strictlyFollowsServerPermissions() {
        JsonObject reopenedGradedJson = JsonParser.parseString("{"
                + "\"assessment_type\": \"homework\","
                + "\"assessment_id\": 25,"
                + "\"class_id\": 9,"
                + "\"student_id\": 102,"
                + "\"original_deadline\": \"2026-11-20T23:59:59+07:00\","
                + "\"extended_deadline\": \"2026-11-28T23:59:59+07:00\","
                + "\"effective_deadline\": \"2026-11-28T23:59:59+07:00\","
                + "\"student_submission_status\": \"Submitted\","
                + "\"reopen_status\": \"Reopened\","
                + "\"window_state\": \"late_open\","
                + "\"original_attempt_limit\": 1,"
                + "\"additional_attempts\": 1,"
                + "\"effective_attempt_limit\": 2,"
                + "\"used_attempts\": 1,"
                + "\"remaining_attempts\": 1,"
                + "\"allow_submission\": true,"
                + "\"allow_late_submission\": true,"
                + "\"allow_resubmission\": true,"
                + "\"can_submit\": true,"
                + "\"can_resubmit\": true,"
                + "\"has_active_override\": true,"
                + "\"blocked_reason\": null"
                + "}").getAsJsonObject();

        ReopenPermissionHelper.ReopenStatusModel m = ReopenPermissionHelper.parseStatusPayload(reopenedGradedJson);
        assertEquals("homework", m.assessmentType);
        assertEquals(25, m.assessmentId);
        assertEquals(Integer.valueOf(9), m.classId);
        assertEquals("Reopened", m.reopenStatus);
        assertEquals("Submitted", m.studentSubmissionStatus);
        assertEquals(Integer.valueOf(2), m.effectiveAttemptLimit);
        assertEquals(Integer.valueOf(1), m.remainingAttempts);
        assertTrue(m.canSubmit);
        assertTrue(m.canResubmit);
        assertTrue(m.allowLateSubmission);
        assertTrue(m.hasActiveOverride);

        Assessment item = new Assessment("25", "HW 1", "Due", 70, 100, "homework", "S1", "CS405", true, true);
        ReopenPermissionHelper.applyToAssessment(item, reopenedGradedJson);
        assertEquals("homework", item.getAssessmentApiType());
        assertEquals("Reopened", item.getReopenStatus());
        assertTrue(item.isCanSubmit());
        assertTrue(item.isCanResubmit());
        assertEquals("2026-11-20T23:59:59+07:00", item.getOriginalDeadline());
        assertEquals("2026-11-28T23:59:59+07:00", item.getExtendedDeadline());

        // Revoked / Expired payload must force canSubmit=false cleanly
        JsonObject revokedJson = JsonParser.parseString("{"
                + "\"assessment_type\": \"assignment\","
                + "\"assessment_id\": 25,"
                + "\"student_id\": 102,"
                + "\"original_attempt_limit\": 1,"
                + "\"additional_attempts\": 0,"
                + "\"used_attempts\": 0,"
                + "\"remaining_attempts\": 1,"
                + "\"student_submission_status\": \"Not Submitted\","
                + "\"reopen_status\": \"Expired\","
                + "\"allow_submission\": false,"
                + "\"allow_resubmission\": false,"
                + "\"can_submit\": false,"
                + "\"can_resubmit\": false,"
                + "\"has_active_override\": false,"
                + "\"blocked_reason\": \"Submission permission has been revoked for your account.\""
                + "}").getAsJsonObject();

        ReopenPermissionHelper.ReopenStatusModel rev = ReopenPermissionHelper.parseStatusPayload(revokedJson);
        assertFalse(rev.allowSubmission);
        assertFalse(rev.canSubmit);
        assertFalse(rev.canResubmit);
        assertEquals("Expired", rev.reopenStatus);
        assertEquals("Submission permission has been revoked for your account.", rev.blockedReason);
    }

    @Test
    public void formatSummaries_preserveOriginalAndExtendedDeadlinesAndAttempts() {
        assertEquals("1 / 2 used (1 left)", ReopenPermissionHelper.formatAttemptSummary(1, 2, 1));
        assertEquals("2 / Unlimited", ReopenPermissionHelper.formatAttemptSummary(2, null, null));

        String dl = ReopenPermissionHelper.formatDeadlineSummary(
                "2026-11-20T23:59:59+07:00",
                "2026-11-25T23:59:59+07:00"
        );
        assertTrue(dl.contains("Original:"));
        assertTrue(dl.contains("Extended:"));
        assertEquals("No deadline", ReopenPermissionHelper.formatDeadlineSummary(null, null));
    }
}
