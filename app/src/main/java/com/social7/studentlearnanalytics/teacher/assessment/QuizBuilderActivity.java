package com.social7.studentlearnanalytics.teacher.assessment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private int attemptsCount = 0;

    private TextView tvBuilderCourseContext, tvBuilderStatus, tvBuilderTitle;
    private TextView tvTotalQuestions, tvTotalMarks, tvQuizDuration, tvQuestionsLockedBanner;
    private TextView tvQuestionsEmptyTitle, tvQuestionsEmptyDesc;
    private MaterialButton btnEditQuizInfo, btnViewAttempts, btnAddQuestion, btnSaveQuizDraft, btnPublishQuizBuilder, btnRetryBuilder;
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
        tvQuestionsLockedBanner = findViewById(R.id.tvQuestionsLockedBanner);

        btnEditQuizInfo = findViewById(R.id.btnEditQuizInfo);
        btnViewAttempts = findViewById(R.id.btnViewAttempts);
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
        btnRetryBuilder.setOnClickListener(v -> loadQuiz());

        btnSaveQuizDraft.setOnClickListener(v -> saveQuizStatus("draft"));
        btnPublishQuizBuilder.setOnClickListener(v -> publishQuiz());

        loadQuiz();
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
        attemptsCount = AssessmentUiHelper.optInt(data, "attempts_count", 0);

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
        btnViewAttempts.setText("Attempts (" + attemptsCount + ")");

        boolean locked = attemptsCount > 0;
        tvQuestionsLockedBanner.setVisibility(locked ? View.VISIBLE : View.GONE);
        btnAddQuestion.setEnabled(!locked);
        btnAddQuestion.setAlpha(locked ? 0.5f : 1f);
        adapter.setLocked(locked);

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
            btnSaveQuizDraft.setText("Revert to Draft");
        } else if ("closed".equalsIgnoreCase(quizStatus)) {
            tvBuilderStatus.setTextColor(getResources().getColor(R.color.warning_orange, null));
            btnPublishQuizBuilder.setText("Re-Publish");
            btnPublishQuizBuilder.setEnabled(true);
            btnSaveQuizDraft.setText("Done");
        } else {
            tvBuilderStatus.setTextColor(getResources().getColor(R.color.admin_purple_accent, null));
            btnPublishQuizBuilder.setText("Publish Quiz");
            btnPublishQuizBuilder.setEnabled(true);
            btnSaveQuizDraft.setText("Save Draft");
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
        if (attemptsCount > 0) {
            Toast.makeText(this, "Questions cannot be modified after students started attempting.", Toast.LENGTH_SHORT).show();
            return;
        }

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
                if (existing.correctAnswer != null) etShortExpected.setText(existing.correctAnswer);
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
                if (!exp.isEmpty()) body.addProperty("correct_answer", exp);
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

    private void showEditQuizInfoDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 8);

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
        edtDur.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtDur.setText(String.valueOf(quizDuration));
        tilDur.addView(edtDur);
        container.addView(tilDur);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Quiz Details")
                .setView(container)
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

                    ApiClient.getApiService(this).updateTeacherQuiz(quizId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                                bindQuizPayload(response.body().getAsJsonObject("data"));
                                Toast.makeText(QuizBuilderActivity.this, "Quiz details updated", Toast.LENGTH_SHORT).show();
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
        ApiClient.getApiService(this).getTeacherQuizAttempts(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Failed to load attempts"), Toast.LENGTH_SHORT).show();
                    return;
                }
                JsonArray arr = AssessmentUiHelper.extractDataArray(response.body());
                if (arr.size() == 0) {
                    new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                            .setTitle("Student Attempts")
                            .setMessage("No students have attempted this quiz yet.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                List<Integer> ids = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    int aId = AssessmentUiHelper.optInt(o, "id", 0);
                    String name = AssessmentUiHelper.optString(o, "student_name", "Student");
                    String st = AssessmentUiHelper.optString(o, "status", "");
                    boolean pending = AssessmentUiHelper.optBoolean(o, "pending_manual_grading", false);
                    String sc = o.has("score") && !o.get("score").isJsonNull()
                            ? AssessmentUiHelper.formatMarks(o.get("score").getAsDouble()) + " pts"
                            : "Pending";
                    ids.add(aId);
                    labels.add(name + " — " + (pending ? "Needs Grading" : (st + " (" + sc + ")")));
                }
                new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                        .setTitle("Quiz Attempts (Tap to Review/Grade)")
                        .setItems(labels.toArray(new String[0]), (d, which) -> openAttemptForGrading(ids.get(which)))
                        .setNegativeButton("Close", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error loading attempts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAttemptForGrading(int attemptId) {
        ApiClient.getApiService(this).getTeacherQuizAttemptDetail(attemptId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Failed to load attempt"), Toast.LENGTH_SHORT).show();
                    return;
                }
                JsonObject data = response.body().getAsJsonObject("data");
                String studentName = AssessmentUiHelper.optString(data, "student_name", "Student");
                JsonArray qs = data.has("questions") && data.get("questions").isJsonArray()
                        ? data.getAsJsonArray("questions") : new JsonArray();

                LinearLayout list = new LinearLayout(QuizBuilderActivity.this);
                list.setOrientation(LinearLayout.VERTICAL);
                list.setPadding(40, 16, 40, 16);

                for (JsonElement el : qs) {
                    if (!el.isJsonObject()) continue;
                    JsonObject q = el.getAsJsonObject();
                    int num = AssessmentUiHelper.optInt(q, "number", 1);
                    String qText = AssessmentUiHelper.optString(q, "question_text", "");
                    String stuAns = AssessmentUiHelper.optString(q, "student_answer", "(No answer)");
                    double maxM = AssessmentUiHelper.optDouble(q, "marks", 1);
                    int answerId = AssessmentUiHelper.optInt(q, "answer_id", 0);
                    boolean needsManual = AssessmentUiHelper.optBoolean(q, "needs_manual_grading", false);
                    String obtained = q.has("marks_obtained") && !q.get("marks_obtained").isJsonNull()
                            ? AssessmentUiHelper.formatMarks(q.get("marks_obtained").getAsDouble()) : "Pending";

                    TextView tv = new TextView(QuizBuilderActivity.this);
                    tv.setText("Q" + num + ": " + qText + "\nAnswer: " + stuAns + "\nMarks: " + obtained + " / " + AssessmentUiHelper.formatMarks(maxM));
                    tv.setPadding(0, 10, 0, 6);
                    list.addView(tv);

                    if (answerId > 0 && (needsManual || "short_answer".equalsIgnoreCase(AssessmentUiHelper.optString(q, "question_type", "")))) {
                        MaterialButton btnGrade = new MaterialButton(QuizBuilderActivity.this);
                        btnGrade.setText(needsManual ? "Grade Short Answer" : "Adjust Marks");
                        btnGrade.setAllCaps(false);
                        btnGrade.setOnClickListener(v -> promptGradeAnswer(answerId, maxM, () -> openAttemptForGrading(attemptId)));
                        list.addView(btnGrade);
                    }
                }

                android.widget.ScrollView scroll = new android.widget.ScrollView(QuizBuilderActivity.this);
                scroll.addView(list);

                new MaterialAlertDialogBuilder(QuizBuilderActivity.this)
                        .setTitle(studentName + " — Attempt Review")
                        .setView(scroll)
                        .setPositiveButton("Done", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(QuizBuilderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promptGradeAnswer(int answerId, double maxMarks, Runnable onGraded) {
        TextInputLayout til = new TextInputLayout(this);
        til.setHint("Marks (0 - " + AssessmentUiHelper.formatMarks(maxMarks) + ")");
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        til.addView(et);
        til.setPadding(48, 16, 48, 0);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Grade Short Answer")
                .setView(til)
                .setPositiveButton("Save Grade", (d, w) -> {
                    double m;
                    try { m = Double.parseDouble(textOf(et)); } catch (Exception e) {
                        Toast.makeText(this, "Enter valid marks", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (m < 0 || m > maxMarks) {
                        Toast.makeText(this, "Marks must be between 0 and " + AssessmentUiHelper.formatMarks(maxMarks), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("marks_obtained", m);
                    ApiClient.getApiService(this).gradeTeacherQuizAnswer(answerId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(QuizBuilderActivity.this, "Answer graded!", Toast.LENGTH_SHORT).show();
                                onGraded.run();
                            } else {
                                Toast.makeText(QuizBuilderActivity.this, AssessmentUiHelper.extractErrorMessage(response, "Grading failed"), Toast.LENGTH_SHORT).show();
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
}
