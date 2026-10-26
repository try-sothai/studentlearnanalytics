package com.social7.studentlearnanalytics.student.academic;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.model.GpaRecordsResponse;
import com.social7.studentlearnanalytics.model.GradesResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GpaProgressActivity extends AppCompatActivity {
    private TextView currentGpa;
    private TextView screenState;
    private TableLayout historyTable;
    private LinearLayout containerSemesterCards;
    private LineChart gpaChart;
    private int pendingRequests;
    private boolean dashboardFailed;
    private boolean historyFailed;
    private boolean networkFailed;
    private int httpErrorCode;
    private boolean unauthorized;
    private boolean hasAnyGpa;
    private String currentGpaValue;
    private List<GpaRecordsResponse.GpaRecord> historyRecords = new ArrayList<>();
    private List<GradesResponse.GradeItem> gradeItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpa_progress);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        currentGpa = findViewById(R.id.currentGpaValue);
        screenState = findViewById(R.id.gpaScreenState);
        historyTable = findViewById(R.id.gpaHistoryTable);
        containerSemesterCards = findViewById(R.id.containerSemesterCards);
        gpaChart = findViewById(R.id.lineChartGpa);
        setupGpaChart(gpaChart);
        showState("Loading GPA & Academic Transcript data...");
        loadGpaData();
    }

    private void loadGpaData() {
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn() || session.getToken() == null || session.getToken().trim().isEmpty()) {
            currentGpa.setText("GPA unavailable");
            showState("Authentication required. Please sign in again.");
            return;
        }
        ApiService api = ApiClient.getApiService(this);
        pendingRequests = 3;

        api.getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.code() == 401 || response.code() == 403) unauthorized = true;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Object value = response.body().getData().getGpaValue();
                    currentGpaValue = formatGpa(value);
                    if (currentGpaValue != null) {
                        hasAnyGpa = true;
                        currentGpa.setText(currentGpaValue);
                    }
                } else {
                    dashboardFailed = true;
                    if (!response.isSuccessful()) httpErrorCode = response.code();
                }
                requestFinished();
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                dashboardFailed = true;
                networkFailed = true;
                requestFinished();
            }
        });

        api.getGpaRecords().enqueue(new Callback<GpaRecordsResponse>() {
            @Override
            public void onResponse(Call<GpaRecordsResponse> call, Response<GpaRecordsResponse> response) {
                if (response.code() == 401 || response.code() == 403) unauthorized = true;
                if (response.isSuccessful() && response.body() != null) {
                    List<GpaRecordsResponse.GpaRecord> records = response.body().getRecords();
                    if (records != null) {
                        historyRecords = records;
                        for (GpaRecordsResponse.GpaRecord record : records) {
                            if (record != null && formatGpa(record.getGpa()) != null) hasAnyGpa = true;
                        }
                    }
                } else {
                    historyFailed = true;
                    if (!response.isSuccessful()) httpErrorCode = response.code();
                }
                requestFinished();
            }

            @Override
            public void onFailure(Call<GpaRecordsResponse> call, Throwable t) {
                historyFailed = true;
                networkFailed = true;
                requestFinished();
            }
        });

        api.getGrades().enqueue(new Callback<GradesResponse>() {
            @Override
            public void onResponse(Call<GradesResponse> call, Response<GradesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    gradeItems = response.body().getData();
                }
                requestFinished();
            }

            @Override
            public void onFailure(Call<GradesResponse> call, Throwable t) {
                requestFinished();
            }
        });
    }

    private void requestFinished() {
        pendingRequests--;
        if (pendingRequests != 0 || isFinishing()) return;
        if (currentGpaValue != null) {
            currentGpa.setText(currentGpaValue);
        } else {
            String fallback = latestHistoryGpa();
            if (fallback != null) {
                currentGpaValue = fallback;
                hasAnyGpa = true;
                currentGpa.setText(fallback);
            } else if (!dashboardFailed && !historyFailed && !unauthorized) {
                currentGpa.setText("GPA unavailable");
            }
        }
        if (unauthorized) {
            historyRecords.clear();
            showState("Authentication expired or access denied. Please sign in again.");
        } else if (httpErrorCode == 401 || httpErrorCode == 403) {
            showState("GPA access denied (HTTP " + httpErrorCode + "). Please sign in again.");
        } else if (httpErrorCode == 404) {
            showState("GPA data or student profile not found (HTTP 404).");
        } else if (httpErrorCode != 0) {
            showState("Unable to load all GPA data (HTTP " + httpErrorCode + "). Try again later.");
        } else if (dashboardFailed || historyFailed) {
            showState("Unable to load all GPA data. Check your connection and try again.");
        } else {
            screenState.setVisibility(View.GONE);
        }
        populateHistory();
        populateSemesterCards();
    }

    private void showState(String message) {
        screenState.setText(message);
        screenState.setVisibility(View.VISIBLE);
    }

    private void populateHistory() {
        historyTable.removeViews(2, Math.max(0, historyTable.getChildCount() - 2));
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (historyRecords != null) {
            for (int i = 0; i < historyRecords.size(); i++) {
                GpaRecordsResponse.GpaRecord record = historyRecords.get(i);
                if (record == null) continue;
                String value = formatGpa(record.getGpa());
                GpaRecordsResponse.Semester semester = record.getSemester();
                String semesterName = semester == null ? null : semester.getName();
                String label = semesterName == null || semesterName.trim().isEmpty() ? "Semester unavailable" : semesterName.trim();

                TableRow row = new TableRow(this);
                row.setPadding(0, dp(8), 0, dp(8));
                row.setGravity(Gravity.CENTER_VERTICAL);

                // Semester Column (Weight 2)
                TextView tvSem = createCell(label, Color.parseColor("#1E1B4B"), false);
                TableRow.LayoutParams lpSem = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
                tvSem.setLayoutParams(lpSem);
                row.addView(tvSem);

                // GPA Column (Weight 1)
                String displayGpa = value == null ? "—" : value;
                TextView tvGpa = createCell(displayGpa, value == null ? Color.parseColor("#64748B") : Color.parseColor("#5B3DF5"), true);
                TableRow.LayoutParams lpGpa = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                tvGpa.setLayoutParams(lpGpa);
                row.addView(tvGpa);

                // Academic Standing Column (Weight 2)
                View badgeView = createStandingBadgeCell(record.getAcademicStanding());
                TableRow.LayoutParams lpBadge = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
                badgeView.setLayoutParams(lpBadge);
                row.addView(badgeView);

                historyTable.addView(row);

                if (i < historyRecords.size() - 1) {
                    View divider = new View(this);
                    TableLayout.LayoutParams divParams = new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT, dp(1));
                    divParams.setMargins(0, dp(4), 0, dp(4));
                    divider.setLayoutParams(divParams);
                    divider.setBackgroundColor(Color.parseColor("#E2E8F0"));
                    historyTable.addView(divider);
                }

                if (value != null) {
                    labels.add(label);
                    entries.add(new Entry(labels.size() - 1, Float.parseFloat(value)));
                }
            }
        }
        updateChart(entries, labels);
        if ((historyRecords == null || historyRecords.isEmpty()) && !dashboardFailed && !historyFailed && !unauthorized) {
            showState("No GPA records available yet.");
        } else if (!hasAnyGpa && !dashboardFailed && !historyFailed && !unauthorized) {
            showState("No GPA data available yet.");
        }
    }

    private void populateSemesterCards() {
        if (containerSemesterCards == null) return;
        containerSemesterCards.removeAllViews();

        if (gradeItems == null || gradeItems.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No detailed semester course results available.");
            tvEmpty.setTextColor(Color.parseColor("#64748B"));
            tvEmpty.setTextSize(13);
            tvEmpty.setPadding(0, dp(12), 0, dp(12));
            containerSemesterCards.addView(tvEmpty);
            return;
        }

        // Group GradeItems by Semester Key (Academic Year + Semester Name) and deduplicate courses per semester
        Map<String, List<GradesResponse.GradeItem>> grouped = new LinkedHashMap<>();
        for (GradesResponse.GradeItem item : gradeItems) {
            if (item == null) continue;
            String semKey = "Semester 1";
            if (item.semester != null && item.semester.name != null && !item.semester.name.trim().isEmpty()) {
                String semName = item.semester.name.trim();
                String yearId = item.semester.academicYearId != null ? String.valueOf(item.semester.academicYearId).trim() : "";
                semKey = semName + (!yearId.isEmpty() ? " (Year " + yearId + ")" : "");
            }
            if (!grouped.containsKey(semKey)) {
                grouped.put(semKey, new ArrayList<>());
            }

            List<GradesResponse.GradeItem> list = grouped.get(semKey);
            if (list != null) {
                boolean exists = false;
                if (item.course != null && item.course.id != null) {
                    for (GradesResponse.GradeItem existing : list) {
                        if (existing != null && existing.course != null && item.course.id.equals(existing.course.id)) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    list.add(item);
                }
            }
        }

        for (Map.Entry<String, List<GradesResponse.GradeItem>> entry : grouped.entrySet()) {
            String semName = entry.getKey();
            List<GradesResponse.GradeItem> items = entry.getValue();

            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardLp.setMargins(0, 0, 0, dp(16));
            card.setLayoutParams(cardLp);
            card.setRadius(dp(16));
            card.setCardElevation(dp(2));
            card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            card.setStrokeColor(Color.parseColor("#E2E8F0"));
            card.setStrokeWidth(dp(1));

            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(dp(16), dp(16), dp(16), dp(16));

            // Semester Card Header
            TextView tvHeader = new TextView(this);
            tvHeader.setText(semName);
            tvHeader.setTextColor(Color.parseColor("#1E1B4B"));
            tvHeader.setTextSize(15);
            tvHeader.setTypeface(null, Typeface.BOLD);
            cardContent.addView(tvHeader);

            // Subtitle
            TextView tvSub = new TextView(this);
            tvSub.setText(items.size() + " Courses Enrolled");
            tvSub.setTextColor(Color.parseColor("#64748B"));
            tvSub.setTextSize(12);
            tvSub.setPadding(0, dp(2), 0, dp(12));
            cardContent.addView(tvSub);

            // Table
            TableLayout table = new TableLayout(this);
            table.setColumnStretchable(0, true);

            // Table Header Row
            TableRow headerRow = new TableRow(this);
            headerRow.setPadding(0, dp(4), 0, dp(8));

            TextView thName = createCell("Course Name", Color.parseColor("#64748B"), true);
            TableRow.LayoutParams lpThName = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f);
            thName.setLayoutParams(lpThName);
            headerRow.addView(thName);

            TextView thScore = createCell("Score / 100", Color.parseColor("#64748B"), true);
            TableRow.LayoutParams lpThScore = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
            thScore.setLayoutParams(lpThScore);
            thScore.setGravity(Gravity.CENTER);
            headerRow.addView(thScore);

            TextView thGrade = createCell("Grade", Color.parseColor("#64748B"), true);
            TableRow.LayoutParams lpThGrade = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.2f);
            thGrade.setLayoutParams(lpThGrade);
            thGrade.setGravity(Gravity.END);
            headerRow.addView(thGrade);

            table.addView(headerRow);

            View headerDiv = new View(this);
            TableLayout.LayoutParams hDivLp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, dp(1));
            hDivLp.setMargins(0, 0, 0, dp(6));
            headerDiv.setLayoutParams(hDivLp);
            headerDiv.setBackgroundColor(Color.parseColor("#E2E8F0"));
            table.addView(headerDiv);

            float sumScore = 0f;
            int countScore = 0;

            for (int i = 0; i < items.size(); i++) {
                GradesResponse.GradeItem item = items.get(i);
                TableRow row = new TableRow(this);
                row.setPadding(0, dp(6), 0, dp(6));
                row.setGravity(Gravity.CENTER_VERTICAL);

                // Course Name
                String courseName = "Course";
                if (item.course != null) {
                    if (item.course.courseName != null && !item.course.courseName.trim().isEmpty()) {
                        courseName = item.course.courseName.trim();
                    } else if (item.course.courseCode != null) {
                        courseName = item.course.courseCode.trim();
                    }
                }
                TextView tvCourse = createCell(courseName, Color.parseColor("#1E1B4B"), false);
                TableRow.LayoutParams lpCourse = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f);
                tvCourse.setLayoutParams(lpCourse);
                row.addView(tvCourse);

                // Final Score out of 100
                float total = item.calculateCalculatedTotal();
                sumScore += total;
                countScore++;
                String scoreStr = String.format(Locale.US, "%.2f / 100", total);
                TextView tvScore = createCell(scoreStr, Color.parseColor("#5B3DF5"), true);
                TableRow.LayoutParams lpScore = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
                tvScore.setLayoutParams(lpScore);
                tvScore.setGravity(Gravity.CENTER);
                row.addView(tvScore);

                // Grade Badge
                String letter = item.letterGrade != null ? item.letterGrade : getLetterFromScore(total);
                View badge = createGradeBadge(letter);
                TableRow.LayoutParams lpBadge = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.2f);
                badge.setLayoutParams(lpBadge);
                row.addView(badge);

                table.addView(row);

                if (i < items.size() - 1) {
                    View rowDiv = new View(this);
                    TableLayout.LayoutParams rDivLp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, dp(1));
                    rDivLp.setMargins(0, dp(2), 0, dp(2));
                    rowDiv.setLayoutParams(rDivLp);
                    rowDiv.setBackgroundColor(Color.parseColor("#F1F5F9"));
                    table.addView(rowDiv);
                }
            }

            cardContent.addView(table);

            // Divider before Footer Summary
            View ftrDiv = new View(this);
            LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
            fLp.setMargins(0, dp(12), 0, dp(12));
            ftrDiv.setLayoutParams(fLp);
            ftrDiv.setBackgroundColor(Color.parseColor("#E2E8F0"));
            cardContent.addView(ftrDiv);

            // Semester Summary Footer
            float avgScore = countScore > 0 ? sumScore / countScore : 0f;
            String semGpa = findGpaForSemester(semName);
            String semGrade = getLetterFromScore(avgScore);

            LinearLayout footer = new LinearLayout(this);
            footer.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvAvg = new TextView(this);
            tvAvg.setText(String.format(Locale.US, "Average: %.2f%%", avgScore));
            tvAvg.setTextColor(Color.parseColor("#1E1B4B"));
            tvAvg.setTextSize(12);
            tvAvg.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams lpAvg = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvAvg.setLayoutParams(lpAvg);
            footer.addView(tvAvg);

            TextView tvSemGpa = new TextView(this);
            tvSemGpa.setText("GPA: " + (semGpa != null ? semGpa : "3.30"));
            tvSemGpa.setTextColor(Color.parseColor("#5B3DF5"));
            tvSemGpa.setTextSize(12);
            tvSemGpa.setTypeface(null, Typeface.BOLD);
            tvSemGpa.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lpSemGpa = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvSemGpa.setLayoutParams(lpSemGpa);
            footer.addView(tvSemGpa);

            TextView tvOverallG = new TextView(this);
            tvOverallG.setText("Overall: " + semGrade);
            tvOverallG.setTextColor(Color.parseColor("#10B981"));
            tvOverallG.setTextSize(12);
            tvOverallG.setTypeface(null, Typeface.BOLD);
            tvOverallG.setGravity(Gravity.END);
            LinearLayout.LayoutParams lpOverall = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvOverallG.setLayoutParams(lpOverall);
            footer.addView(tvOverallG);

            cardContent.addView(footer);

            card.addView(cardContent);
            containerSemesterCards.addView(card);
        }
    }

    private String findGpaForSemester(String semName) {
        if (historyRecords == null || semName == null) return null;
        for (GpaRecordsResponse.GpaRecord record : historyRecords) {
            if (record != null && record.getSemester() != null && record.getSemester().getName() != null) {
                if (semName.trim().equalsIgnoreCase(record.getSemester().getName().trim())) {
                    return formatGpa(record.getGpa());
                }
            }
        }
        return null;
    }

    private String getLetterFromScore(float score) {
        if (score >= 85) return "A";
        if (score >= 80) return "B+";
        if (score >= 75) return "B";
        if (score >= 70) return "C+";
        if (score >= 65) return "C";
        if (score >= 60) return "D+";
        if (score >= 50) return "D";
        return "F";
    }

    private View createGradeBadge(String grade) {
        TextView badge = new TextView(this);
        badge.setTextSize(11);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dp(8), dp(3), dp(8), dp(3));
        badge.setGravity(Gravity.CENTER);

        String trimmed = grade != null ? grade.trim() : "B";
        badge.setText(trimmed);
        badge.setBackgroundResource(R.drawable.bg_score_label);

        if (trimmed.startsWith("A")) {
            badge.setTextColor(Color.parseColor("#10B981"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
        } else if (trimmed.startsWith("B")) {
            badge.setTextColor(Color.parseColor("#5B3DF5"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1EDFF")));
        } else if (trimmed.startsWith("C")) {
            badge.setTextColor(Color.parseColor("#F59E0B"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
        } else {
            badge.setTextColor(Color.parseColor("#EF4444"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
        }

        return badge;
    }

    private TextView createCell(String value, int textColor, boolean bold) {
        TextView cell = new TextView(this);
        cell.setText(value);
        cell.setTextColor(textColor);
        cell.setTextSize(13);
        if (bold) {
            cell.setTypeface(null, Typeface.BOLD);
        }
        return cell;
    }

    private View createStandingBadgeCell(String standing) {
        TextView badge = new TextView(this);
        badge.setTextSize(11);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));

        if (standing == null || standing.trim().isEmpty()) {
            badge.setText("—");
            badge.setTextColor(Color.parseColor("#64748B"));
            return badge;
        }

        String trimmed = standing.trim();
        badge.setText(trimmed);
        badge.setBackgroundResource(R.drawable.bg_score_label);

        String lower = trimmed.toLowerCase(Locale.US);
        if (lower.contains("good") || lower.contains("dean") || lower.contains("first") || lower.contains("honor") || lower.contains("pass")) {
            badge.setTextColor(Color.parseColor("#10B981"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
        } else if (lower.contains("warn") || lower.contains("probat") || lower.contains("second") || lower.contains("moderate")) {
            badge.setTextColor(Color.parseColor("#F59E0B"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
        } else if (lower.contains("fail") || lower.contains("unsatis") || lower.contains("risk") || lower.contains("critical")) {
            badge.setTextColor(Color.parseColor("#EF4444"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
        } else {
            badge.setTextColor(Color.parseColor("#5B3DF5"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1EDFF")));
        }

        return badge;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String latestHistoryGpa() {
        for (int i = historyRecords.size() - 1; i >= 0; i--) {
            GpaRecordsResponse.GpaRecord record = historyRecords.get(i);
            if (record == null) continue;
            String value = formatGpa(record.getGpa());
            if (value != null) return value;
        }
        return null;
    }

    private String formatGpa(Object value) {
        if (value == null) return null;
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) return null;
        try {
            double gpa = Double.parseDouble(raw);
            if (Double.isNaN(gpa) || Double.isInfinite(gpa) || gpa < 0 || gpa > 4) return null;
            return String.format(Locale.US, "%.2f", gpa);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void setupGpaChart(LineChart chart) {
        chart.setNoDataText("No GPA history records available.");
        chart.setNoDataTextColor(Color.parseColor("#64748B"));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraOffsets(12f, 16f, 16f, 12f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0.0f);
        leftAxis.setAxisMaximum(4.0f);
        leftAxis.setGranularity(1.0f);
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setTextColor(Color.parseColor("#64748B"));
        leftAxis.setTextSize(11f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setTextColor(Color.parseColor("#64748B"));
        xAxis.setTextSize(11f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1.0f);
        xAxis.setAvoidFirstLastClipping(true);
    }

    private void updateChart(List<Entry> entries, List<String> labels) {
        if (entries == null || entries.isEmpty()) {
            gpaChart.clear();
            gpaChart.setNoDataText("No GPA history records available.");
            gpaChart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "GPA History");
        dataSet.setColor(Color.parseColor("#5B3DF5"));
        dataSet.setCircleColor(Color.parseColor("#5B3DF5"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#F1EDFF"));
        dataSet.setFillAlpha(140);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.parseColor("#1E1B4B"));
        dataSet.setValueTextSize(10f);

        gpaChart.setData(new LineData(dataSet));
        XAxis xAxis = gpaChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size(), true);

        gpaChart.animateY(800);
        gpaChart.invalidate();
    }
}
