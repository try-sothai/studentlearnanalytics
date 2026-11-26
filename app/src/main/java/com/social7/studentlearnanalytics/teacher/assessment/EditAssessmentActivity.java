package com.social7.studentlearnanalytics.teacher.assessment;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.social7.studentlearnanalytics.model.Exam;
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

public class EditAssessmentActivity extends AppCompatActivity {

    private int examId = 0;
    private String examSubtype = "midterm";
    private String currentRawStatus = "scheduled";
    private int originalOfferingId = 0;
    private int gradedStudentsCount = 0;

    private ProgressBar pbEditExamLoading;
    private TextInputLayout tilEditAcademicYear, tilEditSemester, tilEditClass, tilEditCourse;
    private AutoCompleteTextView actvEditAcademicYear, actvEditSemester, actvEditClass, actvEditCourse, actvEditStatus;
    private TextView tvEditOfferingSummary, tvEditOfferingLockedNote, tvEditCalculatedEndTime;
    private MaterialButton btnEditExamMidterm, btnEditExamFinal, btnCancelEditExam, btnSaveEditExam;
    private TextInputLayout tilEditExamTitle, tilEditExamDate, tilEditStartTime, tilEditDuration, tilEditTotalScore;
    private TextInputEditText etEditExamTitle, etEditExamDate, etEditStartTime, etEditDuration, etEditTotalScore, etEditVenue, etEditDescription;

    private final List<TeachingAssignment> allAssignments = new ArrayList<>();
    private final List<IdLabel> yearOptions = new ArrayList<>();
    private final List<IdLabel> semesterOptions = new ArrayList<>();
    private final List<IdLabel> classOptions = new ArrayList<>();
    private final List<TeachingAssignment> courseOptions = new ArrayList<>();

    private Integer selectedYearId = null;
    private Integer selectedSemesterId = null;
    private Integer selectedClassId = null;
    private TeachingAssignment selectedAssignment = null;

    private static final String[] STATUS_LABELS = new String[]{"Scheduled", "Results Pending", "Completed", "Draft", "Cancelled"};
    private static final String[] STATUS_VALUES = new String[]{"scheduled", "results_pending", "completed", "draft", "cancelled"};

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
        setContentView(R.layout.activity_edit_exam);

