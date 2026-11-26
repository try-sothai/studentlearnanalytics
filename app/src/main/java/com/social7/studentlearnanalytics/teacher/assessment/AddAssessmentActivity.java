package com.social7.studentlearnanalytics.teacher.assessment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.TeachingAssignment;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAssessmentActivity extends AppCompatActivity {

    private TextView tvHeaderTitle, tvFormSectionHeader;
    private MaterialButton btnTypeAssignment, btnTypeHomework, btnTypeQuiz, btnTypeExam;
    private MaterialButton btnExamMidterm, btnExamFinal;
    private View layoutExamSubtype, tilVenue, tilDuration, tilTotalScore, layoutQuizBuilderSection;
    private TextInputLayout tilDueDate, tilQuizStartDate;
    private TextInputEditText etTitle, etDescription, etVenue, etTotalScore, etDuration, etDueDate, etQuizStartDate;
    private AutoCompleteTextView actvQuizMaxAttempts, actvQuizScoringMethod;
    private CheckBox cbQuizAllowLate;
    private MaterialButton btnSaveDraft, btnSubmit;

    // Teaching Assignment Selector
    private ProgressBar pbOfferingsLoading;
    private LinearLayout layoutNoAssignments, layoutAssignmentSelectors;
    private TextView tvNoAssignmentsTitle, tvNoAssignmentsDesc, tvSelectedOfferingSummary;
    private MaterialButton btnRetryOfferings;
    private AutoCompleteTextView actvAcademicYear, actvSemester, actvClass, actvCourse;

    private final List<TeachingAssignment> allAssignments = new ArrayList<>();
    private final List<IdLabel> yearOptions = new ArrayList<>();
    private final List<IdLabel> semesterOptions = new ArrayList<>();
    private final List<IdLabel> classOptions = new ArrayList<>();
    private final List<TeachingAssignment> courseOptions = new ArrayList<>();

    private Integer selectedYearId = null;
    private Integer selectedSemesterId = null;
    private Integer selectedClassId = null;
    private TeachingAssignment selectedAssignment = null;

    private String selectedType = "Assignment";
    private String examSubtype = "midterm";
    private boolean isSaving = false;

    private static class IdLabel {
        final int id;
        final String label;

        IdLabel(int id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assessment);

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Add Assessment", "", true);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvFormSectionHeader = findViewById(R.id.tvFormSectionHeader);

        btnTypeAssignment = findViewById(R.id.btnTypeAssignment);
        btnTypeHomework = findViewById(R.id.btnTypeHomework);
        btnTypeQuiz = findViewById(R.id.btnTypeQuiz);
        btnTypeExam = findViewById(R.id.btnTypeExam);

        btnExamMidterm = findViewById(R.id.btnExamMidterm);
        btnExamFinal = findViewById(R.id.btnExamFinal);

        layoutExamSubtype = findViewById(R.id.layoutExamSubtype);
        tilVenue = findViewById(R.id.tilVenue);
        tilDuration = findViewById(R.id.tilDuration);
        tilTotalScore = findViewById(R.id.tilTotalScore);
        tilDueDate = findViewById(R.id.tilDueDate);
        layoutQuizBuilderSection = findViewById(R.id.layoutQuizBuilderSection);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etVenue = findViewById(R.id.etVenue);
        etTotalScore = findViewById(R.id.etTotalScore);
        etDuration = findViewById(R.id.etDuration);
        etDueDate = findViewById(R.id.etDueDate);
        tilQuizStartDate = findViewById(R.id.tilQuizStartDate);
        etQuizStartDate = findViewById(R.id.etQuizStartDate);
        actvQuizMaxAttempts = findViewById(R.id.actvQuizMaxAttempts);
        actvQuizScoringMethod = findViewById(R.id.actvQuizScoringMethod);
        cbQuizAllowLate = findViewById(R.id.cbQuizAllowLate);

        if (etQuizStartDate != null) {
            etQuizStartDate.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilQuizStartDate, etQuizStartDate, false, true));
            etQuizStartDate.setOnLongClickListener(v -> {
                etQuizStartDate.setText("");
                Toast.makeText(this, "Start date cleared (quiz opens immediately on publish)", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
        if (actvQuizMaxAttempts != null) {
            String[] attOpts = new String[]{"1 Attempt", "2 Attempts", "3 Attempts", "5 Attempts", "Unlimited"};
            actvQuizMaxAttempts.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, attOpts));
            actvQuizMaxAttempts.setText("1 Attempt", false);
        }
        if (actvQuizScoringMethod != null) {
            String[] scoreOpts = new String[]{"Highest Score", "Latest Attempt", "Average Score"};
            actvQuizScoringMethod.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, scoreOpts));
            actvQuizScoringMethod.setText("Highest Score", false);
        }

        pbOfferingsLoading = findViewById(R.id.pbOfferingsLoading);
        layoutNoAssignments = findViewById(R.id.layoutNoAssignments);
        layoutAssignmentSelectors = findViewById(R.id.layoutAssignmentSelectors);
        tvNoAssignmentsTitle = findViewById(R.id.tvNoAssignmentsTitle);
        tvNoAssignmentsDesc = findViewById(R.id.tvNoAssignmentsDesc);
        tvSelectedOfferingSummary = findViewById(R.id.tvSelectedOfferingSummary);
        btnRetryOfferings = findViewById(R.id.btnRetryOfferings);

        actvAcademicYear = findViewById(R.id.actvAcademicYear);
        actvSemester = findViewById(R.id.actvSemester);
        actvClass = findViewById(R.id.actvClass);
        actvCourse = findViewById(R.id.actvCourse);

        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Default due/exam date to +7 days instead of hardcoded 2026-12-31
        Calendar def = Calendar.getInstance();
        def.add(Calendar.DAY_OF_MONTH, 7);
        etDueDate.setText(String.format(Locale.US, "%04d-%02d-%02d 23:59:59",
                def.get(Calendar.YEAR), def.get(Calendar.MONTH) + 1, def.get(Calendar.DAY_OF_MONTH)));
        etDueDate.setOnClickListener(v -> showDatePicker());

        btnRetryOfferings.setOnClickListener(v -> fetchTeachingAssignments());
        btnSaveDraft.setOnClickListener(v -> submitAssessment(true));
        btnSubmit.setOnClickListener(v -> submitAssessment(false));

        setupDropdownListeners();
        setupTypeSelector();

        String initialType = getIntent() != null ? getIntent().getStringExtra("initial_type") : null;
        if (initialType != null && !initialType.trim().isEmpty()) {
            selectType(initialType);
        } else {
            selectType("Assignment");
        }

        setSubmitButtonsEnabled(false);
        fetchTeachingAssignments();
    }

    private void fetchTeachingAssignments() {
        pbOfferingsLoading.setVisibility(View.VISIBLE);
        layoutNoAssignments.setVisibility(View.GONE);
        layoutAssignmentSelectors.setVisibility(View.GONE);
        setSubmitButtonsEnabled(false);

        ApiClient.getApiService(this).getTeacherTeachingAssignments().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbOfferingsLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    showAssignmentState(
                            "Unable to load teaching assignments",
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to retrieve your assigned classes and courses."),
                            true
                    );
                    return;
                }

                allAssignments.clear();
                JsonArray arr = AssessmentUiHelper.extractDataArray(response.body());
                for (JsonElement el : arr) {
                    if (el.isJsonObject()) {
                        TeachingAssignment ta = TeachingAssignment.fromJson(el.getAsJsonObject());
                        if (ta.getCourseOfferingId() > 0) {
                            allAssignments.add(ta);
                        }
                    }
                }

                if (allAssignments.isEmpty()) {
                    showAssignmentState(
                            "No teaching assignments available.",
                            "Please contact the administrator to assign classes and courses to your account.",
                            false
                    );
                    return;
                }

                layoutNoAssignments.setVisibility(View.GONE);
                layoutAssignmentSelectors.setVisibility(View.VISIBLE);
                populateAcademicYears();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbOfferingsLoading.setVisibility(View.GONE);
                showAssignmentState(
                        "Network error",
                        "Could not load your teaching assignments. Check your connection and try again.",
                        true
                );
            }
        });
    }

    private void showAssignmentState(String title, String desc, boolean allowRetry) {
        layoutNoAssignments.setVisibility(View.VISIBLE);
        layoutAssignmentSelectors.setVisibility(View.GONE);
        tvNoAssignmentsTitle.setText(title);
        tvNoAssignmentsDesc.setText(desc);
        btnRetryOfferings.setVisibility(allowRetry ? View.VISIBLE : View.GONE);
        selectedAssignment = null;
        setSubmitButtonsEnabled(false);
    }

    private void setupDropdownListeners() {
        actvAcademicYear.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < yearOptions.size()) {
                int newId = yearOptions.get(position).id;
                if (selectedYearId == null || selectedYearId != newId) {
                    selectedYearId = newId;
                    selectedSemesterId = null;
                    selectedClassId = null;
                    selectedAssignment = null;
                    populateSemesters();
                }
            }
        });

        actvSemester.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < semesterOptions.size()) {
                int newId = semesterOptions.get(position).id;
                if (selectedSemesterId == null || selectedSemesterId != newId) {
                    selectedSemesterId = newId;
                    selectedClassId = null;
                    selectedAssignment = null;
                    populateClasses();
                }
            }
        });

        actvClass.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < classOptions.size()) {
                int newId = classOptions.get(position).id;
                if (selectedClassId == null || selectedClassId != newId) {
                    selectedClassId = newId;
                    selectedAssignment = null;
                    populateCourses();
                }
            }
        });

        actvCourse.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < courseOptions.size()) {
                selectedAssignment = courseOptions.get(position);
                updateSelectedSummary();
            }
        });
    }

    private void populateAcademicYears() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (TeachingAssignment ta : allAssignments) {
            map.put(ta.getAcademicYearId(), ta.getAcademicYearName());
        }
        yearOptions.clear();
        List<String> labels = new ArrayList<>();
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            yearOptions.add(new IdLabel(e.getKey(), e.getValue()));
            labels.add(e.getValue());
        }

        actvAcademicYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (!yearOptions.isEmpty()) {
            IdLabel first = yearOptions.get(0);
            selectedYearId = first.id;
            actvAcademicYear.setText(first.label, false);
            populateSemesters();
        } else {
            selectedYearId = null;
            actvAcademicYear.setText("", false);
            populateSemesters();
        }
    }

    private void populateSemesters() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (TeachingAssignment ta : allAssignments) {
            if (selectedYearId != null && ta.getAcademicYearId() == selectedYearId) {
                map.put(ta.getSemesterId(), ta.getSemesterName());
            }
        }
        semesterOptions.clear();
        List<String> labels = new ArrayList<>();
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            semesterOptions.add(new IdLabel(e.getKey(), e.getValue()));
            labels.add(e.getValue());
        }

        actvSemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (semesterOptions.size() == 1 || (selectedSemesterId == null && !semesterOptions.isEmpty())) {
            IdLabel first = semesterOptions.get(0);
            selectedSemesterId = first.id;
            actvSemester.setText(first.label, false);
        } else if (semesterOptions.isEmpty()) {
            selectedSemesterId = null;
            actvSemester.setText("", false);
        }
        populateClasses();
    }

    private void populateClasses() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (TeachingAssignment ta : allAssignments) {
            if (selectedYearId != null && ta.getAcademicYearId() == selectedYearId
                    && selectedSemesterId != null && ta.getSemesterId() == selectedSemesterId) {
                map.put(ta.getClassId(), ta.getClassName());
            }
        }
        classOptions.clear();
        List<String> labels = new ArrayList<>();
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            classOptions.add(new IdLabel(e.getKey(), e.getValue()));
            labels.add(e.getValue());
        }

        actvClass.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (!classOptions.isEmpty()) {
            IdLabel first = classOptions.get(0);
            selectedClassId = first.id;
            actvClass.setText(first.label, false);
        } else {
            selectedClassId = null;
            actvClass.setText("", false);
        }
        populateCourses();
    }

    private void populateCourses() {
        courseOptions.clear();
        List<String> labels = new ArrayList<>();
        for (TeachingAssignment ta : allAssignments) {
            if (selectedYearId != null && ta.getAcademicYearId() == selectedYearId
                    && selectedSemesterId != null && ta.getSemesterId() == selectedSemesterId
                    && selectedClassId != null && ta.getClassId() == selectedClassId) {
                courseOptions.add(ta);
                labels.add(ta.getCourseDisplayLabel());
            }
        }

        actvCourse.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (!courseOptions.isEmpty()) {
            selectedAssignment = courseOptions.get(0);
            actvCourse.setText(selectedAssignment.getCourseDisplayLabel(), false);
        } else {
            selectedAssignment = null;
            actvCourse.setText("", false);
        }
        updateSelectedSummary();
    }

    private void updateSelectedSummary() {
        if (selectedAssignment == null) {
            tvSelectedOfferingSummary.setText("No course available for the selected academic context.");
            setSubmitButtonsEnabled(false);
        } else {
            tvSelectedOfferingSummary.setText(String.format(Locale.US,
                    "%s • %s (%s) • %d enrolled students",
                    selectedAssignment.getClassName(),
                    selectedAssignment.getSemesterName(),
                    selectedAssignment.getAcademicYearName(),
                    selectedAssignment.getStudentCount()));
            setSubmitButtonsEnabled(true);
        }
    }

    private void setSubmitButtonsEnabled(boolean enabled) {
        boolean ready = enabled && !isSaving;
        if (btnSaveDraft != null) btnSaveDraft.setEnabled(ready);
        if (btnSubmit != null) btnSubmit.setEnabled(ready);
    }

    private void setupTypeSelector() {
        btnTypeAssignment.setOnClickListener(v -> selectType("Assignment"));
        btnTypeHomework.setOnClickListener(v -> selectType("Homework"));
        btnTypeQuiz.setOnClickListener(v -> selectType("Quiz"));
        btnTypeExam.setOnClickListener(v -> selectType("Exam"));

        btnExamMidterm.setOnClickListener(v -> selectExamSubtype("midterm"));
        btnExamFinal.setOnClickListener(v -> selectExamSubtype("final"));
    }

    private void selectType(String type) {
        this.selectedType = type;
        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveColor = getResources().getColor(R.color.admin_text_secondary, null);

        btnTypeAssignment.setTextColor("Assignment".equalsIgnoreCase(type) ? activeColor : inactiveColor);
        btnTypeHomework.setTextColor("Homework".equalsIgnoreCase(type) ? activeColor : inactiveColor);
        btnTypeQuiz.setTextColor("Quiz".equalsIgnoreCase(type) ? activeColor : inactiveColor);
        btnTypeExam.setTextColor("Exam".equalsIgnoreCase(type) ? activeColor : inactiveColor);

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(tvHeaderTitle.getContext().getString(R.string.create_new) + type);
        }
        if (tvFormSectionHeader != null) {
            tvFormSectionHeader.setText(type + " Configuration & Requirements");
        }

        boolean isExam = "Exam".equalsIgnoreCase(type);
        boolean isQuiz = "Quiz".equalsIgnoreCase(type);

        layoutExamSubtype.setVisibility(isExam ? View.VISIBLE : View.GONE);
        tilVenue.setVisibility(isExam ? View.VISIBLE : View.GONE);
        layoutQuizBuilderSection.setVisibility(isQuiz ? View.VISIBLE : View.GONE);
        tilDuration.setVisibility((isQuiz || isExam) ? View.VISIBLE : View.GONE);
        tilTotalScore.setVisibility(isQuiz ? View.GONE : View.VISIBLE);

        if (tilDueDate != null) {
            tilDueDate.setHint(isExam ? "Exam Date & Start Time (YYYY-MM-DD HH:MM)" : (isQuiz ? "Close Date (YYYY-MM-DD HH:MM:SS)" : "Due Date (YYYY-MM-DD HH:MM:SS)"));
        }

        if (isExam && etDuration != null && "30".equals(etDuration.getText() != null ? etDuration.getText().toString().trim() : "")) {
            etDuration.setText("90");
        }

        if (isQuiz) {
            btnSaveDraft.setText("Save Draft");
            btnSubmit.setText("Save & Build Questions");
        } else if (isExam) {
            btnSaveDraft.setText("Save Draft");
            btnSubmit.setText("Schedule Exam");
        } else {
            btnSaveDraft.setText(R.string.save_draft);
            btnSubmit.setText(R.string.publish_assessment);
        }
    }

    private void selectExamSubtype(String subtype) {
        this.examSubtype = subtype;
        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveColor = getResources().getColor(R.color.admin_text_secondary, null);

        btnExamMidterm.setTextColor("midterm".equalsIgnoreCase(subtype) ? activeColor : inactiveColor);
        btnExamFinal.setTextColor("final".equalsIgnoreCase(subtype) ? activeColor : inactiveColor);
    }

    private void showDatePicker() {
        DatePickerHelper.showDateTimePicker(this, tilDueDate, etDueDate, false, true);
    }

    private void submitAssessment(boolean asDraft) {
        if (selectedAssignment == null) {
            Toast.makeText(this, "Please select an assigned class and course first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String totalScoreStr = etTotalScore.getText() != null ? etTotalScore.getText().toString().trim() : "100";
        String dueDate = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etTitle.setError(selectedType + " title is required");
            etTitle.requestFocus();
            return;
        }

        int offeringId = selectedAssignment.getCourseOfferingId();
        JsonObject body = new JsonObject();
        body.addProperty("title", title);
        if (!desc.isEmpty()) body.addProperty("description", desc);

        Call<JsonObject> call;
        boolean openBuilderAfter = false;

        if ("Quiz".equalsIgnoreCase(selectedType)) {
            String durStr = etDuration.getText() != null ? etDuration.getText().toString().trim() : "30";
            int dur = 30;
            try {
                dur = Integer.parseInt(durStr);
            } catch (NumberFormatException ignored) {}
            if (dur < 1 || dur > 300) {
                etDuration.setError("Duration must be between 1 and 300 minutes");
                etDuration.requestFocus();
                return;
            }
            body.addProperty("duration_minutes", dur);
            body.addProperty("status", "draft"); // Quizzes must be drafted first so questions & auto-marks are attached before publishing
            String startAt = etQuizStartDate != null && etQuizStartDate.getText() != null ? etQuizStartDate.getText().toString().trim() : "";
            if (!startAt.isEmpty()) body.addProperty("start_at", startAt);
            if (!dueDate.isEmpty()) body.addProperty("end_at", dueDate);

            String maxSel = actvQuizMaxAttempts != null ? actvQuizMaxAttempts.getText().toString().trim() : "1 Attempt";
            if ("Unlimited".equalsIgnoreCase(maxSel)) {
                body.addProperty("max_attempts", 0);
            } else if (maxSel.startsWith("2")) {
                body.addProperty("max_attempts", 2);
            } else if (maxSel.startsWith("3")) {
                body.addProperty("max_attempts", 3);
            } else if (maxSel.startsWith("5")) {
                body.addProperty("max_attempts", 5);
            } else {
                body.addProperty("max_attempts", 1);
            }

            String scoreSel = actvQuizScoringMethod != null ? actvQuizScoringMethod.getText().toString().trim() : "Highest Score";
            if (scoreSel.toLowerCase(Locale.US).contains("latest")) {
                body.addProperty("scoring_method", "latest");
            } else if (scoreSel.toLowerCase(Locale.US).contains("average")) {
                body.addProperty("scoring_method", "average");
            } else {
                body.addProperty("scoring_method", "highest");
            }

            body.addProperty("allow_late_submission", cbQuizAllowLate != null && cbQuizAllowLate.isChecked());
            openBuilderAfter = !asDraft;
            call = ApiClient.getApiService(this).createTeacherQuiz(offeringId, body);

        } else if ("Exam".equalsIgnoreCase(selectedType)) {
            double totalScore;
            try {
                totalScore = Double.parseDouble(totalScoreStr);
            } catch (NumberFormatException e) {
                etTotalScore.setError("Enter valid total marks");
                return;
            }
            if (totalScore < 1 || totalScore > 1000) {
                etTotalScore.setError("Marks must be between 1 and 1000");
                return;
            }
            if (dueDate.isEmpty()) {
                Toast.makeText(this, "Exam date is required", Toast.LENGTH_SHORT).show();
                return;
            }
            String examDateOnly = dueDate.length() >= 10 ? dueDate.substring(0, 10) : dueDate;
            String startTime = dueDate.length() >= 16 ? dueDate.substring(11, 16) : "";
            String durStr = etDuration.getText() != null ? etDuration.getText().toString().trim() : "";
            String venue = etVenue.getText() != null ? etVenue.getText().toString().trim() : "";

            body.addProperty("exam_type", examSubtype);
            body.addProperty("exam_date", examDateOnly);
            if (!startTime.isEmpty() && startTime.matches("^\\d{2}:\\d{2}$")) {
                body.addProperty("start_time", startTime);
            }
            body.addProperty("total_score", totalScore);
            if (!durStr.isEmpty()) {
                try {
                    int dur = Integer.parseInt(durStr);
                    if (dur < 1 || dur > 600) {
                        etDuration.setError("Duration must be between 1 and 600 minutes");
                        etDuration.requestFocus();
                        return;
                    }
                    body.addProperty("duration_minutes", dur);
                } catch (NumberFormatException e) {
                    etDuration.setError("Enter valid duration");
                    etDuration.requestFocus();
                    return;
                }
            }
            if (!venue.isEmpty()) body.addProperty("venue", venue);
            body.addProperty("status", asDraft ? "draft" : "scheduled");
            call = ApiClient.getApiService(this).createTeacherExam(offeringId, body);

        } else {
            double totalScore = 100.0;
            try {
                if (!totalScoreStr.isEmpty()) totalScore = Double.parseDouble(totalScoreStr);
            } catch (NumberFormatException e) {
                etTotalScore.setError("Enter valid total marks");
                return;
            }
            if (totalScore < 1) {
                etTotalScore.setError("Marks must be at least 1");
                return;
            }
            body.addProperty("total_score", totalScore);
            body.addProperty("assignment_type", "Homework".equalsIgnoreCase(selectedType) ? "homework" : "assignment");
            body.addProperty("due_date", dueDate);
            body.addProperty("status", asDraft ? "draft" : "published");
            call = ApiClient.getApiService(this).createTeacherAssignment(offeringId, body);
        }

        isSaving = true;
        setSubmitButtonsEnabled(true);
        final boolean launchBuilder = openBuilderAfter;

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                isSaving = false;
                setSubmitButtonsEnabled(selectedAssignment != null);

                if (response.isSuccessful() && response.body() != null) {
                    if ("Quiz".equalsIgnoreCase(selectedType) && launchBuilder) {
                        JsonObject data = response.body().has("data") && response.body().get("data").isJsonObject()
                                ? response.body().getAsJsonObject("data") : response.body();
                        int quizId = AssessmentUiHelper.optInt(data, "id", 0);
                        if (quizId > 0) {
                            Toast.makeText(AddAssessmentActivity.this, "Quiz draft created! Add your questions below.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddAssessmentActivity.this, QuizBuilderActivity.class);
                            intent.putExtra("quiz_id", quizId);
                            startActivity(intent);
                            setResult(RESULT_OK);
                            finish();
                            return;
                        }
                    }

                    String actionWord = asDraft ? "saved as draft!" : ("Exam".equalsIgnoreCase(selectedType) ? "scheduled!" : "published!");
                    Toast.makeText(AddAssessmentActivity.this, selectedType + " " + actionWord, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = AssessmentUiHelper.extractErrorMessage(response, "Failed to save " + selectedType);
                    Toast.makeText(AddAssessmentActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                isSaving = false;
                setSubmitButtonsEnabled(selectedAssignment != null);
                Toast.makeText(AddAssessmentActivity.this, "Network error saving " + selectedType, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
