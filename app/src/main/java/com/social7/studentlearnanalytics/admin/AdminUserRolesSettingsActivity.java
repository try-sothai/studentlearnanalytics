package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserRolesSettingsActivity extends AppCompatActivity {

    private MaterialButton btnAddRole, btnSaveUserRoles;
    private TextView tvRoleAdminBadge, tvRoleTeacherBadge, tvRoleStudentBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_admin_user_roles_settings);

        ImageView btnBack = findViewById(R.id.btnBackUserRoles);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnAddRole = findViewById(R.id.btnAddRole);
        btnSaveUserRoles = findViewById(R.id.btnSaveUserRoles);
        tvRoleAdminBadge = findViewById(R.id.tvRoleAdminBadge);
        tvRoleTeacherBadge = findViewById(R.id.tvRoleTeacherBadge);
        tvRoleStudentBadge = findViewById(R.id.tvRoleStudentBadge);

        if (btnAddRole != null) {
            btnAddRole.setOnClickListener(v -> showRbacPolicyDialog());
        }

        if (btnSaveUserRoles != null) {
            btnSaveUserRoles.setOnClickListener(v -> finish());
        }

        loadRoleStatistics();
    }

    private void loadRoleStatistics() {
        ApiClient.getApiService(this).getUserStatistics().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject d = response.body().getAsJsonObject("data");
                    int admins = UiUtils.optInt(d, "total_admins", UiUtils.optInt(d, "total_administrators", 0));
                    int activeAdmins = UiUtils.optInt(d, "active_admins", admins);
                    int teachers = UiUtils.optInt(d, "total_teachers", 0);
                    int activeTeachers = UiUtils.optInt(d, "active_teachers", teachers);
                    int students = UiUtils.optInt(d, "total_students", 0);
                    int activeStudents = UiUtils.optInt(d, "active_students", students);

                    if (tvRoleAdminBadge != null) {
                        tvRoleAdminBadge.setText(String.format(Locale.US, "%d Admins (%d Active)", admins, activeAdmins));
                    }
                    if (tvRoleTeacherBadge != null) {
                        tvRoleTeacherBadge.setText(String.format(Locale.US, "%d Teachers (%d Active)", teachers, activeTeachers));
                    }
                    if (tvRoleStudentBadge != null) {
                        tvRoleStudentBadge.setText(String.format(Locale.US, "%d Students (%d Active)", students, activeStudents));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void showRbacPolicyDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("SLAS Role-Based Access Policy")
                .setMessage("SLAS enforces three fixed system roles authenticated via Laravel Sanctum and EnsureUserHasRole middleware:\n\n"
                        + "• Admin (admin): Full user, catalog, class, schedule, report, and system oversight.\n"
                        + "• Teacher (teacher): Assigned courses/classes, attendance, grading, risk analytics, and Universal Reopen.\n"
                        + "• Student (student): Personal courses, attendance, assignments, quizzes, grades, and analytics.\n\n"
                        + "To assign or change a user's role, edit or provision accounts directly in User Management.")
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