        examId = getIntent().getIntExtra("exam_id", 0);
        if (examId <= 0) {
            String strId = getIntent().getStringExtra("exam_id");
            if (strId != null) {
                try {
                    examId = Integer.parseInt(strId.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            }
        }

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Edit & Schedule Exam", "", true);

        bindViews();
        setupListeners();
        setupStatusDropdown("scheduled");

        if (examId > 0) {
            loadExamDetails();
            loadTeachingAssignments();
        } else {
            Toast.makeText(this, "No exam specified for editing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        pbEditExamLoading = findViewById(R.id.pbEditExamLoading);
        tilEditAcademicYear = findViewById(R.id.tilEditAcademicYear);
        tilEditSemester = findViewById(R.id.tilEditSemester);
        tilEditClass = findViewById(R.id.tilEditClass);
        tilEditCourse = findViewById(R.id.tilEditCourse);
        actvEditAcademicYear = findViewById(R.id.actvEditAcademicYear);
        actvEditSemester = findViewById(R.id.actvEditSemester);
        actvEditClass = findViewById(R.id.actvEditClass);
        actvEditCourse = findViewById(R.id.actvEditCourse);
        tvEditOfferingSummary = findViewById(R.id.tvEditOfferingSummary);
        tvEditOfferingLockedNote = findViewById(R.id.tvEditOfferingLockedNote);

        btnEditExamMidterm = findViewById(R.id.btnEditExamMidterm);
        btnEditExamFinal = findViewById(R.id.btnEditExamFinal);

        tilEditExamTitle = findViewById(R.id.tilEditExamTitle);
        etEditExamTitle = findViewById(R.id.etEditExamTitle);
        tilEditExamDate = findViewById(R.id.tilEditExamDate);
        etEditExamDate = findViewById(R.id.etEditExamDate);
        tilEditStartTime = findViewById(R.id.tilEditStartTime);
        etEditStartTime = findViewById(R.id.etEditStartTime);
        tilEditDuration = findViewById(R.id.tilEditDuration);
        etEditDuration = findViewById(R.id.etEditDuration);
        tvEditCalculatedEndTime = findViewById(R.id.tvEditCalculatedEndTime);
        tilEditTotalScore = findViewById(R.id.tilEditTotalScore);
        etEditTotalScore = findViewById(R.id.etEditTotalScore);
        etEditVenue = findViewById(R.id.etEditVenue);
        actvEditStatus = findViewById(R.id.actvEditStatus);
        etEditDescription = findViewById(R.id.etEditDescription);

        btnCancelEditExam = findViewById(R.id.btnCancelEditExam);
        btnSaveEditExam = findViewById(R.id.btnSaveEditExam);
    }

    private void setupStatusDropdown(String rawStatus) {
        this.currentRawStatus = rawStatus != null ? rawStatus : "scheduled";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, STATUS_LABELS);
        actvEditStatus.setAdapter(adapter);
        String selectedLabel = STATUS_LABELS[0];
        for (int i = 0; i < STATUS_VALUES.length; i++) {
            if (STATUS_VALUES[i].equalsIgnoreCase(this.currentRawStatus)) {
                selectedLabel = STATUS_LABELS[i];
                break;
            }
        }
        if ("published".equalsIgnoreCase(this.currentRawStatus)) {
            selectedLabel = "Published";
        }
        actvEditStatus.setText(selectedLabel, false);
        actvEditStatus.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < STATUS_VALUES.length) {
                currentRawStatus = STATUS_VALUES[position];
            }
        });
    }

    private void setupListeners() {
        btnEditExamMidterm.setOnClickListener(v -> selectExamSubtype("midterm"));
        btnEditExamFinal.setOnClickListener(v -> selectExamSubtype("final"));

        DatePickerHelper.attachDatePicker(tilEditExamDate, etEditExamDate, false);

        etEditStartTime.setOnClickListener(v -> showStartTimePicker());
        tilEditStartTime.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        tilEditStartTime.setEndIconDrawable(R.drawable.ic_calendar);
        tilEditStartTime.setEndIconOnClickListener(v -> showStartTimePicker());

        etEditDuration.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCalculatedEndTimeDisplay();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCancelEditExam.setOnClickListener(v -> finish());
        btnSaveEditExam.setOnClickListener(v -> saveExamChanges());

        actvEditAcademicYear.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < yearOptions.size()) {
                selectedYearId = yearOptions.get(position).id;
                selectedSemesterId = null;
                selectedClassId = null;
                selectedAssignment = null;
                populateSemesters();
            }
        });
        actvEditSemester.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < semesterOptions.size()) {
                selectedSemesterId = semesterOptions.get(position).id;
                selectedClassId = null;
                selectedAssignment = null;
                populateClasses();
            }
        });
        actvEditClass.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < classOptions.size()) {
                selectedClassId = classOptions.get(position).id;
                selectedAssignment = null;
                populateCourses();
            }
        });
        actvEditCourse.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < courseOptions.size()) {
                selectedAssignment = courseOptions.get(position);
                updateSelectedOfferingSummary();
            }
        });
    }

    private void showStartTimePicker() {
        int hour = 9;
        int min = 0;
        String current = etEditStartTime.getText() != null ? etEditStartTime.getText().toString().trim() : "";
        if (current.length() >= 5 && current.charAt(2) == ':') {
            try {
                hour = Integer.parseInt(current.substring(0, 2));
                min = Integer.parseInt(current.substring(3, 5));
            } catch (Exception ignored) {
                Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                min = 0;
            }
        }
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            etEditStartTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            tilEditStartTime.setError(null);
            updateCalculatedEndTimeDisplay();
        }, hour, min, true).show();
    }

    private void updateCalculatedEndTimeDisplay() {
        String start = etEditStartTime.getText() != null ? etEditStartTime.getText().toString().trim() : "";
        String durStr = etEditDuration.getText() != null ? etEditDuration.getText().toString().trim() : "";
        int dur = 0;
        try {
            if (!durStr.isEmpty()) dur = Integer.parseInt(durStr);
        } catch (NumberFormatException ignored) {}

        if (start.isEmpty()) {
            tvEditCalculatedEndTime.setText("End Time: Optional (set Start Time & Duration)");
            return;
        }
        String end = Exam.addMinutesToTime(start, dur);
        if (!end.isEmpty()) {
            tvEditCalculatedEndTime.setText("Scheduled Window: " + start + " – " + end + " (" + dur + " mins)");
        } else {
            tvEditCalculatedEndTime.setText("Start Time: " + start + " (enter Duration to compute End Time)");
        }
    }

    private void selectExamSubtype(String subtype) {
        this.examSubtype = subtype;
        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveColor = getResources().getColor(R.color.admin_text_secondary, null);

        btnEditExamMidterm.setTextColor("midterm".equalsIgnoreCase(subtype) ? activeColor : inactiveColor);
        btnEditExamFinal.setTextColor("final".equalsIgnoreCase(subtype) ? activeColor : inactiveColor);
    }

    private void loadExamDetails() {
        pbEditExamLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).getTeacherExamDetail(examId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbEditExamLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body());
                } else {
                    Toast.makeText(EditAssessmentActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to load exam details"),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbEditExamLoading.setVisibility(View.GONE);
                Toast.makeText(EditAssessmentActivity.this, "Network error loading exam details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadTeachingAssignments() {
        ApiClient.getApiService(this).getTeacherTeachingAssignments().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) return;

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
                populateAcademicYears();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void populateFields(JsonObject body) {
        if (!body.has("data") || !body.get("data").isJsonObject()) return;
        JsonObject data = body.getAsJsonObject("data");
        if (data.has("statistics") && data.get("statistics").isJsonObject()) {
            gradedStudentsCount = AssessmentUiHelper.optInt(data.getAsJsonObject("statistics"), "graded_students", 0);
        }

        if (!data.has("exam") || !data.get("exam").isJsonObject()) return;
        JsonObject exam = data.getAsJsonObject("exam");

        originalOfferingId = AssessmentUiHelper.optInt(exam, "course_offering_id", 0);
        String title = AssessmentUiHelper.optString(exam, "title", "");
        String type = AssessmentUiHelper.optString(exam, "exam_type", "midterm");
        String date = AssessmentUiHelper.optString(exam, "exam_date", "");
        String startTime = AssessmentUiHelper.optString(exam, "start_time", "");
        double marks = AssessmentUiHelper.optDouble(exam, "total_score", 100.0);
        int dur = AssessmentUiHelper.optInt(exam, "duration_minutes", 90);
        String venue = AssessmentUiHelper.optString(exam, "venue", "");
        String desc = AssessmentUiHelper.optString(exam, "description", "");
        String status = AssessmentUiHelper.optString(exam, "status", "scheduled");

        String courseCode = AssessmentUiHelper.optString(exam, "course_code", "");
        String courseName = AssessmentUiHelper.optString(exam, "course_name", "");
        String className = AssessmentUiHelper.optString(exam, "class_name", "");
        String semName = AssessmentUiHelper.optString(exam, "semester_name", "");

        etEditExamTitle.setText(title);
        selectExamSubtype(type);
        etEditExamDate.setText(date.length() > 10 ? date.substring(0, 10) : date);
        etEditStartTime.setText(startTime);
        etEditTotalScore.setText((marks == (long) marks) ? String.format(Locale.US, "%d", (long) marks) : String.format(Locale.US, "%.1f", marks));
        if (dur > 0) etEditDuration.setText(String.valueOf(dur));
        etEditVenue.setText(venue);
        etEditDescription.setText(desc);
        setupStatusDropdown(status);
        updateCalculatedEndTimeDisplay();

        tvEditOfferingSummary.setText(String.format(Locale.US, "%s • %s • %s • %s", courseCode, courseName, className, semName));

        if (gradedStudentsCount > 0) {
            lockOfferingSelectors();
        }
        applyOriginalOfferingToDropdowns();
    }

    private void lockOfferingSelectors() {
        tilEditAcademicYear.setEnabled(false);
        tilEditSemester.setEnabled(false);
        tilEditClass.setEnabled(false);
        tilEditCourse.setEnabled(false);
        tvEditOfferingLockedNote.setVisibility(View.VISIBLE);
    }

    private boolean applyOriginalOfferingToDropdowns() {
        if (allAssignments.isEmpty() || originalOfferingId <= 0) return false;
        for (TeachingAssignment ta : allAssignments) {
            if (ta.getCourseOfferingId() == originalOfferingId) {
                selectedAssignment = ta;
                selectedYearId = ta.getAcademicYearId();
                selectedSemesterId = ta.getSemesterId();
                selectedClassId = ta.getClassId();
                actvEditAcademicYear.setText(ta.getAcademicYearName(), false);
                actvEditSemester.setText(ta.getSemesterName(), false);
                actvEditClass.setText(ta.getClassName(), false);
                actvEditCourse.setText(ta.getCourseDisplayLabel(), false);
                updateSelectedOfferingSummary();
                populateSemesters();
                return true;
            }
        }
        return false;
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
        actvEditAcademicYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (!applyOriginalOfferingToDropdowns() && !yearOptions.isEmpty()) {
            selectedYearId = yearOptions.get(0).id;
            actvEditAcademicYear.setText(yearOptions.get(0).label, false);
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
        actvEditSemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (selectedSemesterId == null && !semesterOptions.isEmpty()) {
            selectedSemesterId = semesterOptions.get(0).id;
            actvEditSemester.setText(semesterOptions.get(0).label, false);
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
        actvEditClass.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (selectedClassId == null && !classOptions.isEmpty()) {
            selectedClassId = classOptions.get(0).id;
            actvEditClass.setText(classOptions.get(0).label, false);
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
        actvEditCourse.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        if (selectedAssignment == null && !courseOptions.isEmpty()) {
            selectedAssignment = courseOptions.get(0);
            actvEditCourse.setText(selectedAssignment.getCourseDisplayLabel(), false);
        }
        updateSelectedOfferingSummary();
    }

    private void updateSelectedOfferingSummary() {
        if (selectedAssignment != null) {
            tvEditOfferingSummary.setText(String.format(Locale.US,
                    "%s • %s • %s (%s) • %d enrolled students",
                    selectedAssignment.getCourseCode(),
                    selectedAssignment.getClassName(),
                    selectedAssignment.getSemesterName(),
                    selectedAssignment.getAcademicYearName(),
                    selectedAssignment.getStudentCount()));
        }
    }

    private void saveExamChanges() {
        String title = etEditExamTitle.getText() != null ? etEditExamTitle.getText().toString().trim() : "";
        String desc = etEditDescription.getText() != null ? etEditDescription.getText().toString().trim() : "";
        String totalScoreStr = etEditTotalScore.getText() != null ? etEditTotalScore.getText().toString().trim() : "";
        String examDate = etEditExamDate.getText() != null ? etEditExamDate.getText().toString().trim() : "";
        String startTime = etEditStartTime.getText() != null ? etEditStartTime.getText().toString().trim() : "";
        String venue = etEditVenue.getText() != null ? etEditVenue.getText().toString().trim() : "";
        String durStr = etEditDuration.getText() != null ? etEditDuration.getText().toString().trim() : "";

        tilEditExamTitle.setError(null);
        tilEditExamDate.setError(null);
        tilEditTotalScore.setError(null);
        tilEditDuration.setError(null);
        tilEditStartTime.setError(null);

        if (title.isEmpty()) {
            tilEditExamTitle.setError("Exam title is required");
            etEditExamTitle.requestFocus();
            return;
        }

        double totalScore;
        try {
            totalScore = Double.parseDouble(totalScoreStr);
        } catch (NumberFormatException e) {
            tilEditTotalScore.setError("Enter valid total marks");
            return;
        }
        if (totalScore < 1 || totalScore > 1000) {
            tilEditTotalScore.setError("Total marks must be between 1 and 1000");
            return;
        }

        if (!DatePickerHelper.isValidIsoDate(examDate)) {
            tilEditExamDate.setError("Valid date (YYYY-MM-DD) is required");
            return;
        }

        Integer durationMins = null;
        if (!durStr.isEmpty()) {
            try {
                int parsedDur = Integer.parseInt(durStr);
                if (parsedDur < 1 || parsedDur > 600) {
                    tilEditDuration.setError("Duration must be between 1 and 600 minutes");
                    return;
                }
                durationMins = parsedDur;
            } catch (NumberFormatException e) {
                tilEditDuration.setError("Enter valid duration in minutes");
                return;
            }
        }

        if (!startTime.isEmpty() && !startTime.matches("^\\d{2}:\\d{2}$")) {
            tilEditStartTime.setError("Start time must be HH:MM");
            return;
        }

        JsonObject body = new JsonObject();
        if (gradedStudentsCount == 0 && selectedAssignment != null
                && selectedAssignment.getCourseOfferingId() > 0
                && selectedAssignment.getCourseOfferingId() != originalOfferingId) {
            body.addProperty("course_offering_id", selectedAssignment.getCourseOfferingId());
        }
        body.addProperty("title", title);
        body.addProperty("exam_type", examSubtype);
        body.addProperty("exam_date", examDate);
        if (!startTime.isEmpty()) {
            body.addProperty("start_time", startTime);
            if (durationMins != null) {
                String computedEnd = Exam.addMinutesToTime(startTime, durationMins);
                if (!computedEnd.isEmpty() && computedEnd.compareTo(startTime) > 0) {
                    body.addProperty("end_time", computedEnd);
                }
            }
        } else {
            body.add("start_time", com.google.gson.JsonNull.INSTANCE);
        }
        body.addProperty("total_score", totalScore);
        if (durationMins != null) {
            body.addProperty("duration_minutes", durationMins);
        }
        body.addProperty("venue", venue);
        body.addProperty("description", desc);
        body.addProperty("status", currentRawStatus);

        btnSaveEditExam.setEnabled(false);
        btnSaveEditExam.setText("Saving...");

        ApiClient.getApiService(this).updateTeacherExam(examId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                btnSaveEditExam.setEnabled(true);
                btnSaveEditExam.setText("Save Changes");
                if (response.isSuccessful()) {
                    Toast.makeText(EditAssessmentActivity.this, "Exam updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditAssessmentActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to update exam"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                btnSaveEditExam.setEnabled(true);
                btnSaveEditExam.setText("Save Changes");
                Toast.makeText(EditAssessmentActivity.this, "Network error updating exam", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
