package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.ReopenPermissionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Unified Teacher Reopen, Deadline Extension, Additional Attempts, History & Audit Log Manager
 * supporting Assignment, Homework, and Quiz from one server-authoritative workflow.
 */
public class AssessmentReopenManagerActivity extends AppCompatActivity {

    public static final String EXTRA_ASSESSMENT_TYPE = "assessment_type";
    public static final String EXTRA_ASSESSMENT_ID = "assessment_id";
    public static final String EXTRA_ASSESSMENT_TITLE = "assessment_title";

    private String assessmentType = "assignment";
    private int assessmentId = 0;
    private String assessmentTitle = "Assessment";
    private String courseCode = "";
    private String courseName = "";

    private TextView tvReopenAssessmentType, tvReopenAssessmentTitle, tvReopenSummaryMeta, tvReopenEmpty;
    private TextInputEditText edtSearchReopenStudents;
    private CheckBox cbSelectAllStudents;
    private ProgressBar pbReopenLoading;
    private RecyclerView rvReopenStudents;

    private final List<StudentAccessRow> allRows = new ArrayList<>();
    private final List<StudentAccessRow> filteredRows = new ArrayList<>();
    private final Set<Integer> selectedStudentIds = new HashSet<>();
    private StudentAccessAdapter adapter;
    private boolean suppressSelectAll = false;

    public static class StudentAccessRow {
        int studentId;
        String studentCode;
        String studentName;
        String email;
        String reopenStatus;
        String submissionStatus;
        String originalDeadline;
        String extendedDeadline;
        String effectiveDeadline;
        Integer originalAttemptLimit;
        int additionalAttempts;
        Integer effectiveAttemptLimit;
        int usedAttempts;
        Integer remainingAttempts;
        boolean allowSubmission;
        boolean allowLateSubmission;
        boolean allowResubmission;
        boolean hasActiveOverride;
        Double latestScore;
        String overrideReason;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Teacher", "Admin")) {
            return;
        }
        setContentView(R.layout.activity_assessment_reopen_manager);

        if (getIntent() != null) {
            assessmentType = ReopenPermissionHelper.normalizeAssessmentType(
                    getIntent().getStringExtra(EXTRA_ASSESSMENT_TYPE));
            assessmentId = getIntent().getIntExtra(EXTRA_ASSESSMENT_ID, 0);
            String extraTitle = getIntent().getStringExtra(EXTRA_ASSESSMENT_TITLE);
            if (extraTitle != null && !extraTitle.trim().isEmpty()) {
                assessmentTitle = extraTitle.trim();
            }
        }

        if (assessmentId <= 0) {
            Toast.makeText(this, "Invalid assessment selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerReopenManager),
                "Access & Reopen Manager", assessmentTitle, true);

        bindViews();
        loadReopenStatus();
    }

