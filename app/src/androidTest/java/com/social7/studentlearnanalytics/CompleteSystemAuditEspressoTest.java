package com.social7.studentlearnanalytics;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.admin.AdminSystemConfigActivity;
import com.social7.studentlearnanalytics.admin.classes.AssignTeacherClassActivity;
import com.social7.studentlearnanalytics.admin.classes.BulkClassEnrollmentActivity;
import com.social7.studentlearnanalytics.admin.users.AccountSecurityActivity;
import com.social7.studentlearnanalytics.admin.users.EditAdminActivity;
import com.social7.studentlearnanalytics.admin.users.ProfileSettingsActivity;
import com.social7.studentlearnanalytics.model.Admin;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.AllFeaturesActivity;
import com.social7.studentlearnanalytics.student.ExamDetailActivity;
import com.social7.studentlearnanalytics.student.LearningReportActivity;
import com.social7.studentlearnanalytics.student.NavigationMenuActivity;
import com.social7.studentlearnanalytics.student.OverallPerformanceActivity;
import com.social7.studentlearnanalytics.student.RiskAnalysisActivity;
import com.social7.studentlearnanalytics.student.StudentMainActivity;
import com.social7.studentlearnanalytics.student.academic.GpaProgressActivity;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.grades.GradesOverviewActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;
import com.social7.studentlearnanalytics.student.recommendations.SmartRecommendationsActivity;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end Espresso functional audit across Admin, Teacher, and Student screens,
 * validating RoleGuard enforcement, repaired navigation/deep links, and live activity launches.
 */
@RunWith(AndroidJUnit4.class)
public class CompleteSystemAuditEspressoTest {

    private Context targetContext;
    private boolean prevLoggedIn;
    private String prevUserId, prevNumericId, prevStuDbId, prevEmail, prevToken, prevRole, prevName;

