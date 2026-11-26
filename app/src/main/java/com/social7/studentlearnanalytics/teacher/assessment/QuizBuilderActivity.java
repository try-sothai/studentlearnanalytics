package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.social7.studentlearnanalytics.model.QuizQuestionItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizBuilderActivity extends AppCompatActivity {

    private int quizId = 0;
    private String quizTitle = "";
    private String quizDescription = "";
    private int quizDuration = 30;
    private String quizStatus = "draft";
    private String quizStartAt = "";
    private String quizEndAt = "";
    private Integer quizMaxAttempts = 1; // null = Unlimited
    private String quizScoringMethod = "highest";
    private boolean quizAllowLate = false;
    private int attemptsCount = 0;
    private int pendingGradingCount = 0;
    private int overridesCount = 0;
    private int quizVersion = 1;
    private boolean openAttemptsOnLoad = false;
    private boolean autoOpenAttemptsHandled = false;
    private boolean hasLoadedOnce = false;

    private TextView tvBuilderCourseContext, tvBuilderStatus, tvBuilderTitle;
    private TextView tvTotalQuestions, tvTotalMarks, tvQuizDuration, tvQuizPolicySummary, tvQuestionsLockedBanner;
    private TextView tvQuestionsEmptyTitle, tvQuestionsEmptyDesc;
    private MaterialButton btnEditQuizInfo, btnViewAttempts, btnManageQuiz, btnQuizOverrides;
    private MaterialButton btnAddQuestion, btnSaveQuizDraft, btnPublishQuizBuilder, btnRetryBuilder;
    private RecyclerView rvQuizQuestions;
    private ProgressBar pbBuilderLoading;
    private LinearLayout layoutQuestionsEmpty;

    private final List<QuizQuestionItem> questions = new ArrayList<>();
    private QuizBuilderQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_builder);

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerQuizBuilder), "Quiz Question Builder", "", true);

        quizId = getIntent() != null ? getIntent().getIntExtra("quiz_id", 0) : 0;
        openAttemptsOnLoad = getIntent() != null && getIntent().getBooleanExtra("open_attempts", false);
        if (quizId <= 0) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvBuilderCourseContext = findViewById(R.id.tvBuilderCourseContext);
        tvBuilderStatus = findViewById(R.id.tvBuilderStatus);
        tvBuilderTitle = findViewById(R.id.tvBuilderTitle);
        tvTotalQuestions = findViewById(R.id.tvTotalQuestions);
        tvTotalMarks = findViewById(R.id.tvTotalMarks);
        tvQuizDuration = findViewById(R.id.tvQuizDuration);
        tvQuizPolicySummary = findViewById(R.id.tvQuizPolicySummary);
        tvQuestionsLockedBanner = findViewById(R.id.tvQuestionsLockedBanner);

        btnEditQuizInfo = findViewById(R.id.btnEditQuizInfo);
        btnViewAttempts = findViewById(R.id.btnViewAttempts);
        btnManageQuiz = findViewById(R.id.btnManageQuiz);
        btnQuizOverrides = findViewById(R.id.btnQuizOverrides);
        btnAddQuestion = findViewById(R.id.btnAddQuestion);
        btnSaveQuizDraft = findViewById(R.id.btnSaveQuizDraft);
        btnPublishQuizBuilder = findViewById(R.id.btnPublishQuizBuilder);
        btnRetryBuilder = findViewById(R.id.btnRetryBuilder);

        rvQuizQuestions = findViewById(R.id.rvQuizQuestions);
        pbBuilderLoading = findViewById(R.id.pbBuilderLoading);
        layoutQuestionsEmpty = findViewById(R.id.layoutQuestionsEmpty);
        tvQuestionsEmptyTitle = findViewById(R.id.tvQuestionsEmptyTitle);
        tvQuestionsEmptyDesc = findViewById(R.id.tvQuestionsEmptyDesc);

        adapter = new QuizBuilderQuestionAdapter(questions, new QuizBuilderQuestionAdapter.Listener() {
            @Override
            public void onEdit(QuizQuestionItem item) {
                showQuestionEditorDialog(item);
            }

            @Override
            public void onDelete(QuizQuestionItem item) {
                confirmDeleteQuestion(item);
            }

            @Override
            public void onDuplicate(QuizQuestionItem item) {
                duplicateQuestion(item);
            }

            @Override
            public void onMove(int fromPos, int toPos) {
                moveQuestion(fromPos, toPos);
            }
        });
        rvQuizQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuizQuestions.setAdapter(adapter);

        btnAddQuestion.setOnClickListener(v -> showQuestionEditorDialog(null));
        btnEditQuizInfo.setOnClickListener(v -> showEditQuizInfoDialog());
        btnViewAttempts.setOnClickListener(v -> fetchAndShowAttempts());
        if (btnManageQuiz != null) btnManageQuiz.setOnClickListener(v -> showManageQuizMenu());
        if (btnQuizOverrides != null) btnQuizOverrides.setOnClickListener(v -> showStudentOverridesDialog());
        btnRetryBuilder.setOnClickListener(v -> loadQuiz());

        btnSaveQuizDraft.setOnClickListener(v -> {
            if ("published".equalsIgnoreCase(quizStatus)) {
                unpublishQuiz();
            } else {
                saveQuizStatus("draft");
            }
        });
        btnPublishQuizBuilder.setOnClickListener(v -> publishQuiz());

        loadQuiz();
        hasLoadedOnce = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLoadedOnce) {
            loadQuiz();
        }
    }

    private void loadQuiz() {
        pbBuilderLoading.setVisibility(View.VISIBLE);
        layoutQuestionsEmpty.setVisibility(View.GONE);
        rvQuizQuestions.setVisibility(View.GONE);

        ApiClient.getApiService(this).getTeacherQuizDetail(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbBuilderLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    showEmptyOrError(
                            "Could not load quiz",
                            AssessmentUiHelper.extractErrorMessage(response, "Unable to fetch quiz details."),
                            true
                    );
                    return;
                }

                bindQuizPayload(response.body().getAsJsonObject("data"));
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbBuilderLoading.setVisibility(View.GONE);
                showEmptyOrError("Network error", "Check your connection and retry.", true);
            }
        });
    }

    private void bindQuizPayload(JsonObject data) {
        quizTitle = AssessmentUiHelper.optString(data, "title", "Untitled Quiz");
        quizDescription = AssessmentUiHelper.optString(data, "description", "");
        quizDuration = AssessmentUiHelper.optInt(data, "duration_minutes", 30);
        quizStatus = AssessmentUiHelper.optString(data, "status", "draft");
        quizStartAt = AssessmentUiHelper.optString(data, "start_at", "");
        quizEndAt = AssessmentUiHelper.optString(data, "end_at", "");
        quizMaxAttempts = (data.has("max_attempts") && !data.get("max_attempts").isJsonNull())
                ? AssessmentUiHelper.optInt(data, "max_attempts", 1) : null;
        if (quizMaxAttempts != null && quizMaxAttempts <= 0) quizMaxAttempts = null;
        quizScoringMethod = AssessmentUiHelper.optString(data, "scoring_method", "highest");
        quizAllowLate = AssessmentUiHelper.optBoolean(data, "allow_late_submission", false);
        attemptsCount = AssessmentUiHelper.optInt(data, "attempts_count", 0);
        pendingGradingCount = AssessmentUiHelper.optInt(data, "pending_grading_count", 0);
        overridesCount = AssessmentUiHelper.optInt(data, "overrides_count", 0);
        quizVersion = Math.max(1, AssessmentUiHelper.optInt(data, "version", 1));

        String courseCode = AssessmentUiHelper.optString(data, "course_code", "");
        String courseName = AssessmentUiHelper.optString(data, "course_name", "Course");
        String className = AssessmentUiHelper.optString(data, "class_name", "");
        String semName = AssessmentUiHelper.optString(data, "semester_name", "");

        StringBuilder ctx = new StringBuilder();
        if (!courseCode.isEmpty()) ctx.append(courseCode).append(" – ");
        ctx.append(courseName);
        if (!className.isEmpty()) ctx.append(" • ").append(className);
        if (!semName.isEmpty()) ctx.append(" • ").append(semName);

        tvBuilderCourseContext.setText(ctx.toString());
        tvBuilderTitle.setText(quizTitle);
        tvQuizDuration.setText(quizDuration + " mins");
        btnViewAttempts.setText("Attempts (" + attemptsCount + ")"
                + (pendingGradingCount > 0 ? " • " + pendingGradingCount + " Pending" : ""));
        if (btnQuizOverrides != null) {
            btnQuizOverrides.setText(overridesCount > 0 ? ("Overrides (" + overridesCount + ")") : "Retakes & Overrides");
        }
        if (tvQuizPolicySummary != null) {
            String scoringLabel = "latest".equalsIgnoreCase(quizScoringMethod) ? "Latest"
                    : ("average".equalsIgnoreCase(quizScoringMethod) ? "Average" : "Highest");
            StringBuilder policySb = new StringBuilder();
            policySb.append("Attempts: ").append(quizMaxAttempts == null ? "Unlimited" : quizMaxAttempts);
            policySb.append(" • Scoring: ").append(scoringLabel);
            policySb.append(" • v").append(quizVersion);
            if (!quizEndAt.isEmpty()) {
                policySb.append(" • Due: ").append(AssessmentUiHelper.formatDateTime(quizEndAt));
            }
            if (quizAllowLate) {
                policySb.append(" • Late Allowed");
            }
            if (pendingGradingCount > 0) {
                policySb.append(" • Pending Grading: ").append(pendingGradingCount);
            }
            tvQuizPolicySummary.setText(policySb.toString());
        }

        // Allow safe editing/reordering even when attempts exist; backend snapshots prior attempts automatically.
        tvQuestionsLockedBanner.setVisibility(attemptsCount > 0 ? View.VISIBLE : View.GONE);
        btnAddQuestion.setEnabled(true);
        btnAddQuestion.setAlpha(1f);
        adapter.setLocked(false);

        questions.clear();
        if (data.has("questions") && data.get("questions").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("questions");
            for (JsonElement el : arr) {
                if (el.isJsonObject()) {
                    questions.add(QuizQuestionItem.fromJson(el.getAsJsonObject()));
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateSummaryCounts();
        updateStatusUi();

        if (questions.isEmpty()) {
            showEmptyOrError("No questions added yet", "Tap '+ Add Question' below to add Multiple Choice, True/False, or Short Answer questions.", false);
        } else {
            layoutQuestionsEmpty.setVisibility(View.GONE);
            rvQuizQuestions.setVisibility(View.VISIBLE);
        }

        if (openAttemptsOnLoad && !autoOpenAttemptsHandled) {
            autoOpenAttemptsHandled = true;
            fetchAndShowAttempts();
        }
    }

    private void updateSummaryCounts() {
        double sum = 0;
        for (QuizQuestionItem q : questions) {
            sum += q.marks;
        }
        tvTotalQuestions.setText(String.valueOf(questions.size()));
        tvTotalMarks.setText(AssessmentUiHelper.formatMarks(sum));
    }

    private void updateStatusUi() {
        String cap = quizStatus.isEmpty() ? "Draft"
                : quizStatus.substring(0, 1).toUpperCase(Locale.US) + quizStatus.substring(1).toLowerCase(Locale.US);
        tvBuilderStatus.setText(cap);
        if ("published".equalsIgnoreCase(quizStatus)) {
            tvBuilderStatus.setTextColor(getResources().getColor(R.color.success_green, null));
            btnPublishQuizBuilder.setText("Published");
            btnPublishQuizBuilder.setEnabled(false);
            btnSaveQuizDraft.setText("Unpublish");
            btnSaveQuizDraft.setEnabled(true);
        } else if ("closed".equalsIgnoreCase(quizStatus)) {
            tvBuilderStatus.setTextColor(getResources().getColor(R.color.warning_orange, null));
            btnPublishQuizBuilder.setText("Re-Publish");
            btnPublishQuizBuilder.setEnabled(true);
            btnSaveQuizDraft.setText("Done");
            btnSaveQuizDraft.setEnabled(true);
        } else if ("archived".equalsIgnoreCase(quizStatus)) {
            tvBuilderStatus.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            btnPublishQuizBuilder.setText("Archived");
            btnPublishQuizBuilder.setEnabled(false);
            btnSaveQuizDraft.setText("Restore Draft");
            btnSaveQuizDraft.setEnabled(true);
        } else {
            tvBuilderStatus.setTextColor(getResources().getColor(R.color.admin_purple_accent, null));
            btnPublishQuizBuilder.setText("Publish Quiz");
            btnPublishQuizBuilder.setEnabled(true);
            btnSaveQuizDraft.setText("Save Draft");
            btnSaveQuizDraft.setEnabled(true);
        }
    }

    private void showEmptyOrError(String title, String desc, boolean allowRetry) {
        rvQuizQuestions.setVisibility(View.GONE);
        layoutQuestionsEmpty.setVisibility(View.VISIBLE);
        tvQuestionsEmptyTitle.setText(title);
        tvQuestionsEmptyDesc.setText(desc);
        btnRetryBuilder.setVisibility(allowRetry ? View.VISIBLE : View.GONE);
    }

    private void showQuestionEditorDialog(@Nullable QuizQuestionItem existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_quiz_question_editor, null, false);
        RadioGroup rgQuestionType = form.findViewById(R.id.rgQuestionType);
        RadioButton rbTypeMc = form.findViewById(R.id.rbTypeMc);
        RadioButton rbTypeTf = form.findViewById(R.id.rbTypeTf);
        RadioButton rbTypeShort = form.findViewById(R.id.rbTypeShort);

        TextInputEditText etQuestionText = form.findViewById(R.id.etQuestionText);
        TextInputEditText etQuestionMarks = form.findViewById(R.id.etQuestionMarks);

        LinearLayout layoutMcSection = form.findViewById(R.id.layoutMcSection);
        LinearLayout layoutTfSection = form.findViewById(R.id.layoutTfSection);
        LinearLayout layoutShortSection = form.findViewById(R.id.layoutShortSection);

        TextInputEditText etOptionA = form.findViewById(R.id.etOptionA);
        TextInputEditText etOptionB = form.findViewById(R.id.etOptionB);
        TextInputEditText etOptionC = form.findViewById(R.id.etOptionC);
        TextInputEditText etOptionD = form.findViewById(R.id.etOptionD);
        RadioGroup rgMcCorrect = form.findViewById(R.id.rgMcCorrect);
        RadioButton rbCorrectA = form.findViewById(R.id.rbCorrectA);
        RadioButton rbCorrectB = form.findViewById(R.id.rbCorrectB);
        RadioButton rbCorrectC = form.findViewById(R.id.rbCorrectC);
        RadioButton rbCorrectD = form.findViewById(R.id.rbCorrectD);

        RadioButton rbCorrectTrue = form.findViewById(R.id.rbCorrectTrue);
        RadioButton rbCorrectFalse = form.findViewById(R.id.rbCorrectFalse);
        TextInputEditText etShortExpected = form.findViewById(R.id.etShortExpected);
        CheckBox cbQuestionRequired = form.findViewById(R.id.cbQuestionRequired);
        if (cbQuestionRequired != null) {
            cbQuestionRequired.setChecked(existing == null || existing.isRequired);
        }

        Runnable syncTypeVisibility = () -> {
            int checked = rgQuestionType.getCheckedRadioButtonId();
            layoutMcSection.setVisibility(checked == R.id.rbTypeMc ? View.VISIBLE : View.GONE);
            layoutTfSection.setVisibility(checked == R.id.rbTypeTf ? View.VISIBLE : View.GONE);
            layoutShortSection.setVisibility(checked == R.id.rbTypeShort ? View.VISIBLE : View.GONE);
        };
        rgQuestionType.setOnCheckedChangeListener((g, id) -> syncTypeVisibility.run());

        if (existing != null) {
            etQuestionText.setText(existing.questionText);
            etQuestionMarks.setText(AssessmentUiHelper.formatMarks(existing.marks));
            if ("true_false".equalsIgnoreCase(existing.questionType)) {
                rbTypeTf.setChecked(true);
                if ("false".equalsIgnoreCase(existing.correctAnswer)) rbCorrectFalse.setChecked(true);
                else rbCorrectTrue.setChecked(true);
            } else if ("short_answer".equalsIgnoreCase(existing.questionType)) {
                rbTypeShort.setChecked(true);
                String guidancePrefill = existing.gradingGuidance != null && !existing.gradingGuidance.trim().isEmpty()
                        ? existing.gradingGuidance : existing.correctAnswer;
                if (guidancePrefill != null) etShortExpected.setText(guidancePrefill);
            } else {
                rbTypeMc.setChecked(true);
                etOptionA.setText(existing.optionA != null ? existing.optionA : "");
                etOptionB.setText(existing.optionB != null ? existing.optionB : "");
                etOptionC.setText(existing.optionC != null ? existing.optionC : "");
                etOptionD.setText(existing.optionD != null ? existing.optionD : "");
                String corr = existing.correctAnswer != null ? existing.correctAnswer.toUpperCase(Locale.US) : "A";
                if ("B".equals(corr)) rbCorrectB.setChecked(true);
                else if ("C".equals(corr)) rbCorrectC.setChecked(true);
                else if ("D".equals(corr)) rbCorrectD.setChecked(true);
                else rbCorrectA.setChecked(true);
            }
        }
        syncTypeVisibility.run();

        AlertDialog dlg = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? "Add Question " + (questions.size() + 1) : "Edit Question")
                .setView(form)
                .setPositiveButton(existing == null ? "Add Question" : "Save Changes", null)
                .setNegativeButton("Cancel", null)
                .create();

        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qText = etQuestionText.getText() != null ? etQuestionText.getText().toString().trim() : "";
            String marksStr = etQuestionMarks.getText() != null ? etQuestionMarks.getText().toString().trim() : "";
            if (qText.isEmpty()) {
                etQuestionText.setError("Question text is required");
                return;
            }
            double marks;
            try {
                marks = Double.parseDouble(marksStr);
            } catch (NumberFormatException e) {
                etQuestionMarks.setError("Enter valid marks");
                return;
            }
            if (marks < 0.5 || marks > 100) {
                etQuestionMarks.setError("Marks must be between 0.5 and 100");
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("question_text", qText);
            body.addProperty("marks", marks);
            body.addProperty("is_required", cbQuestionRequired == null || cbQuestionRequired.isChecked());

            int typeId = rgQuestionType.getCheckedRadioButtonId();
            if (typeId == R.id.rbTypeMc) {
                String a = textOf(etOptionA);
                String b = textOf(etOptionB);
                String c = textOf(etOptionC);
                String dOpt = textOf(etOptionD);
                int filled = (!a.isEmpty() ? 1 : 0) + (!b.isEmpty() ? 1 : 0) + (!c.isEmpty() ? 1 : 0) + (!dOpt.isEmpty() ? 1 : 0);
                if (filled < 2) {
                    Toast.makeText(this, "Enter at least 2 answer options", Toast.LENGTH_SHORT).show();
                    return;
                }
                int corrId = rgMcCorrect.getCheckedRadioButtonId();
                String corrKey = "A";
                String corrVal = a;
                if (corrId == R.id.rbCorrectB) { corrKey = "B"; corrVal = b; }
                else if (corrId == R.id.rbCorrectC) { corrKey = "C"; corrVal = c; }
                else if (corrId == R.id.rbCorrectD) { corrKey = "D"; corrVal = dOpt; }

                if (corrVal.isEmpty()) {
                    Toast.makeText(this, "Correct answer (" + corrKey + ") cannot point to an empty option", Toast.LENGTH_SHORT).show();
                    return;
                }

                body.addProperty("question_type", "multiple_choice");
                if (!a.isEmpty()) body.addProperty("option_a", a);
                if (!b.isEmpty()) body.addProperty("option_b", b);
                if (!c.isEmpty()) body.addProperty("option_c", c);
                if (!dOpt.isEmpty()) body.addProperty("option_d", dOpt);
                body.addProperty("correct_answer", corrKey);

            } else if (typeId == R.id.rbTypeTf) {
                body.addProperty("question_type", "true_false");
                body.addProperty("correct_answer", rbCorrectFalse.isChecked() ? "false" : "true");

            } else {
                body.addProperty("question_type", "short_answer");
                String exp = textOf(etShortExpected);
                if (!exp.isEmpty()) {
                    body.addProperty("correct_answer", exp);
                    body.addProperty("grading_guidance", exp);
                }
            }

            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            Call<JsonObject> call = existing == null
                    ? ApiClient.getApiService(this).addTeacherQuizQuestion(quizId, body)
                    : ApiClient.getApiService(this).updateTeacherQuizQuestion(existing.id, body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful()) {
                        dlg.dismiss();
                        Toast.makeText(QuizBuilderActivity.this, existing == null ? "Question added!" : "Question updated!", Toast.LENGTH_SHORT).show();
                        loadQuiz();
                    } else {
                        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Failed to save question"), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(QuizBuilderActivity.this, "Network error saving question", Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dlg.show();
    }

    private String textOf(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void confirmDeleteQuestion(QuizQuestionItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Question?")
                .setMessage("Remove Question " + item.questionOrder + " from this quiz? Total marks will be recalculated automatically.")
                .setPositiveButton("Delete", (d, w) -> {
                    ApiClient.getApiService(this).deleteTeacherQuizQuestion(item.id).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(QuizBuilderActivity.this, "Question deleted", Toast.LENGTH_SHORT).show();
                                loadQuiz();
                            } else {
                                Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Cannot delete question"), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void duplicateQuestion(QuizQuestionItem item) {
        ApiClient.getApiService(this).duplicateTeacherQuizQuestion(item.id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(QuizBuilderActivity.this, "Question duplicated", Toast.LENGTH_SHORT).show();
                    loadQuiz();
                } else {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Could not duplicate question"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveQuestion(int fromPos, int toPos) {
        Collections.swap(questions, fromPos, toPos);
        adapter.notifyDataSetChanged();

        JsonArray orderArr = new JsonArray();
        for (QuizQuestionItem q : questions) {
            orderArr.add(q.id);
        }
        JsonObject body = new JsonObject();
        body.add("order", orderArr);

        ApiClient.getApiService(this).reorderTeacherQuizQuestions(quizId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    bindQuizPayload(response.body().getAsJsonObject("data"));
                } else {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Could not reorder"), Toast.LENGTH_SHORT).show();
                    loadQuiz();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error reordering", Toast.LENGTH_SHORT).show();
                loadQuiz();
            }
        });
    }

    private String toEditableDateTime(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        java.util.Date parsed = AssessmentUiHelper.parseIsoDate(iso);
        if (parsed != null) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(parsed);
        }
        return iso.trim();
    }

    private void showEditQuizInfoDialog() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 12);
        scroll.addView(container);

        TextInputLayout tilT = new TextInputLayout(this);
        tilT.setHint("Quiz Title");
        TextInputEditText edtT = new TextInputEditText(tilT.getContext());
        edtT.setText(quizTitle);
        tilT.addView(edtT);
        container.addView(tilT);

        TextInputLayout tilD = new TextInputLayout(this);
        tilD.setHint("Instructions / Description");
        TextInputEditText edtD = new TextInputEditText(tilD.getContext());
        edtD.setText(quizDescription);
        tilD.addView(edtD);
        container.addView(tilD);

        TextInputLayout tilDur = new TextInputLayout(this);
        tilDur.setHint("Duration (Minutes)");
        TextInputEditText edtDur = new TextInputEditText(tilDur.getContext());
        edtDur.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtDur.setText(String.valueOf(quizDuration));
        tilDur.addView(edtDur);
        container.addView(tilDur);

        TextInputLayout tilStart = new TextInputLayout(this);
        tilStart.setHint("Start Date & Time (Optional)");
        TextInputEditText edtStart = new TextInputEditText(tilStart.getContext());
        edtStart.setFocusable(false);
        edtStart.setInputType(InputType.TYPE_NULL);
        edtStart.setText(toEditableDateTime(quizStartAt));
        edtStart.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilStart, edtStart, false, true));
        tilStart.addView(edtStart);
        container.addView(tilStart);

        TextInputLayout tilEnd = new TextInputLayout(this);
        tilEnd.setHint("Close Deadline (Optional)");
        TextInputEditText edtEnd = new TextInputEditText(tilEnd.getContext());
        edtEnd.setFocusable(false);
        edtEnd.setInputType(InputType.TYPE_NULL);
        edtEnd.setText(toEditableDateTime(quizEndAt));
        edtEnd.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilEnd, edtEnd, false, true));
        tilEnd.addView(edtEnd);
        container.addView(tilEnd);

        TextView lblMaxAtt = new TextView(this);
        lblMaxAtt.setText("Max Attempts");
        lblMaxAtt.setPadding(0, 16, 0, 4);
        container.addView(lblMaxAtt);

        Spinner spMaxAtt = new Spinner(this);
        String[] attLabels = new String[]{"1 Attempt", "2 Attempts", "3 Attempts", "5 Attempts", "Unlimited"};
        Integer[] attValues = new Integer[]{1, 2, 3, 5, 0};
        spMaxAtt.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, attLabels));
        int attIdx = 0;
        if (quizMaxAttempts == null) attIdx = 4;
        else if (quizMaxAttempts == 2) attIdx = 1;
        else if (quizMaxAttempts == 3) attIdx = 2;
        else if (quizMaxAttempts == 5) attIdx = 3;
        spMaxAtt.setSelection(attIdx);
        container.addView(spMaxAtt);

        TextView lblScoreMethod = new TextView(this);
        lblScoreMethod.setText("Scoring Method (Multiple Attempts)");
        lblScoreMethod.setPadding(0, 16, 0, 4);
        container.addView(lblScoreMethod);

        Spinner spScoreMethod = new Spinner(this);
        String[] scoreLabels = new String[]{"Highest Score", "Latest Attempt", "Average Score"};
        String[] scoreValues = new String[]{"highest", "latest", "average"};
        spScoreMethod.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, scoreLabels));
        int scIdx = "latest".equalsIgnoreCase(quizScoringMethod) ? 1 : ("average".equalsIgnoreCase(quizScoringMethod) ? 2 : 0);
        spScoreMethod.setSelection(scIdx);
        container.addView(spScoreMethod);

        CheckBox cbAllowLate = new CheckBox(this);
        cbAllowLate.setText("Allow late submissions after deadline");
        cbAllowLate.setChecked(quizAllowLate);
        cbAllowLate.setPadding(0, 12, 0, 4);
        container.addView(cbAllowLate);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Quiz Info & Policies")
                .setView(scroll)
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = textOf(edtT);
                    if (newTitle.isEmpty()) {
                        Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int dur = quizDuration;
                    try { dur = Integer.parseInt(textOf(edtDur)); } catch (Exception ignored) {}
                    if (dur < 1 || dur > 300) {
                        Toast.makeText(this, "Duration must be 1..300 mins", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("title", newTitle);
                    body.addProperty("description", textOf(edtD));
                    body.addProperty("duration_minutes", dur);
                    String stStr = textOf(edtStart);
                    String enStr = textOf(edtEnd);
                    if (stStr.isEmpty()) body.add("start_at", com.google.gson.JsonNull.INSTANCE);
                    else body.addProperty("start_at", stStr);
                    if (enStr.isEmpty()) body.add("end_at", com.google.gson.JsonNull.INSTANCE);
                    else body.addProperty("end_at", enStr);
                    body.addProperty("max_attempts", attValues[spMaxAtt.getSelectedItemPosition()]);
                    body.addProperty("scoring_method", scoreValues[spScoreMethod.getSelectedItemPosition()]);
                    body.addProperty("allow_late_submission", cbAllowLate.isChecked());

                    ApiClient.getApiService(this).updateTeacherQuiz(quizId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                                bindQuizPayload(response.body().getAsJsonObject("data"));
                                Toast.makeText(QuizBuilderActivity.this, "Quiz details updated", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                            } else {
                                Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Update failed"), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void unpublishQuiz() {
        ApiClient.getApiService(this).unpublishTeacherQuiz(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    bindQuizPayload(response.body().getAsJsonObject("data"));
                    Toast.makeText(QuizBuilderActivity.this, "Quiz unpublished to Draft", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Could not unpublish quiz"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showManageQuizMenu() {
        List<String> options = new ArrayList<>();
        if ("published".equalsIgnoreCase(quizStatus)) {
            options.add("Unpublish Quiz");
        } else {
            options.add("Publish Quiz");
        }
        options.add("Reopen Quiz / Extend Deadline");
        options.add("Universal Reopen & Permissions");
        options.add("View Override & Reopen Audit Logs");
        options.add("Archive / Delete Quiz");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Manage Quiz")
                .setItems(options.toArray(new String[0]), (d, which) -> {
                    String choice = options.get(which);
                    if (choice.startsWith("Unpublish")) {
                        unpublishQuiz();
                    } else if (choice.startsWith("Publish")) {
                        publishQuiz();
                    } else if (choice.startsWith("Reopen")) {
                        showReopenQuizDialog();
                    } else if (choice.startsWith("Universal Reopen")) {
                        android.content.Intent intent = new android.content.Intent(this, AssessmentReopenManagerActivity.class);
                        intent.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_TYPE, "quiz");
                        intent.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_ID, quizId);
                        intent.putExtra(AssessmentReopenManagerActivity.EXTRA_ASSESSMENT_TITLE, quizTitle);
                        startActivity(intent);
                    } else if (choice.startsWith("View Override")) {
                        fetchAndShowAuditLogs();
                    } else if (choice.startsWith("Archive")) {
                        confirmArchiveQuiz();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReopenQuizDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

        TextInputLayout tilNewEnd = new TextInputLayout(this);
        tilNewEnd.setHint("New Close Date & Time (Optional)");
        TextInputEditText edtNewEnd = new TextInputEditText(tilNewEnd.getContext());
        edtNewEnd.setFocusable(false);
        edtNewEnd.setInputType(InputType.TYPE_NULL);
        edtNewEnd.setText(toEditableDateTime(quizEndAt));
        edtNewEnd.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilNewEnd, edtNewEnd, false, true));
        tilNewEnd.addView(edtNewEnd);
        container.addView(tilNewEnd);

        CheckBox cbLate = new CheckBox(this);
        cbLate.setText("Allow late submissions");
        cbLate.setChecked(true);
        cbLate.setPadding(0, 12, 0, 8);
        container.addView(cbLate);

        TextInputLayout tilReason = new TextInputLayout(this);
        tilReason.setHint("Reason for Reopening (Logged in Audit)");
        TextInputEditText edtReason = new TextInputEditText(tilReason.getContext());
        tilReason.addView(edtReason);
        container.addView(tilReason);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Reopen Quiz")
                .setView(container)
                .setPositiveButton("Reopen Quiz", (d, w) -> {
                    JsonObject body = new JsonObject();
                    String endStr = textOf(edtNewEnd);
                    if (!endStr.isEmpty()) body.addProperty("end_at", endStr);
                    body.addProperty("allow_late_submission", cbLate.isChecked());
                    String reason = textOf(edtReason);
                    if (!reason.isEmpty()) body.addProperty("reason", reason);

                    ApiClient.getApiService(this).reopenTeacherQuiz(quizId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                                bindQuizPayload(response.body().getAsJsonObject("data"));
                                Toast.makeText(QuizBuilderActivity.this, "Quiz reopened!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                            } else {
                                Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Could not reopen quiz"), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmArchiveQuiz() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(attemptsCount > 0 ? "Archive Quiz?" : "Delete Quiz?")
                .setMessage(attemptsCount > 0
                        ? "This quiz has student attempts. Archiving hides it from active student lists while preserving all existing attempt history and scores."
                        : "Permanently delete this quiz? This cannot be undone.")
                .setPositiveButton(attemptsCount > 0 ? "Archive" : "Delete", (d, w) -> {
                    ApiClient.getApiService(this).deleteTeacherQuiz(quizId).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(QuizBuilderActivity.this, attemptsCount > 0 ? "Quiz archived" : "Quiz deleted", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Action failed"), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchAndShowAuditLogs() {
        ApiClient.getApiService(this).getTeacherQuizAuditLogs(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Failed to load audit logs"), Toast.LENGTH_SHORT).show();
                    return;
                }
                JsonArray arr = AssessmentUiHelper.extractDataArray(response.body());
                if (arr.size() == 0) {
                    new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                            .setTitle("Override & Reopen Audit Logs")
                            .setMessage("No reopen or student override events logged yet.")
                            .setPositiveButton("Close", null)
                            .show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject log = el.getAsJsonObject();
                    String action = AssessmentUiHelper.optString(log, "action", "action");
                    String actor = AssessmentUiHelper.optString(log, "acted_by_name", "Teacher");
                    String stName = AssessmentUiHelper.optString(log, "student_name", "");
                    String reason = AssessmentUiHelper.optString(log, "reason", "");
                    String at = AssessmentUiHelper.optString(log, "created_at", "");
                    sb.append("• ").append(action.toUpperCase(Locale.US));
                    if (!stName.isEmpty()) sb.append(" — ").append(stName);
                    sb.append("\n  By: ").append(actor);
                    if (!at.isEmpty()) sb.append(" (").append(AssessmentUiHelper.formatDateTime(at)).append(")");
                    if (!reason.isEmpty()) sb.append("\n  Reason: ").append(reason);
                    sb.append("\n\n");
                }
                new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                        .setTitle("Override & Reopen Audit Logs")
                        .setMessage(sb.toString().trim())
                        .setPositiveButton("Close", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error loading audit logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStudentOverridesDialog() {
        ApiClient.getApiService(this).getTeacherQuizStudents(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Failed to load students"), Toast.LENGTH_SHORT).show();
                    return;
                }
                JsonArray arr = AssessmentUiHelper.extractDataArray(response.body());
                if (arr.size() == 0) {
                    new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                            .setTitle("Student Retakes & Overrides")
                            .setMessage("No enrolled students found for this quiz.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                List<JsonObject> students = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject st = el.getAsJsonObject();
                    students.add(st);
                    String name = AssessmentUiHelper.optString(st, "name", "Student");
                    int used = AssessmentUiHelper.optInt(st, "attempts_used", 0);
                    String maxStr = (st.has("max_attempts") && !st.get("max_attempts").isJsonNull())
                            ? String.valueOf(AssessmentUiHelper.optInt(st, "max_attempts", 1)) : "∞";
                    boolean hasOverride = AssessmentUiHelper.optBoolean(st, "has_override", false);
                    boolean pending = AssessmentUiHelper.optBoolean(st, "pending_manual_grading", false);
                    String scStr = pending ? "Pending Grading"
                            : (st.has("effective_score") && !st.get("effective_score").isJsonNull()
                            ? AssessmentUiHelper.formatMarks(st.get("effective_score").getAsDouble()) + " pts"
                            : "No score");
                    labels.add(name + " — " + used + "/" + maxStr + " • " + scStr + (hasOverride ? " [Override]" : ""));
                }

                new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                        .setTitle("Select Student for Retake / Override")
                        .setItems(labels.toArray(new String[0]), (d, which) -> showSingleStudentOverrideDialog(students.get(which)))
                        .setNeutralButton("Audit Logs", (d, w) -> fetchAndShowAuditLogs())
                        .setNegativeButton("Close", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error loading students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSingleStudentOverrideDialog(JsonObject student) {
        int studentId = AssessmentUiHelper.optInt(student, "student_id", 0);
        String studentName = AssessmentUiHelper.optString(student, "name", "Student");
        boolean hasOverride = AssessmentUiHelper.optBoolean(student, "has_override", false);
        JsonObject currentOv = (student.has("override") && student.get("override").isJsonObject())
                ? student.getAsJsonObject("override") : null;

        ScrollView scroll = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);
        scroll.addView(container);

        TextView lblExtra = new TextView(this);
        lblExtra.setText("Grant Additional Extra Attempt(s)");
        container.addView(lblExtra);

        Spinner spExtra = new Spinner(this);
        String[] extraLabels = new String[]{"0 (Keep current)", "+1 Extra Attempt", "+2 Extra Attempts", "+3 Extra Attempts", "+5 Extra Attempts"};
        int[] extraVals = new int[]{0, 1, 2, 3, 5};
        spExtra.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, extraLabels));
        container.addView(spExtra);

        TextInputLayout tilExtEnd = new TextInputLayout(this);
        tilExtEnd.setHint("Extended Student Deadline (Optional)");
        TextInputEditText edtExtEnd = new TextInputEditText(tilExtEnd.getContext());
        edtExtEnd.setFocusable(false);
        edtExtEnd.setInputType(InputType.TYPE_NULL);
        if (currentOv != null) {
            edtExtEnd.setText(toEditableDateTime(AssessmentUiHelper.optString(currentOv, "extended_end_at", "")));
        }
        edtExtEnd.setOnClickListener(v -> DatePickerHelper.showDateTimePicker(this, tilExtEnd, edtExtEnd, false, true));
        tilExtEnd.addView(edtExtEnd);
        container.addView(tilExtEnd);

        CheckBox cbLate = new CheckBox(this);
        cbLate.setText("Allow late submission for this student");
        cbLate.setChecked(currentOv != null && AssessmentUiHelper.optBoolean(currentOv, "allow_late_submission", false));
        container.addView(cbLate);

        CheckBox cbReset = new CheckBox(this);
        cbReset.setText("Soft-reset previous attempts (archive prior attempts as reset)");
        cbReset.setChecked(false);
        container.addView(cbReset);

        TextInputLayout tilReason = new TextInputLayout(this);
        tilReason.setHint("Override Reason (Audit Logged)");
        TextInputEditText edtReason = new TextInputEditText(tilReason.getContext());
        if (currentOv != null) {
            edtReason.setText(AssessmentUiHelper.optString(currentOv, "reason", ""));
        }
        tilReason.addView(edtReason);
        container.addView(tilReason);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Override: " + studentName)
                .setView(scroll)
                .setPositiveButton("Apply Override", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("student_id", studentId);
                    int grantExtra = extraVals[spExtra.getSelectedItemPosition()];
                    if (grantExtra > 0) body.addProperty("grant_extra_attempts", grantExtra);
                    String extEnd = textOf(edtExtEnd);
                    if (!extEnd.isEmpty()) body.addProperty("extended_end_at", extEnd);
                    body.addProperty("allow_late_submission", cbLate.isChecked());
                    body.addProperty("reset_attempts", cbReset.isChecked());
                    String reason = textOf(edtReason);
                    if (!reason.isEmpty()) body.addProperty("reason", reason);

                    ApiClient.getApiService(this).grantTeacherQuizOverride(quizId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(QuizBuilderActivity.this, "Student override applied!", Toast.LENGTH_SHORT).show();
                                loadQuiz();
                            } else {
                                Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Failed to save override"), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null);

        if (hasOverride) {
            builder.setNeutralButton("Revoke Override", (d, w) -> {
                ApiClient.getApiService(this).revokeTeacherQuizOverride(quizId, studentId).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(QuizBuilderActivity.this, "Override revoked", Toast.LENGTH_SHORT).show();
                            loadQuiz();
                        } else {
                            Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Could not revoke override"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        builder.show();
    }

    private void saveQuizStatus(String targetStatus) {
        if ("closed".equalsIgnoreCase(quizStatus) && "draft".equalsIgnoreCase(targetStatus)) {
            finish();
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("status", targetStatus);
        ApiClient.getApiService(this).updateTeacherQuiz(quizId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    bindQuizPayload(response.body().getAsJsonObject("data"));
                    Toast.makeText(QuizBuilderActivity.this, "Saved as draft", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Could not save draft"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publishQuiz() {
        if (questions.isEmpty()) {
            Toast.makeText(this, "Add at least 1 question before publishing.", Toast.LENGTH_LONG).show();
            return;
        }
        btnPublishQuizBuilder.setEnabled(false);
        ApiClient.getApiService(this).publishTeacherQuiz(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    bindQuizPayload(response.body().getAsJsonObject("data"));
                    Toast.makeText(QuizBuilderActivity.this, R.string.quiz_published_to_students, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                } else {
                    btnPublishQuizBuilder.setEnabled(true);
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Cannot publish quiz"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                btnPublishQuizBuilder.setEnabled(true);
                Toast.makeText(QuizBuilderActivity.this, "Network error publishing quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* ---------------- Quiz Attempts & Short-Answer Manual Grading ---------------- */

    private void fetchAndShowAttempts() {
        Intent intent = new Intent(this, TeacherQuizAttemptsActivity.class);
        intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_QUIZ_ID, quizId);
        intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_QUIZ_TITLE, quizTitle);
        if (tvBuilderCourseContext != null && tvBuilderCourseContext.getText() != null) {
            intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_COURSE_CONTEXT, tvBuilderCourseContext.getText().toString());
        }
        intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_FILTER_PENDING, pendingGradingCount > 0);
        startActivity(intent);
    }
}
