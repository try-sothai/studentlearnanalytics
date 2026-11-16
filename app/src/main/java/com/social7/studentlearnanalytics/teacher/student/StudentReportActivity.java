package com.social7.studentlearnanalytics.teacher.student;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentReportActivity extends AppCompatActivity {

    private MaterialButton tabOverview, tabScores, tabAttendance, tabProgress, tabNotes;
    private LinearLayout layoutReportOverview, layoutReportScores, layoutReportAttendance, layoutReportProgress, layoutReportNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report);

        initViews();
        setupStudentData();
        setupTabListeners();
    }

    private void initViews() {
        com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), getString(R.string.student_reports), "", true);

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
                    edtComposerContent.setText("");
                    Toast.makeText(this, R.string.note_saved_successfully, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                Toast.makeText(this, "Note saved: " + title, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void setupStudentData() {
        Student student = (Student) getIntent().getSerializableExtra("student");

        TextView tvReportAvatar = findViewById(R.id.tvReportAvatar);
        TextView tvReportStudentName = findViewById(R.id.tvReportStudentName);
        TextView tvReportStudentId = findViewById(R.id.tvReportStudentId);
        TextView tvReportCourseSemester = findViewById(R.id.tvReportCourseSemester);
        TextView tvReportPerformanceBadge = findViewById(R.id.tvReportPerformanceBadge);

        if (student != null) {
            if (tvReportStudentName != null) tvReportStudentName.setText(student.getName());
            if (tvReportStudentId != null) tvReportStudentId.setText(student.getId());
            if (tvReportCourseSemester != null) {
                tvReportCourseSemester.setText(student.getCourseCode() + " - " + student.getProgram() + " • Sem 1, 2024");
            }
            if (tvReportPerformanceBadge != null) {
                tvReportPerformanceBadge.setText(student.getRiskLevel() + " Performance");
            }
            if (tvReportAvatar != null && student.getName() != null && !student.getName().isEmpty()) {
                tvReportAvatar.setText(String.valueOf(student.getName().charAt(0)).toUpperCase());
            }
        } else {
            if (tvReportStudentName != null) tvReportStudentName.setText("Sophea");
            if (tvReportStudentId != null) tvReportStudentId.setText("STU023001");
            if (tvReportCourseSemester != null) tvReportCourseSemester.setText("CS101 - Data Structures • Sem 1, 2024");
            if (tvReportPerformanceBadge != null) tvReportPerformanceBadge.setText(R.string.good_performance);
            if (tvReportAvatar != null) tvReportAvatar.setText("S");
        }

        LineChart lineChart = findViewById(R.id.lineChartReportTrend);
        if (lineChart != null) setupTrendChart(lineChart);

        BarChart barChartAttendance = findViewById(R.id.barChartMonthlyAttendance);
        if (barChartAttendance != null) setupAttendanceChart(barChartAttendance);

        LineChart lineChartProgress = findViewById(R.id.lineChartProgressTrend);
        if (lineChartProgress != null) setupTrendChart(lineChartProgress);
    }

    private void setupAttendanceChart(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 100f));
        entries.add(new BarEntry(1, 95f));
        entries.add(new BarEntry(2, 90f));
        entries.add(new BarEntry(3, 98f));
        entries.add(new BarEntry(4, 92f));
        entries.add(new BarEntry(5, 96f));

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
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"}));
        xAxis.setTextColor(Color.parseColor("#111B64"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(Color.parseColor("#6477B8"));
        leftAxis.setGridColor(Color.parseColor("#DCE6FF"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }

    private void setupTrendChart(LineChart chart) {
        List<Entry> assignmentEntries = new ArrayList<>();
        assignmentEntries.add(new Entry(0, 80f));
        assignmentEntries.add(new Entry(1, 82f));
        assignmentEntries.add(new Entry(2, 85f));
        assignmentEntries.add(new Entry(3, 84f));
        assignmentEntries.add(new Entry(4, 88f));
        assignmentEntries.add(new Entry(5, 86f));
        assignmentEntries.add(new Entry(6, 90f));
        assignmentEntries.add(new Entry(7, 92f));

        List<Entry> quizEntries = new ArrayList<>();
        quizEntries.add(new Entry(0, 75f));
        quizEntries.add(new Entry(1, 78f));
        quizEntries.add(new Entry(2, 80f));
        quizEntries.add(new Entry(3, 82f));
        quizEntries.add(new Entry(4, 85f));
        quizEntries.add(new Entry(5, 84f));
        quizEntries.add(new Entry(6, 87f));
        quizEntries.add(new Entry(7, 88f));

        List<Entry> examEntries = new ArrayList<>();
        examEntries.add(new Entry(0, 70f));
        examEntries.add(new Entry(1, 72f));
        examEntries.add(new Entry(2, 75f));
        examEntries.add(new Entry(3, 74f));
        examEntries.add(new Entry(4, 78f));
        examEntries.add(new Entry(5, 76f));
        examEntries.add(new Entry(6, 78f));
        examEntries.add(new Entry(7, 80f));

        LineDataSet setAssignment = new LineDataSet(assignmentEntries, getString(R.string.assignment));
        setAssignment.setColor(Color.parseColor("#2463F5"));
        setAssignment.setLineWidth(2.5f);
        setAssignment.setCircleColor(Color.parseColor("#2463F5"));
        setAssignment.setCircleRadius(3.5f);
        setAssignment.setDrawValues(false);

        LineDataSet setQuiz = new LineDataSet(quizEntries, getString(R.string.quiz));
        setQuiz.setColor(Color.parseColor("#F59E0B"));
        setQuiz.setLineWidth(2.5f);
        setQuiz.setCircleColor(Color.parseColor("#F59E0B"));
        setQuiz.setCircleRadius(3.5f);
        setQuiz.setDrawValues(false);

        LineDataSet setExam = new LineDataSet(examEntries, getString(R.string.exam));
        setExam.setColor(Color.parseColor("#7C3AED"));
        setExam.setLineWidth(2.5f);
        setExam.setCircleColor(Color.parseColor("#7C3AED"));
        setExam.setCircleRadius(3.5f);
        setExam.setDrawValues(false);

        LineData lineData = new LineData(setAssignment, setQuiz, setExam);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8"}));
        xAxis.setTextColor(Color.parseColor("#111B64"));
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(Color.parseColor("#6477B8"));
        leftAxis.setGridColor(Color.parseColor("#DCE6FF"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.parseColor("#111B64"));
        legend.setTextSize(10f);

        chart.animateX(800);
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
