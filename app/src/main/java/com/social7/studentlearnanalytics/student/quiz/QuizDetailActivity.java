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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail);

        initViews();
        setupSemesterSpinner();
        setupRecyclerView();
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

        ApiService apiService = ApiClient.getApiService(this);
        Call<JsonObject> call = apiService.getStudentQuizzes(null);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        allQuizList.clear();
                        JsonArray dataArray = response.body().getAsJsonArray("data");
                        Set<String> uniqueSemesters = new LinkedHashSet<>();

                        if (dataArray != null && dataArray.size() > 0) {
                            for (JsonElement element : dataArray) {
                                if (!element.isJsonObject()) continue;
                                JsonObject obj = element.getAsJsonObject();

                                String id = obj.has("id") && !obj.get("id").isJsonNull()
                                        ? obj.get("id").getAsString() : "";
                                String title = obj.has("title") && !obj.get("title").isJsonNull()
                                        ? obj.get("title").getAsString() : "Untitled Quiz";

                                String rawDate = obj.has("start_at") && !obj.get("start_at").isJsonNull()
                                        ? obj.get("start_at").getAsString() : "";
                                String displayDate = formatDate(rawDate);

                                int totalScore = obj.has("total_score") && !obj.get("total_score").isJsonNull()
                                        ? (int) Math.round(obj.get("total_score").getAsDouble()) : 100;
                                int score = 0;
                                boolean isSubmitted = false;
                                boolean isGraded = false;

                                if (obj.has("attempts") && obj.get("attempts").isJsonArray()) {
                                    JsonArray attempts = obj.getAsJsonArray("attempts");
                                    if (attempts.size() > 0) {
                                        JsonObject latestAttempt = attempts.get(0).getAsJsonObject();
                                        String status = latestAttempt.has("status") && !latestAttempt.get("status").isJsonNull()
                                                ? latestAttempt.get("status").getAsString() : "";
                                        isSubmitted = "completed".equals(status) || "timed_out".equals(status);
                                        if (latestAttempt.has("score") && !latestAttempt.get("score").isJsonNull()) {
                                            score = (int) Math.round(latestAttempt.get("score").getAsDouble());
                                            isGraded = isSubmitted;
                                        }
                                    }
                                }

                                String semesterName = null;
                                if (obj.has("semester") && !obj.get("semester").isJsonNull()) {
                                    JsonObject semObj = obj.getAsJsonObject("semester");
                                    if (semObj.has("name") && !semObj.get("name").isJsonNull()) {
                                        semesterName = semObj.get("name").getAsString();
                                    }
                                } else if (obj.has("semester_id") && !obj.get("semester_id").isJsonNull()) {
                                    semesterName = "Semester " + obj.get("semester_id").getAsString();
                                }

                                if (semesterName != null && !semesterName.trim().isEmpty()) {
                                    uniqueSemesters.add(semesterName.trim());
                                }

                                String courseName = null;
                                if (obj.has("course") && !obj.get("course").isJsonNull()) {
                                    JsonObject courseObj = obj.getAsJsonObject("course");
                                    if (courseObj.has("course_name") && !courseObj.get("course_name").isJsonNull()) {
                                        courseName = courseObj.get("course_name").getAsString();
                                    } else if (courseObj.has("name") && !courseObj.get("name").isJsonNull()) {
                                        courseName = courseObj.get("name").getAsString();
                                    }
                                }

                                allQuizList.add(new Assessment(
                                        id,
                                        title,
                                        displayDate,
                                        score,
                                        totalScore,
                                        "quiz",
                                        semesterName,
                                        courseName,
                                        isSubmitted,
                                        isGraded
                                ));
                            }
                        }

                        updateSemesterSpinnerOptions(uniqueSemesters);

                        String currentSemester = (semesterSpinner != null && semesterSpinner.getSelectedItem() != null)
                                ? semesterSpinner.getSelectedItem().toString()
                                : "All Semesters";
                        filterQuizzes(currentSemester);

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
            public void onFailure(Call<JsonObject> call, Throwable t) {
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

    private void updateAverageScore() {
        if (quizList.isEmpty()) {
            if (averageScoreText != null) averageScoreText.setText("--");
            return;
        }

        int totalEarned = 0;
        int totalPossible = 0;
        int gradedCount = 0;

        for (Assessment a : quizList) {
            if (a.isGraded()) {
                totalEarned += a.getScore();
                totalPossible += a.getTotalScore();
                gradedCount++;
            }
        }

        if (gradedCount > 0 && totalPossible > 0) {
            int avg = (totalEarned * 100) / totalPossible;
            if (averageScoreText != null) averageScoreText.setText(avg + "%");
        } else {
            if (averageScoreText != null) averageScoreText.setText("--");
        }
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "No date";
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
