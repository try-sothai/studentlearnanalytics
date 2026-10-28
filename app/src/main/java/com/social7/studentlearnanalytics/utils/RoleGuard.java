package com.social7.studentlearnanalytics.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.StudentMainActivity;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

/**
 * Utility class for enforcing role-based access control and session verification in Activities.
 */
public class RoleGuard {

    private static final String TAG = "SLAS_ROLE_GUARD";

    /**
     * Checks if a valid session exists and the current user's role matches any of the allowed roles.
     * If unauthenticated, redirects to LoginActivity.
     * If authenticated but unauthorized, redirects to the user's authorized role dashboard.
     *
     * @param activity The target Activity being protected.
     * @param allowedRoles Array of allowed role strings (case-insensitive).
     * @return true if authorized, false if unauthorized (and redirect executed).
     */
    public static boolean checkAccess(Activity activity, String... allowedRoles) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }

        SessionManager sessionManager = new SessionManager(activity);

        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, "Unauthenticated access attempt to " + activity.getClass().getSimpleName() + ". Redirecting to Login.");
            redirectToLogin(activity);
            return false;
        }

        String currentRole = sessionManager.getUserRole();
        if (currentRole == null) {
            currentRole = "";
        }

        boolean isAuthorized = false;
        for (String role : allowedRoles) {
            if (role != null && role.trim().equalsIgnoreCase(currentRole.trim())) {
                isAuthorized = true;
                break;
            }
        }

        if (!isAuthorized) {
            Log.w(TAG, "Unauthorized access attempt by role '" + currentRole + "' to " + activity.getClass().getSimpleName() + ". Redirecting to authorized dashboard.");
            redirectToAuthorizedDashboard(activity, currentRole);
            return false;
        }

        return true;
    }

    public static void redirectToLogin(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void redirectToAuthorizedDashboard(Activity activity, String currentRole) {
        if (activity == null || activity.isFinishing()) return;
        Intent intent;
        if ("Teacher".equalsIgnoreCase(currentRole)) {
            intent = new Intent(activity, TeacherMainActivity.class);
        } else if ("Admin".equalsIgnoreCase(currentRole)) {
            intent = new Intent(activity, AdminMainActivity.class);
        } else if ("Student".equalsIgnoreCase(currentRole)) {
            intent = new Intent(activity, StudentMainActivity.class);
        } else {
            intent = new Intent(activity, LoginActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
