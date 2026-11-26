package com.social7.studentlearnanalytics.teacher.assessment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherExamDetailActivity extends AppCompatActivity {

    private int examId = 0;
    private double totalMarks = 100.0;
    private boolean isPublished = false;
    private String examTitle = "";
    private String examStatus = "scheduled";
    private int totalStudentsCount = 0;
    private int gradedStudentsCount = 0;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar pbLoading;
    private TextView tvExamTypeBadge, tvExamStatusBadge, tvExamTitle, tvCourseClassInfo;
    private TextView tvExamDateDuration, tvExamVenueMarks, tvExamDescription;
    private MaterialButton btnDeleteExam, btnEditExam, btnPublishResults;

    private TextView tvStatTotalStudents, tvStatGraded, tvStatPending, tvStatAverage, tvStatHighest, tvStatLowest;
    private TextView tabModeScoreEntry, tabModeViewResults;
    private View layoutScoreEntrySection, layoutResultsTableSection;
    private TextView tvRosterCount, tvEmptyRoster, tvUnsavedChangesCount, tvEmptyResultsTable;
    private MaterialButton btnSaveAllScores;
    private EditText edtSearchStudents;
    private RecyclerView rvStudentScores;
    private TableLayout tblExamResults;

    private ExamStudentScoreAdapter scoreAdapter;
    private final List<ExamStudentScoreAdapter.StudentScoreItem> studentList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> editExamLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    fetchExamDetails();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_exam_detail);

        examId = getIntent().getIntExtra("exam_id", 0);
        if (examId <= 0) {
            String strId = getIntent().getStringExtra("exam_id");
            if (strId != null) {
                try { examId = Integer.parseInt(strId.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            }
        }

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherExamDetail), "Exam & Paper Grading", "", true);

        bindViews();
        setupListeners();

        if (examId > 0) {
            fetchExamDetails();
        } else {
            Toast.makeText(this, "Invalid Exam ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefreshExams);
        pbLoading = findViewById(R.id.pbLoading);
        tvExamTypeBadge = findViewById(R.id.tvExamTypeBadge);
        tvExamStatusBadge = findViewById(R.id.tvExamStatusBadge);
        tvExamTitle = findViewById(R.id.tvExamTitle);
        tvCourseClassInfo = findViewById(R.id.tvCourseClassInfo);
        tvExamDateDuration = findViewById(R.id.tvExamDateDuration);
        tvExamVenueMarks = findViewById(R.id.tvExamVenueMarks);
        tvExamDescription = findViewById(R.id.tvExamDescription);
        btnDeleteExam = findViewById(R.id.btnDeleteExam);
        btnEditExam = findViewById(R.id.btnEditExam);
        btnPublishResults = findViewById(R.id.btnPublishResults);

        tvStatTotalStudents = findViewById(R.id.tvStatTotalStudents);
        tvStatGraded = findViewById(R.id.tvStatGraded);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatAverage = findViewById(R.id.tvStatAverage);
        tvStatHighest = findViewById(R.id.tvStatHighest);
        tvStatLowest = findViewById(R.id.tvStatLowest);

        tabModeScoreEntry = findViewById(R.id.tabModeScoreEntry);
        tabModeViewResults = findViewById(R.id.tabModeViewResults);
        layoutScoreEntrySection = findViewById(R.id.layoutScoreEntrySection);
        layoutResultsTableSection = findViewById(R.id.layoutResultsTableSection);

        tvRosterCount = findViewById(R.id.tvRosterCount);
        tvEmptyRoster = findViewById(R.id.tvEmptyRoster);
        tvUnsavedChangesCount = findViewById(R.id.tvUnsavedChangesCount);
        btnSaveAllScores = findViewById(R.id.btnSaveAllScores);
        edtSearchStudents = findViewById(R.id.edtSearchStudents);
        rvStudentScores = findViewById(R.id.rvStudentScores);

        tblExamResults = findViewById(R.id.tblExamResults);
        tvEmptyResultsTable = findViewById(R.id.tvEmptyResultsTable);

        rvStudentScores.setLayoutManager(new LinearLayoutManager(this));
        initScoreAdapter();
    }

    private void initScoreAdapter() {
        scoreAdapter = new ExamStudentScoreAdapter(this, studentList, totalMarks, new ExamStudentScoreAdapter.OnScoreActionListener() {
            @Override
            public void onGradeStudent(ExamStudentScoreAdapter.StudentScoreItem item) {
                showScoreEntryDialog(item);
            }

            @Override
            public void onDraftScoresChanged(int modifiedCount, boolean hasInvalid) {
                updateBatchSaveBar(modifiedCount, hasInvalid);
            }
        });
        rvStudentScores.setAdapter(scoreAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::fetchExamDetails);

        btnEditExam.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditAssessmentActivity.class);
            intent.putExtra("assessment_type", "Exam");
            intent.putExtra("exam_id", examId);
            editExamLauncher.launch(intent);
        });

        btnPublishResults.setOnClickListener(v -> {
            if (isPublished) {
                confirmUnpublishResults();
            } else {
                confirmPublishResults();
            }
        });

        btnDeleteExam.setOnClickListener(v -> confirmDeleteExam());

        tabModeScoreEntry.setOnClickListener(v -> switchMode(true));
        tabModeViewResults.setOnClickListener(v -> switchMode(false));

        btnSaveAllScores.setOnClickListener(v -> confirmSaveBatchScores());

        edtSearchStudents.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (scoreAdapter != null) scoreAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void switchMode(boolean scoreEntryMode) {
        layoutScoreEntrySection.setVisibility(scoreEntryMode ? View.VISIBLE : View.GONE);
        layoutResultsTableSection.setVisibility(scoreEntryMode ? View.GONE : View.VISIBLE);

        if (scoreEntryMode) {
            tabModeScoreEntry.setBackgroundResource(R.drawable.bg_notif_tab_active);
            tabModeScoreEntry.setTextColor(Color.WHITE);
            tabModeViewResults.setBackground(null);
            tabModeViewResults.setTextColor(getResources().getColor(R.color.text_secondary, null));
        } else {
            tabModeViewResults.setBackgroundResource(R.drawable.bg_notif_tab_active);
            tabModeViewResults.setTextColor(Color.WHITE);
            tabModeScoreEntry.setBackground(null);
            tabModeScoreEntry.setTextColor(getResources().getColor(R.color.text_secondary, null));
            renderResultsTable();
        }
    }

    private void updateBatchSaveBar(int modifiedCount, boolean hasInvalid) {
        if (hasInvalid) {
            btnSaveAllScores.setEnabled(false);
            btnSaveAllScores.setText("Save Scores");
            tvUnsavedChangesCount.setText("Fix highlighted invalid score(s) before saving");
            tvUnsavedChangesCount.setTextColor(Color.parseColor("#EF4444"));
        } else if (modifiedCount > 0) {
            btnSaveAllScores.setEnabled(true);
            btnSaveAllScores.setText("Save Scores (" + modifiedCount + ")");
            tvUnsavedChangesCount.setText(modifiedCount + " pending change(s) ready to save");
            tvUnsavedChangesCount.setTextColor(Color.parseColor("#2563EB"));
        } else {
            btnSaveAllScores.setEnabled(false);
            btnSaveAllScores.setText("Save Scores");
            tvUnsavedChangesCount.setText("Type paper scores below, then save");
            tvUnsavedChangesCount.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void fetchExamDetails() {
        pbLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).getTeacherExamDetail(examId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    renderExamData(response.body());
                } else {
                    Toast.makeText(TeacherExamDetailActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to load exam details"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(TeacherExamDetailActivity.this, "Network error loading exam details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderExamData(JsonObject body) {
        if (!body.has("data") || !body.get("data").isJsonObject()) return;
        JsonObject data = body.getAsJsonObject("data");

        if (data.has("exam") && data.get("exam").isJsonObject()) {
            JsonObject exam = data.getAsJsonObject("exam");
            examTitle = optString(exam, "title", "Exam");
            String examType = optString(exam, "exam_type", "Exam");
            examStatus = optString(exam, "status", "scheduled");
            String displayStatus = optString(exam, "display_status", capitalize(examStatus));
            isPublished = exam.has("is_published") && !exam.get("is_published").isJsonNull() && exam.get("is_published").getAsBoolean();
            totalMarks = optDouble(exam, "total_score", 100.0);
            int durationMins = optInt(exam, "duration_minutes", 0);
            String venue = optString(exam, "venue", "");
            String examDate = optString(exam, "exam_date", "-");
            String startTime = optString(exam, "start_time", "");
            String endTime = optString(exam, "end_time", "");
            String desc = optString(exam, "description", "");
            String courseCode = optString(exam, "course_code", "");
            String courseName = optString(exam, "course_name", "");
            String className = optString(exam, "class_name", "");
            String semName = optString(exam, "semester_name", "");

            tvExamTitle.setText(examTitle);
            tvExamTypeBadge.setText("midterm".equalsIgnoreCase(examType) ? "Midterm Examination"
                    : ("final".equalsIgnoreCase(examType) ? "Final Examination" : capitalize(examType)));

            tvExamStatusBadge.setText(displayStatus);
            String lowerStatus = displayStatus.toLowerCase(Locale.US);
            if (isPublished || lowerStatus.contains("published")) {
                tvExamStatusBadge.setTextColor(Color.parseColor("#10B981"));
                tvExamStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_green);
                btnPublishResults.setText("Unpublish");
                btnPublishResults.setBackgroundColor(Color.parseColor("#F59E0B"));
            } else if (lowerStatus.contains("pending")) {
                tvExamStatusBadge.setTextColor(Color.parseColor("#D97706"));
                tvExamStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_orange);
                btnPublishResults.setText("Publish Results");
                btnPublishResults.setBackgroundColor(Color.parseColor("#10B981"));
            } else if ("draft".equalsIgnoreCase(examStatus)) {
                tvExamStatusBadge.setTextColor(Color.parseColor("#9333EA"));
                tvExamStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_purple);
                btnPublishResults.setText("Publish Results");
                btnPublishResults.setBackgroundColor(Color.parseColor("#10B981"));
            } else {
                tvExamStatusBadge.setTextColor(Color.parseColor("#2563EB"));
                tvExamStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_blue);
                btnPublishResults.setText("Publish Results");
                btnPublishResults.setBackgroundColor(Color.parseColor("#10B981"));
            }

            StringBuilder courseClass = new StringBuilder();
            if (!courseCode.isEmpty()) courseClass.append(courseCode);
            if (!courseName.isEmpty()) {
                if (courseClass.length() > 0) courseClass.append(" • ");
                courseClass.append(courseName);
            }
            if (!className.isEmpty()) {
                if (courseClass.length() > 0) courseClass.append(" • ");
                courseClass.append(className);
            }
            if (!semName.isEmpty()) {
                if (courseClass.length() > 0) courseClass.append(" • ");
                courseClass.append(semName);
            }
            tvCourseClassInfo.setText(courseClass.toString());

            String timeRange = Exam.calculateTimeRange(startTime, endTime, durationMins > 0 ? durationMins : null);
            StringBuilder sched = new StringBuilder(examDate);
            if (!timeRange.isEmpty()) sched.append(" • ").append(timeRange);
            if (durationMins > 0) sched.append(" (").append(durationMins).append(" mins)");
            tvExamDateDuration.setText(sched.toString());

            String marksFormatted = ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(totalMarks);
            tvExamVenueMarks.setText((!venue.isEmpty() ? venue + " • " : "") + marksFormatted + " Marks");

            if (!desc.trim().isEmpty()) {
                tvExamDescription.setVisibility(View.VISIBLE);
                tvExamDescription.setText("Instructions: " + desc.trim());
            } else {
                tvExamDescription.setVisibility(View.GONE);
            }
        }

        if (data.has("statistics") && data.get("statistics").isJsonObject()) {
            JsonObject stats = data.getAsJsonObject("statistics");
            totalStudentsCount = optInt(stats, "total_students", 0);
            gradedStudentsCount = optInt(stats, "graded_students", 0);
            int pendingCount = optInt(stats, "pending_students", Math.max(0, totalStudentsCount - gradedStudentsCount));
            double avg = optDouble(stats, "average_score", 0.0);
            double high = optDouble(stats, "highest_score", 0.0);
            double low = optDouble(stats, "lowest_score", 0.0);

            tvStatTotalStudents.setText(String.valueOf(totalStudentsCount));
            tvStatGraded.setText(String.valueOf(gradedStudentsCount));
            tvStatPending.setText(String.valueOf(pendingCount));
            tvStatAverage.setText(gradedStudentsCount > 0 ? String.format(Locale.US, "%.1f", avg) : "—");
            tvStatHighest.setText(gradedStudentsCount > 0 ? ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(high) : "—");
            tvStatLowest.setText(gradedStudentsCount > 0 ? ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(low) : "—");
        }

        studentList.clear();
        if (data.has("students") && data.get("students").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("students");
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject sObj = el.getAsJsonObject();
                int sId = optInt(sObj, "student_id", 0);
                String sCode = optString(sObj, "student_code", "");
                String sName = optString(sObj, "name", "Student");
                String sEmail = optString(sObj, "email", "");
                String sClass = optString(sObj, "class_name", "");
                boolean hasScore = sObj.has("has_score") && !sObj.get("has_score").isJsonNull() && sObj.get("has_score").getAsBoolean();
                Double score = sObj.has("score") && !sObj.get("score").isJsonNull() ? sObj.get("score").getAsDouble() : null;
                String grade = optString(sObj, "grade", null);
                String remarks = optString(sObj, "remarks", null);
                String updatedAt = optString(sObj, "updated_at", null);

                ExamStudentScoreAdapter.StudentScoreItem item = new ExamStudentScoreAdapter.StudentScoreItem(
                        sId, sCode, sName, sEmail, sClass, hasScore, score, grade, remarks, updatedAt
                );
                if (sObj.has("percentage") && !sObj.get("percentage").isJsonNull()) {
                    item.percentage = sObj.get("percentage").getAsDouble();
                } else if (hasScore && score != null && totalMarks > 0) {
                    item.percentage = Math.round((score / totalMarks) * 1000.0) / 10.0;
                }
                item.resultStatus = optString(sObj, "result_status",
                        item.percentage != null ? (item.percentage >= 50.0 ? "Pass" : "Fail") : null);
                item.publicationStatus = optString(sObj, "publication_status", isPublished ? "Published" : "Unpublished");
                studentList.add(item);
            }
        }

        tvRosterCount.setText(studentList.size() + " students");
        tvEmptyRoster.setVisibility(studentList.isEmpty() ? View.VISIBLE : View.GONE);
        rvStudentScores.setVisibility(studentList.isEmpty() ? View.GONE : View.VISIBLE);

        initScoreAdapter();
        updateBatchSaveBar(0, false);
        if (edtSearchStudents != null && !edtSearchStudents.getText().toString().isEmpty()) {
            scoreAdapter.filter(edtSearchStudents.getText().toString());
        }
        renderResultsTable();
    }

    private void confirmSaveBatchScores() {
        if (scoreAdapter == null) return;
        if (scoreAdapter.hasInvalidScores()) {
            Toast.makeText(this, "Please fix invalid scores before saving.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<ExamStudentScoreAdapter.StudentScoreItem> pending = scoreAdapter.getPendingScoreSubmissions();
        if (pending.isEmpty()) {
            Toast.makeText(this, "No modified scores to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        String dialogTitle = isPublished ? "Update Published Exam Scores?" : "Save Exam Scores (Unpublished)";
        String dialogMsg = isPublished
                ? "Results for '" + examTitle + "' are already published to students. Saving changes for "
                  + pending.size() + " student(s) will immediately update their visible scores. Continue?"
                : "Save entered scores for " + pending.size() + " student(s)? Results will remain unpublished and hidden from students until you click Publish Results.";

        new MaterialAlertDialogBuilder(this)
                .setTitle(dialogTitle)
                .setMessage(dialogMsg)
                .setPositiveButton("Save Scores", (d, w) -> submitBatchScores(pending))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitBatchScores(List<ExamStudentScoreAdapter.StudentScoreItem> pending) {
        JsonArray scoresArr = new JsonArray();
        for (ExamStudentScoreAdapter.StudentScoreItem item : pending) {
            double val;
            try {
                val = Double.parseDouble(item.draftScoreInput);
            } catch (NumberFormatException e) {
                continue;
            }
            if (val < 0 || val > totalMarks) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("student_id", item.studentId);
            entry.addProperty("score", val);
            if (item.remarks != null && !item.remarks.trim().isEmpty()) {
                entry.addProperty("remarks", item.remarks.trim());
            }
            scoresArr.add(entry);
        }

        JsonObject body = new JsonObject();
        body.add("scores", scoresArr);

        pbLoading.setVisibility(View.VISIBLE);
        btnSaveAllScores.setEnabled(false);

        ApiClient.getApiService(this).storeTeacherExamScores(examId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherExamDetailActivity.this,
                            scoresArr.size() + " score(s) saved successfully!", Toast.LENGTH_LONG).show();
                    fetchExamDetails();
                } else {
                    btnSaveAllScores.setEnabled(true);
                    Toast.makeText(TeacherExamDetailActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to save scores"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                btnSaveAllScores.setEnabled(true);
                Toast.makeText(TeacherExamDetailActivity.this, "Network error saving scores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderResultsTable() {
        if (tblExamResults == null) return;
        tblExamResults.removeAllViews();

        if (studentList.isEmpty()) {
            tvEmptyResultsTable.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyResultsTable.setVisibility(View.GONE);

        // Header Row
        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#EEF2FF"));
        String[] cols = new String[]{"Student ID", "Student Name", "Score", "Total", "%", "Grade", "Result", "Status", "Publication"};
        for (String col : cols) {
            TextView tv = createTableCell(col, true, Color.parseColor("#1E1B4B"));
            header.addView(tv);
        }
        tblExamResults.addView(header);

        String maxStr = ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(totalMarks);
        for (int i = 0; i < studentList.size(); i++) {
            ExamStudentScoreAdapter.StudentScoreItem item = studentList.get(i);
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#F8FAFC"));

            String scoreStr = (item.hasScore && item.score != null)
                    ? ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(item.score) : "—";
            String pctStr = (item.hasScore && item.percentage != null)
                    ? String.format(Locale.US, "%.1f%%", item.percentage) : "—";
            String gradeStr = (item.hasScore && item.grade != null) ? item.grade : "—";
            String resStr = (item.hasScore && item.resultStatus != null) ? item.resultStatus : "—";
            String statusStr = item.hasScore ? "Graded" : "Pending";
            String pubStr = isPublished ? "Published" : "Unpublished";

            int resColor = "Fail".equalsIgnoreCase(resStr) ? Color.parseColor("#EF4444")
                    : ("Pass".equalsIgnoreCase(resStr) ? Color.parseColor("#10B981") : Color.parseColor("#64748B"));
            int stColor = item.hasScore ? Color.parseColor("#10B981") : Color.parseColor("#D97706");

            row.addView(createTableCell(item.studentCode != null ? item.studentCode : "-", false, Color.parseColor("#1E1B4B")));
            row.addView(createTableCell(item.name != null ? item.name : "-", false, Color.parseColor("#1E1B4B")));
            row.addView(createTableCell(scoreStr, true, Color.parseColor("#1E1B4B")));
            row.addView(createTableCell(maxStr, false, Color.parseColor("#64748B")));
            row.addView(createTableCell(pctStr, false, Color.parseColor("#1E1B4B")));
            row.addView(createTableCell(gradeStr, true, resColor));
            row.addView(createTableCell(resStr, true, resColor));
            row.addView(createTableCell(statusStr, false, stColor));
            row.addView(createTableCell(pubStr, false, isPublished ? Color.parseColor("#10B981") : Color.parseColor("#64748B")));

            tblExamResults.addView(row);
        }
    }

    private TextView createTableCell(String text, boolean bold, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(12f);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(18, 16, 18, 16);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        return tv;
    }

    private void showScoreEntryDialog(ExamStudentScoreAdapter.StudentScoreItem item) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_enter_exam_score);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvInfo = dialog.findViewById(R.id.tvDialogStudentInfo);
        TextView tvMax = dialog.findViewById(R.id.tvDialogExamMaxInfo);
        TextInputLayout tilScore = dialog.findViewById(R.id.tilScoreInput);
        TextInputEditText etScore = dialog.findViewById(R.id.etScoreInput);
        TextView tvGradePreview = dialog.findViewById(R.id.tvLiveGradePreview);
        TextInputEditText etRemarks = dialog.findViewById(R.id.etRemarksInput);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelScoreDialog);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveScoreDialog);

        tvTitle.setText(item.hasScore ? "Edit Exam Score & Remarks" : "Enter Exam Score & Remarks");
        tvInfo.setText(item.name + (item.studentCode != null && !item.studentCode.isEmpty() ? " (" + item.studentCode + ")" : ""));

        String marksFormatted = ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(totalMarks);
        tvMax.setText("Total Marks: " + marksFormatted);

        if (item.draftScoreInput != null && !item.draftScoreInput.isEmpty()) {
            etScore.setText(item.draftScoreInput);
            try {
                updateGradePreview(Double.parseDouble(item.draftScoreInput), tvGradePreview);
            } catch (NumberFormatException ignored) {}
        } else if (item.hasScore && item.score != null) {
            etScore.setText(ExamStudentScoreAdapter.StudentScoreItem.formatScoreNumber(item.score));
            updateGradePreview(item.score, tvGradePreview);
        }

        if (item.remarks != null) {
            etRemarks.setText(item.remarks);
        }

        etScore.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilScore.setError(null);
                try {
                    double val = Double.parseDouble(s.toString().trim());
                    updateGradePreview(val, tvGradePreview);
                } catch (NumberFormatException e) {
                    tvGradePreview.setText("Grade: -");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String scoreStr = etScore.getText() != null ? etScore.getText().toString().trim() : "";
            if (scoreStr.isEmpty()) {
                tilScore.setError("Score is required");
                return;
            }
            double scoreVal;
            try {
                scoreVal = Double.parseDouble(scoreStr);
            } catch (NumberFormatException e) {
                tilScore.setError("Invalid number");
                return;
            }

            if (scoreVal < 0 || scoreVal > totalMarks) {
                tilScore.setError("Score must be between 0 and " + marksFormatted);
                return;
            }

            String remarksStr = etRemarks.getText() != null ? etRemarks.getText().toString().trim() : "";

            if (isPublished) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Update Published Score?")
                        .setMessage("Results for '" + examTitle + "' are already published. Updating " + item.name + "'s score will immediately update what the student sees. Continue?")
                        .setPositiveButton("Update Score", (d2, w2) -> submitSingleScore(dialog, btnSave, item, scoreVal, remarksStr))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            } else {
                submitSingleScore(dialog, btnSave, item, scoreVal, remarksStr);
            }
        });

        dialog.show();
    }

    private void submitSingleScore(Dialog dialog, MaterialButton btnSave, ExamStudentScoreAdapter.StudentScoreItem item, double scoreVal, String remarksStr) {
        JsonObject body = new JsonObject();
        body.addProperty("student_id", item.studentId);
        body.addProperty("score", scoreVal);
        if (!remarksStr.isEmpty()) {
            body.addProperty("remarks", remarksStr);
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        ApiClient.getApiService(this).storeTeacherExamScores(examId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherExamDetailActivity.this, "Score saved for " + item.name, Toast.LENGTH_SHORT).show();
                    fetchExamDetails();
                } else {
                    Toast.makeText(TeacherExamDetailActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to save score"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                dialog.dismiss();
                Toast.makeText(TeacherExamDetailActivity.this, "Network error saving score", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGradePreview(double score, TextView tvGrade) {
        if (totalMarks <= 0) {
            tvGrade.setText("Grade: -");
            return;
        }
        double pct = (score / totalMarks) * 100.0;
        String letter = ExamStudentScoreAdapter.StudentScoreItem.calculateGrade(score, totalMarks);
        String passFail = pct >= 50.0 ? "Pass" : "Fail";
        tvGrade.setText(String.format(Locale.US, "Calculated: %.1f%% • Grade %s (%s)", pct, letter, passFail));
    }

    private void confirmPublishResults() {
        if (gradedStudentsCount <= 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cannot Publish Results")
                    .setMessage("Please enter and save at least one student's score before publishing exam results.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        int pending = Math.max(0, totalStudentsCount - gradedStudentsCount);
        StringBuilder msg = new StringBuilder();
        msg.append("Graded: ").append(gradedStudentsCount).append(" of ").append(totalStudentsCount).append(" students");
        if (pending > 0) {
            msg.append(" (").append(pending).append(" still pending)");
        }
        msg.append(".\n\nPublishing will make entered scores visible to enrolled students in their portal. Are you ready to publish results for '")
           .append(examTitle).append("'?");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Publish Exam Results?")
                .setMessage(msg.toString())
                .setPositiveButton("Publish", (d, w) -> {
                    pbLoading.setVisibility(View.VISIBLE);
                    ApiClient.getApiService(this).publishTeacherExam(examId).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            pbLoading.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(TeacherExamDetailActivity.this, "Exam results published successfully!", Toast.LENGTH_LONG).show();
                                fetchExamDetails();
                            } else {
                                Toast.makeText(TeacherExamDetailActivity.this,
                                        AssessmentUiHelper.extractErrorMessage(response, "Cannot publish results"),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(TeacherExamDetailActivity.this, "Network error publishing exam", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmUnpublishResults() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Unpublish Exam Results?")
                .setMessage("Unpublishing will hide the exam scores from students until you publish them again.")
                .setPositiveButton("Unpublish", (d, w) -> {
                    pbLoading.setVisibility(View.VISIBLE);
                    ApiClient.getApiService(this).unpublishTeacherExam(examId).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            pbLoading.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(TeacherExamDetailActivity.this, "Exam results unpublished.", Toast.LENGTH_SHORT).show();
                                fetchExamDetails();
                            } else {
                                Toast.makeText(TeacherExamDetailActivity.this,
                                        AssessmentUiHelper.extractErrorMessage(response, "Failed to unpublish"),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(TeacherExamDetailActivity.this, "Network error unpublishing exam", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteExam() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Exam?")
                .setMessage("Are you sure you want to delete '" + examTitle + "' and all associated student scores? This action cannot be undone.")
                .setPositiveButton(R.string.delete, (d, w) -> {
                    pbLoading.setVisibility(View.VISIBLE);
                    ApiClient.getApiService(this).deleteTeacherExamForce(examId, true).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            pbLoading.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(TeacherExamDetailActivity.this, "Exam deleted successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(TeacherExamDetailActivity.this,
                                        AssessmentUiHelper.extractErrorMessage(response, "Failed to delete exam"),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(TeacherExamDetailActivity.this, "Network error deleting exam", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static String optString(JsonObject obj, String key, String fallback) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return fallback;
    }

    private static int optInt(JsonObject obj, String key, int fallback) {
        try {
            if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsInt();
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private static double optDouble(JsonObject obj, String key, double fallback) {
        try {
            if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsDouble();
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }
}
