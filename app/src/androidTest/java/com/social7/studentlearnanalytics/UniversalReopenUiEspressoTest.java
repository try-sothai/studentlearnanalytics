package com.social7.studentlearnanalytics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.social7.studentlearnanalytics.model.LoginRequest;
import com.social7.studentlearnanalytics.model.LoginResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.assignment.StudentSubmissionDetailActivity;
import com.social7.studentlearnanalytics.student.assignment.SubmitAssignmentActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.TakeQuizActivity;
import com.social7.studentlearnanalytics.teacher.assessment.AssessmentReopenManagerActivity;
import com.social7.studentlearnanalytics.teacher.assessment.QuizBuilderActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import retrofit2.Response;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Real-device Espresso UI audit across all 6 Universal Reopen & Retake screens:
 * - AssessmentReopenManagerActivity (reopen button, DatePicker/TimePicker, +Attempts, Audit Logs, Revoke, Error state)
 * - SubmitAssignmentActivity (locked expired UI, reopened UI, submission history dialog v2/v1)
 * - StudentSubmissionDetailActivity (archived v1 graded view + latest v2 view)
 * - QuizBuilderActivity -> Universal Reopen & Permissions navigation
 * - QuizDetailActivity (quiz list render)
 * - TakeQuizActivity (attempt history 10/10 & 5/10 + error state 999999)
 */
@RunWith(AndroidJUnit4.class)
public class UniversalReopenUiEspressoTest {

    private static final String TAG = "SLAS_ESPRESSO_UI";

    private Context targetContext;
    private boolean prevLoggedIn;
    private String prevUserId, prevNumericId, prevStuDbId, prevEmail, prevToken, prevRole, prevName;

    private String teacherCode, teacherPass, studentCode, studentPass;
    private int expiredAsnId, reopenedAsnId, quizId, subV1Id, subV2Id;

    @Before
    public void saveDeviceSessionAndReadArgs() {
        Bundle args = InstrumentationRegistry.getArguments();
        teacherCode = args.getString("ui_audit_teacher_code", "");
        Assume.assumeTrue("Skipping Espresso UI audit when ui_audit_teacher_code is absent",
                teacherCode != null && !teacherCode.trim().isEmpty());

        teacherPass = args.getString("ui_audit_teacher_password", "");
        studentCode = args.getString("ui_audit_student_code", "");
        studentPass = args.getString("ui_audit_student_password", "");
        expiredAsnId = Integer.parseInt(args.getString("ui_audit_expired_asn_id", "0"));
        reopenedAsnId = Integer.parseInt(args.getString("ui_audit_reopened_asn_id", "0"));
        quizId = Integer.parseInt(args.getString("ui_audit_quiz_id", "0"));
        subV1Id = Integer.parseInt(args.getString("ui_audit_sub_v1_id", "0"));
        subV2Id = Integer.parseInt(args.getString("ui_audit_sub_v2_id", "0"));

        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SessionManager sm = new SessionManager(targetContext);
        prevLoggedIn = sm.isLoggedIn();
        prevUserId = sm.getUserId();
        prevNumericId = sm.getNumericUserId();
        prevStuDbId = sm.getStudentDbId();
        prevEmail = sm.getUserEmail();
        prevToken = sm.getToken();
        prevRole = sm.getUserRole();
        prevName = sm.getUserName();
    }

    @After
    public void restoreDeviceSession() {
        if (targetContext == null) return;
        SessionManager sm = new SessionManager(targetContext);
        sm.clearSession();
        if (prevLoggedIn && prevToken != null && !prevToken.isEmpty()) {
            sm.saveSession(prevUserId, prevNumericId, prevStuDbId, prevEmail, prevToken, prevRole, prevName);
        }
    }

    private void switchSession(String loginId, String password, String expectedRole) throws Exception {
        SessionManager sm = new SessionManager(targetContext);
        sm.clearSession();
        Response<LoginResponse> res = ApiClient.getApiService(targetContext)
                .login(new LoginRequest(loginId, password))
                .execute();
        if (!res.isSuccessful() || res.body() == null) {
            throw new AssertionError("Login failed for " + loginId + ": HTTP " + res.code());
        }
        LoginResponse lr = res.body();
        String uid = lr.getUser() != null ? String.valueOf(lr.getUser().getId()) : "";
        String nm = lr.getUser() != null ? lr.getUser().getName() : loginId;
        String em = lr.getUser() != null ? lr.getUser().getEmail() : "";
        String stuDbId = "";
        sm.saveSession(loginId, uid, stuDbId, em, lr.getToken(), expectedRole, nm);
    }

