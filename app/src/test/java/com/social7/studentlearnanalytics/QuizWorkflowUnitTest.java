package com.social7.studentlearnanalytics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.model.QuizQuestionItem;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import org.junit.Test;

import static org.junit.Assert.*;

public class QuizWorkflowUnitTest {

    @Test
    public void quizQuestionItem_parsesRequiredFlagAndGradingGuidance() {
        JsonObject shortObj = JsonParser.parseString("{"
                + "\"id\": 42,"
                + "\"quiz_id\": 7,"
                + "\"question_order\": 3,"
                + "\"question_text\": \"Explain normalization.\","
                + "\"question_type\": \"short_answer\","
                + "\"marks\": 5.5,"
                + "\"is_required\": false,"
                + "\"correct_answer\": \"1NF, 2NF, 3NF\","
                + "\"grading_guidance\": \"Must cover removing transitive dependencies.\""
                + "}").getAsJsonObject();

        QuizQuestionItem shortItem = QuizQuestionItem.fromJson(shortObj);
        assertEquals(42, shortItem.id);
        assertEquals(3, shortItem.questionOrder);
        assertEquals(5.5, shortItem.marks, 0.001);
        assertFalse(shortItem.isRequired);
        assertEquals("Must cover removing transitive dependencies.", shortItem.gradingGuidance);
        assertEquals("Short Answer", shortItem.getTypeLabel());

        JsonObject mcqObj = JsonParser.parseString("{"
                + "\"id\": 43,"
                + "\"question_text\": \"Pick A\","
                + "\"question_type\": \"multiple_choice\","
                + "\"option_a\": \"Alpha\","
                + "\"option_b\": \"Beta\","
                + "\"correct_answer\": \"A\""
                + "}").getAsJsonObject();

        QuizQuestionItem mcqItem = QuizQuestionItem.fromJson(mcqObj);
        assertTrue(mcqItem.isRequired);
        assertEquals(2, mcqItem.optionCount);
        assertEquals("Multiple Choice", mcqItem.getTypeLabel());
    }

    @Test
    public void assessment_formattedScore_masksPendingGradingAndFormatsGraded() {
        Assessment pending = new Assessment("1", "Midterm Quiz", "Dec 10", 0, 20, "quiz", "Sem 1", "CS101", true, false);
        pending.setStatusLabel("Pending Grading");
        assertEquals("Pending Grading", pending.getFormattedScore());

        Assessment graded = new Assessment("1", "Midterm Quiz", "Dec 10", 17, 20, "quiz", "Sem 1", "CS101", true, true);
        assertEquals("17 / 20", graded.getFormattedScore());

        Assessment available = new Assessment("2", "Practice Quiz", "Dec 12", 0, 10, "quiz", "Sem 1", "CS101", false, false);
        assertEquals("Available", available.getFormattedScore());

        Assessment inProgress = new Assessment("3", "Timed Quiz", "Dec 12", 0, 10, "quiz", "Sem 1", "CS101", false, false);
        inProgress.setInProgress(true);
        assertEquals("In Progress", inProgress.getFormattedScore());
    }

    @Test
    public void assessmentUiHelper_handlesBooleansMarksAndStringsSafely() {
        JsonObject json = JsonParser.parseString("{"
                + "\"can_start\": true,"
                + "\"has_pending_grading\": false,"
                + "\"max_attempts\": null,"
                + "\"scoring_method\": \"  average  \""
                + "}").getAsJsonObject();

        assertTrue(AssessmentUiHelper.optBoolean(json, "can_start", false));
        assertFalse(AssessmentUiHelper.optBoolean(json, "has_pending_grading", true));
        assertTrue(AssessmentUiHelper.optBoolean(json, "max_attempts", true));
        assertEquals("average", AssessmentUiHelper.optString(json, "scoring_method", "highest"));
        assertEquals("3.5", AssessmentUiHelper.formatMarks(3.5));
        assertEquals("10", AssessmentUiHelper.formatMarks(10.0));
    }

