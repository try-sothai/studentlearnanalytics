package com.social7.studentlearnanalytics.student.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TakeQuizActivity extends AppCompatActivity {

    private static final String TAG = "TakeQuizActivity";

    private String quizId = "1";
    private String attemptId = null;
    private SessionManager sessionManager;

    private TextView tvHeaderTitle, tvTimer;
    private ProgressBar pbLoading;
    private ScrollView scrollQuizContent;

    // Screen A: Instructions
    private LinearLayout layoutInstructions;
    private TextView tvInstructionsTitle, tvCourseInfo, tvQuizMetaDetails, tvInstructionsBody;
    private MaterialButton btnStartQuiz;

    // Screen B: Taking
    private LinearLayout layoutTaking;
    private TextView tvQuestionProgress, tvQuestionText;
    private RadioGroup rgAnswers;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private MaterialButton btnPrevQuestion, btnNextQuestion, btnSubmitQuiz;

    // Screen C: Result
    private LinearLayout layoutResult;
    private TextView tvResultTitle, tvResultSubtitle;
    private MaterialButton btnReturnHub;

    private CountDownTimer countDownTimer;
    private final List<JsonObject> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private final List<String> selectedAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_quiz);

        sessionManager = new SessionManager(this);

        if (getIntent() != null && getIntent().hasExtra("quiz_id")) {
            quizId = getIntent().getStringExtra("quiz_id");
        }

        initViews();
        fetchQuizDetails();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> handleBackNavigation());

        tvHeaderTitle = findViewById(R.id.tvQuizHeaderTitle);
        tvTimer = findViewById(R.id.tvTimer);
        pbLoading = findViewById(R.id.pbLoading);
        scrollQuizContent = findViewById(R.id.scrollQuizContent);

        // Screen A
        layoutInstructions = findViewById(R.id.layoutInstructions);
        tvInstructionsTitle = findViewById(R.id.tvInstructionsTitle);
        tvCourseInfo = findViewById(R.id.tvCourseInfo);
        tvQuizMetaDetails = findViewById(R.id.tvQuizMetaDetails);
        tvInstructionsBody = findViewById(R.id.tvInstructionsBody);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnStartQuiz.setOnClickListener(v -> startQuizAttempt());

        // Screen B
        layoutTaking = findViewById(R.id.layoutTaking);
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbOptionA = findViewById(R.id.rbOptionA);
        rbOptionB = findViewById(R.id.rbOptionB);
        rbOptionC = findViewById(R.id.rbOptionC);
        rbOptionD = findViewById(R.id.rbOptionD);

        btnPrevQuestion = findViewById(R.id.btnPrevQuestion);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);

        btnPrevQuestion.setOnClickListener(v -> navigateQuestion(-1));
        btnNextQuestion.setOnClickListener(v -> navigateQuestion(1));
        btnSubmitQuiz.setOnClickListener(v -> confirmAndSubmitQuiz());

        rgAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            if (currentQuestionIndex < selectedAnswers.size()) {
                String ans = "";
                if (checkedId == R.id.rbOptionA) ans = "A";
                else if (checkedId == R.id.rbOptionB) ans = "B";
                else if (checkedId == R.id.rbOptionC) ans = "C";
                else if (checkedId == R.id.rbOptionD) ans = "D";

                selectedAnswers.set(currentQuestionIndex, ans);
                saveAnswerToBackend(currentQuestionIndex, ans);
            }
        });

        // Screen C
        layoutResult = findViewById(R.id.layoutResult);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultSubtitle = findViewById(R.id.tvResultSubtitle);
        btnReturnHub = findViewById(R.id.btnReturnHub);
        btnReturnHub.setOnClickListener(v -> finish());
    }

    private void fetchQuizDetails() {
        pbLoading.setVisibility(View.VISIBLE);
        scrollQuizContent.setVisibility(View.GONE);

        ApiClient.getApiService(this).getQuizDetails(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);
                scrollQuizContent.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject data = root.has("data") ? root.getAsJsonObject("data") : root;

                    bindQuizInstructions(data);
                } else {
                    Toast.makeText(TakeQuizActivity.this, "Failed to load quiz details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                scrollQuizContent.setVisibility(View.VISIBLE);
                Toast.makeText(TakeQuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindQuizInstructions(JsonObject quizData) {
        String title = quizData.has("title") && !quizData.get("title").isJsonNull()
                ? quizData.get("title").getAsString() : "Quiz Assessment";
        tvInstructionsTitle.setText(title);
        tvHeaderTitle.setText(title);

        String course = "Course";
        if (quizData.has("course") && !quizData.get("course").isJsonNull()) {
            JsonObject cObj = quizData.getAsJsonObject("course");
            if (cObj.has("course_name")) course = cObj.get("course_name").getAsString();
        }
        tvCourseInfo.setText(course);

        int totalScore = quizData.has("total_score") && !quizData.get("total_score").isJsonNull()
                ? quizData.get("total_score").getAsInt() : 100;
        int duration = quizData.has("duration_minutes") && !quizData.get("duration_minutes").isJsonNull()
                ? quizData.get("duration_minutes").getAsInt() : 30;

        questions.clear();
        selectedAnswers.clear();
        if (quizData.has("questions") && quizData.get("questions").isJsonArray()) {
            JsonArray qArray = quizData.getAsJsonArray("questions");
            for (JsonElement el : qArray) {
                if (el.isJsonObject()) {
                    questions.add(el.getAsJsonObject());
                    selectedAnswers.add("");
                }
            }
        }

        tvQuizMetaDetails.setText(String.format(Locale.US, "Questions: %d • Total Marks: %d • Time Limit: %d Mins",
                questions.size(), totalScore, duration));

        if (quizData.has("description") && !quizData.get("description").isJsonNull()) {
            tvInstructionsBody.setText(quizData.get("description").getAsString());
        }
    }

    private void startQuizAttempt() {
        pbLoading.setVisibility(View.VISIBLE);

        JsonObject body = new JsonObject();
        body.addProperty("quiz_id", Integer.parseInt(quizId));
        String userId = sessionManager.getUserId();
        try {
            body.addProperty("student_id", Integer.parseInt(userId));
        } catch (Exception ignored) {
            body.addProperty("student_id", 1);
        }

        ApiClient.getApiService(this).startQuizAttempt(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject data = root.has("data") ? root.getAsJsonObject("data") : root;
                    if (data.has("id")) {
                        attemptId = data.get("id").getAsString();
                    }

                    showTakingScreen();
                } else {
                    // Fallback to taking screen if attempt was already created
                    attemptId = "1";
                    showTakingScreen();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                attemptId = "1";
                showTakingScreen();
            }
        });
    }

    private void showTakingScreen() {
        layoutInstructions.setVisibility(View.GONE);
        layoutTaking.setVisibility(View.VISIBLE);
        tvTimer.setVisibility(View.VISIBLE);

        startTimer(30 * 60 * 1000); // 30 mins
        currentQuestionIndex = 0;
        displayQuestion(currentQuestionIndex);
    }

    private void displayQuestion(int index) {
        if (questions.isEmpty()) {
            tvQuestionText.setText("No questions available for this quiz.");
            return;
        }

        tvQuestionProgress.setText(String.format(Locale.US, "Question %d of %d", index + 1, questions.size()));

        JsonObject q = questions.get(index);
        String qText = q.has("question_text") && !q.get("question_text").isJsonNull()
                ? q.get("question_text").getAsString() : "Question " + (index + 1);
        tvQuestionText.setText(qText);

        rbOptionA.setText(q.has("option_a") && !q.get("option_a").isJsonNull() ? "A: " + q.get("option_a").getAsString() : "Option A");
        rbOptionB.setText(q.has("option_b") && !q.get("option_b").isJsonNull() ? "B: " + q.get("option_b").getAsString() : "Option B");
        rbOptionC.setText(q.has("option_c") && !q.get("option_c").isJsonNull() ? "C: " + q.get("option_c").getAsString() : "Option C");
        rbOptionD.setText(q.has("option_d") && !q.get("option_d").isJsonNull() ? "D: " + q.get("option_d").getAsString() : "Option D");

        rgAnswers.clearCheck();
        String currentSelection = selectedAnswers.get(index);
        if ("A".equals(currentSelection)) rbOptionA.setChecked(true);
        else if ("B".equals(currentSelection)) rbOptionB.setChecked(true);
        else if ("C".equals(currentSelection)) rbOptionC.setChecked(true);
        else if ("D".equals(currentSelection)) rbOptionD.setChecked(true);

        btnPrevQuestion.setEnabled(index > 0);
        btnNextQuestion.setEnabled(index < questions.size() - 1);
    }

    private void navigateQuestion(int delta) {
        int target = currentQuestionIndex + delta;
        if (target >= 0 && target < questions.size()) {
            currentQuestionIndex = target;
            displayQuestion(currentQuestionIndex);
        }
    }

    private void saveAnswerToBackend(int qIndex, String answerStr) {
        if (attemptId == null || qIndex >= questions.size() || answerStr.isEmpty()) return;

        JsonObject q = questions.get(qIndex);
        if (!q.has("id")) return;

        JsonObject body = new JsonObject();
        body.addProperty("quiz_attempt_id", Integer.parseInt(attemptId));
        body.addProperty("question_id", q.get("id").getAsInt());
        body.addProperty("answer", answerStr);

        ApiClient.getApiService(this).submitQuizAnswer(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.d(TAG, "Answer saved for question " + qIndex);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Error saving answer: " + t.getMessage());
            }
        });
    }

    private void confirmAndSubmitQuiz() {
        new AlertDialog.Builder(this)
                .setTitle("Submit Quiz")
                .setMessage("Are you sure you want to submit your quiz?")
                .setPositiveButton("Submit", (dialog, which) -> submitQuizFinal())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitQuizFinal() {
        pbLoading.setVisibility(View.VISIBLE);

        if (countDownTimer != null) countDownTimer.cancel();

        JsonObject body = new JsonObject();
        body.addProperty("status", "completed");

        String targetAttemptId = attemptId != null ? attemptId : "1";

        ApiClient.getApiService(this).updateQuizAttempt(targetAttemptId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);
                showResultScreen();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                showResultScreen();
            }
        });
    }

    private void showResultScreen() {
        layoutTaking.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
    }

    private void startTimer(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mins = millisUntilFinished / 60000;
                long secs = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                Toast.makeText(TakeQuizActivity.this, "Time expired! Submitting quiz...", Toast.LENGTH_LONG).show();
                submitQuizFinal();
            }
        }.start();
    }

    private void handleBackNavigation() {
        if (layoutTaking.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(this)
                    .setTitle("Leave Quiz?")
                    .setMessage("Your progress is saved, but the timer will continue running.")
                    .setPositiveButton("Leave", (dialog, which) -> finish())
                    .setNegativeButton("Stay", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
