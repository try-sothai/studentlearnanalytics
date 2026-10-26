package com.social7.studentlearnanalytics.student.assignment;

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

public class AssessmentDetailActivity extends AppCompatActivity {

    private static final String TAG = "AssessmentDetail";

    private RecyclerView recyclerView;
    private AssessmentAdapter adapter;
    private final List<Assessment> assessmentList = new ArrayList<>();
    private final List<Assessment> allAssessmentList = new ArrayList<>();
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
        setContentView(R.layout.activity_assessment_detail);

        initViews();
        setupSemesterSpinner();
        setupRecyclerView();
        fetchAssignments();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.assessment_recycler_view);
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
                filterAssignments(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AssessmentAdapter(assessmentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchAssignments() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (averageScoreText != null) {
            averageScoreText.setText("--");
        }

        ApiService apiService = ApiClient.getApiService(this);
        Call<JsonObject> call = apiService.getStudentAssignments();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        allAssessmentList.clear();
                        JsonArray dataArray = response.body().getAsJsonArray("data");
                        Set<String> uniqueSemesters = new LinkedHashSet<>();

                        if (dataArray != null && dataArray.size() > 0) {
                            for (JsonElement element : dataArray) {
                                if (!element.isJsonObject()) continue;
                                JsonObject obj = element.getAsJsonObject();

                                String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : "";
                                String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Untitled Assignment";

                                String rawDate = obj.has("due_date") && !obj.get("due_date").isJsonNull() ? obj.get("due_date").getAsString() : "";
                                String displayDate = formatDueDate(rawDate);

                                int totalScore = obj.has("total_score") && !obj.get("total_score").isJsonNull() ? obj.get("total_score").getAsInt() : 100;
                                int score = 0;
                                boolean isSubmitted = false;
                                boolean isGraded = false;

                                // Check for student's submission
                                if (obj.has("submissions") && obj.get("submissions").isJsonArray()) {
                                    JsonArray submissions = obj.getAsJsonArray("submissions");
                                    if (submissions.size() > 0) {
                                        JsonObject firstSubmission = submissions.get(0).getAsJsonObject();
                                        isSubmitted = true;
                                        if (firstSubmission.has("score") && !firstSubmission.get("score").isJsonNull()) {
                                            score = (int) Math.round(firstSubmission.get("score").getAsDouble());
                                            isGraded = true;
                                        }
                                    }
                                }

                                // Fallback: check attempts if not found in submissions
                                if (!isSubmitted && obj.has("attempts") && obj.get("attempts").isJsonArray()) {
                                    JsonArray attempts = obj.getAsJsonArray("attempts");
                                    if (attempts.size() > 0) {
                                        JsonObject firstAttempt = attempts.get(0).getAsJsonObject();
                                        isSubmitted = true;
                                        if (firstAttempt.has("score") && !firstAttempt.get("score").isJsonNull()) {
                                            score = (int) Math.round(firstAttempt.get("score").getAsDouble());
                                            isGraded = true;
                                        }
                                    }
                                }

                                String type = obj.has("assignment_type") && !obj.get("assignment_type").isJsonNull()
                                        ? obj.get("assignment_type").getAsString()
                                        : "assignment";

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

                                allAssessmentList.add(new Assessment(
                                        id,
                                        title,
                                        displayDate,
                                        score,
                                        totalScore,
                                        type,
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
                        filterAssignments(currentSemester);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing assignments JSON", e);
                        Toast.makeText(AssessmentDetailActivity.this, "Error parsing assignments data", Toast.LENGTH_SHORT).show();
                        showEmptyState("Error loading assignments");
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(AssessmentDetailActivity.this, "Session expired, please login again", Toast.LENGTH_LONG).show();
                    showEmptyState("Session expired. Please log in again.");
                } else {
                    Toast.makeText(AssessmentDetailActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
                    showEmptyState("No assignments available");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Network failure fetching assignments", t);
                Toast.makeText(AssessmentDetailActivity.this, "Network error: Unable to load assignments", Toast.LENGTH_SHORT).show();
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

    private void filterAssignments(String semesterFilter) {
        assessmentList.clear();

        if (semesterFilter == null || "All Semesters".equalsIgnoreCase(semesterFilter)) {
            assessmentList.addAll(allAssessmentList);
        } else {
            for (Assessment a : allAssessmentList) {
                if (semesterFilter.equalsIgnoreCase(a.getSemester())) {
                    assessmentList.add(a);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (assessmentList.isEmpty()) {
            showEmptyState("No assignments found");
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
        if (assessmentList.isEmpty()) {
            if (averageScoreText != null) averageScoreText.setText("--");
            return;
        }

        int totalEarned = 0;
        int totalPossible = 0;
        int gradedCount = 0;

        for (Assessment a : assessmentList) {
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

    private String formatDueDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "No due date";
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