    @Before
    public void saveSession() {
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
    public void restoreSession() {
        if (targetContext == null) return;
        SessionManager sm = new SessionManager(targetContext);
        sm.clearSession();
        if (prevLoggedIn && prevToken != null && !prevToken.isEmpty()) {
            sm.saveSession(prevUserId, prevNumericId, prevStuDbId, prevEmail, prevToken, prevRole, prevName);
        }
    }

    private void seedRoleSession(String role) {
        SessionManager sm = new SessionManager(targetContext);
        sm.saveSession(
                "AUDIT_" + role.toUpperCase(),
                "1",
                "Student".equalsIgnoreCase(role) ? "39" : "",
                role.toLowerCase() + "@slas.local",
                "audit-ui-token",
                role,
                "Audit " + role
        );
    }

    @Test
    public void roleGuard_blocksWrongRoleFromRepairedScreens() {
        seedRoleSession("Student");
        try (ActivityScenario<AccountSecurityActivity> s1 = ActivityScenario.launch(AccountSecurityActivity.class)) {
            assertTrue(s1.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }
        try (ActivityScenario<ProfileSettingsActivity> s2 = ActivityScenario.launch(ProfileSettingsActivity.class)) {
            assertTrue(s2.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }
        try (ActivityScenario<AdminSystemConfigActivity> s3 = ActivityScenario.launch(AdminSystemConfigActivity.class)) {
            assertTrue(s3.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }
        try (ActivityScenario<AssignTeacherClassActivity> s4 = ActivityScenario.launch(AssignTeacherClassActivity.class)) {
            assertTrue(s4.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }

        seedRoleSession("Teacher");
        try (ActivityScenario<AllFeaturesActivity> s5 = ActivityScenario.launch(AllFeaturesActivity.class)) {
            assertTrue(s5.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }
        try (ActivityScenario<NavigationMenuActivity> s6 = ActivityScenario.launch(NavigationMenuActivity.class)) {
            assertTrue(s6.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }
    }

    @Test
    public void adminScreens_andRepairedSettingsWorkCleanly() {
        seedRoleSession("Admin");

        try (ActivityScenario<AdminMainActivity> ignored = ActivityScenario.launch(AdminMainActivity.class)) {
            onView(withId(R.id.nav_admin_dashboard)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_admin_users)).perform(click());
            onView(withId(R.id.nav_admin_courses)).perform(click());
            onView(withId(R.id.nav_admin_reports)).perform(click());
            onView(withId(R.id.nav_admin_settings)).perform(click());
        }

        try (ActivityScenario<AccountSecurityActivity> ignored = ActivityScenario.launch(AccountSecurityActivity.class)) {
            onView(withId(R.id.btnUpdatePassword)).perform(click());
            onView(withId(R.id.btnEnable2FA)).perform(scrollTo(), click());
            onView(withText("Two-Factor Authentication")).inRoot(isDialog()).check(matches(isDisplayed()));
            onView(withText(android.R.string.ok)).inRoot(isDialog()).perform(click());
            onView(withId(R.id.tvBadge2FAStatus)).check(matches(not(withText("Enabled"))));
        }

        try (ActivityScenario<ProfileSettingsActivity> ignored = ActivityScenario.launch(ProfileSettingsActivity.class)) {
            onView(withId(R.id.etProfileName)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSaveProfileSettings)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<AdminSystemConfigActivity> ignored = ActivityScenario.launch(AdminSystemConfigActivity.class)) {
            onView(withId(R.id.spDefaultYear)).check(matches(isDisplayed()));
            onView(withId(R.id.spDefaultSemester)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSaveSysConfig)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<AssignTeacherClassActivity> ignored = ActivityScenario.launch(AssignTeacherClassActivity.class)) {
            onView(withId(R.id.etAssignClass)).check(matches(isDisplayed()));
            onView(withId(R.id.etAssignTeacher)).check(matches(isDisplayed()));
            onView(withId(R.id.btnAssignSave)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<BulkClassEnrollmentActivity> ignored = ActivityScenario.launch(BulkClassEnrollmentActivity.class)) {
            onView(withId(R.id.cardSelectTargetClass)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<EditAdminActivity> noExtra = ActivityScenario.launch(EditAdminActivity.class)) {
            assertTrue(noExtra.getState() == androidx.lifecycle.Lifecycle.State.DESTROYED);
        }

        Admin realAdmin = new Admin("ADM999", "Real Admin", "real@slas.local", "System Administrator", "Active");
        Intent editIntent = new Intent(targetContext, EditAdminActivity.class);
        editIntent.putExtra("admin", realAdmin);
        try (ActivityScenario<EditAdminActivity> ignored = ActivityScenario.launch(editIntent)) {
            onView(withId(R.id.etEditAdminName)).check(matches(withText("Real Admin")));
            onView(withId(R.id.etEditAdminEmail)).check(matches(withText("real@slas.local")));
        }
    }

    @Test
    public void teacherMainAndDeepLinks_renderWithoutCrashing() {
        seedRoleSession("Teacher");

        try (ActivityScenario<TeacherMainActivity> ignored = ActivityScenario.launch(TeacherMainActivity.class)) {
            onView(withId(R.id.teacher_bottom_navigation)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_teacher_students)).perform(click());
            onView(withId(R.id.nav_teacher_courses)).perform(click());
            onView(withId(R.id.nav_teacher_reports)).perform(click());
            onView(withId(R.id.nav_teacher_profile)).perform(click());
        }

        Intent notifIntent = new Intent(targetContext, TeacherMainActivity.class);
        notifIntent.putExtra("navigate_to", "notifications");
        try (ActivityScenario<TeacherMainActivity> ignored = ActivityScenario.launch(notifIntent)) {
            onView(withId(R.id.teacher_fragment_container)).check(matches(isDisplayed()));
        }

        Intent profileIntent = new Intent(targetContext, TeacherMainActivity.class);
        profileIntent.putExtra("navigate_to", "profile");
        try (ActivityScenario<TeacherMainActivity> ignored = ActivityScenario.launch(profileIntent)) {
            onView(withId(R.id.tvOverviewFullName)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void studentMainAllFeaturesAndDetailScreens_openCleanly() {
        seedRoleSession("Student");

        try (ActivityScenario<StudentMainActivity> ignored = ActivityScenario.launch(StudentMainActivity.class)) {
            onView(withId(R.id.nav_home)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_courses)).perform(click());
            onView(withId(R.id.nav_analytics)).perform(click());
            onView(withId(R.id.nav_notifications)).perform(click());
            onView(withId(R.id.nav_profile)).perform(click());
        }

        Intent settingsIntent = new Intent(targetContext, StudentMainActivity.class);
        settingsIntent.putExtra("open_fragment", "settings");
        try (ActivityScenario<StudentMainActivity> ignored = ActivityScenario.launch(settingsIntent)) {
            onView(withId(R.id.cardStudentChangePassword)).check(matches(isDisplayed()));
            onView(withId(R.id.cardStudentAppearance)).check(matches(isDisplayed()));
            onView(withId(R.id.cardStudentAbout)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<AllFeaturesActivity> ignored = ActivityScenario.launch(AllFeaturesActivity.class)) {
            onView(withId(R.id.itemCourses)).check(matches(isDisplayed()));
            onView(withId(R.id.itemAttendance)).check(matches(isDisplayed()));
            onView(withId(R.id.itemAssignments)).check(matches(isDisplayed()));
            onView(withId(R.id.itemQuizzes)).check(matches(isDisplayed()));
            onView(withId(R.id.itemExams)).check(matches(isDisplayed()));
            onView(withId(R.id.itemAnalytics)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.itemRecommendations)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.itemNotifications)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.itemReports)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.itemRecommendations)).perform(click());
        }

        try (ActivityScenario<NavigationMenuActivity> ignored = ActivityScenario.launch(NavigationMenuActivity.class)) {
            onView(withId(R.id.menu_home)).check(matches(isDisplayed()));
            onView(withId(R.id.menu_recommendation)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.menu_settings)).perform(scrollTo()).check(matches(isDisplayed()));
        }

        try (ActivityScenario<ExamDetailActivity> ignored = ActivityScenario.launch(ExamDetailActivity.class)) {
            onView(withId(R.id.tabUpcoming)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<GpaProgressActivity> ignored = ActivityScenario.launch(GpaProgressActivity.class)) {
            onView(withId(R.id.tvCumulativeGpa)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<GradesOverviewActivity> ignored = ActivityScenario.launch(GradesOverviewActivity.class)) {
            onView(withId(R.id.rvGrades)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<SmartRecommendationsActivity> ignored = ActivityScenario.launch(SmartRecommendationsActivity.class)) {
            onView(withId(R.id.rvRecommendations)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<LearningReportActivity> ignored = ActivityScenario.launch(LearningReportActivity.class)) {
            onView(withId(R.id.tvReportStudentName)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<OverallPerformanceActivity> ignored = ActivityScenario.launch(OverallPerformanceActivity.class)) {
            onView(withId(R.id.headerLayout)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<RiskAnalysisActivity> ignored = ActivityScenario.launch(RiskAnalysisActivity.class)) {
            onView(withId(R.id.tvRiskLevel)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<AttendanceDetailActivity> ignored = ActivityScenario.launch(AttendanceDetailActivity.class)) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<AssessmentDetailActivity> ignored = ActivityScenario.launch(AssessmentDetailActivity.class)) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<QuizDetailActivity> ignored = ActivityScenario.launch(QuizDetailActivity.class)) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        }
    }
}
