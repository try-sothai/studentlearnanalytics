package com.social7.studentlearnanalytics;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.social7.studentlearnanalytics.admin.AdminBackupDataActivity;
import com.social7.studentlearnanalytics.admin.AdminUserRolesSettingsActivity;
import com.social7.studentlearnanalytics.admin.NotificationDetailActivity;
import com.social7.studentlearnanalytics.admin.classes.AdminClassDetailActivity;
import com.social7.studentlearnanalytics.admin.courses.AdminCourseDetailActivity;
import com.social7.studentlearnanalytics.admin.reports.CourseReportActivity;
import com.social7.studentlearnanalytics.admin.reports.StudentReportActivity;
import com.social7.studentlearnanalytics.auth.ForgotPasswordActivity;
import com.social7.studentlearnanalytics.model.ClassModel;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.teacher.TeacherCourseDetailActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Instrumented Espresso verification for all newly upgraded Detail Activities,
 * de-stubbed Admin/Auth screens, and removal of hardcoded sample placeholders.
 */
@RunWith(AndroidJUnit4.class)
public class SystemAuditUiEspressoTest {

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
        sm.saveSession("AUDIT_" + role.toUpperCase(), "1", "", role.toLowerCase() + "@slas.local", "audit-ui-token", role, "Audit " + role);
    }

    @Test
    public void forgotPassword_validEmailShowsAdminAssistedResetDialog() {
        try (ActivityScenario<ForgotPasswordActivity> ignored = ActivityScenario.launch(ForgotPasswordActivity.class)) {
            onView(withId(R.id.edtForgotEmail)).perform(replaceText("student@slas.edu.kh"), closeSoftKeyboard());
            onView(withId(R.id.btnSendReset)).perform(click());
            onView(withText("Administrator Password Reset Required")).inRoot(isDialog()).check(matches(isDisplayed()));
            onView(withText(containsString("student@slas.edu.kh"))).inRoot(isDialog()).check(matches(isDisplayed()));
            onView(withText(R.string.ok)).inRoot(isDialog()).perform(click());
        }
    }

    @Test
    public void notificationDetail_bindsRealPayloadWithoutHardcodedTeacher() {
        seedRoleSession("Admin");
        Notification notif = new Notification("42", "System Maintenance", "Scheduled check at midnight.", "10m ago", "System", "TODAY", false, true);
        Intent intent = new Intent(targetContext, NotificationDetailActivity.class);
        intent.putExtra("notification", notif);

        try (ActivityScenario<NotificationDetailActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvDetailTitle)).check(matches(withText("System Maintenance")));
            onView(withId(R.id.tvDetailMessage)).check(matches(withText("Scheduled check at midnight.")));
            onView(withId(R.id.tvDetailDateTime)).check(matches(withText("10m ago")));
            onView(withText(containsString("Ms. Dany"))).check(doesNotExist());
            onView(withText(containsString("Oct 28, 2024"))).check(doesNotExist());
        }
    }

    @Test
    public void adminCourseDetail_rendersCourseFieldsAndActions() {
        seedRoleSession("Admin");
        Course course = new Course("7", "CS305", "Software Architecture", "Prof. Sok", "Semester 1", 3, "Room 301", "Mon 08:00-10:00", 24, 88, "Active");
        course.setDepartment("Computer Science");
        course.setDescription("Layered and event-driven systems.");

        Intent intent = new Intent(targetContext, AdminCourseDetailActivity.class);
        intent.putExtra(AdminCourseDetailActivity.EXTRA_COURSE, course);

        try (ActivityScenario<AdminCourseDetailActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvCourseDetailCode)).check(matches(withText("CS305")));
            onView(withId(R.id.tvCourseDetailName)).check(matches(withText("Software Architecture")));
            onView(withId(R.id.tvCourseDetailDepartment)).check(matches(withText("Computer Science")));
            onView(withId(R.id.tvCourseDetailTeacher)).check(matches(withText("Prof. Sok")));
            onView(withId(R.id.btnCourseDetailEdit)).check(matches(isDisplayed()));
            onView(withId(R.id.btnCourseDetailToggleStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.btnCourseDetailDelete)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminClassDetail_rendersClassSummaryAndActions() {
        seedRoleSession("Admin");
        ClassModel cm = new ClassModel("3", "SE-Y3-A", "SEY3A", "Software Engineering", "2025-2026", "", "Prof. Dara", "Active", 18);
        cm.setCapacity(40);
        cm.setYearLevel(3);
        cm.setLocationLabel("Building A • Floor 2");

        Intent intent = new Intent(targetContext, AdminClassDetailActivity.class);
        intent.putExtra(AdminClassDetailActivity.EXTRA_CLASS, cm);

        try (ActivityScenario<AdminClassDetailActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvClassDetailName)).check(matches(withText("SE-Y3-A")));
            onView(withId(R.id.tvClassDetailCode)).check(matches(withText("SEY3A")));
            onView(withId(R.id.tvClassDetailDepartment)).check(matches(withText("Software Engineering")));
            onView(withId(R.id.tvClassDetailLocation)).check(matches(withText("Building A • Floor 2")));
            onView(withId(R.id.btnClassDetailEdit)).check(matches(isDisplayed()));
            onView(withId(R.id.btnClassDetailEnroll)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void teacherCourseDetail_rendersOfferingDetailsAndNavigationButtons() {
        seedRoleSession("Teacher");
        Course course = new Course("11", "IT410", "Mobile Systems", "Computer Science", "Wed 13:00-16:00", "Lab B2", 20, 2, 1, 0, 0, "Semester 2");

        Intent intent = new Intent(targetContext, TeacherCourseDetailActivity.class);
        intent.putExtra(TeacherCourseDetailActivity.EXTRA_COURSE, course);

        try (ActivityScenario<TeacherCourseDetailActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvTeacherCourseCode)).check(matches(withText("IT410")));
            onView(withId(R.id.tvTeacherCourseName)).check(matches(withText("Mobile Systems")));
            onView(withId(R.id.tvTeacherCourseSchedule)).check(matches(withText("Wed 13:00-16:00")));
            onView(withId(R.id.tvTeacherCourseRoom)).check(matches(withText("Lab B2")));
            onView(withId(R.id.btnTeacherCourseViewStudents)).check(matches(isDisplayed()));
            onView(withId(R.id.btnTeacherCourseAnalytics)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminSettingsAndReportScreens_haveZeroHardcodedPlaceholders() {
        seedRoleSession("Admin");

        try (ActivityScenario<AdminBackupDataActivity> ignored = ActivityScenario.launch(AdminBackupDataActivity.class)) {
            onView(withText(containsString("PostgreSQL 15"))).check(doesNotExist());
            onView(withText(containsString("142.8 MB"))).check(doesNotExist());
            onView(withText(containsString("slas_backup_20260310"))).check(doesNotExist());
            onView(withId(R.id.btnCreateBackup)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<AdminUserRolesSettingsActivity> ignored = ActivityScenario.launch(AdminUserRolesSettingsActivity.class)) {
            onView(withText(R.string.super_administrator)).check(doesNotExist());
            onView(withId(R.id.btnAddRole)).perform(click());
            onView(withText("SLAS Role-Based Access Policy")).inRoot(isDialog()).check(matches(isDisplayed()));
            onView(withText(R.string.ok)).inRoot(isDialog()).perform(click());
        }

        try (ActivityScenario<CourseReportActivity> ignored = ActivityScenario.launch(CourseReportActivity.class)) {
            onView(withId(R.id.tvStatTotalEnrollments)).check(matches(not(withText("6,245"))));
            onView(withText(containsString("CS101 — Intro to Computer Science"))).check(doesNotExist());
            onView(withId(R.id.btnExportCourseReport)).check(matches(isDisplayed()));
        }

        try (ActivityScenario<StudentReportActivity> ignored = ActivityScenario.launch(StudentReportActivity.class)) {
            onView(withId(R.id.tvStatTotalStudents)).check(matches(not(withText("2,845"))));
            onView(withId(R.id.btnExportStudentReport)).check(matches(isDisplayed()));
        }
    }
}
