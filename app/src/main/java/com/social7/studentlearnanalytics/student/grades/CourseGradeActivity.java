package com.social7.studentlearnanalytics.student.grades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseGradeActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvBreakdown;
    private TextView tvFinalScore;
    private TextView tvLetterGrade;
    private View layoutEmptyState, btnRetryGrade;
    private View cardWeightedBreakdown;
    private LinearLayout llWeightedBreakdownRows;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private int courseOfferingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_course_grade);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.course_grade_details);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvBreakdown = findViewById(R.id.rvBreakdown);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvLetterGrade = findViewById(R.id.tvLetterGrade);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        cardWeightedBreakdown = findViewById(R.id.cardWeightedBreakdown);
        llWeightedBreakdownRows = findViewById(R.id.llWeightedBreakdownRows);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnRetryGrade = findViewById(R.id.btnRetryGrade);
        rvBreakdown.setLayoutManager(new LinearLayoutManager(this));

        if (btnRetryGrade != null) {
            btnRetryGrade.setOnClickListener(v -> loadGradeDetails());
        }

        courseOfferingId = getIntent().getIntExtra("course_offering_id", 0);
        if (courseOfferingId > 0) {
            loadGradeDetails();
        } else {
            Toast.makeText(this, "Invalid Course Offering", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadGradeDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleGradesCourse(courseOfferingId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonObject()) {
                        JsonObject data = body.getAsJsonObject("data");

                        if (data.has("course_name") && !data.get("course_name").isJsonNull() && getSupportActionBar() != null) {
                            String cname = data.get("course_name").getAsString().trim();
                            if (!cname.isEmpty()) {
                                getSupportActionBar().setTitle(cname);
                            }
                        }

                        String score = "N/A";
                        String letter = "N/A";
                        if (data.has("final_score") && !data.get("final_score").isJsonNull()) {
                            score = data.get("final_score").getAsString();
                        } else if (data.has("final_grade") && data.get("final_grade").isJsonObject()) {
                            JsonObject grade = data.getAsJsonObject("final_grade");
                            if (grade.has("total_score") && !grade.get("total_score").isJsonNull()) {
                                score = grade.get("total_score").getAsString();
                            }
                        }
                        if (data.has("letter_grade") && !data.get("letter_grade").isJsonNull()) {
                            letter = data.get("letter_grade").getAsString();
                        } else if (data.has("final_grade") && data.get("final_grade").isJsonObject()) {
                            JsonObject grade = data.getAsJsonObject("final_grade");
                            if (grade.has("letter_grade") && !grade.get("letter_grade").isJsonNull()) {
                                letter = grade.get("letter_grade").getAsString();
                            }
                        }
                        tvFinalScore.setText(score);
                        tvFinalScore.setTextColor(ContextCompat.getColor(CourseGradeActivity.this,
                                "N/A".equalsIgnoreCase(score) ? R.color.slas_text_secondary : R.color.slas_primary_purple));

                        tvLetterGrade.setText(letter);
                        if ("F".equalsIgnoreCase(letter)) {
                            tvLetterGrade.setTextColor(ContextCompat.getColor(CourseGradeActivity.this, R.color.slas_danger));
                        } else if ("N/A".equalsIgnoreCase(letter)) {
                            tvLetterGrade.setTextColor(ContextCompat.getColor(CourseGradeActivity.this, R.color.slas_text_secondary));
                        } else {
                            tvLetterGrade.setTextColor(ContextCompat.getColor(CourseGradeActivity.this, R.color.slas_success));
                        }

                        if (data.has("weighted_breakdown") && data.get("weighted_breakdown").isJsonObject()) {
                            bindWeightedBreakdown(data.getAsJsonObject("weighted_breakdown"));
                        }

                        com.google.gson.JsonArray breakdown = data.has("breakdown") && data.get("breakdown").isJsonArray()
                                ? data.getAsJsonArray("breakdown") : new com.google.gson.JsonArray();
                        if (breakdown.size() > 0) {
                            rvBreakdown.setAdapter(new GradeBreakdownAdapter(breakdown));
                            rvBreakdown.setVisibility(View.VISIBLE);
                            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                        } else {
                            showState("No Assessment Breakdown Yet",
                                    "No assignments, quizzes, exams, or attendance records exist for this course.",
                                    false);
                        }
                        return;
                    }
                }
                Toast.makeText(CourseGradeActivity.this, "Failed to load grade details.", Toast.LENGTH_SHORT).show();
                showState("Unable to Load Course Grade",
                        "Server returned HTTP " + response.code() + ". Please try again.",
                        true);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(CourseGradeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showState("Connection Error", "Network error loading grade details. Please retry.", true);
            }
        });
    }

    private void bindWeightedBreakdown(JsonObject wb) {
        if (cardWeightedBreakdown == null || llWeightedBreakdownRows == null) return;
        JsonArray groups = wb.has("groups") && wb.get("groups").isJsonArray()
                ? wb.getAsJsonArray("groups") : new JsonArray();
        if (groups.size() == 0) {
            cardWeightedBreakdown.setVisibility(View.GONE);
            return;
        }
        llWeightedBreakdownRows.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).isJsonObject()) continue;
            JsonObject g = groups.get(i).getAsJsonObject();
            View row = inflater.inflate(R.layout.item_score_breakdown_row, llWeightedBreakdownRows, false);
            TextView tvLabel = row.findViewById(R.id.tvGroupLabel);
            TextView tvRaw = row.findViewById(R.id.tvGroupRaw);
            TextView tvWeighted = row.findViewById(R.id.tvGroupWeighted);
            TextView tvComponents = row.findViewById(R.id.tvGroupComponents);
            View divider = row.findViewById(R.id.viewGroupDivider);

            String label = g.has("label") && !g.get("label").isJsonNull() ? g.get("label").getAsString() : "Group";
            String rawDisp = g.has("raw_display") && !g.get("raw_display").isJsonNull() ? g.get("raw_display").getAsString() : "N/A";
            String wDisp = g.has("weighted_display") && !g.get("weighted_display").isJsonNull() ? g.get("weighted_display").getAsString() : "N/A";
            boolean hasScore = g.has("has_score") && !g.get("has_score").isJsonNull() && g.get("has_score").getAsBoolean();

            tvLabel.setText(label);
            tvRaw.setText(rawDisp);
            tvWeighted.setText(wDisp);
            tvWeighted.setTextColor(ContextCompat.getColor(this,
                    hasScore ? R.color.slas_primary_purple : R.color.slas_text_secondary));
            if (tvComponents != null && g.has("components") && g.get("components").isJsonArray()) {
                JsonArray comps = g.getAsJsonArray("components");
                if (comps.size() > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < comps.size(); j++) {
                        if (!comps.get(j).isJsonObject()) continue;
                        JsonObject c = comps.get(j).getAsJsonObject();
                        String cLabel = c.has("label") && !c.get("label").isJsonNull() ? c.get("label").getAsString() : "";
                        String cRaw = c.has("raw_display") && !c.get("raw_display").isJsonNull() ? c.get("raw_display").getAsString() : "N/A";
                        if (cLabel.isEmpty()) continue;
                        if (sb.length() > 0) sb.append(" • ");
                        sb.append(cLabel).append(": ").append(cRaw);
                    }
                    if (sb.length() > 0) {
                        tvComponents.setText(sb.toString());
                        tvComponents.setVisibility(View.VISIBLE);
                    }
                }
            }
            divider.setVisibility(i == groups.size() - 1 ? View.GONE : View.VISIBLE);
            llWeightedBreakdownRows.addView(row);
        }
        cardWeightedBreakdown.setVisibility(View.VISIBLE);
    }

    private void showState(String title, String msg, boolean showRetry) {
        if (rvBreakdown != null) rvBreakdown.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyTitle != null) tvEmptyTitle.setText(title);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(msg);
        if (btnRetryGrade != null) btnRetryGrade.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