    private void bindViews() {
        tvReopenAssessmentType = findViewById(R.id.tvReopenAssessmentType);
        tvReopenAssessmentTitle = findViewById(R.id.tvReopenAssessmentTitle);
        tvReopenSummaryMeta = findViewById(R.id.tvReopenSummaryMeta);
        tvReopenEmpty = findViewById(R.id.tvReopenEmpty);
        edtSearchReopenStudents = findViewById(R.id.edtSearchReopenStudents);
        cbSelectAllStudents = findViewById(R.id.cbSelectAllStudents);
        pbReopenLoading = findViewById(R.id.pbReopenLoading);
        rvReopenStudents = findViewById(R.id.rvReopenStudents);

        tvReopenAssessmentType.setText(assessmentType.toUpperCase(Locale.US));
        tvReopenAssessmentTitle.setText(assessmentTitle);

        adapter = new StudentAccessAdapter();
        rvReopenStudents.setLayoutManager(new LinearLayoutManager(this));
        rvReopenStudents.setAdapter(adapter);

        findViewById(R.id.btnBulkReopen).setOnClickListener(v -> handleBulkReopen());
        findViewById(R.id.btnBulkExtend).setOnClickListener(v -> handleBulkExtend());
        findViewById(R.id.btnBulkRevoke).setOnClickListener(v -> handleBulkRevoke());
        findViewById(R.id.btnViewAuditLogs).setOnClickListener(v -> showAuditLogsFilterAndListDialog());

        edtSearchReopenStudents.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cbSelectAllStudents.setOnCheckedChangeListener((btn, isChecked) -> {
            if (suppressSelectAll) return;
            for (StudentAccessRow r : filteredRows) {
                if (isChecked) {
                    selectedStudentIds.add(r.studentId);
                } else {
                    selectedStudentIds.remove(r.studentId);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void loadReopenStatus() {
        pbReopenLoading.setVisibility(View.VISIBLE);
        tvReopenEmpty.setVisibility(View.GONE);

        ApiClient.getApiService(this)
                .getTeacherAssessmentReopenStatus(assessmentType, assessmentId, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        pbReopenLoading.setVisibility(View.GONE);
                        if (!response.isSuccessful() || response.body() == null) {
                            String errMsg = AssessmentUiHelper.extractErrorMessage(response, "Unable to load reopen status");
                            Toast.makeText(AssessmentReopenManagerActivity.this, errMsg, Toast.LENGTH_LONG).show();
                            tvReopenEmpty.setText(errMsg);
                            tvReopenEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        JsonObject data = unwrapData(response.body());
                        if (data.has("assessment") && data.get("assessment").isJsonObject()) {
                            bindAssessmentSummary(data.getAsJsonObject("assessment"));
                        }

                        allRows.clear();
                        if (data.has("students") && data.get("students").isJsonArray()) {
                            for (JsonElement el : data.getAsJsonArray("students")) {
                                if (!el.isJsonObject()) continue;
                                allRows.add(parseStudentRow(el.getAsJsonObject()));
                            }
                        }

                        tvReopenEmpty.setText("No eligible students found for this assessment.");
                        String query = edtSearchReopenStudents.getText() != null
                                ? edtSearchReopenStudents.getText().toString() : "";
                        applyFilter(query);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        pbReopenLoading.setVisibility(View.GONE);
                        tvReopenEmpty.setText("Network error loading reopen status");
                        tvReopenEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(AssessmentReopenManagerActivity.this,
                                "Network error loading reopen status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindAssessmentSummary(JsonObject a) {
        String title = AssessmentUiHelper.optString(a, "title", assessmentTitle);
        assessmentTitle = title;
        courseCode = AssessmentUiHelper.optString(a, "course_code", "");
        courseName = AssessmentUiHelper.optString(a, "course_name", "");
        tvReopenAssessmentTitle.setText(title);
        tvReopenAssessmentType.setText(assessmentType.toUpperCase(Locale.US));

        String origDl = ReopenPermissionHelper.formatIsoShort(AssessmentUiHelper.optString(a, "original_deadline", ""));
        String limitText = (a.has("original_attempt_limit") && !a.get("original_attempt_limit").isJsonNull())
                ? String.valueOf(a.get("original_attempt_limit").getAsInt())
                : "Unlimited";
        String classNm = AssessmentUiHelper.optString(a, "class_name", "");
        StringBuilder sb = new StringBuilder();
        if (!courseCode.isEmpty()) sb.append(courseCode).append(" • ");
        if (!classNm.isEmpty()) sb.append(classNm).append(" • ");
        sb.append("Due: ").append(origDl.isEmpty() ? "No deadline" : origDl);
        sb.append(" • Attempt Limit: ").append(limitText);
        tvReopenSummaryMeta.setText(sb.toString());
    }

    private StudentAccessRow parseStudentRow(JsonObject obj) {
        ReopenPermissionHelper.ReopenStatusModel m = ReopenPermissionHelper.parseStatusPayload(obj);
        StudentAccessRow r = new StudentAccessRow();
        r.studentId = m.studentId;
        r.studentCode = AssessmentUiHelper.optString(obj, "student_code", "STU-" + m.studentId);
        r.studentName = AssessmentUiHelper.optString(obj, "student_name", "Student");
        r.email = AssessmentUiHelper.optString(obj, "email", "");
        r.reopenStatus = m.reopenStatus;
        r.submissionStatus = m.studentSubmissionStatus;
        r.originalDeadline = m.originalDeadline;
        r.extendedDeadline = m.extendedDeadline;
        r.effectiveDeadline = m.effectiveDeadline;
        r.originalAttemptLimit = m.originalAttemptLimit;
        r.additionalAttempts = m.additionalAttempts;
        r.effectiveAttemptLimit = m.effectiveAttemptLimit;
        r.usedAttempts = m.usedAttempts;
        r.remainingAttempts = m.remainingAttempts;
        r.allowSubmission = m.allowSubmission;
        r.allowLateSubmission = m.allowLateSubmission;
        r.allowResubmission = m.allowResubmission;
        r.hasActiveOverride = m.hasActiveOverride;
        r.latestScore = (obj.has("latest_score") && !obj.get("latest_score").isJsonNull())
                ? obj.get("latest_score").getAsDouble() : null;
        if (obj.has("override") && obj.get("override").isJsonObject()) {
            r.overrideReason = AssessmentUiHelper.optString(obj.getAsJsonObject("override"), "reason", "");
        }
        return r;
    }

    private void applyFilter(String query) {
        filteredRows.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.US) : "";
        for (StudentAccessRow r : allRows) {
            if (q.isEmpty()
                    || (r.studentName != null && r.studentName.toLowerCase(Locale.US).contains(q))
                    || (r.studentCode != null && r.studentCode.toLowerCase(Locale.US).contains(q))
                    || (r.reopenStatus != null && r.reopenStatus.toLowerCase(Locale.US).contains(q))) {
                filteredRows.add(r);
            }
        }
        tvReopenEmpty.setVisibility(filteredRows.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
        syncSelectAllState();
    }

    private void syncSelectAllState() {
        suppressSelectAll = true;
        if (filteredRows.isEmpty()) {
            cbSelectAllStudents.setChecked(false);
        } else {
            boolean allSel = true;
            for (StudentAccessRow r : filteredRows) {
                if (!selectedStudentIds.contains(r.studentId)) {
                    allSel = false;
                    break;
                }
            }
            cbSelectAllStudents.setChecked(allSel);
        }
        suppressSelectAll = false;
    }

    private void handleBulkReopen() {
        List<Integer> ids = new ArrayList<>(selectedStudentIds);
        if (ids.isEmpty()) {
            showReopenDialog(null, true, "Reopen Whole Assessment (All Students)");
        } else {
            showReopenDialog(ids, false, "Reopen for " + ids.size() + " Selected Student(s)");
        }
    }

    private void handleBulkExtend() {
        List<Integer> ids = new ArrayList<>(selectedStudentIds);
        if (ids.isEmpty()) {
            Toast.makeText(this, "Select one or more students to extend deadline.", Toast.LENGTH_SHORT).show();
            return;
        }
        showExtendDeadlineDialog(ids, "Extend Deadline (" + ids.size() + " Students)");
    }

    private void handleBulkRevoke() {
        List<Integer> ids = new ArrayList<>(selectedStudentIds);
        if (ids.isEmpty()) {
            Toast.makeText(this, "Select one or more students to revoke reopen permissions.", Toast.LENGTH_SHORT).show();
            return;
        }
        showRevokeDialog(ids, "Revoke Access (" + ids.size() + " Students)");
    }

    private void showReopenDialog(@Nullable List<Integer> studentIds, boolean assessmentScope, String title) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        CheckBox cbAllowSub = new CheckBox(this);
        cbAllowSub.setText("Allow Submission");
        cbAllowSub.setChecked(true);
        container.addView(cbAllowSub);

        CheckBox cbAllowResub = new CheckBox(this);
        cbAllowResub.setText("Allow Resubmission / Retake");
        cbAllowResub.setChecked(true);
        container.addView(cbAllowResub);

        CheckBox cbAllowLate = new CheckBox(this);
        cbAllowLate.setText("Allow Late Submission");
        cbAllowLate.setChecked(true);
        container.addView(cbAllowLate);

        TextInputLayout tilDeadline = new TextInputLayout(this);
        tilDeadline.setHint("New Deadline (YYYY-MM-DD HH:mm:ss)");
        TextInputEditText edtDeadline = new TextInputEditText(tilDeadline.getContext());
        edtDeadline.setContentDescription("Reopen Deadline Input");
        edtDeadline.setFocusable(false);
        edtDeadline.setFocusableInTouchMode(false);
        edtDeadline.setInputType(InputType.TYPE_NULL);
        edtDeadline.setClickable(true);
        edtDeadline.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilDeadline, edtDeadline, false, true));
        tilDeadline.addView(edtDeadline);
        container.addView(tilDeadline);

        TextInputLayout tilAttempts = new TextInputLayout(this);
        tilAttempts.setHint("Additional Attempts (0 for none)");
        TextInputEditText edtAttempts = new TextInputEditText(tilAttempts.getContext());
        edtAttempts.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtAttempts.setText("1");
        tilAttempts.addView(edtAttempts);
        container.addView(tilAttempts);

        TextInputLayout tilReason = new TextInputLayout(this);
        tilReason.setHint("Reason (Recorded in Audit Log)");
        TextInputEditText edtReason = new TextInputEditText(tilReason.getContext());
        tilReason.addView(edtReason);
        container.addView(tilReason);

        TextInputLayout tilNotes = new TextInputLayout(this);
        tilNotes.setHint("Teacher Notes (Optional)");
        TextInputEditText edtNotes = new TextInputEditText(tilNotes.getContext());
        tilNotes.addView(edtNotes);
        container.addView(tilNotes);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(wrapScroll(container))
                .setPositiveButton("Apply Reopen", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("assessment_type", assessmentType);
                    body.addProperty("assessment_id", assessmentId);
                    if (assessmentScope) {
                        body.addProperty("scope", "assessment");
                    } else if (studentIds != null) {
                        JsonArray arr = new JsonArray();
                        for (Integer sid : studentIds) arr.add(sid);
                        body.add("student_ids", arr);
                    }
                    body.addProperty("allow_submission", cbAllowSub.isChecked());
                    body.addProperty("allow_resubmission", cbAllowResub.isChecked());
                    body.addProperty("allow_late_submission", cbAllowLate.isChecked());

                    int addAtt = parseNonNegativeInt(edtAttempts.getText() != null ? edtAttempts.getText().toString() : "0");
                    body.addProperty("additional_attempts", addAtt);

                    String dl = textOf(edtDeadline);
                    if (!dl.isEmpty()) body.addProperty("new_deadline", dl);
                    String rsn = textOf(edtReason);
                    if (!rsn.isEmpty()) body.addProperty("reason", rsn);
                    String nts = textOf(edtNotes);
                    if (!nts.isEmpty()) body.addProperty("teacher_notes", nts);

                    submitMutation(ApiClient.getApiService(this).reopenTeacherAssessment(body), "Assessment reopened.");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showExtendDeadlineDialog(List<Integer> studentIds, String title) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        TextInputLayout tilDeadline = new TextInputLayout(this);
        tilDeadline.setHint("Extended Deadline *");
        TextInputEditText edtDeadline = new TextInputEditText(tilDeadline.getContext());
        edtDeadline.setContentDescription("Extend Deadline Input");
        edtDeadline.setFocusable(false);
        edtDeadline.setFocusableInTouchMode(false);
        edtDeadline.setInputType(InputType.TYPE_NULL);
        edtDeadline.setClickable(true);
        edtDeadline.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilDeadline, edtDeadline, false, true));
        tilDeadline.addView(edtDeadline);
        container.addView(tilDeadline);

        TextInputLayout tilReason = new TextInputLayout(this);
        tilReason.setHint("Extension Reason");
        TextInputEditText edtReason = new TextInputEditText(tilReason.getContext());
        tilReason.addView(edtReason);
        container.addView(tilReason);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Extend Deadline", (d, w) -> {
                    String dl = textOf(edtDeadline);
                    if (dl.isEmpty()) {
                        Toast.makeText(this, "Select an extended deadline first.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("assessment_type", assessmentType);
                    body.addProperty("assessment_id", assessmentId);
                    JsonArray arr = new JsonArray();
                    for (Integer sid : studentIds) arr.add(sid);
                    body.add("student_ids", arr);
                    body.addProperty("new_deadline", dl);
                    String rsn = textOf(edtReason);
                    if (!rsn.isEmpty()) body.addProperty("reason", rsn);

                    submitMutation(ApiClient.getApiService(this).extendTeacherAssessmentDeadline(body),
                            "Deadline extended.");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showGrantAttemptsDialog(StudentAccessRow row) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        TextInputLayout tilCount = new TextInputLayout(this);
        tilCount.setHint("Additional Attempts *");
        TextInputEditText edtCount = new TextInputEditText(tilCount.getContext());
        edtCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtCount.setText("1");
        tilCount.addView(edtCount);
        container.addView(tilCount);

        TextInputLayout tilReason = new TextInputLayout(this);
        tilReason.setHint("Reason");
        TextInputEditText edtReason = new TextInputEditText(tilReason.getContext());
        tilReason.addView(edtReason);
        container.addView(tilReason);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Grant Attempts — " + row.studentName)
                .setView(container)
                .setPositiveButton("Grant", (d, w) -> {
                    int add = parseNonNegativeInt(textOf(edtCount));
                    if (add <= 0) {
                        Toast.makeText(this, "Enter at least 1 additional attempt.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("assessment_type", assessmentType);
                    body.addProperty("assessment_id", assessmentId);
                    body.addProperty("student_id", row.studentId);
                    body.addProperty("additional_attempts", add);
                    String rsn = textOf(edtReason);
                    if (!rsn.isEmpty()) body.addProperty("reason", rsn);

                    submitMutation(ApiClient.getApiService(this).grantTeacherAssessmentAttempts(body),
                            "Additional attempts granted.");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRevokeDialog(List<Integer> studentIds, String title) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        TextView note = new TextView(this);
        note.setText("Revoking removes reopen/override permissions while preserving all past submissions, grades, and attempts.");
        note.setTextColor(getResources().getColor(R.color.text_secondary, null));
        note.setTextSize(12.5f);
        container.addView(note);

        TextInputLayout tilReason = new TextInputLayout(this);
        tilReason.setHint("Revocation Reason (Optional)");
        TextInputEditText edtReason = new TextInputEditText(tilReason.getContext());
        tilReason.addView(edtReason);
        container.addView(tilReason);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Revoke", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("assessment_type", assessmentType);
                    body.addProperty("assessment_id", assessmentId);
                    JsonArray arr = new JsonArray();
                    for (Integer sid : studentIds) arr.add(sid);
                    body.add("student_ids", arr);
                    String rsn = textOf(edtReason);
                    if (!rsn.isEmpty()) body.addProperty("reason", rsn);

                    submitMutation(ApiClient.getApiService(this).revokeTeacherAssessmentReopen(body),
                            "Reopen permissions revoked.");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitMutation(Call<JsonObject> call, String successMsg) {
        pbReopenLoading.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbReopenLoading.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AssessmentReopenManagerActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    selectedStudentIds.clear();
                    loadReopenStatus();
                } else {
                    Toast.makeText(AssessmentReopenManagerActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Operation failed"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbReopenLoading.setVisibility(View.GONE);
                Toast.makeText(AssessmentReopenManagerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openStudentHistory(StudentAccessRow row) {
        pbReopenLoading.setVisibility(View.VISIBLE);
        if ("quiz".equals(assessmentType)) {
            ApiClient.getApiService(this).getTeacherQuizAttemptHistory(assessmentId, row.studentId)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            pbReopenLoading.setVisibility(View.GONE);
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(AssessmentReopenManagerActivity.this,
                                        AssessmentUiHelper.extractErrorMessage(response, "Failed to load quiz attempt history"),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            showQuizHistoryDialog(row, unwrapData(response.body()));
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            pbReopenLoading.setVisibility(View.GONE);
                            Toast.makeText(AssessmentReopenManagerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ApiClient.getApiService(this).getTeacherAssessmentSubmissionHistory(assessmentType, assessmentId, row.studentId)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            pbReopenLoading.setVisibility(View.GONE);
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(AssessmentReopenManagerActivity.this,
                                        AssessmentUiHelper.extractErrorMessage(response, "Failed to load submission history"),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            showSubmissionHistoryDialog(row, unwrapData(response.body()));
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            pbReopenLoading.setVisibility(View.GONE);
                            Toast.makeText(AssessmentReopenManagerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showSubmissionHistoryDialog(StudentAccessRow row, JsonObject data) {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(36, 20, 36, 12);

        JsonArray subs = (data.has("submissions") && data.get("submissions").isJsonArray())
                ? data.getAsJsonArray("submissions") : new JsonArray();

        if (subs.size() == 0) {
            TextView tv = new TextView(this);
            tv.setText("No submission versions recorded yet for " + row.studentName + ".");
            list.addView(tv);
        } else {
            for (JsonElement el : subs) {
                if (!el.isJsonObject()) continue;
                JsonObject s = el.getAsJsonObject();
                int subId = AssessmentUiHelper.optInt(s, "submission_id", AssessmentUiHelper.optInt(s, "id", 0));
                int ver = AssessmentUiHelper.optInt(s, "version_number", 1);
                boolean isLatest = AssessmentUiHelper.optBoolean(s, "is_latest", false);
                boolean isLate = AssessmentUiHelper.optBoolean(s, "is_late", false);
                String status = AssessmentUiHelper.optString(s, "submission_status", "submitted");
                String submittedAt = ReopenPermissionHelper.formatIsoShort(AssessmentUiHelper.optString(s, "submitted_at", ""));
                double maxScore = AssessmentUiHelper.optDouble(s, "max_score", 100.0);
                Double sc = (s.has("score") && !s.get("score").isJsonNull()) ? s.get("score").getAsDouble() : null;
                String fb = AssessmentUiHelper.optString(s, "feedback", "");
                String subText = AssessmentUiHelper.optString(s, "submission_text", "");
                boolean hasFile = AssessmentUiHelper.optBoolean(s, "has_file", false);
                boolean fileExists = AssessmentUiHelper.optBoolean(s, "file_exists", false);
                String fName = AssessmentUiHelper.optString(s, "file_name", "");

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(24, 18, 24, 18);
                card.setBackgroundColor(Color.parseColor("#F8FAFC"));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = 16;
                card.setLayoutParams(lp);

                TextView tvHeader = new TextView(this);
                tvHeader.setText(String.format(Locale.US, "Version v%d %s • %s%s",
                        ver, isLatest ? "(Latest)" : "(Archived)", status.toUpperCase(Locale.US), isLate ? " [LATE]" : ""));
                tvHeader.setTextColor(Color.parseColor("#1E1B4B"));
                tvHeader.setTextSize(13.5f);
                tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                card.addView(tvHeader);

                TextView tvMeta = new TextView(this);
                StringBuilder ms = new StringBuilder();
                ms.append("Submitted: ").append(submittedAt.isEmpty() ? "-" : submittedAt);
                if (sc != null) {
                    ms.append("\nScore: ").append(AssessmentUiHelper.formatMarks(sc))
                            .append(" / ").append(AssessmentUiHelper.formatMarks(maxScore));
                }
                if (!fb.isEmpty()) ms.append("\nFeedback: ").append(fb);
                if (!subText.isEmpty()) ms.append("\nNotes: ").append(subText);
                if (hasFile) ms.append("\nFile: ").append(fName.isEmpty() ? "Attachment" : fName);
                tvMeta.setText(ms.toString());
                tvMeta.setTextSize(12f);
                tvMeta.setTextColor(Color.parseColor("#475569"));
                card.addView(tvMeta);

                LinearLayout actions = new LinearLayout(this);
                actions.setOrientation(LinearLayout.HORIZONTAL);
                actions.setPadding(0, 10, 0, 0);

                if (hasFile) {
                    MaterialButton btnFile = new MaterialButton(this, null,
                            com.google.android.material.R.attr.materialButtonOutlinedStyle);
                    btnFile.setText("View File");
                    btnFile.setAllCaps(false);
                    btnFile.setOnClickListener(v -> {
                        Intent intent = new Intent(this, SubmissionFileViewerActivity.class);
                        intent.putExtra("submission_id", String.valueOf(subId));
                        intent.putExtra("student_code", row.studentCode);
                        intent.putExtra("student_name", row.studentName);
                        intent.putExtra("assessment_title", assessmentTitle);
                        intent.putExtra("course_code", courseCode);
                        intent.putExtra("course_name", courseName);
                        intent.putExtra("submitted_at", submittedAt);
                        intent.putExtra("file_name", fName);
                        intent.putExtra("file_type", AssessmentUiHelper.optString(s, "file_type", ""));
                        intent.putExtra("file_size_label", AssessmentUiHelper.optString(s, "file_size_label", ""));
                        intent.putExtra("mime_type", AssessmentUiHelper.optString(s, "mime_type", ""));
                        intent.putExtra("preview_type", AssessmentUiHelper.optString(s, "preview_type", ""));
                        intent.putExtra("has_file", true);
                        intent.putExtra("file_exists", fileExists);
                        startActivity(intent);
                    });
                    actions.addView(btnFile);
                }

                MaterialButton btnGrade = new MaterialButton(this);
                btnGrade.setText("Grade v" + ver);
                btnGrade.setAllCaps(false);
                LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                glp.leftMargin = 12;
                btnGrade.setLayoutParams(glp);
                btnGrade.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EnterScoreActivity.class);
                    intent.putExtra("submission_id", String.valueOf(subId));
                    intent.putExtra("assessment_title", assessmentTitle + " (v" + ver + ")");
                    intent.putExtra("assessment_type", assessmentType);
                    intent.putExtra("course_code", courseCode);
                    intent.putExtra("course_name", courseName);
                    intent.putExtra("student_name", row.studentName);
                    intent.putExtra("student_code", row.studentCode);
                    intent.putExtra("submitted_at", submittedAt);
                    intent.putExtra("status", status);
                    intent.putExtra("max_score", maxScore);
                    if (sc != null) intent.putExtra("existing_score", sc);
                    intent.putExtra("existing_feedback", fb);
                    intent.putExtra("has_file", hasFile);
                    intent.putExtra("file_exists", fileExists);
                    intent.putExtra("file_name", fName);
                    startActivity(intent);
                });
                actions.addView(btnGrade);
                card.addView(actions);
                list.addView(card);
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Submission History — " + row.studentName)
                .setView(wrapScroll(list))
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showQuizHistoryDialog(StudentAccessRow row, JsonObject data) {
        String scoring = AssessmentUiHelper.optString(data, "scoring_method", "highest");
        StringBuilder sb = new StringBuilder();
        sb.append("Scoring Policy: ").append(scoring.toUpperCase(Locale.US)).append("\n");
        if (data.has("student_summaries") && data.get("student_summaries").isJsonArray()) {
            JsonArray sums = data.getAsJsonArray("student_summaries");
            if (sums.size() > 0 && sums.get(0).isJsonObject()) {
                JsonObject s0 = sums.get(0).getAsJsonObject();
                if (s0.has("effective_score") && !s0.get("effective_score").isJsonNull()) {
                    sb.append("Official Score: ").append(AssessmentUiHelper.formatMarks(s0.get("effective_score").getAsDouble())).append("\n");
                }
            }
        }
        sb.append("\n");

        JsonArray atts = (data.has("attempts") && data.get("attempts").isJsonArray())
                ? data.getAsJsonArray("attempts") : new JsonArray();
        if (atts.size() == 0) {
            sb.append("No quiz attempts recorded yet.");
        } else {
            for (JsonElement el : atts) {
                if (!el.isJsonObject()) continue;
                JsonObject a = el.getAsJsonObject();
                int num = AssessmentUiHelper.optInt(a, "attempt_number", 1);
                String st = AssessmentUiHelper.optString(a, "attempt_status", "submitted");
                String gs = AssessmentUiHelper.optString(a, "grading_status", "");
                boolean isLate = AssessmentUiHelper.optBoolean(a, "is_late", false);
                double tot = AssessmentUiHelper.optDouble(a, "total_marks", 0);
                String scStr = (a.has("score") && !a.get("score").isJsonNull())
                        ? (AssessmentUiHelper.formatMarks(a.get("score").getAsDouble()) + " / " + AssessmentUiHelper.formatMarks(tot))
                        : gs;

                sb.append("Attempt #").append(num).append(" [").append(st).append(isLate ? ", LATE" : "").append("]\n");
                sb.append("Score: ").append(scStr).append("\n");
                if (a.has("answers") && a.get("answers").isJsonArray()) {
                    for (JsonElement qEl : a.getAsJsonArray("answers")) {
                        if (!qEl.isJsonObject()) continue;
                        JsonObject ans = qEl.getAsJsonObject();
                        int qNum = AssessmentUiHelper.optInt(ans, "number", 1);
                        String given = AssessmentUiHelper.optString(ans, "student_answer", "(blank)");
                        sb.append("  • Q").append(qNum).append(": ").append(given);
                        if (ans.has("marks_obtained") && !ans.get("marks_obtained").isJsonNull()) {
                            sb.append(" (").append(AssessmentUiHelper.formatMarks(ans.get("marks_obtained").getAsDouble())).append(" pts)");
                        }
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Quiz Attempt History — " + row.studentName)
                .setMessage(sb.toString().trim())
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showAuditLogsFilterAndListDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        Spinner spAction = new Spinner(this);
        String[] actionOptions = {"All Actions", "reopen", "extend_deadline", "grant_attempts", "revoke"};
        spAction.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, actionOptions));
        container.addView(spAction);

        List<String> studentLabels = new ArrayList<>();
        List<Integer> studentIdsList = new ArrayList<>();
        studentLabels.add("All Students");
        studentIdsList.add(0);
        for (StudentAccessRow r : allRows) {
            studentLabels.add(r.studentName + " (" + r.studentCode + ")");
            studentIdsList.add(r.studentId);
        }
        Spinner spStudent = new Spinner(this);
        spStudent.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, studentLabels));
        container.addView(spStudent);

        TextInputLayout tilFrom = new TextInputLayout(this);
        tilFrom.setHint("Date From (Optional)");
        TextInputEditText edtFrom = new TextInputEditText(tilFrom.getContext());
        DatePickerHelper.attachDatePicker(tilFrom, edtFrom, true);
        tilFrom.addView(edtFrom);
        container.addView(tilFrom);

        TextInputLayout tilTo = new TextInputLayout(this);
        tilTo.setHint("Date To (Optional)");
        TextInputEditText edtTo = new TextInputEditText(tilTo.getContext());
        DatePickerHelper.attachDatePicker(tilTo, edtTo, true);
        tilTo.addView(edtTo);
        container.addView(tilTo);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Filter Reopen & Extension Audit Logs")
                .setView(container)
                .setPositiveButton("Load Logs", (d, w) -> {
                    Map<String, String> params = new HashMap<>();
                    params.put("assessment_type", assessmentType);
                    params.put("assessment_id", String.valueOf(assessmentId));
                    int actPos = spAction.getSelectedItemPosition();
                    if (actPos > 0) params.put("action_type", actionOptions[actPos]);
                    int stPos = spStudent.getSelectedItemPosition();
                    if (stPos > 0 && stPos < studentIdsList.size()) {
                        params.put("student_id", String.valueOf(studentIdsList.get(stPos)));
                    }
                    String df = textOf(edtFrom);
                    String dt = textOf(edtTo);
                    if (!df.isEmpty()) params.put("date_from", df);
                    if (!dt.isEmpty()) params.put("date_to", dt);

                    fetchAndShowAuditLogs(params);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void fetchAndShowAuditLogs(Map<String, String> params) {
        pbReopenLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).getTeacherAssessmentAuditLogs(params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbReopenLoading.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AssessmentReopenManagerActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Unable to fetch audit logs"),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                JsonArray logs = response.body().has("data") && response.body().get("data").isJsonArray()
                        ? response.body().getAsJsonArray("data") : new JsonArray();
                StringBuilder sb = new StringBuilder();
                if (logs.size() == 0) {
                    sb.append("No matching audit log events recorded.");
                } else {
                    for (JsonElement el : logs) {
                        if (!el.isJsonObject()) continue;
                        JsonObject l = el.getAsJsonObject();
                        String action = AssessmentUiHelper.optString(l, "action_type", "event");
                        String by = AssessmentUiHelper.optString(l, "performed_by_name", "System");
                        String st = AssessmentUiHelper.optString(l, "student_name", "Whole Assessment");
                        String prevDl = ReopenPermissionHelper.formatIsoShort(AssessmentUiHelper.optString(l, "previous_deadline", ""));
                        String newDl = ReopenPermissionHelper.formatIsoShort(AssessmentUiHelper.optString(l, "new_deadline", ""));
                        String reason = AssessmentUiHelper.optString(l, "reason", "");
                        String created = ReopenPermissionHelper.formatIsoShort(AssessmentUiHelper.optString(l, "created_at", ""));

                        sb.append("[").append(action.toUpperCase(Locale.US)).append("] ").append(st).append("\n");
                        sb.append("By: ").append(by).append(" • ").append(created).append("\n");
                        if (!prevDl.isEmpty() || !newDl.isEmpty()) {
                            sb.append("Deadline: ").append(prevDl.isEmpty() ? "-" : prevDl)
                                    .append(" → ").append(newDl.isEmpty() ? "-" : newDl).append("\n");
                        }
                        if (l.has("previous_attempt_limit") && !l.get("previous_attempt_limit").isJsonNull()
                                || l.has("new_attempt_limit") && !l.get("new_attempt_limit").isJsonNull()) {
                            String pl = l.has("previous_attempt_limit") && !l.get("previous_attempt_limit").isJsonNull()
                                    ? String.valueOf(l.get("previous_attempt_limit").getAsInt()) : "-";
                            String nl = l.has("new_attempt_limit") && !l.get("new_attempt_limit").isJsonNull()
                                    ? String.valueOf(l.get("new_attempt_limit").getAsInt()) : "-";
                            sb.append("Attempt Limit: ").append(pl).append(" → ").append(nl).append("\n");
                        }
                        if (!reason.isEmpty()) sb.append("Reason: ").append(reason).append("\n");
                        sb.append("\n");
                    }
                }
                new MaterialAlertDialogBuilder(AssessmentReopenManagerActivity.this)
                        .setTitle("Audit Logs (" + logs.size() + ")")
                        .setMessage(sb.toString().trim())
                        .setPositiveButton(R.string.close, null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbReopenLoading.setVisibility(View.GONE);
                Toast.makeText(AssessmentReopenManagerActivity.this, "Network error loading logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class StudentAccessAdapter extends RecyclerView.Adapter<StudentAccessAdapter.RowVH> {
        @NonNull
        @Override
        public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reopen_student_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RowVH h, int pos) {
            StudentAccessRow r = filteredRows.get(pos);
            h.tvStudentName.setText(r.studentName);
            h.tvStudentCode.setText(r.studentCode + (r.latestScore != null
                    ? (" • Score: " + AssessmentUiHelper.formatMarks(r.latestScore)) : ""));

            h.tvReopenStatusBadge.setText(r.reopenStatus);
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(12f);
            badgeBg.setColor(Color.parseColor(ReopenPermissionHelper.resolveBadgeColorHex(r.reopenStatus)));
            h.tvReopenStatusBadge.setBackground(badgeBg);

            h.tvAttemptInfo.setText("Attempts: " + ReopenPermissionHelper.formatAttemptSummary(
                    r.usedAttempts, r.effectiveAttemptLimit, r.remainingAttempts));
            h.tvDeadlineInfo.setText(ReopenPermissionHelper.formatDeadlineSummary(
                    r.originalDeadline, r.extendedDeadline));

            List<String> flags = new ArrayList<>();
            if (r.hasActiveOverride) {
                if (!r.allowSubmission) flags.add("Blocked");
                if (r.allowLateSubmission) flags.add("Late Allowed");
                if (r.allowResubmission) flags.add("Resubmit Allowed");
                if (r.additionalAttempts > 0) flags.add("+" + r.additionalAttempts + " Attempts");
                if (r.overrideReason != null && !r.overrideReason.isEmpty()) flags.add(r.overrideReason);
            }
            if (flags.isEmpty()) {
                h.tvOverrideSummary.setVisibility(View.GONE);
            } else {
                h.tvOverrideSummary.setVisibility(View.VISIBLE);
                h.tvOverrideSummary.setText("Override: " + String.join(" • ", flags));
            }

            h.cbSelect.setOnCheckedChangeListener(null);
            h.cbSelect.setChecked(selectedStudentIds.contains(r.studentId));
            h.cbSelect.setOnCheckedChangeListener((b, chk) -> {
                if (chk) selectedStudentIds.add(r.studentId);
                else selectedStudentIds.remove(r.studentId);
                syncSelectAllState();
            });

            List<Integer> singleList = new ArrayList<>();
            singleList.add(r.studentId);
            h.btnRowReopen.setOnClickListener(v -> showReopenDialog(singleList, false, "Reopen — " + r.studentName));
            h.btnRowExtend.setOnClickListener(v -> showExtendDeadlineDialog(singleList, "Extend — " + r.studentName));
            h.btnRowAttempts.setOnClickListener(v -> showGrantAttemptsDialog(r));
            h.btnRowHistory.setOnClickListener(v -> openStudentHistory(r));
            h.btnRowRevoke.setVisibility(r.hasActiveOverride ? View.VISIBLE : View.GONE);
            h.btnRowRevoke.setOnClickListener(v -> showRevokeDialog(singleList, "Revoke — " + r.studentName));
        }

        @Override
        public int getItemCount() {
            return filteredRows.size();
        }

        class RowVH extends RecyclerView.ViewHolder {
            final CheckBox cbSelect;
            final TextView tvStudentName, tvStudentCode, tvReopenStatusBadge, tvAttemptInfo, tvDeadlineInfo, tvOverrideSummary;
            final MaterialButton btnRowReopen, btnRowExtend, btnRowAttempts, btnRowHistory, btnRowRevoke;

            RowVH(@NonNull View v) {
                super(v);
                cbSelect = v.findViewById(R.id.cbStudentSelect);
                tvStudentName = v.findViewById(R.id.tvStudentName);
                tvStudentCode = v.findViewById(R.id.tvStudentCode);
                tvReopenStatusBadge = v.findViewById(R.id.tvReopenStatusBadge);
                tvAttemptInfo = v.findViewById(R.id.tvAttemptInfo);
                tvDeadlineInfo = v.findViewById(R.id.tvDeadlineInfo);
                tvOverrideSummary = v.findViewById(R.id.tvOverrideSummary);
                btnRowReopen = v.findViewById(R.id.btnRowReopen);
                btnRowExtend = v.findViewById(R.id.btnRowExtend);
                btnRowAttempts = v.findViewById(R.id.btnRowAttempts);
                btnRowHistory = v.findViewById(R.id.btnRowHistory);
                btnRowRevoke = v.findViewById(R.id.btnRowRevoke);
            }
        }
    }

    private JsonObject unwrapData(JsonObject root) {
        return root != null && root.has("data") && root.get("data").isJsonObject()
                ? root.getAsJsonObject("data") : (root != null ? root : new JsonObject());
    }

    private ScrollView wrapScroll(View child) {
        ScrollView sv = new ScrollView(this);
        sv.addView(child);
        return sv;
    }

    private String textOf(TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }

    private int parseNonNegativeInt(String raw) {
        try {
            return Math.max(0, Integer.parseInt(raw.trim()));
        } catch (Exception e) {
            return 0;
        }
    }
}