    @Test
    public void parseStudentQuizItem_gradedMcqAndTrueFalse_displaysFinalScoreAndGradedStatus() {
        JsonObject quizJson = JsonParser.parseString("{"
                + "\"id\": 10,"
                + "\"title\": \"MC & TF Quiz\","
                + "\"total_score\": 4,"
                + "\"questions_count\": 3,"
                + "\"duration_minutes\": 20,"
                + "\"status\": \"published\","
                + "\"window_state\": \"open\","
                + "\"attempts_used\": 1,"
                + "\"max_attempts\": 1,"
                + "\"can_start\": false,"
                + "\"can_continue\": false,"
                + "\"has_pending_grading\": false,"
                + "\"effective_score\": 3,"
                + "\"student_submission_status\": \"Submitted\","
                + "\"reopen_status\": \"Submitted\","
                + "\"course\": {\"code\": \"CS101\", \"name\": \"Intro CS\"},"
                + "\"academic_context\": {\"class_name\": \"Class A\", \"semester_name\": \"Sem 1\"},"
                + "\"attempts\": [{"
                + "  \"id\": 101,"
                + "  \"attempt_number\": 1,"
                + "  \"status\": \"completed\","
                + "  \"grading_status\": \"auto_graded\","
                + "  \"score\": 3,"
                + "  \"total_marks_snapshot\": 4,"
                + "  \"is_reset\": false"
                + "}]"
                + "}").getAsJsonObject();

        Assessment item = QuizScoreUiHelper.parseStudentQuizItem(quizJson, null);
        assertTrue(item.isSubmitted());
        assertTrue(item.isGraded());
        assertFalse(item.isPendingGrading());
        assertEquals("3 / 4", item.getFormattedScore());
        assertEquals(QuizScoreUiHelper.STATUS_GRADED, item.getGradingStatusLabel());
        assertEquals("101", item.getLatestAttemptId());
        assertTrue(item.isReviewAllowed());
        assertEquals("Class A", item.getClassName());
        assertEquals("Submitted", item.getSubmissionStatusLabel());
    }

    @Test
    public void parseStudentQuizItem_shortAnswerPending_isNotClobberedBySubmittedReopenStatus() {
        JsonObject quizJson = JsonParser.parseString("{"
                + "\"id\": 11,"
                + "\"title\": \"Short Answer Quiz\","
                + "\"total_score\": 4,"
                + "\"questions_count\": 3,"
                + "\"status\": \"published\","
                + "\"window_state\": \"open\","
                + "\"attempts_used\": 1,"
                + "\"max_attempts\": 1,"
                + "\"can_start\": false,"
                + "\"can_continue\": false,"
                + "\"has_pending_grading\": true,"
                + "\"effective_score\": null,"
                + "\"student_submission_status\": \"Submitted\","
                + "\"reopen_status\": \"Submitted\","
                + "\"attempts\": [{"
                + "  \"id\": 202,"
                + "  \"attempt_number\": 1,"
                + "  \"status\": \"submitted\","
                + "  \"grading_status\": \"pending_grading\","
                + "  \"score\": null,"
                + "  \"auto_graded_score\": null,"
                + "  \"manual_graded_score\": null,"
                + "  \"total_marks_snapshot\": 4,"
                + "  \"is_reset\": false"
                + "}]"
                + "}").getAsJsonObject();

        Assessment item = QuizScoreUiHelper.parseStudentQuizItem(quizJson, null);
        assertTrue(item.isSubmitted());
        assertFalse(item.isGraded());
        assertTrue(item.isPendingGrading());
        assertEquals(QuizScoreUiHelper.STATUS_PENDING_GRADING, item.getStatusLabel());
        assertEquals(QuizScoreUiHelper.STATUS_PENDING_GRADING, item.getGradingStatusLabel());
        assertEquals("202", item.getLatestAttemptId());
        assertTrue(item.isReviewAllowed());
    }

