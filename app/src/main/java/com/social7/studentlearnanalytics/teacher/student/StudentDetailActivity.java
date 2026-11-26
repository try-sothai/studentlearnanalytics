package com.social7.studentlearnanalytics.teacher.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.model.Score;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.teacher.assessment.ScoreListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDetailActivity extends AppCompatActivity {

    private TextView tabOverview, tabScores, tabAnalytics, tabHistory;
    private View containerOverview, containerScores, containerAnalytics, containerHistory;
    private Student currentStudent;
    private RecyclerView rvScores;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        if (getIntent() != null) {
            if (getIntent().hasExtra("student")) {
                Serializable extra = getIntent().getSerializableExtra("student");
                if (extra instanceof Student) {
                    currentStudent = (Student) extra;
                }
            }
            if (currentStudent == null && getIntent().hasExtra("student_id")) {
                int sid = getIntent().getIntExtra("student_id", 0);
                if (sid > 0) {
                    currentStudent = new Student();
                    currentStudent.setDbId((long) sid);
                    String sCode = getIntent().getStringExtra("student_code");
                    String sName = getIntent().getStringExtra("student_name");
                    if (sCode != null && !sCode.isEmpty()) currentStudent.setStudentCode(sCode);
                    if (sName != null && !sName.isEmpty()) currentStudent.setName(sName);
                }
            }
        }

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Student Details", "", true);

        tabOverview = findViewById(R.id.tabOverview);
        tabScores = findViewById(R.id.tabScores);
        tabAnalytics = findViewById(R.id.tabAnalytics);
        tabHistory = findViewById(R.id.tabHistory);

        containerOverview = findViewById(R.id.containerOverview);
        containerScores = findViewById(R.id.containerScores);
        containerAnalytics = findViewById(R.id.containerAnalytics);
        containerHistory = findViewById(R.id.containerHistory);

        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabScores != null) tabScores.setOnClickListener(v -> selectTab(1));
        if (tabAnalytics != null) tabAnalytics.setOnClickListener(v -> selectTab(2));
        if (tabHistory != null) tabHistory.setOnClickListener(v -> selectTab(3));

        rvScores = findViewById(R.id.rvStudentScores);
        if (rvScores != null) {
            rvScores.setLayoutManager(new LinearLayoutManager(this));
        }

        chart = findViewById(R.id.lineChartStudentAnalytics);
        if (chart != null) {
            chart.setNoDataText("Loading student performance...");
        }

        bindHeaderFromModel();
        fetchStudentDetailFromApi();
        selectTab(0);
    }

    private void bindHeaderFromModel() {
        if (currentStudent == null) return;
        TextView tvName = findViewById(R.id.tvStudentDetailName);
        TextView tvId = findViewById(R.id.tvStudentDetailId);
        TextView tvDept = findViewById(R.id.tvStudentDetailDept);
        TextView tvEmail = findViewById(R.id.tvDetailEmail);
        TextView tvGPA = findViewById(R.id.tvDetailGPA);

        if (tvName != null) tvName.setText(currentStudent.getName());
        if (tvId != null) tvId.setText(currentStudent.getId());
        String headerYl = com.social7.studentlearnanalytics.utils.YearLevelHelper.toFullLabel(
                this,
                currentStudent.getYearLevel() != null ? currentStudent.getYearLevel() : currentStudent.getYear());
        if (tvDept != null) tvDept.setText(currentStudent.getDepartment() + " • " + headerYl);
        if (tvEmail != null) tvEmail.setText(currentStudent.getEmail());
        if (tvGPA != null) tvGPA.setText(String.format(Locale.US, "%.2f / 4.0", currentStudent.getGpa()));
    }

    private void fetchStudentDetailFromApi() {
        String targetId = null;
        if (currentStudent != null && currentStudent.getDbId() != null && currentStudent.getDbId() > 0) {
            targetId = String.valueOf(currentStudent.getDbId());
        } else if (currentStudent != null && currentStudent.getUserId() != null && currentStudent.getUserId() > 0) {
            targetId = String.valueOf(currentStudent.getUserId());
        }
        if (targetId == null) return;

        ApiClient.getApiService(this).getTeacherStudentDetail(targetId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    if (data.has("student") && data.get("student").isJsonObject()) {
                        JsonObject st = data.getAsJsonObject("student");
                        TextView tvName = findViewById(R.id.tvStudentDetailName);
                        TextView tvId = findViewById(R.id.tvStudentDetailId);
                        TextView tvDept = findViewById(R.id.tvStudentDetailDept);
                        TextView tvEmail = findViewById(R.id.tvDetailEmail);
                        TextView tvGPA = findViewById(R.id.tvDetailGPA);
                        if (tvName != null && st.has("name") && !st.get("name").isJsonNull()) tvName.setText(st.get("name").getAsString());
                        if (tvId != null && st.has("student_code") && !st.get("student_code").isJsonNull()) tvId.setText(st.get("student_code").getAsString());
                        if (tvDept != null && st.has("department") && !st.get("department").isJsonNull()) {
                            String yrRaw = st.has("year_level") && !st.get("year_level").isJsonNull() ? st.get("year_level").getAsString() : "";
                            Integer yrParsed = com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(yrRaw);
                            String yrDisplay = yrParsed != null
                                    ? com.social7.studentlearnanalytics.utils.YearLevelHelper.toFullLabel(StudentDetailActivity.this, yrParsed)
                                    : "";
                            tvDept.setText(st.get("department").getAsString() + (yrDisplay.isEmpty() ? "" : " • " + yrDisplay));
                        }
                        if (tvEmail != null && st.has("email") && !st.get("email").isJsonNull()) tvEmail.setText(st.get("email").getAsString());
                        if (tvGPA != null && st.has("gpa") && !st.get("gpa").isJsonNull()) {
                            tvGPA.setText(String.format(Locale.US, "%.2f / 4.0", st.get("gpa").getAsDouble()));
                        }
                    }

                    List<Score> scores = new ArrayList<>();
                    List<Entry> entries = new ArrayList<>();
                    if (data.has("scores") && data.get("scores").isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray("scores");
                        int idx = 0;
                        for (JsonElement el : arr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject sc = el.getAsJsonObject();
                            String title = sc.has("title") && !sc.get("title").isJsonNull() ? sc.get("title").getAsString() : "Assessment";
                            String type = sc.has("type") && !sc.get("type").isJsonNull() ? sc.get("type").getAsString() : "Score";
                            int val = sc.has("score") && !sc.get("score").isJsonNull() ? Math.round(sc.get("score").getAsFloat()) : 0;
                            int max = sc.has("total_score") && !sc.get("total_score").isJsonNull() ? Math.round(sc.get("total_score").getAsFloat()) : 100;
                            scores.add(new Score(String.valueOf(idx + 1), title, type, val, max, type));
                            entries.add(new Entry(idx, max > 0 ? (val * 100f / max) : val));
                            idx++;
                        }
                    }
                    if (rvScores != null) {
                        rvScores.setAdapter(new ScoreListAdapter(StudentDetailActivity.this, scores, score -> {}));
                    }
                    bindChart(entries);
                } else {
                    Toast.makeText(StudentDetailActivity.this, "Unable to load student details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(StudentDetailActivity.this, "Network error loading student details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectTab(int index) {
        resetTabs();
        if (index == 0 && containerOverview != null) {
            containerOverview.setVisibility(View.VISIBLE);
            setTabActive(tabOverview);
        } else if (index == 1 && containerScores != null) {
            containerScores.setVisibility(View.VISIBLE);
            setTabActive(tabScores);
        } else if (index == 2 && containerAnalytics != null) {
            containerAnalytics.setVisibility(View.VISIBLE);
            setTabActive(tabAnalytics);
        } else if (index == 3 && containerHistory != null) {
            containerHistory.setVisibility(View.VISIBLE);
            setTabActive(tabHistory);
        }
    }

    private void resetTabs() {
        if (containerOverview != null) containerOverview.setVisibility(View.GONE);
        if (containerScores != null) containerScores.setVisibility(View.GONE);
        if (containerAnalytics != null) containerAnalytics.setVisibility(View.GONE);
        if (containerHistory != null) containerHistory.setVisibility(View.GONE);

        setTabInactive(tabOverview);
        setTabInactive(tabScores);
        setTabInactive(tabAnalytics);
        setTabInactive(tabHistory);
    }

    private void setTabActive(TextView tv) {
        if (tv == null) return;
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
    }

    private void setTabInactive(TextView tv) {
        if (tv == null) return;
        tv.setTextColor(Color.parseColor("#7A7D8F"));
        tv.setBackground(null);
    }

    private void bindChart(List<Entry> entries) {
        if (chart == null) return;
        if (entries == null || entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No performance scores available yet");
            chart.invalidate();
            return;
        }
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.performance));
        dataSet.setColor(Color.parseColor("#5F34F2"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#5F34F2"));

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
