package com.social7.studentlearnanalytics.student;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.ExamAdapter;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamDetailActivity extends AppCompatActivity {

    private static final String TAG = "ExamDetail";

    private TextView tabUpcoming, tabCompleted;
    private RecyclerView recyclerExams;
    private ProgressBar loadingProgressBar;
    private TextView emptyStateText;
    private final List<Exam> upcomingExams = new ArrayList<>();
    private final List<Exam> completedExams = new ArrayList<>();
    private boolean showingUpcoming = true;
    private boolean isLoading = false;
    private String errorMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_detail);

        View headerContainer = findViewById(R.id.headerContainer);
        if (headerContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerContainer, (v, insets) -> {
                Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        statusBarInsets.top + 24,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
                return insets;
            });
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tabUpcoming = findViewById(R.id.tabUpcoming);
        tabCompleted = findViewById(R.id.tabCompleted);
        recyclerExams = findViewById(R.id.recyclerExams);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);

        recyclerExams.setLayoutManager(new LinearLayoutManager(this));

        showUpcomingExams();

        tabUpcoming.setOnClickListener(v -> showUpcomingExams());
        tabCompleted.setOnClickListener(v -> showCompletedExams());

        fetchExams();
    }

    private void fetchExams() {
        isLoading = true;
        errorMessage = null;
        refreshList();

        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentExams(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        parseExams(response.body());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing exams JSON", e);
                        upcomingExams.clear();
                        completedExams.clear();
                        errorMessage = "Error loading exams";
                        Toast.makeText(ExamDetailActivity.this, "Error parsing exam data", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 401) {
                    errorMessage = "Session expired. Please log in again.";
                    Toast.makeText(ExamDetailActivity.this, "Session expired, please login again", Toast.LENGTH_LONG).show();
                } else if (response.code() == 404) {
                    errorMessage = "Student profile not found.";
                } else {
                    Log.w(TAG, "Non-success response: HTTP " + response.code());
                    errorMessage = "Failed to load exams (HTTP " + response.code() + ")";
                    Toast.makeText(ExamDetailActivity.this, "Failed to load exams", Toast.LENGTH_SHORT).show();
                }
                refreshList();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                isLoading = false;
                Log.e(TAG, "Network failure fetching exams", t);
                errorMessage = "Network error. Please check connection.";
                Toast.makeText(ExamDetailActivity.this, "Network error: Unable to load exams", Toast.LENGTH_SHORT).show();
                refreshList();
            }
        });
    }

    private void parseExams(JsonObject body) {
        upcomingExams.clear();
        completedExams.clear();

        if (!body.has("data") || !body.get("data").isJsonArray()) {
            throw new IllegalStateException("Missing 'data' array");
        }
        JsonArray dataArray = body.getAsJsonArray("data");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        DecimalFormat numberFormat = new DecimalFormat("0.##");

        for (JsonElement element : dataArray) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();

            String id = getString(obj, "id", "");
            String title = getString(obj, "title", "Untitled Exam");
            String examType = getString(obj, "exam_type", null);
            String status = getString(obj, "status", "scheduled");
            String rawDate = getString(obj, "exam_date", "");
            String startTime = getString(obj, "start_time", getString(obj, "exam_time", null));
            String room = getString(obj, "room", getString(obj, "location", null));
            String isoDate = rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;

            String month = "--";
            String day = "--";
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
                if (date != null) {
                    month = new SimpleDateFormat("MMM", Locale.US).format(date).toUpperCase(Locale.US);
                    day = new SimpleDateFormat("dd", Locale.US).format(date);
                }
            } catch (Exception ignored) {
            }

            String courseName = null;
            if (obj.has("course") && obj.get("course").isJsonObject()) {
                JsonObject course = obj.getAsJsonObject("course");
                courseName = getString(course, "course_name", getString(course, "name", null));
                if (room == null) {
                    room = getString(course, "room", null);
                }
            }
            if (courseName == null) courseName = title;

            Double totalScore = getDouble(obj, "total_score");
            Integer duration = obj.has("duration_minutes") && !obj.get("duration_minutes").isJsonNull()
                    ? obj.get("duration_minutes").getAsInt() : null;

            String scoreText = null;
            String grade = null;
            if (obj.has("results") && obj.get("results").isJsonArray()) {
                JsonArray results = obj.getAsJsonArray("results");
                if (results.size() > 0 && results.get(0).isJsonObject()) {
                    JsonObject result = results.get(0).getAsJsonObject();
                    Double score = getDouble(result, "score");
                    grade = getString(result, "grade", null);
                    if (score != null) {
                        scoreText = numberFormat.format(score)
                                + (totalScore != null ? " / " + numberFormat.format(totalScore) : "");
                    }
                }
            }

            boolean completed = scoreText != null
                    || "completed".equalsIgnoreCase(status)
                    || (!isoDate.isEmpty() && isoDate.compareTo(today) < 0);

            StringBuilder details = new StringBuilder();
            details.append(examType != null ? capitalize(examType) : "Exam");
            if (!title.equals(courseName)) details.append(" • ").append(title);
            if (startTime != null && !startTime.isEmpty()) details.append(" • ").append(startTime);
            if (room != null && !room.isEmpty()) details.append(" • Room ").append(room);
            if (completed) {
                if (scoreText == null) {
                    details.append(" • Result pending");
                } else if (grade != null) {
                    details.append(" • Grade ").append(grade);
                }
            } else {
                if (duration != null) details.append(" • ").append(duration).append(" min");
                details.append(" • ").append(capitalize(status));
            }

            Exam exam = new Exam(id, month, day, courseName, details.toString(), completed, scoreText);
            if (completed) {
                completedExams.add(0, exam);
            } else {
                upcomingExams.add(exam);
            }
        }
    }

    private void showUpcomingExams() {
        showingUpcoming = true;
        tabUpcoming.setBackgroundResource(R.drawable.bg_notif_tab_active);
        tabUpcoming.setTextColor(getResources().getColor(R.color.white));

        tabCompleted.setBackgroundResource(R.drawable.bg_notif_tab_inactive);
        tabCompleted.setTextColor(getResources().getColor(R.color.slas_indigo));

        refreshList();
    }

    private void showCompletedExams() {
        showingUpcoming = false;
        tabCompleted.setBackgroundResource(R.drawable.bg_notif_tab_active);
        tabCompleted.setTextColor(getResources().getColor(R.color.white));

        tabUpcoming.setBackgroundResource(R.drawable.bg_notif_tab_inactive);
        tabUpcoming.setTextColor(getResources().getColor(R.color.slas_indigo));

        refreshList();
    }

    private void refreshList() {
        List<Exam> list = showingUpcoming ? upcomingExams : completedExams;
        recyclerExams.setAdapter(new ExamAdapter(this, list));

        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        String message = null;
        if (!isLoading) {
            if (errorMessage != null) {
                message = errorMessage;
            } else if (list.isEmpty()) {
                message = showingUpcoming ? "No upcoming exams" : "No completed exams";
            }
        }

        if (emptyStateText != null) {
            emptyStateText.setText(message != null ? message : "");
            emptyStateText.setVisibility(message != null ? View.VISIBLE : View.GONE);
        }
        recyclerExams.setVisibility(!isLoading && message == null ? View.VISIBLE : View.GONE);
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        if (obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonPrimitive()) {
            return obj.get(key).getAsString();
        }
        return fallback;
    }

    private static Double getDouble(JsonObject obj, String key) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsDouble();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }
}
