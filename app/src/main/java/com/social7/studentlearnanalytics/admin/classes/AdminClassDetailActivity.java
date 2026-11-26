package com.social7.studentlearnanalytics.admin.classes;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.users.AdminStudentDetailActivity;
import com.social7.studentlearnanalytics.model.ClassModel;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;
import com.social7.studentlearnanalytics.utils.UiUtils;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminClassDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CLASS = "extra_class";

    private ClassModel classModel;
    private TextView tvToolbarTitle, tvCode, tvStatus, tvName, tvYearLevel, tvCapacity;
    private TextView tvDepartment, tvAcademicYear, tvLocation;
    private TextView tvOfferingsTitle, tvOfferingsEmpty, tvStudentsTitle, tvStudentsEmpty;
    private LinearLayout containerOfferings, containerStudents;
    private MaterialButton btnEdit, btnEnroll;

    private final ActivityResultLauncher<Intent> editClassLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    fetchClassDetail();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_admin_class_detail);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_CLASS)) {
            Object extra = getIntent().getSerializableExtra(EXTRA_CLASS);
            if (extra instanceof ClassModel) {
                classModel = (ClassModel) extra;
            }
        }

        if (classModel == null) {
            Toast.makeText(this, "Class details unavailable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindSummary();
        setupListeners();
        fetchClassDetail();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAdminClassDetail);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvToolbarTitle = findViewById(R.id.tvClassDetailToolbarTitle);
        tvCode = findViewById(R.id.tvClassDetailCode);
        tvStatus = findViewById(R.id.tvClassDetailStatus);
        tvName = findViewById(R.id.tvClassDetailName);
        tvYearLevel = findViewById(R.id.tvClassDetailYearLevel);
        tvCapacity = findViewById(R.id.tvClassDetailCapacity);
        tvDepartment = findViewById(R.id.tvClassDetailDepartment);
        tvAcademicYear = findViewById(R.id.tvClassDetailAcademicYear);
        tvLocation = findViewById(R.id.tvClassDetailLocation);

        tvOfferingsTitle = findViewById(R.id.tvClassDetailOfferingsTitle);
        tvOfferingsEmpty = findViewById(R.id.tvClassOfferingsEmpty);
        containerOfferings = findViewById(R.id.containerClassOfferings);

        tvStudentsTitle = findViewById(R.id.tvClassDetailStudentsTitle);
        tvStudentsEmpty = findViewById(R.id.tvClassStudentsEmpty);
        containerStudents = findViewById(R.id.containerClassStudents);

        btnEdit = findViewById(R.id.btnClassDetailEdit);
        btnEnroll = findViewById(R.id.btnClassDetailEnroll);
    }

    private void bindSummary() {
        String name = classModel.getName() != null ? classModel.getName() : "Class";
        String code = classModel.getCode() != null ? classModel.getCode() : "—";
        String dept = classModel.getDepartment() != null && !classModel.getDepartment().isEmpty() ? classModel.getDepartment() : "Unassigned";
        String ay = classModel.getAcademicYear() != null && !classModel.getAcademicYear().isEmpty() ? classModel.getAcademicYear() : "—";
        String loc = classModel.getLocationLabel() != null && !classModel.getLocationLabel().isEmpty() ? classModel.getLocationLabel() : "Unassigned";
        String status = classModel.getStatus() != null && !classModel.getStatus().isEmpty() ? classModel.getStatus() : "Active";
        boolean isActive = !"Inactive".equalsIgnoreCase(status);

        if (tvToolbarTitle != null) tvToolbarTitle.setText(name + " — Detail");
        if (tvCode != null) tvCode.setText(code);
        if (tvName != null) tvName.setText(name);
        if (tvStatus != null) {
            tvStatus.setText(isActive ? getString(R.string.status_active) : getString(R.string.status_inactive));
            tvStatus.setTextColor(getColor(isActive ? R.color.admin_active_green : R.color.admin_inactive_red));
        }
        if (tvYearLevel != null) tvYearLevel.setText(YearLevelHelper.toFullLabel(this, classModel.getYearLevel()));
        if (tvCapacity != null) tvCapacity.setText(classModel.getStudentCount() + " / " + classModel.getCapacity() + " Seats");
        if (tvDepartment != null) tvDepartment.setText(dept);
        if (tvAcademicYear != null) tvAcademicYear.setText(ay);
        if (tvLocation != null) tvLocation.setText(loc);
    }

    private void fetchClassDetail() {
        if (classModel.getId() == null || classModel.getId().isEmpty()) return;
        ApiClient.getApiService(this).getClassDetail(classModel.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    renderOfferings(data.has("offerings") && data.get("offerings").isJsonArray()
                            ? data.getAsJsonArray("offerings") : new JsonArray());
                    renderStudents(data.has("students") && data.get("students").isJsonArray()
                            ? data.getAsJsonArray("students") : new JsonArray());
                } else {
                    Toast.makeText(AdminClassDetailActivity.this, "Unable to refresh class roster.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AdminClassDetailActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOfferings(JsonArray offerings) {
        if (containerOfferings == null) return;
        containerOfferings.removeAllViews();
        if (tvOfferingsTitle != null) {
            tvOfferingsTitle.setText("Courses & Teachers (" + offerings.size() + ")");
        }
        if (offerings.size() == 0) {
            if (tvOfferingsEmpty != null) tvOfferingsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvOfferingsEmpty != null) tvOfferingsEmpty.setVisibility(View.GONE);

        float density = getResources().getDisplayMetrics().density;
        for (JsonElement el : offerings) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            String cName = UiUtils.optString(o, "course", "Course");
            String tName = "Unassigned";
            if (o.has("active_teachers") && o.get("active_teachers").isJsonArray() && o.getAsJsonArray("active_teachers").size() > 0) {
                JsonObject tObj = o.getAsJsonArray("active_teachers").get(0).getAsJsonObject();
                String code = UiUtils.optString(tObj, "teacher_code", "");
                String full = UiUtils.optString(tObj, "user",
                        (UiUtils.optString(tObj, "first_name", "") + " " + UiUtils.optString(tObj, "last_name", "")).trim());
                tName = code.isEmpty() ? full : (full + " (" + code + ")");
            }
            int enrCount = UiUtils.optInt(o, "enrolled_count", 0);

            TextView tv = new TextView(this);
            tv.setText("• " + cName + " — " + tName + " [" + enrCount + " enrolled]");
            tv.setTextColor(getColor(R.color.admin_text_primary));
            tv.setTextSize(13.5f);
            tv.setPadding(0, Math.round(4 * density), 0, Math.round(4 * density));
            containerOfferings.addView(tv);
        }
    }

    private void renderStudents(JsonArray students) {
        if (containerStudents == null) return;
        containerStudents.removeAllViews();
        if (tvStudentsTitle != null) {
            tvStudentsTitle.setText("Enrolled Students (" + students.size() + ")");
        }
        if (tvCapacity != null) {
            tvCapacity.setText(students.size() + " / " + classModel.getCapacity() + " Seats");
        }
        if (students.size() == 0) {
            if (tvStudentsEmpty != null) tvStudentsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvStudentsEmpty != null) tvStudentsEmpty.setVisibility(View.GONE);

        float density = getResources().getDisplayMetrics().density;
        for (JsonElement el : students) {
            if (!el.isJsonObject()) continue;
            JsonObject s = el.getAsJsonObject();
            long stId = UiUtils.optInt(s, "id", 0);
            long userId = UiUtils.optInt(s, "user_id", 0);
            String sc = UiUtils.optString(s, "student_code", "");
            String sn = UiUtils.optString(s, "khmer_name",
                    UiUtils.optString(s, "english_name", UiUtils.optString(s, "first_name", "Student")));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(Math.round(8 * density), Math.round(8 * density), Math.round(8 * density), Math.round(8 * density));

            TextView tvNameRow = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvNameRow.setLayoutParams(lp);
            tvNameRow.setText((sc.isEmpty() ? "" : sc + " — ") + sn);
            tvNameRow.setTextColor(getColor(R.color.admin_text_primary));
            tvNameRow.setTextSize(14f);
            tvNameRow.setTypeface(null, Typeface.BOLD);

            row.addView(tvNameRow);
            if (stId > 0 || userId > 0) {
                TextView tvOpen = new TextView(this);
                tvOpen.setText("View >");
                tvOpen.setTextColor(getColor(R.color.admin_primary_blue));
                tvOpen.setTextSize(12.5f);
                tvOpen.setTypeface(null, Typeface.BOLD);
                row.addView(tvOpen);
                row.setOnClickListener(v -> {
                    Student st = new Student();
                    st.setStudentCode(sc);
                    st.setName(sn);
                    if (stId > 0) st.setDbId(stId);
                    if (userId > 0) st.setUserId(userId);
                    Intent i = new Intent(this, AdminStudentDetailActivity.class);
                    i.putExtra("student", st);
                    startActivity(i);
                });
            }
            containerStudents.addView(row);
        }
    }

    private void setupListeners() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddClassActivity.class);
                intent.putExtra(AddClassActivity.EXTRA_EDIT_CLASS, classModel);
                editClassLauncher.launch(intent);
            });
        }

        if (btnEnroll != null) {
            btnEnroll.setOnClickListener(v -> {
                Intent intent = new Intent(this, BulkClassEnrollmentActivity.class);
                startActivity(intent);
            });
        }
    }
}
