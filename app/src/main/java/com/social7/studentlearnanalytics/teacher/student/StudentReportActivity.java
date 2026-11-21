package com.social7.studentlearnanalytics.teacher.student;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentReportActivity extends AppCompatActivity {

    private MaterialButton tabOverview, tabScores, tabAttendance, tabProgress, tabNotes;
    private LinearLayout layoutReportOverview, layoutReportScores, layoutReportAttendance, layoutReportProgress, layoutReportNotes;
    private Student currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report);

        initViews();
        setupStudentData();
        setupTabListeners();
    }

    private void initViews() {
        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), getString(R.string.student_reports), "", true);

        ImageView btnMoreOptions = findViewById(R.id.btnMoreOptions);
        if (btnMoreOptions != null) btnMoreOptions.setOnClickListener(this::showMoreOptionsPopup);

        tabOverview = findViewById(R.id.tabOverview);
        tabScores = findViewById(R.id.tabScores);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabProgress = findViewById(R.id.tabProgress);
        tabNotes = findViewById(R.id.tabNotes);

        layoutReportOverview = findViewById(R.id.layoutReportOverview);
        layoutReportScores = findViewById(R.id.layoutReportScores);
        layoutReportAttendance = findViewById(R.id.layoutReportAttendance);
        layoutReportProgress = findViewById(R.id.layoutReportProgress);
        layoutReportNotes = findViewById(R.id.layoutReportNotes);

        MaterialButton btnAddNoteDialog = findViewById(R.id.btnAddNoteDialog);
        if (btnAddNoteDialog != null) {
            btnAddNoteDialog.setOnClickListener(v -> showAddNoteModal());
        }

        MaterialButton btnSaveComposerNote = findViewById(R.id.btnSaveComposerNote);
        EditText edtComposerContent = findViewById(R.id.edtComposerContent);
        if (btnSaveComposerNote != null && edtComposerContent != null) {
            btnSaveComposerNote.setOnClickListener(v -> {
                String text = edtComposerContent.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(this, "Please write a note before saving", Toast.LENGTH_SHORT).show();
                } else {
                    saveInterventionNote("Academic Note", text, edtComposerContent);
                }
            });
        }
    }

    private void saveInterventionNote(String title, String content, EditText toClear) {
        if (currentStudent == null || currentStudent.getDbId() == null || currentStudent.getDbId() <= 0) {
            if (toClear != null) toClear.setText("");
            Toast.makeText(this, R.string.note_saved_successfully, Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("student_id", currentStudent.getDbId());
        body.addProperty("title", title);
        body.addProperty("message", content);
        body.addProperty("priority", "medium");
        ApiClient.getApiService(this).createTeacherRecommendation(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (toClear != null) toClear.setText("");
                Toast.makeText(StudentReportActivity.this, R.string.note_saved_successfully, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(StudentReportActivity.this, "Network error saving note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMoreOptionsPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Export Report");
        popup.getMenu().add(0, 2, 1, "Print Report");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Toast.makeText(this, "Exporting Student Report PDF...", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 2) {
                Toast.makeText(this, "Printing Student Report...", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void showAddNoteModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_student_note);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText edtTitle = new EditText(this);
        edtTitle.setHint("Note Title (e.g. Academic Progress)");
        layout.addView(edtTitle);

        final EditText edtContent = new EditText(this);
        edtContent.setHint("Write a note about this student...");
        edtContent.setMinLines(3);
        layout.addView(edtContent);

        builder.setView(layout);

        builder.setPositiveButton(R.string.save_note, (dialog, which) -> {
            String title = edtTitle.getText().toString().trim();
            String content = edtContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please enter title and content", Toast.LENGTH_SHORT).show();
            } else {
                saveInterventionNote(title, content, null);
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void setupStudentData() {
        if (getIntent() != null && getIntent().hasExtra("student")) {
            Serializable extra = getIntent().getSerializableExtra("student");
            if (extra instanceof Student) {
                currentStudent = (Student) extra;
            }
        }

        TextView tvReportAvatar = findViewById(R.id.tvReportAvatar);
        TextView tvReportStudentName = findViewById(R.id.tvReportStudentName);
        TextView tvReportStudentId = findViewById(R.id.tvReportStudentId);
        TextView tvReportCourseSemester = findViewById(R.id.tvReportCourseSemester);
        TextView tvReportPerformanceBadge = findViewById(R.id.tvReportPerformanceBadge);

        if (currentStudent != null) {
            if (tvReportStudentName != null) tvReportStudentName.setText(currentStudent.getName());
            if (tvReportStudentId != null) tvReportStudentId.setText(currentStudent.getId());
            if (tvReportCourseSemester != null) {
                tvReportCourseSemester.setText(currentStudent.getCourseCode() + " • " + currentStudent.getProgram());
            }
            if (tvReportPerformanceBadge != null) {
                tvReportPerformanceBadge.setText(currentStudent.getRiskLevel() + " Performance");
            }
            if (tvReportAvatar != null && currentStudent.getName() != null && !currentStudent.getName().isEmpty()) {
                tvReportAvatar.setText(String.valueOf(currentStudent.getName().charAt(0)).toUpperCase(Locale.ROOT));
            }
        }

        LineChart lineChart = findViewById(R.id.lineChartReportTrend);
        BarChart barChartAttendance = findViewById(R.id.barChartMonthlyAttendance);
        LineChart lineChartProgress = findViewById(R.id.lineChartProgressTrend);

        if (lineChart != null) lineChart.setNoDataText("Loading performance trend...");
        if (barChartAttendance != null) barChartAttendance.setNoDataText("Loading attendance history...");
        if (lineChartProgress != null) lineChartProgress.setNoDataText("Loading progress trend...");

        fetchRealStudentReport(lineChart, barChartAttendance, lineChartProgress);
    }

    private void fetchRealStudentReport(LineChart lineChart, BarChart barChartAttendance, LineChart lineChartProgress) {
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
                    bindAttendanceChartFromApi(barChartAttendance, data);
                    bindTrendChartFromApi(lineChart, data);
                    bindTrendChartFromApi(lineChartProgress, data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(StudentReportActivity.this, "Network error loading student report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindAttendanceChartFromApi(BarChart chart, JsonObject data) {
        if (chart == null) return;
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (data.has("attendance") && data.get("attendance").isJsonObject()) {
            JsonObject att = data.getAsJsonObject("attendance");
            if (att.has("monthly") && att.get("monthly").isJsonArray()) {
                JsonArray arr = att.getAsJsonArray("monthly");
                int idx = 0;
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject m = el.getAsJsonObject();
                    labels.add(m.has("month") && !m.get("month").isJsonNull() ? m.get("month").getAsString() : ("M" + (idx + 1)));
                    float rate = m.has("rate") && !m.get("rate").isJsonNull() ? m.get("rate").getAsFloat() : 0f;
                    entries.add(new BarEntry(idx, rate));
                    idx++;
                }
            }
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No attendance records available");
            chart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.attendance_7266c9));
        dataSet.setColor(Color.parseColor("#10B981"));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#111B64"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chart.setData(barData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
        xAxis.setTextColor(Color.parseColor("#111B64"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(Color.parseColor("#6477B8"));
        leftAxis.setGridColor(Color.parseColor("#DCE6FF"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(600);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }

    private void bindTrendChartFromApi(LineChart chart, JsonObject data) {
        if (chart == null) return;

        List<Entry> assignmentEntries = new ArrayList<>();
        List<Entry> quizEntries = new ArrayList<>();
        List<Entry> examEntries = new ArrayList<>();

        if (data.has("scores") && data.get("scores").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("scores");
            int aIdx = 0, qIdx = 0, eIdx = 0;
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject sc = el.getAsJsonObject();
                String type = sc.has("type") && !sc.get("type").isJsonNull() ? sc.get("type").getAsString() : "";
                float val = sc.has("score") && !sc.get("score").isJsonNull() ? sc.get("score").getAsFloat() : 0f;
                float max = sc.has("total_score") && !sc.get("total_score").isJsonNull() ? sc.get("total_score").getAsFloat() : 100f;
                float pct = max > 0 ? (val / max) * 100f : val;
                if ("Quiz".equalsIgnoreCase(type)) {
                    quizEntries.add(new Entry(qIdx++, pct));
                } else if ("Exam".equalsIgnoreCase(type) || "Midterm".equalsIgnoreCase(type) || "Final".equalsIgnoreCase(type)) {
                    examEntries.add(new Entry(eIdx++, pct));
                } else {
                    assignmentEntries.add(new Entry(aIdx++, pct));
                }
            }
        }

        LineData lineData = new LineData();
        if (!assignmentEntries.isEmpty()) {
            LineDataSet setAssignment = new LineDataSet(assignmentEntries, getString(R.string.assignment));
            setAssignment.setColor(Color.parseColor("#2463F5"));
            setAssignment.setLineWidth(2.5f);
            setAssignment.setCircleColor(Color.parseColor("#2463F5"));
            setAssignment.setCircleRadius(3.5f);
            setAssignment.setDrawValues(false);
            lineData.addDataSet(setAssignment);
        }
        if (!quizEntries.isEmpty()) {
            LineDataSet setQuiz = new LineDataSet(quizEntries, getString(R.string.quiz));
            setQuiz.setColor(Color.parseColor("#F59E0B"));
            setQuiz.setLineWidth(2.5f);
            setQuiz.setCircleColor(Color.parseColor("#F59E0B"));
            setQuiz.setCircleRadius(3.5f);
            setQuiz.setDrawValues(false);
            lineData.addDataSet(setQuiz);
        }
        if (!examEntries.isEmpty()) {
            LineDataSet setExam = new LineDataSet(examEntries, getString(R.string.exam));
            setExam.setColor(Color.parseColor("#7C3AED"));
            setExam.setLineWidth(2.5f);
            setExam.setCircleColor(Color.parseColor("#7C3AED"));
            setExam.setCircleRadius(3.5f);
            setExam.setDrawValues(false);
            lineData.addDataSet(setExam);
        }

        if (lineData.getDataSetCount() == 0) {
            chart.clear();
            chart.setNoDataText("No assessment trend data available");
            chart.invalidate();
            return;
        }

        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#111B64"));
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(Color.parseColor("#6477B8"));
        leftAxis.setGridColor(Color.parseColor("#DCE6FF"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.parseColor("#111B64"));
        legend.setTextSize(10f);

        chart.animateX(600);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }

    private void setupTabListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabScores != null) tabScores.setOnClickListener(v -> selectTab(1));
        if (tabAttendance != null) tabAttendance.setOnClickListener(v -> selectTab(2));
        if (tabProgress != null) tabProgress.setOnClickListener(v -> selectTab(3));
        if (tabNotes != null) tabNotes.setOnClickListener(v -> selectTab(4));
    }

    private void selectTab(int index) {
        resetTabStyles();

        int primaryBlue = Color.parseColor("#2463F5");
        int white = Color.WHITE;

        if (index == 0) {
            setButtonActive(tabOverview, primaryBlue, white);
            showOnlyContainer(layoutReportOverview);
        } else if (index == 1) {
            setButtonActive(tabScores, primaryBlue, white);
            showOnlyContainer(layoutReportScores);
        } else if (index == 2) {
            setButtonActive(tabAttendance, primaryBlue, white);
            showOnlyContainer(layoutReportAttendance);
        } else if (index == 3) {
            setButtonActive(tabProgress, primaryBlue, white);
            showOnlyContainer(layoutReportProgress);
        } else if (index == 4) {
            setButtonActive(tabNotes, primaryBlue, white);
            showOnlyContainer(layoutReportNotes);
        }
    }

    private void showOnlyContainer(LinearLayout activeLayout) {
        if (layoutReportOverview != null) layoutReportOverview.setVisibility(activeLayout == layoutReportOverview ? View.VISIBLE : View.GONE);
        if (layoutReportScores != null) layoutReportScores.setVisibility(activeLayout == layoutReportScores ? View.VISIBLE : View.GONE);
        if (layoutReportAttendance != null) layoutReportAttendance.setVisibility(activeLayout == layoutReportAttendance ? View.VISIBLE : View.GONE);
        if (layoutReportProgress != null) layoutReportProgress.setVisibility(activeLayout == layoutReportProgress ? View.VISIBLE : View.GONE);
        if (layoutReportNotes != null) layoutReportNotes.setVisibility(activeLayout == layoutReportNotes ? View.VISIBLE : View.GONE);
    }

    private void setButtonActive(MaterialButton btn, int bgTint, int textColor) {
        if (btn == null) return;
        btn.setBackgroundColor(bgTint);
        btn.setTextColor(textColor);
        btn.setIconTintResource(R.color.white);
    }

    private void resetTabStyles() {
        int surfaceLight = Color.parseColor("#EAF2FF");
        int primaryBlue = Color.parseColor("#2463F5");

        setButtonInactive(tabOverview, surfaceLight, primaryBlue);
        setButtonInactive(tabScores, surfaceLight, primaryBlue);
        setButtonInactive(tabAttendance, surfaceLight, primaryBlue);
        setButtonInactive(tabProgress, surfaceLight, primaryBlue);
        setButtonInactive(tabNotes, surfaceLight, primaryBlue);
    }

    private void setButtonInactive(MaterialButton btn, int bgTint, int textColor) {
        if (btn == null) return;
        btn.setBackgroundColor(bgTint);
        btn.setTextColor(textColor);
        btn.setIconTintResource(R.color.teacher_primary_blue);
    }
}