    @Test
    public void studentReviewSummary_handlesPendingPartiallyGradedAndFinalizedMixedQuizzes() {
        // 1. Mixed quiz awaiting short answer grading: Q1(1/1), Q2(Short Answer pending /2), Q3(1/1) -> 2/4 earned
        JsonObject pendingReview = JsonParser.parseString("{"
                + "\"result\": {\"status\": \"submitted\", \"grading_status\": \"pending_grading\", \"score\": null, \"total_marks\": 4, \"score_released\": false, \"awaiting_manual_grading\": true, \"correct_answers_visible\": false},"
                + "\"questions\": ["
                + "  {\"question_id\": 1, \"question_type\": \"multiple_choice\", \"marks\": 1, \"grading_status\": \"auto_graded\", \"marks_obtained\": 1},"
                + "  {\"question_id\": 2, \"question_type\": \"short_answer\", \"marks\": 2, \"grading_status\": \"pending_grading\", \"marks_obtained\": null},"
                + "  {\"question_id\": 3, \"question_type\": \"true_false\", \"marks\": 1, \"grading_status\": \"auto_graded\", \"marks_obtained\": 1}"
                + "]"
                + "}").getAsJsonObject();

        QuizScoreUiHelper.ReviewScoreSummary s1 = QuizScoreUiHelper.parseStudentReviewSummary(pendingReview);
        assertTrue(s1.isProvisional);
        assertEquals(QuizScoreUiHelper.STATUS_PENDING_GRADING, s1.gradingStatus);
        assertEquals(2.0, s1.earnedMarks, 0.001);
        assertEquals(4.0, s1.totalMarks, 0.001);
        assertEquals("Score: 2 / 4 earned", s1.formatScoreLine());
        assertNotNull(s1.formatProvisionalNote());

        // 2. Partially graded: 2 short answers, one graded (1.5/2), one still pending (/1) + MC(1/1) -> 2.5/4 earned
        JsonObject partialReview = JsonParser.parseString("{"
                + "\"result\": {\"status\": \"submitted\", \"grading_status\": \"pending_grading\", \"score\": null, \"total_marks\": 4, \"score_released\": false, \"awaiting_manual_grading\": true},"
                + "\"questions\": ["
                + "  {\"question_id\": 1, \"question_type\": \"multiple_choice\", \"marks\": 1, \"grading_status\": \"auto_graded\", \"marks_obtained\": 1},"
                + "  {\"question_id\": 2, \"question_type\": \"short_answer\", \"marks\": 2, \"grading_status\": \"graded\", \"marks_obtained\": 1.5, \"teacher_feedback\": \"Good start\"},"
                + "  {\"question_id\": 3, \"question_type\": \"short_answer\", \"marks\": 1, \"grading_status\": \"pending_grading\", \"marks_obtained\": null}"
                + "]"
                + "}").getAsJsonObject();

        QuizScoreUiHelper.ReviewScoreSummary s2 = QuizScoreUiHelper.parseStudentReviewSummary(partialReview);
        assertTrue(s2.isProvisional);
        assertEquals(QuizScoreUiHelper.STATUS_PARTIALLY_GRADED, s2.gradingStatus);
        assertEquals(2.5, s2.earnedMarks, 0.001);
        assertEquals("Score: 2.5 / 4 earned", s2.formatScoreLine());

        // 3. Fully graded after teacher saves Q3 (1/1) -> final Score: 3.5 / 4, Status: Graded
        JsonObject gradedReview = JsonParser.parseString("{"
                + "\"result\": {\"status\": \"completed\", \"grading_status\": \"graded\", \"score\": 3.5, \"total_marks\": 4, \"score_released\": true, \"awaiting_manual_grading\": false, \"correct_answers_visible\": true},"
                + "\"questions\": ["
                + "  {\"question_id\": 1, \"question_type\": \"multiple_choice\", \"marks\": 1, \"grading_status\": \"auto_graded\", \"marks_obtained\": 1},"
                + "  {\"question_id\": 2, \"question_type\": \"short_answer\", \"marks\": 2, \"grading_status\": \"graded\", \"marks_obtained\": 1.5},"
                + "  {\"question_id\": 3, \"question_type\": \"short_answer\", \"marks\": 1, \"grading_status\": \"graded\", \"marks_obtained\": 1}"
                + "]"
                + "}").getAsJsonObject();

        QuizScoreUiHelper.ReviewScoreSummary s3 = QuizScoreUiHelper.parseStudentReviewSummary(gradedReview);
        assertFalse(s3.isProvisional);
        assertEquals(QuizScoreUiHelper.STATUS_GRADED, s3.gradingStatus);
        assertEquals("Score: 3.5 / 4", s3.formatScoreLine());
        assertNull(s3.formatProvisionalNote());

        Assessment card = new Assessment("11", "Mixed Quiz", "Nov 25", 0, 4, "quiz", "S1", "CS101", true, false);
        card.setPendingGrading(true);
        QuizScoreUiHelper.applyReviewSummaryToAssessment(card, s3);
        assertTrue(card.isGraded());
        assertFalse(card.isPendingGrading());
        assertEquals("3.5 / 4", card.getFormattedScore());
        assertEquals(QuizScoreUiHelper.STATUS_GRADED, card.getGradingStatusLabel());
    }