    @Test
    public void verifyTeacherAndStudentScreensEndToEnd() throws Exception {
        // =====================================================================
        // PART 1: TEACHER UI SCREENS
        // =====================================================================
        switchSession(teacherCode, teacherPass, "teacher");

        // 1A. AssessmentReopenManagerActivity: Reopen, Date/Time picker, +Attempts, Audit Logs, Revoke
        Intent reopenIntent = new Intent(targetContext, AssessmentReopenManagerActivity.class);
        reopenIntent.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_TYPE, "assignment");
        reopenIntent.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_ID, expiredAsnId);
        reopenIntent.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_TITLE, "[AUDIT-UI] Expired Assignment");

        try (ActivityScenario<AssessmentReopenManagerActivity> ignored = ActivityScenario.launch(reopenIntent)) {
            waitForDisplayed(withId(R.id.tvReopenAssessmentTitle), false, 10000);
            onView(withId(R.id.tvReopenAssessmentType)).check(matches(withText("ASSIGNMENT")));
            waitForDisplayed(allOf(withId(R.id.tvReopenStatusBadge), withText("Expired")), false, 10000);
            onView(withId(R.id.tvStudentName)).check(matches(withText(containsString("AuditUI Alice"))));

            // Verify empty filter state & recovery
            onView(withId(R.id.edtSearchReopenStudents)).perform(replaceText("NO_SUCH_STUDENT"), closeSoftKeyboard());
            waitForDisplayed(allOf(withId(R.id.tvReopenEmpty), withText(containsString("No eligible students"))), false, 5000);
            onView(withId(R.id.edtSearchReopenStudents)).perform(replaceText(""), closeSoftKeyboard());
            waitForDisplayed(allOf(withId(R.id.tvStudentName), withText(containsString("AuditUI Alice"))), false, 5000);
            Log.i(TAG, "UI_OK AssessmentReopenManagerActivity empty filter state verified");

            // Click Reopen -> toggle/verify controls -> pick future deadline via DatePicker & TimePicker -> Apply
            onView(withId(R.id.btnRowReopen)).perform(click());
            waitForDisplayed(withText("Allow Submission"), true, 5000);
            waitForDisplayed(withText("Allow Resubmission / Retake"), true, 5000);
            waitForDisplayed(withText("Allow Late Submission"), true, 5000);

            onView(withContentDescription("Reopen Deadline Input")).inRoot(isDialog()).perform(scrollTo(), click());
            waitForDisplayed(isAssignableFrom(DatePicker.class), true, 5000);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 3);
            onView(isAssignableFrom(DatePicker.class)).inRoot(isDialog())
                    .perform(setDateAction(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());

            waitForDisplayed(isAssignableFrom(TimePicker.class), true, 5000);
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());

            waitForDisplayed(withText("Apply Reopen"), true, 5000);
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());

            // Status badge refreshes cleanly from live Laravel API to "Reopened"
            waitForDisplayed(allOf(withId(R.id.tvReopenStatusBadge), withText("Reopened")), false, 10000);
            onView(withId(R.id.btnRowRevoke)).check(matches(isDisplayed()));
            Log.i(TAG, "UI_OK AssessmentReopenManagerActivity reopen + DatePicker + TimePicker -> Reopened");

            // Grant +1 Additional Attempt -> attempts count updates 0 / 2 used (2 left)
            onView(withId(R.id.btnRowAttempts)).perform(click());
            waitForDisplayed(withText("Grant"), true, 5000);
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            waitForDisplayed(allOf(withId(R.id.tvAttemptInfo), withText(containsString("0 / 2"))), false, 10000);
            Log.i(TAG, "UI_OK AssessmentReopenManagerActivity +Attempts -> 0 / 2");

            // View Audit Logs -> Filter dialog -> Load Logs -> displays REOPEN event
            onView(withId(R.id.btnViewAuditLogs)).perform(click());
            waitForDisplayed(withText("Load Logs"), true, 5000);
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            waitForDisplayed(withText(containsString("[REOPEN]")), true, 10000);
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            waitForDisplayed(withId(R.id.tvReopenAssessmentTitle), false, 5000);
            Log.i(TAG, "UI_OK AssessmentReopenManagerActivity Audit Logs dialog rendered");

            // Revoke Permission -> status returns to "Expired" and Revoke button hides
            onView(withId(R.id.btnRowRevoke)).perform(click());
            waitForDisplayed(withText("Revoke"), true, 5000);
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            waitForDisplayed(allOf(withId(R.id.tvReopenStatusBadge), withText("Expired")), false, 10000);
            onView(withId(R.id.btnRowRevoke)).check(matches(not(isDisplayed())));
            Log.i(TAG, "UI_OK AssessmentReopenManagerActivity Revoke -> Expired");
        }

        // 1B. AssessmentReopenManagerActivity Error State (invalid assessment ID = 999999)
        Intent invalidReopen = new Intent(targetContext, AssessmentReopenManagerActivity.class);
        invalidReopen.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_TYPE, "assignment");
        invalidReopen.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_ID, 999999);
        invalidReopen.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_TITLE, "Missing");
        try (ActivityScenario<AssessmentReopenManagerActivity> ignored = ActivityScenario.launch(invalidReopen)) {
            waitForDisplayed(withId(R.id.tvReopenEmpty), false, 10000);
            Log.i(TAG, "UI_OK AssessmentReopenManagerActivity error state clean (no crash)");
        }

        // 1C. QuizBuilderActivity + Navigation to Universal Reopen & Permissions
        Intent qbIntent = new Intent(targetContext, QuizBuilderActivity.class);
        qbIntent.putExtra("quiz_id", quizId);
        try (ActivityScenario<QuizBuilderActivity> ignored = ActivityScenario.launch(qbIntent)) {
            waitForDisplayed(allOf(withId(R.id.tvBuilderTitle), withText(containsString("[AUDIT-UI]"))), false, 10000);
            onView(withId(R.id.btnQuizOverrides)).check(matches(isDisplayed()));
            onView(withId(R.id.btnViewAttempts)).check(matches(isDisplayed()));
            onView(withId(R.id.btnManageQuiz)).perform(click());
            waitForDisplayed(withText("Universal Reopen & Permissions"), true, 5000);
            onView(withText("Universal Reopen & Permissions")).inRoot(isDialog()).perform(click());
            waitForDisplayed(allOf(withId(R.id.tvReopenAssessmentType), withText("QUIZ")), false, 10000);
            Log.i(TAG, "UI_OK QuizBuilderActivity -> AssessmentReopenManagerActivity (QUIZ)");
        }

        // =====================================================================
        // PART 2: STUDENT UI SCREENS
        // =====================================================================
        switchSession(studentCode, studentPass, "student");

        // 2A. SubmitAssignmentActivity — Expired / Locked State
        Intent expiredStuIntent = new Intent(targetContext, SubmitAssignmentActivity.class);
        expiredStuIntent.putExtra("assignment_id", String.valueOf(expiredAsnId));
        expiredStuIntent.putExtra("assessment_type", "assignment");
        try (ActivityScenario<SubmitAssignmentActivity> ignored = ActivityScenario.launch(expiredStuIntent)) {
            waitForDisplayed(allOf(withId(R.id.tvReopenStatusBadge), withText("Expired")), false, 10000);
            onView(withId(R.id.tvBlockedReasonBanner)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSubmitWork)).perform(scrollTo()).check(matches(not(isEnabled())));
            Log.i(TAG, "UI_OK SubmitAssignmentActivity locked/expired UI verified");
        }

        // 2B. SubmitAssignmentActivity — Reopened State + Submission History Dialog (v2 + graded v1)
        Intent reopenedStuIntent = new Intent(targetContext, SubmitAssignmentActivity.class);
        reopenedStuIntent.putExtra("assignment_id", String.valueOf(reopenedAsnId));
        reopenedStuIntent.putExtra("assessment_type", "assignment");
        try (ActivityScenario<SubmitAssignmentActivity> ignored = ActivityScenario.launch(reopenedStuIntent)) {
            waitForDisplayed(allOf(withId(R.id.tvReopenStatusBadge), withText("Reopened")), false, 10000);
            onView(withId(R.id.tvDeadlineComparison)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSubmitWork)).perform(scrollTo()).check(matches(allOf(isEnabled(), withText("Resubmit Work"))));

            onView(withId(R.id.btnSubmissionHistory)).perform(scrollTo(), click());
            waitForDisplayed(withText(containsString("Version v2")), true, 10000);
            onView(withText(containsString("Version v1"))).inRoot(isDialog()).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText(containsString("72 / 100"))).inRoot(isDialog()).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText("Details v1")).inRoot(isDialog()).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            Log.i(TAG, "UI_OK SubmitAssignmentActivity reopened + submission history v2/v1 verified");
        }

        // 2C. StudentSubmissionDetailActivity — Archived v1 (graded 72/100) + Latest v2
        Intent detV1Intent = new Intent(targetContext, StudentSubmissionDetailActivity.class);
        detV1Intent.putExtra("submission_id", subV1Id);
        try (ActivityScenario<StudentSubmissionDetailActivity> ignored = ActivityScenario.launch(detV1Intent)) {
            waitForDisplayed(allOf(withId(R.id.tvDetailSubmittedAt), withText(containsString("v1 (Previous Version)"))), false, 10000);
            onView(withId(R.id.tvDetailScore)).perform(scrollTo()).check(matches(withText(containsString("72 / 100"))));
            Log.i(TAG, "UI_OK StudentSubmissionDetailActivity archived v1 verified");
        }

        Intent detV2Intent = new Intent(targetContext, StudentSubmissionDetailActivity.class);
        detV2Intent.putExtra("submission_id", subV2Id);
        try (ActivityScenario<StudentSubmissionDetailActivity> ignored = ActivityScenario.launch(detV2Intent)) {
            waitForDisplayed(allOf(withId(R.id.tvDetailSubmittedAt), withText(containsString("v2 (Latest)"))), false, 10000);
            waitForScrollAndDisplayed(withId(R.id.layoutGradePending), 8000);
            Log.i(TAG, "UI_OK StudentSubmissionDetailActivity latest v2 verified");
        }

        // 2D. QuizDetailActivity — Real list loads without crash
        try (ActivityScenario<QuizDetailActivity> ignored = ActivityScenario.launch(QuizDetailActivity.class)) {
            waitForDisplayed(withText(containsString("[AUDIT-UI] Dual Attempt Quiz")), false, 10000);
            Log.i(TAG, "UI_OK QuizDetailActivity list verified");
        }

        // 2E. TakeQuizActivity — Completed 2-attempt history + Error State
        Intent takeQuizIntent = new Intent(targetContext, TakeQuizActivity.class);
        takeQuizIntent.putExtra("quiz_id", String.valueOf(quizId));
        try (ActivityScenario<TakeQuizActivity> ignored = ActivityScenario.launch(takeQuizIntent)) {
            waitForDisplayed(allOf(withId(R.id.tvInstructionsTitle), withText(containsString("[AUDIT-UI]"))), false, 10000);
            onView(withId(R.id.tvAttemptStatusTitle)).perform(scrollTo()).check(matches(withText(containsString("10 / 10"))));
            onView(withId(R.id.tvAttemptStatusDetail)).perform(scrollTo())
                    .check(matches(allOf(
                            withText(containsString("Attempt #2: 10 / 10")),
                            withText(containsString("Attempt #1: 5 / 10"))
                    )));
            Log.i(TAG, "UI_OK TakeQuizActivity attempt history verified");
        }

        Intent badQuizIntent = new Intent(targetContext, TakeQuizActivity.class);
        badQuizIntent.putExtra("quiz_id", "999999");
        try (ActivityScenario<TakeQuizActivity> ignored = ActivityScenario.launch(badQuizIntent)) {
            waitForDisplayed(withId(R.id.layoutQuizError), false, 10000);
            onView(withId(R.id.tvQuizErrorTitle)).check(matches(withText("Cannot open quiz")));
            Log.i(TAG, "UI_OK TakeQuizActivity error state verified");
        }
    }

    private static void waitForDisplayed(Matcher<View> matcher, boolean inDialog, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable lastErr = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (inDialog) {
                    onView(matcher).inRoot(isDialog()).check(matches(isDisplayed()));
                } else {
                    onView(matcher).check(matches(isDisplayed()));
                }
                return;
            } catch (Throwable e) {
                lastErr = e;
                Thread.sleep(200);
            }
        }
        throw new AssertionError("Timed out waiting for view: " + matcher, lastErr);
    }

    private static void waitForScrollAndDisplayed(Matcher<View> matcher, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable lastErr = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(matcher).perform(scrollTo()).check(matches(isDisplayed()));
                return;
            } catch (Throwable e) {
                lastErr = e;
                Thread.sleep(250);
            }
        }
        throw new AssertionError("Timed out waiting to scroll to displayed view: " + matcher, lastErr);
    }

    private static ViewAction setDateAction(int year, int monthZeroBased, int day) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DatePicker.class);
            }

            @Override
            public String getDescription() {
                return "Set DatePicker date to " + year + "-" + (monthZeroBased + 1) + "-" + day;
            }

            @Override
            public void perform(UiController uiController, View view) {
                DatePicker dp = (DatePicker) view;
                dp.updateDate(year, monthZeroBased, day);
            }
        };
    }
}
