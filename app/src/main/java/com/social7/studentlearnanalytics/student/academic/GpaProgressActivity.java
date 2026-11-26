package com.social7.studentlearnanalytics.student.academic;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.student.grades.CourseGradeActivity;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GpaProgressActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvStudentMajorContext;
    private TextView tvStudentCurrentPeriod;
    private TextView tvCumulativeGpa;
    private TextView tvCompletedCredits;
    private TextView tvSemesterGpa;
    private View cardGpaStatus, btnRetryGpa;
    private TextView tvGpaStatus;

    private MaterialButton btnYear1, btnYear2, btnYear3, btnYear4;
    private LinearLayout layoutSemestersContainer;
    private LinearLayout layoutRetakesContainer;

    private JsonArray yearsData = new JsonArray();
    private int selectedYear = 1;
    private boolean userManuallySelectedYear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_gpa_progress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.gpa_academic_progress);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) progressBar = new ProgressBar(this);

        tvStudentMajorContext = findViewById(R.id.tvStudentMajorContext);
        tvStudentCurrentPeriod = findViewById(R.id.tvStudentCurrentPeriod);
        tvCumulativeGpa = findViewById(R.id.tvCumulativeGpa);
        tvCompletedCredits = findViewById(R.id.tvCompletedCredits);
        tvSemesterGpa = findViewById(R.id.tvSemesterGpa);
        cardGpaStatus = findViewById(R.id.cardGpaStatus);
        tvGpaStatus = findViewById(R.id.tvGpaStatus);
        btnRetryGpa = findViewById(R.id.btnRetryGpa);

        btnYear1 = findViewById(R.id.btnYear1);
        btnYear2 = findViewById(R.id.btnYear2);
        btnYear3 = findViewById(R.id.btnYear3);
        btnYear4 = findViewById(R.id.btnYear4);
        layoutSemestersContainer = findViewById(R.id.layoutSemestersContainer);
        layoutRetakesContainer = findViewById(R.id.layoutRetakesContainer);

        if (getIntent() != null && getIntent().hasExtra("selected_year")) {
            int intentYear = getIntent().getIntExtra("selected_year", 1);
            if (intentYear >= 1 && intentYear <= 4) {
                selectedYear = intentYear;
                userManuallySelectedYear = true;
            }
        }

        setupYearTabs();

        if (btnRetryGpa != null) {
            btnRetryGpa.setOnClickListener(v -> loadGpaHistory());
        }

        loadGpaHistory();
    }

    private void setupYearTabs() {
        if (btnYear1 != null) btnYear1.setOnClickListener(v -> selectYearTab(1, true));
        if (btnYear2 != null) btnYear2.setOnClickListener(v -> selectYearTab(2, true));
        if (btnYear3 != null) btnYear3.setOnClickListener(v -> selectYearTab(3, true));
        if (btnYear4 != null) btnYear4.setOnClickListener(v -> selectYearTab(4, true));
        updateYearTabStyles();
    }

    private void selectYearTab(int year, boolean fromUser) {
        if (year < 1 || year > 4) year = 1;
        this.selectedYear = year;
        if (fromUser) {
            this.userManuallySelectedYear = true;
        }
        updateYearTabStyles();
        renderSelectedYearSemesters();
    }

    private void updateYearTabStyles() {
        MaterialButton[] buttons = new MaterialButton[]{btnYear1, btnYear2, btnYear3, btnYear4};
        int activeBg = ContextCompat.getColor(this, R.color.slas_primary_purple);
        int activeText = ContextCompat.getColor(this, R.color.white);
        int inactiveBg = ContextCompat.getColor(this, R.color.slas_surface);
        int inactiveText = ContextCompat.getColor(this, R.color.slas_text_secondary);
        int borderStroke = ContextCompat.getColor(this, R.color.slas_border);

        for (int i = 0; i < buttons.length; i++) {
            MaterialButton btn = buttons[i];
            if (btn == null) continue;
            boolean isSelected = (i + 1) == selectedYear;
            btn.setBackgroundTintList(ColorStateList.valueOf(isSelected ? activeBg : inactiveBg));
            btn.setTextColor(isSelected ? activeText : inactiveText);
            btn.setStrokeColor(ColorStateList.valueOf(isSelected ? activeBg : borderStroke));
            btn.setStrokeWidth(dpToPx(1));
        }
    }

    private void loadGpaHistory() {
        progressBar.setVisibility(View.VISIBLE);
        if (cardGpaStatus != null) cardGpaStatus.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentGpaHistory(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().has("data") && response.body().get("data").isJsonObject()) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    bindGpaHistoryResponse(data);
                } else {
                    showStatus("Failed to load GPA academic history (HTTP " + response.code() + ").");
                    loadLegacyFallback();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GpaProgressActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
                showStatus("Network error while loading GPA academic history.");
            }
        });
    }

    private void bindGpaHistoryResponse(JsonObject data) {
        if (data.has("student") && data.get("student").isJsonObject()) {
            JsonObject st = data.getAsJsonObject("student");
            String major = getJsonString(st, "program_name", "Major");
            String facCode = getJsonString(st, "faculty_code", "");
            if (tvStudentMajorContext != null) {
                tvStudentMajorContext.setText(!facCode.isEmpty() ? (major + " (" + facCode + ")") : major);
            }
        }

        int currentStudyYear = 1;
        if (data.has("current_period") && data.get("current_period").isJsonObject()) {
            JsonObject cp = data.getAsJsonObject("current_period");
            currentStudyYear = cp.has("study_year") && !cp.get("study_year").isJsonNull()
                    ? cp.get("study_year").getAsInt() : 1;
            String ylName = getJsonString(cp, "year_level_name", "Year " + currentStudyYear);
            String semName = getJsonString(cp, "semester_name", "Semester 1");
            String ayName = getJsonString(cp, "academic_year_name", "");
            if (tvStudentCurrentPeriod != null) {
                String periodLabel = "Current: " + ylName + " • " + semName;
                if (!ayName.isEmpty()) {
                    periodLabel += " (" + ayName + ")";
                }
                tvStudentCurrentPeriod.setText(periodLabel);
            }
        }

        String cgpaDisplay = getJsonString(data, "cumulative_gpa_display", "N/A");
        if (tvCumulativeGpa != null) {
            tvCumulativeGpa.setText(cgpaDisplay);
            tvCumulativeGpa.setTextColor(ContextCompat.getColor(this,
                    "N/A".equalsIgnoreCase(cgpaDisplay) ? R.color.slas_text_secondary : R.color.slas_primary_purple));
        }

        if (data.has("completed_credits") && !data.get("completed_credits").isJsonNull()) {
            double credits = data.get("completed_credits").getAsDouble();
            String creditsStr = credits == Math.floor(credits)
                    ? String.valueOf((long) credits)
                    : String.format(Locale.US, "%.1f", credits);
            if (tvCompletedCredits != null) {
                tvCompletedCredits.setText(creditsStr);
                tvCompletedCredits.setTextColor(ContextCompat.getColor(this, R.color.slas_text_primary));
            }
        } else if (tvCompletedCredits != null) {
            tvCompletedCredits.setText("0");
            tvCompletedCredits.setTextColor(ContextCompat.getColor(this, R.color.slas_text_primary));
        }

        if (data.has("years") && data.get("years").isJsonArray()) {
            yearsData = data.getAsJsonArray("years");
        } else {
            yearsData = new JsonArray();
        }

        String currentSemGpaDisplay = "N/A";
        for (int i = 0; i < yearsData.size(); i++) {
            JsonObject yObj = yearsData.get(i).getAsJsonObject();
            if (yObj.has("semesters") && yObj.get("semesters").isJsonArray()) {
                JsonArray sems = yObj.getAsJsonArray("semesters");
                for (int j = 0; j < sems.size(); j++) {
                    JsonObject sem = sems.get(j).getAsJsonObject();
                    String state = getJsonString(sem, "state", "");
                    if ("Current".equalsIgnoreCase(state)) {
                        currentSemGpaDisplay = getJsonString(sem, "semester_gpa_display", "N/A");
                        break;
                    }
                }
            }
        }
        if (tvSemesterGpa != null) {
            tvSemesterGpa.setText(currentSemGpaDisplay);
            tvSemesterGpa.setTextColor(ContextCompat.getColor(this,
                    "N/A".equalsIgnoreCase(currentSemGpaDisplay) ? R.color.slas_text_secondary : R.color.slas_primary_purple));
        }

        if (!userManuallySelectedYear && currentStudyYear >= 1 && currentStudyYear <= 4) {
            selectedYear = currentStudyYear;
        }
        updateYearTabStyles();
        renderSelectedYearSemesters();

        if (data.has("retakes") && data.get("retakes").isJsonObject()) {
            renderRetakesSection(data.getAsJsonObject("retakes"));
        }
    }

    private void renderSelectedYearSemesters() {
        if (layoutSemestersContainer == null) return;
        layoutSemestersContainer.removeAllViews();

        JsonObject targetYearObj = null;
        for (int i = 0; i < yearsData.size(); i++) {
            if (!yearsData.get(i).isJsonObject()) continue;
            JsonObject yObj = yearsData.get(i).getAsJsonObject();
            int sy = yObj.has("study_year") && !yObj.get("study_year").isJsonNull()
                    ? yObj.get("study_year").getAsInt() : 0;
            if (sy == selectedYear) {
                targetYearObj = yObj;
                break;
            }
        }

        if (targetYearObj == null || !targetYearObj.has("semesters") || !targetYearObj.get("semesters").isJsonArray()) {
            addEmptyYearCard("Year " + selectedYear, "No semester curriculum or GPA records found for Year " + selectedYear + ".");
            return;
        }

        JsonArray semesters = targetYearObj.getAsJsonArray("semesters");
        if (semesters.size() == 0) {
            addEmptyYearCard("Year " + selectedYear, getJsonString(targetYearObj, "empty_state",
                    "No semesters configured for Year " + selectedYear + "."));
            return;
        }

        for (int i = 0; i < semesters.size(); i++) {
            if (!semesters.get(i).isJsonObject()) continue;
            JsonObject sem = semesters.get(i).getAsJsonObject();
            layoutSemestersContainer.addView(buildSemesterCard(sem));
        }
    }

    private View buildSemesterCard(JsonObject sem) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_gpa_semester_card, layoutSemestersContainer, false);

        TextView tvSemTitle = card.findViewById(R.id.tvSemesterTitle);
        TextView tvSemDates = card.findViewById(R.id.tvSemesterDates);
        TextView tvCalMsg = card.findViewById(R.id.tvCalendarNotice);
        TextView tvBadge = card.findViewById(R.id.tvSemesterState);
        LinearLayout llCourses = card.findViewById(R.id.llCoursesContainer);
        TextView tvNoCourses = card.findViewById(R.id.tvNoCourses);
        TextView tvAvg = card.findViewById(R.id.tvSemesterAverage);
        TextView tvGpa = card.findViewById(R.id.tvSemesterGpa);

        String semName = getJsonString(sem, "semester_name", "Semester");
        int semStudyYear = sem.has("study_year") && !sem.get("study_year").isJsonNull()
                ? sem.get("study_year").getAsInt() : selectedYear;
        tvSemTitle.setText("Year " + semStudyYear + " — " + semName);

        String ayName = getJsonString(sem, "academic_year_name", "");
        String startDate = getJsonString(sem, "start_date", "");
        String endDate = getJsonString(sem, "end_date", "");
        StringBuilder subBuilder = new StringBuilder();
        if (!ayName.isEmpty()) subBuilder.append(ayName);
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            if (subBuilder.length() > 0) subBuilder.append(" • ");
            subBuilder.append(startDate).append(" to ").append(endDate);
        }
        if (subBuilder.length() > 0) {
            tvSemDates.setText(subBuilder.toString());
            tvSemDates.setVisibility(View.VISIBLE);
        }

        String calendarMessage = getJsonString(sem, "calendar_message", "");
        if (!calendarMessage.isEmpty()) {
            tvCalMsg.setText(calendarMessage);
            tvCalMsg.setVisibility(View.VISIBLE);
        }

        String state = getJsonString(sem, "state", "Planned");
        tvBadge.setText(state);
        if ("Completed".equalsIgnoreCase(state)) {
            tvBadge.setBackgroundResource(R.drawable.bg_slas_pill_green);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.slas_success));
        } else if ("Current".equalsIgnoreCase(state)) {
            tvBadge.setBackgroundResource(R.drawable.bg_slas_pill_purple);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.slas_primary_purple));
        } else {
            tvBadge.setBackgroundResource(R.drawable.bg_slas_pill_neutral);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.slas_text_secondary));
        }

        JsonArray courses = sem.has("courses") && sem.get("courses").isJsonArray()
                ? sem.getAsJsonArray("courses") : new JsonArray();

        if (courses.size() == 0) {
            tvNoCourses.setVisibility(View.VISIBLE);
        } else {
            tvNoCourses.setVisibility(View.GONE);
            for (int i = 0; i < courses.size(); i++) {
                if (!courses.get(i).isJsonObject()) continue;
                JsonObject c = courses.get(i).getAsJsonObject();
                boolean isLast = (i == courses.size() - 1);
                llCourses.addView(buildCourseRow(c, llCourses, isLast));
            }
        }

        String semAvgDisplay = getJsonString(sem, "semester_average_display", "N/A");
        String semGpaDisplay = getJsonString(sem, "semester_gpa_display", "N/A");

        tvAvg.setText("Semester Average: " + semAvgDisplay);
        tvAvg.setTextColor(ContextCompat.getColor(this,
                "N/A".equalsIgnoreCase(semAvgDisplay) ? R.color.slas_text_secondary : R.color.slas_text_primary));

        tvGpa.setText("Semester GPA: " + semGpaDisplay);
        tvGpa.setTextColor(ContextCompat.getColor(this,
                "N/A".equalsIgnoreCase(semGpaDisplay) ? R.color.slas_text_secondary : R.color.slas_primary_purple));

        return card;
    }

    private View buildCourseRow(JsonObject course, ViewGroup parent, boolean isLast) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_gpa_course_row, parent, false);

        TextView tvName = row.findViewById(R.id.tvCourseTitle);
        TextView tvMeta = row.findViewById(R.id.tvCourseMeta);
        TextView tvScore = row.findViewById(R.id.tvCourseScore);
        TextView tvGrade = row.findViewById(R.id.tvCourseGrade);
        View divider = row.findViewById(R.id.viewRowDivider);

        String courseName = getJsonString(course, "course_name", "Course");
        String courseCode = getJsonString(course, "course_code", "");
        boolean isRetake = course.has("is_retake") && !course.get("is_retake").isJsonNull() && course.get("is_retake").getAsBoolean();
        boolean requiresRetake = course.has("requires_retake") && !course.get("requires_retake").isJsonNull() && course.get("requires_retake").getAsBoolean();
        int attemptNumber = course.has("attempt_number") && !course.get("attempt_number").isJsonNull()
                ? course.get("attempt_number").getAsInt() : 1;

        String titleText = !courseCode.isEmpty() ? (courseCode + " — " + courseName) : courseName;
        if (isRetake || attemptNumber > 1) {
            titleText += " [Retake #" + attemptNumber + "]";
        } else if (requiresRetake) {
            titleText += " [Retake Required]";
        }
        tvName.setText(titleText);

        String credits = getJsonString(course, "credits", "");
        String categoryName = getJsonString(course, "course_category_name", getJsonString(course, "category_name", ""));
        StringBuilder meta = new StringBuilder();
        if (!credits.isEmpty()) meta.append(credits).append(" Credits");
        if (!categoryName.isEmpty()) {
            if (meta.length() > 0) meta.append(" • ");
            meta.append(categoryName);
        }
        if (meta.length() > 0) {
            tvMeta.setText(meta.toString());
            tvMeta.setVisibility(View.VISIBLE);
        }

        String scoreDisplay = getJsonString(course, "score_display", "N/A");
        String gradeDisplay = getJsonString(course, "grade_display", "N/A");

        tvScore.setText(scoreDisplay);
        tvScore.setTextColor(ContextCompat.getColor(this,
                "N/A".equalsIgnoreCase(scoreDisplay) ? R.color.slas_text_secondary : R.color.slas_text_primary));

        tvGrade.setText(gradeDisplay);
        if ("F".equalsIgnoreCase(gradeDisplay)) {
            tvGrade.setTextColor(ContextCompat.getColor(this, R.color.slas_danger));
        } else if ("N/A".equalsIgnoreCase(gradeDisplay)) {
            tvGrade.setTextColor(ContextCompat.getColor(this, R.color.slas_text_secondary));
        } else {
            tvGrade.setTextColor(ContextCompat.getColor(this, R.color.slas_success));
        }

        divider.setVisibility(isLast ? View.GONE : View.VISIBLE);

        final int offeringId = course.has("course_offering_id") && !course.get("course_offering_id").isJsonNull()
                ? course.get("course_offering_id").getAsInt() : 0;
        if (offeringId > 0) {
            final String displayCourseTitle = titleText;
            final String displayMeta = meta.toString();
            row.setOnClickListener(v -> showScoreBreakdownDialog(offeringId, displayCourseTitle, displayMeta));
        } else {
            row.setClickable(false);
            row.setBackground(null);
        }

        return row;
    }

    private void showScoreBreakdownDialog(int offeringId, String courseTitle, String courseMeta) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_score_breakdown, null, false);
        TextView tvTitle = dialogView.findViewById(R.id.tvBreakdownCourseTitle);
        TextView tvSub = dialogView.findViewById(R.id.tvBreakdownCourseSub);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbBreakdownLoading);
        View layoutTable = dialogView.findViewById(R.id.layoutBreakdownTable);
        LinearLayout llRows = dialogView.findViewById(R.id.llBreakdownRows);
        TextView tvTotal = dialogView.findViewById(R.id.tvBreakdownTotal);
        TextView tvGrade = dialogView.findViewById(R.id.tvBreakdownGrade);
        TextView tvError = dialogView.findViewById(R.id.tvBreakdownError);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseBreakdown);
        MaterialButton btnOpenFull = dialogView.findViewById(R.id.btnOpenFullCourseGrade);

        tvTitle.setText(courseTitle);
        if (courseMeta != null && !courseMeta.isEmpty()) {
            tvSub.setText(courseMeta);
            tvSub.setVisibility(View.VISIBLE);
        }

        pbLoading.setVisibility(View.VISIBLE);
        layoutTable.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnOpenFull.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(GpaProgressActivity.this, CourseGradeActivity.class);
            intent.putExtra("course_offering_id", offeringId);
            startActivity(intent);
        });

        dialog.show();

        ApiClient.getApiService(this).getStudentModuleGradesCourse(offeringId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed() || !dialog.isShowing()) return;
                pbLoading.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().has("data") || !response.body().get("data").isJsonObject()) {
                    tvError.setText("Unable to load score breakdown (HTTP " + response.code() + ").");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }

                JsonObject data = response.body().getAsJsonObject("data");
                String teacher = getJsonString(data, "teacher", "");
                String semester = getJsonString(data, "semester", "");
                String ay = getJsonString(data, "academic_year", "");
                StringBuilder sub = new StringBuilder();
                if (!semester.isEmpty()) sub.append(semester);
                if (!ay.isEmpty()) {
                    if (sub.length() > 0) sub.append(" • ");
                    sub.append(ay);
                }
                if (!teacher.isEmpty()) {
                    if (sub.length() > 0) sub.append(" • ");
                    sub.append(teacher);
                }
                if (sub.length() > 0) {
                    tvSub.setText(sub.toString());
                    tvSub.setVisibility(View.VISIBLE);
                }

                if (data.has("weighted_breakdown") && data.get("weighted_breakdown").isJsonObject()) {
                    JsonObject wb = data.getAsJsonObject("weighted_breakdown");
                    JsonArray groups = wb.has("groups") && wb.get("groups").isJsonArray()
                            ? wb.getAsJsonArray("groups") : new JsonArray();
                    llRows.removeAllViews();
                    LayoutInflater inf = LayoutInflater.from(GpaProgressActivity.this);
                    for (int i = 0; i < groups.size(); i++) {
                        if (!groups.get(i).isJsonObject()) continue;
                        JsonObject g = groups.get(i).getAsJsonObject();
                        View rView = inf.inflate(R.layout.item_score_breakdown_row, llRows, false);
                        TextView gLabel = rView.findViewById(R.id.tvGroupLabel);
                        TextView gRaw = rView.findViewById(R.id.tvGroupRaw);
                        TextView gWeighted = rView.findViewById(R.id.tvGroupWeighted);
                        TextView gComponents = rView.findViewById(R.id.tvGroupComponents);
                        View divider = rView.findViewById(R.id.viewGroupDivider);

                        gLabel.setText(getJsonString(g, "label", "Group"));
                        gRaw.setText(getJsonString(g, "raw_display", "N/A"));
                        gWeighted.setText(getJsonString(g, "weighted_display", "N/A"));
                        boolean hasScore = g.has("has_score") && !g.get("has_score").isJsonNull() && g.get("has_score").getAsBoolean();
                        gWeighted.setTextColor(ContextCompat.getColor(GpaProgressActivity.this,
                                hasScore ? R.color.slas_primary_purple : R.color.slas_text_secondary));
                        String compSummary = buildComponentSummary(g);
                        if (gComponents != null && !compSummary.isEmpty()) {
                            gComponents.setText(compSummary);
                            gComponents.setVisibility(View.VISIBLE);
                        }
                        divider.setVisibility(i == groups.size() - 1 ? View.GONE : View.VISIBLE);
                        llRows.addView(rView);
                    }

                    String totalDisp = getJsonString(wb, "total_display", "N/A");
                    String letter = getJsonString(wb, "letter_grade", "N/A");
                    tvTotal.setText(totalDisp);
                    tvTotal.setTextColor(ContextCompat.getColor(GpaProgressActivity.this,
                            "N/A".equalsIgnoreCase(totalDisp) ? R.color.slas_text_secondary : R.color.slas_primary_purple));
                    tvGrade.setText(letter);
                    if ("F".equalsIgnoreCase(letter)) {
                        tvGrade.setBackgroundResource(R.drawable.bg_slas_pill_red);
                        tvGrade.setTextColor(ContextCompat.getColor(GpaProgressActivity.this, R.color.slas_danger));
                    } else if ("N/A".equalsIgnoreCase(letter)) {
                        tvGrade.setBackgroundResource(R.drawable.bg_slas_pill_neutral);
                        tvGrade.setTextColor(ContextCompat.getColor(GpaProgressActivity.this, R.color.slas_text_secondary));
                    } else {
                        tvGrade.setBackgroundResource(R.drawable.bg_slas_pill_green);
                        tvGrade.setTextColor(ContextCompat.getColor(GpaProgressActivity.this, R.color.slas_success));
                    }
                    layoutTable.setVisibility(View.VISIBLE);
                } else {
                    tvError.setText("No weighted score breakdown available yet.");
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed() || !dialog.isShowing()) return;
                pbLoading.setVisibility(View.GONE);
                tvError.setText("Network error loading score breakdown.");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private String buildComponentSummary(JsonObject group) {
        if (group == null || !group.has("components") || !group.get("components").isJsonArray()) {
            return "";
        }
        JsonArray comps = group.getAsJsonArray("components");
        if (comps.size() <= 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < comps.size(); i++) {
            if (!comps.get(i).isJsonObject()) continue;
            JsonObject c = comps.get(i).getAsJsonObject();
            String label = getJsonString(c, "label", "");
            String raw = getJsonString(c, "raw_display", "N/A");
            if (label.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" • ");
            sb.append(label).append(": ").append(raw);
        }
        return sb.toString();
    }

    private void renderRetakesSection(JsonObject retakesObj) {
        if (layoutRetakesContainer == null) return;
        layoutRetakesContainer.removeAllViews();

        JsonArray retakes = retakesObj.has("retakes") && retakesObj.get("retakes").isJsonArray()
                ? retakesObj.getAsJsonArray("retakes") : new JsonArray();

        if (retakes.size() == 0) {
            layoutRetakesContainer.setVisibility(View.GONE);
            return;
        }

        layoutRetakesContainer.setVisibility(View.VISIBLE);

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dpToPx(10);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(12));
        card.setCardElevation(0f);
        card.setStrokeColor(ContextCompat.getColor(this, R.color.slas_border));
        card.setStrokeWidth(dpToPx(1));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.slas_surface));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(14);
        container.setPadding(pad, pad, pad, pad);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("Course Retake Management & History");
        tvHeader.setTextColor(ContextCompat.getColor(this, R.color.slas_text_primary));
        tvHeader.setTextSize(14f);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(tvHeader);

        for (int i = 0; i < retakes.size(); i++) {
            if (!retakes.get(i).isJsonObject()) continue;
            JsonObject r = retakes.get(i).getAsJsonObject();

            String cName = getJsonString(r, "course_name", "Course");
            String cCode = getJsonString(r, "course_code", "");
            String status = getJsonString(r, "status", "required");
            JsonObject origObj = r.has("original_attempt") && r.get("original_attempt").isJsonObject()
                    ? r.getAsJsonObject("original_attempt") : null;
            JsonObject retakeObj = r.has("retake_attempt") && r.get("retake_attempt").isJsonObject()
                    ? r.getAsJsonObject("retake_attempt") : null;
            String origScore = origObj != null
                    ? getJsonString(origObj, "score", getJsonString(r, "original_score", "N/A"))
                    : getJsonString(r, "original_score", "N/A");
            String origGrade = origObj != null
                    ? getJsonString(origObj, "letter_grade", getJsonString(r, "original_letter_grade", "F"))
                    : getJsonString(r, "original_letter_grade", "F");
            String retakeScore = retakeObj != null
                    ? getJsonString(retakeObj, "score", getJsonString(r, "retake_score", ""))
                    : getJsonString(r, "retake_score", "");
            String retakeGrade = retakeObj != null
                    ? getJsonString(retakeObj, "letter_grade", getJsonString(r, "retake_letter_grade", ""))
                    : getJsonString(r, "retake_letter_grade", "");

            TextView tvItem = new TextView(this);
            tvItem.setPadding(0, dpToPx(8), 0, dpToPx(2));
            StringBuilder line = new StringBuilder();
            line.append(!cCode.isEmpty() ? (cCode + " — " + cName) : cName);
            line.append("\nOriginal Attempt: ").append(origScore).append(" (").append(origGrade).append(")");
            if (!retakeGrade.isEmpty()) {
                line.append("  →  Retake Attempt: ").append(retakeScore).append(" (").append(retakeGrade).append(")");
            }
            line.append(" • Status: ").append(status.toUpperCase(Locale.US));

            tvItem.setText(line.toString());
            tvItem.setTextColor(ContextCompat.getColor(this, R.color.slas_text_secondary));
            tvItem.setTextSize(12.5f);
            container.addView(tvItem);
        }

        card.addView(container);
        layoutRetakesContainer.addView(card);
    }

    private void addEmptyYearCard(String yearLabel, String message) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(10);
        card.setLayoutParams(params);
        card.setRadius(dpToPx(12));
        card.setCardElevation(0f);
        card.setStrokeColor(ContextCompat.getColor(this, R.color.slas_border));
        card.setStrokeWidth(dpToPx(1));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.slas_surface));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(20);
        box.setPadding(pad, pad, pad, pad);
        box.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(yearLabel);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.slas_text_primary));
        tvTitle.setTextSize(14.5f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        box.addView(tvTitle);

        TextView tvMsg = new TextView(this);
        tvMsg.setText(message);
        tvMsg.setTextColor(ContextCompat.getColor(this, R.color.slas_text_secondary));
        tvMsg.setTextSize(12.5f);
        tvMsg.setGravity(Gravity.CENTER);
        tvMsg.setPadding(0, dpToPx(6), 0, 0);
        box.addView(tvMsg);

        card.addView(box);
        layoutSemestersContainer.addView(card);
    }

    private void loadLegacyFallback() {
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleDashboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().has("data") && response.body().get("data").isJsonObject()) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    if (data.has("metrics") && data.get("metrics").isJsonObject()) {
                        JsonObject metrics = data.getAsJsonObject("metrics");
                        String cgpa = getJsonString(metrics, "cumulative_gpa", "N/A");
                        String sgpa = getJsonString(metrics, "current_semester_gpa", getJsonString(metrics, "semester_gpa", "N/A"));
                        if (tvCumulativeGpa != null) {
                            tvCumulativeGpa.setText(cgpa);
                            tvCumulativeGpa.setTextColor(ContextCompat.getColor(GpaProgressActivity.this,
                                    "N/A".equalsIgnoreCase(cgpa) ? R.color.slas_text_secondary : R.color.slas_primary_purple));
                        }
                        if (tvSemesterGpa != null) {
                            tvSemesterGpa.setText(sgpa);
                            tvSemesterGpa.setTextColor(ContextCompat.getColor(GpaProgressActivity.this,
                                    "N/A".equalsIgnoreCase(sgpa) ? R.color.slas_text_secondary : R.color.slas_primary_purple));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    private String getJsonString(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            String val = el.getAsString();
            return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void showStatus(String message) {
        if (cardGpaStatus != null) cardGpaStatus.setVisibility(View.VISIBLE);
        if (tvGpaStatus != null) tvGpaStatus.setText(message);
    }
}
