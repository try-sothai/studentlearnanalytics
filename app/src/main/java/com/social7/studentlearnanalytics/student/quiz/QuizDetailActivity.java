package com.social7.studentlearnanalytics.student.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.AssessmentAdapter;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizDetailActivity extends AppCompatActivity {

    private static final String TAG = "QuizDetail";

    private RecyclerView recyclerView;
    private AssessmentAdapter adapter;
    private final List<Assessment> quizList = new ArrayList<>();
    private final List<Assessment> allQuizList = new ArrayList<>();
    private Spinner semesterSpinner;
    private TextView averageScoreText;
    private ProgressBar loadingProgressBar;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;

    private final List<String> semesterFilterList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private int quizLoadGeneration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail);

        initViews();
        setupSemesterSpinner();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchQuizzes();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.quiz_recycler_view);
        semesterSpinner = findViewById(R.id.semester_spinner);
        averageScoreText = findViewById(R.id.average_score_text);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        emptyStateText = findViewById(R.id.empty_state_text);
    }

    private void setupSemesterSpinner() {
        if (semesterSpinner == null) return;

        semesterFilterList.clear();
        semesterFilterList.add("All Semesters");

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesterFilterList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semesterSpinner.setAdapter(spinnerAdapter);

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = semesterFilterList.get(position);
                filterQuizzes(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AssessmentAdapter(quizList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchQuizzes() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (averageScoreText != null) {
            averageScoreText.setText("--");
        }

        final int generation = ++quizLoadGeneration;
        ApiService apiService = ApiClient.getApiService(this);
        Call<JsonObject> call = apiService.getStudentQuizzes(null);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed() || generation != quizLoadGeneration) return;
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        allQuizList.clear();
                        JsonObject body = response.body();
                        JsonArray dataArray = body.has("data") && body.get("data").isJsonArray()
                                ? body.getAsJsonArray("data")
                                : (body.has("quizzes") && body.get("quizzes").isJsonArray()
                                ? body.getAsJsonArray("quizzes") : new JsonArray());
                        Set<String> uniqueSemesters = new LinkedHashSet<>();

                        if (dataArray != null && dataArray.size() > 0) {
                            for (JsonElement element : dataArray) {
                                if (!element.isJsonObject()) continue;
                                JsonObject obj = element.getAsJsonObject();

                                String rawDate = "";
                                for (String k : new String[]{"extended_deadline", "effective_end_at", "start_at", "quiz_date", "end_at", "created_at"}) {
                                    if (obj.has(k) && !obj.get(k).isJsonNull()) {
                                        String v = obj.get(k).getAsString();
                                        if (v != null && !v.trim().isEmpty()) {
                                            rawDate = v;
                                            break;
                                        }
                                    }
                                }
                                Assessment item = QuizScoreUiHelper.parseStudentQuizItem(obj, formatDate(rawDate));
                                if (item.getSemester() != null && !item.getSemester().trim().isEmpty()) {
                                    uniqueSemesters.add(item.getSemester().trim());
                                }
                                allQuizList.add(item);
                            }
                        }

                        updateSemesterSpinnerOptions(uniqueSemesters);

                        String currentSemester = (semesterSpinner != null && semesterSpinner.getSelectedItem() != null)
                                ? semesterSpinner.getSelectedItem().toString()
                                : "All Semesters";
                        filterQuizzes(currentSemester);
                        enrichPendingQuizzesFromReview(generation);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing quizzes JSON", e);
                        Toast.makeText(QuizDetailActivity.this, "Error parsing quiz data", Toast.LENGTH_SHORT).show();
                        showEmptyState("Error loading quizzes");
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(QuizDetailActivity.this, "Session expired, please login again", Toast.LENGTH_LONG).show();
                    showEmptyState("Session expired. Please log in again.");
                } else {
                    Log.w(TAG, "Non-success response: HTTP " + response.code());
                    Toast.makeText(QuizDetailActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
                    showEmptyState("No quizzes available");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Network failure fetching quizzes", t);
                Toast.makeText(QuizDetailActivity.this, "Network error: Unable to load quizzes", Toast.LENGTH_SHORT).show();
                showEmptyState("Network error. Please check connection.");
            }
        });
    }

    private void updateSemesterSpinnerOptions(Set<String> uniqueSemesters) {
        String currentSelection = (semesterSpinner != null && semesterSpinner.getSelectedItem() != null)
                ? semesterSpinner.getSelectedItem().toString()
                : "All Semesters";

        semesterFilterList.clear();
        semesterFilterList.add("All Semesters");
        semesterFilterList.addAll(uniqueSemesters);

        if (spinnerAdapter != null) {
            spinnerAdapter.notifyDataSetChanged();
            int selectedIndex = semesterFilterList.indexOf(currentSelection);
            if (selectedIndex >= 0 && semesterSpinner != null) {
                semesterSpinner.setSelection(selectedIndex, false);
            }
        }
    }

    private void filterQuizzes(String semesterFilter) {
        quizList.clear();

        if (semesterFilter == null || "All Semesters".equalsIgnoreCase(semesterFilter)) {
            quizList.addAll(allQuizList);
        } else {
            for (Assessment a : allQuizList) {
                if (semesterFilter.equalsIgnoreCase(a.getSemester())) {
                    quizList.add(a);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (quizList.isEmpty()) {
            showEmptyState("No quizzes found");
        } else {
            if (emptyStateLayout != null) {
                emptyStateLayout.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
            updateAverageScore();
        }
    }

    private void showEmptyState(String message) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }
        if (emptyStateText != null) {
            emptyStateText.setText(message);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (averageScoreText != null) {
            averageScoreText.setText("--");
        }
    }

    private void enrichPendingQuizzesFromReview(int generation) {
        ApiService apiService = ApiClient.getApiService(this);
        for (Assessment item : allQuizList) {
            if (!item.isPendingGrading() || item.getLatestAttemptId() == null || item.getLatestAttemptId().isEmpty()) {
                continue;
            }
            apiService.reviewStudentQuizAttempt(item.getLatestAttemptId()).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed() || generation != quizLoadGeneration) return;
                    if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                        return;
                    }
                    QuizScoreUiHelper.ReviewScoreSummary summary =
                            QuizScoreUiHelper.parseStudentReviewSummary(response.body().getAsJsonObject("data"));
                    QuizScoreUiHelper.applyReviewSummaryToAssessment(item, summary);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    updateAverageScore();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                }
            });
        }
    }

    private void updateAverageScore() {
        if (quizList.isEmpty()) {
            if (averageScoreText != null) averageScoreText.setText("--");
            return;
        }

        double totalEarned = 0.0;
        double totalPossible = 0.0;
        int gradedCount = 0;

        for (Assessment a : quizList) {
            if (a.isGraded() && !a.isPendingGrading()) {
                totalEarned += a.getExactScore() != null ? a.getExactScore() : a.getScore();
                totalPossible += a.getExactTotalScore() != null ? a.getExactTotalScore() : a.getTotalScore();
                gradedCount++;
            }
        }

        if (gradedCount > 0 && totalPossible > 0) {
            int avg = (int) Math.round((totalEarned * 100.0) / totalPossible);
            if (averageScoreText != null) averageScoreText.setText(avg + "%");
        } else {
            if (averageScoreText != null) averageScoreText.setText("--");
        }
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "Scheduled";
        }
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                Date date = sdf.parse(rawDate);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (Exception ignored) {
            }
        }
        return rawDate;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