    @Test
    public void parseStudentQuizItem_multiAttemptWithInProgress_preservesLatestSubmittedAttempt() {
        JsonObject quizJson = JsonParser.parseString("{"
                + "\"id\": 12,"
                + "\"title\": \"Multi-Attempt Quiz\","
                + "\"total_score\": 10,"
                + "\"attempts_used\": 1,"
                + "\"can_continue\": true,"
                + "\"has_pending_grading\": false,"
                + "\"effective_score\": 8,"
                + "\"attempts\": ["
                + "  {\"id\": 302, \"attempt_number\": 2, \"status\": \"in_progress\", \"is_reset\": false},"
                + "  {\"id\": 301, \"attempt_number\": 1, \"status\": \"completed\", \"grading_status\": \"graded\", \"score\": 8, \"total_marks_snapshot\": 10, \"is_reset\": false}"
                + "]"
                + "}").getAsJsonObject();

        Assessment item = QuizScoreUiHelper.parseStudentQuizItem(quizJson, null);
        assertTrue(item.isInProgress());
        assertTrue(item.isSubmitted());
        assertTrue(item.isGraded());
        assertEquals("301", item.getLatestAttemptId());
        assertTrue(item.isReviewAllowed());
    }

    @Test
    public void teacherAttemptStatusAndMarksValidation_workAccurately() {
        assertEquals(QuizScoreUiHelper.STATUS_PENDING_GRADING,
                QuizScoreUiHelper.resolveAttemptGradingStatus("pending_grading", "submitted", true, 2, 0, 0.0));
        assertEquals(QuizScoreUiHelper.STATUS_PARTIALLY_GRADED,
                QuizScoreUiHelper.resolveAttemptGradingStatus("pending_grading", "submitted", true, 1, 1, 1.5));
        assertEquals(QuizScoreUiHelper.STATUS_GRADED,
                QuizScoreUiHelper.resolveAttemptGradingStatus("graded", "completed", false, 0, 2, 3.0));

        assertFalse(QuizScoreUiHelper.validateMarksInput("", 4.0).valid);
        assertFalse(QuizScoreUiHelper.validateMarksInput("-0.5", 4.0).valid);
        assertFalse(QuizScoreUiHelper.validateMarksInput("4.5", 4.0).valid);
        assertFalse(QuizScoreUiHelper.validateMarksInput("abc", 4.0).valid);

        QuizScoreUiHelper.MarksValidationResult okZero = QuizScoreUiHelper.validateMarksInput("0", 4.0);
        assertTrue(okZero.valid);
        assertEquals(0.0, okZero.marks, 0.001);

        QuizScoreUiHelper.MarksValidationResult okDecimal = QuizScoreUiHelper.validateMarksInput("2.75", 4.0);
        assertTrue(okDecimal.valid);
        assertEquals(2.75, okDecimal.marks, 0.001);
    }
}
