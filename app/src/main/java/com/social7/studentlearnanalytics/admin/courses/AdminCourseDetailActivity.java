package com.social7.studentlearnanalytics.admin.courses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCourseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "extra_course";

    private Course course;
    private TextView tvToolbarTitle, tvCode, tvStatusBadge, tvName, tvCreditsPill, tvYearPill, tvEnrollCount;
    private TextView tvDepartment, tvTerm, tvTeacher, tvClasses, tvDescription;
    private MaterialButton btnEdit, btnToggleStatus, btnDelete;

    private final ActivityResultLauncher<Intent> editCourseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_admin_course_detail);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_COURSE)) {
            Object extra = getIntent().getSerializableExtra(EXTRA_COURSE);
            if (extra instanceof Course) {
                course = (Course) extra;
            }
        }

        if (course == null) {
            Toast.makeText(this, "Course details unavailable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindCourse();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAdminCourseDetail);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvToolbarTitle = findViewById(R.id.tvCourseDetailToolbarTitle);
        tvCode = findViewById(R.id.tvCourseDetailCode);
        tvStatusBadge = findViewById(R.id.tvCourseDetailStatusBadge);
        tvName = findViewById(R.id.tvCourseDetailName);
        tvCreditsPill = findViewById(R.id.tvCourseDetailCreditsPill);
        tvYearPill = findViewById(R.id.tvCourseDetailYearPill);
        tvEnrollCount = findViewById(R.id.tvCourseDetailEnrollCount);
        tvDepartment = findViewById(R.id.tvCourseDetailDepartment);
        tvTerm = findViewById(R.id.tvCourseDetailTerm);
        tvTeacher = findViewById(R.id.tvCourseDetailTeacher);
        tvClasses = findViewById(R.id.tvCourseDetailClasses);
        tvDescription = findViewById(R.id.tvCourseDetailDescription);

        btnEdit = findViewById(R.id.btnCourseDetailEdit);
        btnToggleStatus = findViewById(R.id.btnCourseDetailToggleStatus);
        btnDelete = findViewById(R.id.btnCourseDetailDelete);
    }

    private void bindCourse() {
        String code = course.getCode().isEmpty() ? "COURSE" : course.getCode();
        String name = course.getName().isEmpty() ? "Untitled Course" : course.getName();
        String dept = course.getDepartment().isEmpty() ? "Unassigned" : course.getDepartment();
        String majors = course.getMajorsSummary();
        String cat = course.getCourseCategoryName();
        StringBuilder deptDisplay = new StringBuilder(dept);
        if (!majors.isEmpty()) {
            deptDisplay.append(" • Major(s): ").append(majors);
        }
        if (!cat.isEmpty()) {
            deptDisplay.append(" • Category: ").append(cat);
        }
        String ay = course.getAcademicYear().isEmpty() ? "—" : course.getAcademicYear();
        String sem = course.getSemester().isEmpty() ? "—" : course.getSemester();
        String yl = course.getYearLevelLabel().isEmpty() ? "Cross-Year" : course.getYearLevelLabel();
        String teacher = course.getTeacher().isEmpty() ? "Unassigned" : course.getTeacher();
        String classes = course.getClassesSummary().isEmpty() ? "No classes assigned yet" : course.getClassesSummary();
        String desc = course.getDescription().isEmpty() ? "No description provided." : course.getDescription();
        boolean isActive = !"Inactive".equalsIgnoreCase(course.getStatus());

        if (tvToolbarTitle != null) tvToolbarTitle.setText(code + " Details");
        if (tvCode != null) tvCode.setText(code);
        if (tvName != null) tvName.setText(name);
        if (tvStatusBadge != null) {
            tvStatusBadge.setText(isActive ? getString(R.string.status_active) : getString(R.string.status_inactive));
            tvStatusBadge.setTextColor(getColor(isActive ? R.color.admin_active_green : R.color.admin_inactive_red));
        }
        if (tvCreditsPill != null) tvCreditsPill.setText(course.getCredits() + " Credits");
        if (tvYearPill != null) tvYearPill.setText(yl);
        if (tvEnrollCount != null) tvEnrollCount.setText(course.getEnrollmentsCount() + " Enrolled");
        if (tvDepartment != null) tvDepartment.setText(deptDisplay.toString());
        if (tvTerm != null) tvTerm.setText(ay + " • " + sem);
        if (tvTeacher != null) tvTeacher.setText(teacher);
        if (tvClasses != null) tvClasses.setText(classes);
        if (tvDescription != null) tvDescription.setText(desc);

        if (btnToggleStatus != null) {
            btnToggleStatus.setText(isActive ? "Set Inactive" : "Set Active");
        }
    }

    private void setupListeners() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddCourseActivity.class);
                intent.putExtra(AddCourseActivity.EXTRA_EDIT_COURSE, course);
                editCourseLauncher.launch(intent);
            });
        }

        if (btnToggleStatus != null) {
            btnToggleStatus.setOnClickListener(v -> {
                boolean isActive = !"Inactive".equalsIgnoreCase(course.getStatus());
                toggleCourseStatus(isActive ? "inactive" : "active");
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDeleteCourse());
        }
    }

    private void toggleCourseStatus(String newStatus) {
        if (course.getId().isEmpty()) {
            Toast.makeText(this, "Missing course ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("status", newStatus);
        btnToggleStatus.setEnabled(false);

        ApiClient.getApiService(this).updateCourseStatus(course.getId(), body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        btnToggleStatus.setEnabled(true);
                        if (response.isSuccessful()) {
                            course.setStatus("active".equalsIgnoreCase(newStatus) ? "Active" : "Inactive");
                            bindCourse();
                            setResult(RESULT_OK);
                            Toast.makeText(AdminCourseDetailActivity.this,
                                    "Course " + course.getCode() + " marked as " + newStatus + ".",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminCourseDetailActivity.this, "Failed to update course status.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        btnToggleStatus.setEnabled(true);
                        Toast.makeText(AdminCourseDetailActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteCourse() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Course " + course.getCode() + "?")
                .setMessage("Are you sure you want to delete \"" + course.getName() + "\"?\n\nCourses referenced by active classes, schedules, or enrollments cannot be permanently deleted.")
                .setPositiveButton(R.string.delete, (dialog, which) -> executeDelete())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeDelete() {
        if (course.getId().isEmpty()) return;
        btnDelete.setEnabled(false);
        ApiClient.getApiService(this).deleteCourse(course.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    setResult(RESULT_OK);
                    Toast.makeText(AdminCourseDetailActivity.this, "Course deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnDelete.setEnabled(true);
                    Toast.makeText(AdminCourseDetailActivity.this, "Cannot delete course with active references.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                btnDelete.setEnabled(true);
                Toast.makeText(AdminCourseDetailActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
