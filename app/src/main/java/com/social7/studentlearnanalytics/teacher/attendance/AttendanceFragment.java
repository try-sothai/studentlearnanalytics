package com.social7.studentlearnanalytics.teacher.attendance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.reports.GenerateReportActivity;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabAttOverview, tabAttTake, tabAttRecords, tabAttStudent, tabAttReports;

    // Sub-Tab Containers
    private View secAttOverviewContainer, secAttTakeContainer, secAttRecordsContainer, secAttStudentContainer, secAttReportsContainer;

    // Sub-Tab 0: Overview
    private PieChart pieChartAttendance;

    // Sub-Tab 1: Take Attendance
    private RecyclerView rvTakeAttendanceRoster;
    private TakeAttendanceAdapter takeAttendanceAdapter;
    private final List<TakeAttendanceAdapter.StudentAttendanceItem> takeAttendanceList = new ArrayList<>();
    private MaterialButton btnTakeAttDatePicker, btnMarkAllPresent, btnSaveAttendance;
    private TextView tvTakeCountPresent, tvTakeCountAbsent, tvTakeCountLate, tvTakeCountExcused;
    private EditText edtSearchTakeAttendance;

    // Sub-Tab 2: Attendance Records
    private AttendanceAdapter recordsAdapter;
    private RecyclerView rvAttendanceRecords;
    private List<AttendanceAdapter.AttendanceRecordItem> recordList;
    private EditText edtSearchAttendanceRecords;

    // Sub-Tab 3: Student Attendance
    private RecyclerView rvStudentAttendanceRoster;
    private StudentAttendanceAdapter studentAttendanceAdapter;
    private final List<StudentAttendanceAdapter.StudentAttendanceRecord> studentAttendanceList = new ArrayList<>();
    private EditText edtSearchStudentAttendance;

    // Sub-Tab 4: Attendance Reports
    private MaterialButton btnExportAttendancePdf, btnExportAttendanceExcel;

    // Selected Sub-Tab Flag (0=Overview, 1=Take, 2=Records, 3=Student, 4=Reports)
    private int selectedSubTab = 0;

    public AttendanceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        // Header Integration
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view,
                    "Attendance Management",
                    "Manage daily attendance, roster, and attendance analytics"
            );
        }

        // Sub-Tab Buttons
        tabAttOverview = view.findViewById(R.id.tabAttOverview);
        tabAttTake = view.findViewById(R.id.tabAttTake);
        tabAttRecords = view.findViewById(R.id.tabAttRecords);
        tabAttStudent = view.findViewById(R.id.tabAttStudent);
        tabAttReports = view.findViewById(R.id.tabAttReports);

        // Containers
        secAttOverviewContainer = view.findViewById(R.id.secAttOverviewContainer);
        secAttTakeContainer = view.findViewById(R.id.secAttTakeContainer);
        secAttRecordsContainer = view.findViewById(R.id.secAttRecordsContainer);
        secAttStudentContainer = view.findViewById(R.id.secAttStudentContainer);
        secAttReportsContainer = view.findViewById(R.id.secAttReportsContainer);

        // Bind Sub-views
        bindOverviewViews(view);
        bindTakeAttendanceViews(view);
        bindRecordsViews(view);
        bindStudentAttendanceViews(view);
        bindReportsViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabAttOverview != null) tabAttOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabAttTake != null) tabAttTake.setOnClickListener(v -> selectSubTab(1));
        if (tabAttRecords != null) tabAttRecords.setOnClickListener(v -> selectSubTab(2));
        if (tabAttStudent != null) tabAttStudent.setOnClickListener(v -> selectSubTab(3));
        if (tabAttReports != null) tabAttReports.setOnClickListener(v -> selectSubTab(4));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secAttOverviewContainer != null) secAttOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secAttTakeContainer != null) secAttTakeContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secAttRecordsContainer != null) secAttRecordsContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secAttStudentContainer != null) secAttStudentContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secAttReportsContainer != null) secAttReportsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupOverviewPieChart();
        } else if (subIndex == 1) {
            setupTakeAttendanceList();
        } else if (subIndex == 2) {
            setupRecordsList();
        } else if (subIndex == 3) {
            setupStudentAttendanceRoster();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabAttOverview, tabAttTake, tabAttRecords, tabAttStudent, tabAttReports};
        for (int i = 0; i < tabs.length; i++) {
            TextView tv = tabs[i];
            if (tv == null) continue;
            if (i == activeIndex) {
                tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    // ==========================================
    // 0. OVERVIEW SUB-TAB
    // ==========================================
    private void bindOverviewViews(View root) {
        pieChartAttendance = root.findViewById(R.id.pieChartAttendance);
    }

    private void setupOverviewPieChart() {
        if (pieChartAttendance == null || pieChartAttendance.getData() != null) return;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(92.8f, "Present"));
        entries.add(new PieEntry(4.5f, "Absent"));
        entries.add(new PieEntry(1.8f, "Late"));
        entries.add(new PieEntry(0.9f, "Excused"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#10B981"), Color.parseColor("#EF4444"), Color.parseColor("#F59E0B"), Color.parseColor("#9333EA")
        });
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChartAttendance.setData(pieData);
        pieChartAttendance.setDrawHoleEnabled(true);
        pieChartAttendance.setHoleColor(Color.WHITE);
        pieChartAttendance.setHoleRadius(65f);
        pieChartAttendance.setTransparentCircleRadius(70f);
        pieChartAttendance.setCenterText("92.8%");
        pieChartAttendance.setCenterTextColor(Color.parseColor("#10165C"));
        pieChartAttendance.setCenterTextSize(22f);
        pieChartAttendance.getDescription().setEnabled(false);
        pieChartAttendance.getLegend().setEnabled(false);
        pieChartAttendance.animateY(800);
        pieChartAttendance.invalidate();
    }

    // ==========================================
    // 1. TAKE ATTENDANCE SUB-TAB
    // ==========================================
    private void bindTakeAttendanceViews(View root) {
        btnTakeAttDatePicker = root.findViewById(R.id.btnTakeAttDatePicker);
        btnMarkAllPresent = root.findViewById(R.id.btnMarkAllPresent);
        btnSaveAttendance = root.findViewById(R.id.btnSaveAttendance);
        rvTakeAttendanceRoster = root.findViewById(R.id.rvTakeAttendanceRoster);

        tvTakeCountPresent = root.findViewById(R.id.tvTakeCountPresent);
        tvTakeCountAbsent = root.findViewById(R.id.tvTakeCountAbsent);
        tvTakeCountLate = root.findViewById(R.id.tvTakeCountLate);
        tvTakeCountExcused = root.findViewById(R.id.tvTakeCountExcused);

        edtSearchTakeAttendance = root.findViewById(R.id.edtSearchTakeAttendance);
        if (edtSearchTakeAttendance != null) {
            edtSearchTakeAttendance.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (takeAttendanceAdapter != null) {
                        takeAttendanceAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnTakeAttDatePicker != null) {
            btnTakeAttDatePicker.setOnClickListener(v -> showDatePicker());
        }

        if (btnMarkAllPresent != null) {
            btnMarkAllPresent.setOnClickListener(v -> {
                if (takeAttendanceAdapter != null) {
                    takeAttendanceAdapter.markAllPresent();
                    updateTakeCounters();
                    Toast.makeText(requireContext(), "Marked all students as Present", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnSaveAttendance != null) {
            btnSaveAttendance.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirm Attendance Submission")
                        .setMessage("Submit attendance record for CS101 (BSIT 4-A) on " +
                                (btnTakeAttDatePicker != null ? btnTakeAttDatePicker.getText() : "May 12, 2026") + "?")
                        .setPositiveButton("Submit Attendance", (dialog, which) -> {
                            Toast.makeText(requireContext(), "Class attendance saved successfully!", Toast.LENGTH_LONG).show();
                            selectSubTab(2); // Navigate to Attendance Records
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void setupTakeAttendanceList() {
        if (rvTakeAttendanceRoster == null || takeAttendanceAdapter != null) return;

        takeAttendanceList.clear();
        takeAttendanceList.add(new TakeAttendanceAdapter.StudentAttendanceItem("STU23001", "Sophea", "P"));
        takeAttendanceList.add(new TakeAttendanceAdapter.StudentAttendanceItem("STU23002", "Dara", "A"));
        takeAttendanceList.add(new TakeAttendanceAdapter.StudentAttendanceItem("STU23003", "Rathana", "L"));
        takeAttendanceList.add(new TakeAttendanceAdapter.StudentAttendanceItem("STU23004", "Sokha", "P"));
        takeAttendanceList.add(new TakeAttendanceAdapter.StudentAttendanceItem("STU23005", "Bopha", "E"));

        takeAttendanceAdapter = new TakeAttendanceAdapter(requireContext(), takeAttendanceList, this::updateTakeCounters);
        rvTakeAttendanceRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTakeAttendanceRoster.setAdapter(takeAttendanceAdapter);

        updateTakeCounters();
    }

    private void updateTakeCounters() {
        if (takeAttendanceAdapter == null) return;
        int p = 0, a = 0, l = 0, e = 0;
        for (TakeAttendanceAdapter.StudentAttendanceItem item : takeAttendanceAdapter.getItems()) {
            if ("A".equalsIgnoreCase(item.status)) a++;
            else if ("L".equalsIgnoreCase(item.status)) l++;
            else if ("E".equalsIgnoreCase(item.status)) e++;
            else p++;
        }
        if (tvTakeCountPresent != null) tvTakeCountPresent.setText("P: " + p);
        if (tvTakeCountAbsent != null) tvTakeCountAbsent.setText("A: " + a);
        if (tvTakeCountLate != null) tvTakeCountLate.setText("L: " + l);
        if (tvTakeCountExcused != null) tvTakeCountExcused.setText("E: " + e);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(java.util.Locale.US, "%02d/%02d/%d", month + 1, dayOfMonth, year);
            if (btnTakeAttDatePicker != null) {
                btnTakeAttDatePicker.setText(selectedDate);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // ==========================================
    // 2. ATTENDANCE RECORDS SUB-TAB
    // ==========================================
    private void bindRecordsViews(View root) {
        rvAttendanceRecords = root.findViewById(R.id.rvAttendanceRecords);
        edtSearchAttendanceRecords = root.findViewById(R.id.edtSearchAttendanceRecords);

        if (edtSearchAttendanceRecords != null) {
            edtSearchAttendanceRecords.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (recordsAdapter != null) {
                        recordsAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        MaterialButton btnAdd = root.findViewById(R.id.btnAddAttendanceRecord);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showAddAttendanceDialog());
        }
    }

    private void setupRecordsList() {
        if (rvAttendanceRecords == null || recordsAdapter != null) return;

        rvAttendanceRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        recordList = getSampleAttendance();
        recordsAdapter = new AttendanceAdapter(requireContext(), recordList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Attendance Session Detail")
                    .setMessage("Date: " + item.date + "\n" +
                            "Course: CS101 - Intro to CS\n" +
                            "Class: BSIT 4-A\n" +
                            "Session Status: " + item.status + "\n" +
                            "Enrolled Roster: 35 Students\n" +
                            "Present: 32 • Absent: 2 • Late: 1")
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        rvAttendanceRecords.setAdapter(recordsAdapter);
    }

    private void showAddAttendanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Attendance Session");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText edtDate = new EditText(requireContext());
        edtDate.setHint("Date (e.g. May 12, 2026)");
        layout.addView(edtDate);

        final EditText edtStatus = new EditText(requireContext());
        edtStatus.setHint("Status (Present / Absent)");
        layout.addView(edtStatus);

        builder.setView(layout);

        builder.setPositiveButton("Record Session", (dialog, which) -> {
            String date = edtDate.getText().toString().trim();
            String status = edtStatus.getText().toString().trim();

            if (date.isEmpty()) date = "May 12, 2026";
            if (status.isEmpty()) status = "Present";

            if (recordList != null && recordsAdapter != null) {
                recordList.add(0, new AttendanceAdapter.AttendanceRecordItem(date, status));
                recordsAdapter.filter("");
                Toast.makeText(requireContext(), "Attendance session recorded successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private List<AttendanceAdapter.AttendanceRecordItem> getSampleAttendance() {
        List<AttendanceAdapter.AttendanceRecordItem> list = new ArrayList<>();
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 10, 2026", "Present"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 08, 2026", "Present"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 06, 2026", "Absent"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 03, 2026", "Present"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 01, 2026", "Present"));
        return list;
    }

    // ==========================================
    // 3. STUDENT ATTENDANCE SUB-TAB
    // ==========================================
    private void bindStudentAttendanceViews(View root) {
        rvStudentAttendanceRoster = root.findViewById(R.id.rvStudentAttendanceRoster);
        edtSearchStudentAttendance = root.findViewById(R.id.edtSearchStudentAttendance);

        if (edtSearchStudentAttendance != null) {
            edtSearchStudentAttendance.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (studentAttendanceAdapter != null) {
                        studentAttendanceAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupStudentAttendanceRoster() {
        if (rvStudentAttendanceRoster == null || studentAttendanceAdapter != null) return;

        studentAttendanceList.clear();
        studentAttendanceList.add(new StudentAttendanceAdapter.StudentAttendanceRecord("STU23001", "Sophea", "CS101 - Intro to CS • BSIT 4-A", "Present: 118 Days • Absent: 2 Days • Late: 0 • Excused: 0", "98.5% Rate"));
        studentAttendanceList.add(new StudentAttendanceAdapter.StudentAttendanceRecord("STU23002", "Dara", "IT402 - Web Systems • BSIT 4-B", "Present: 70 Days • Absent: 48 Days • Late: 2 • Excused: 0", "58.3% Low Attendance"));
        studentAttendanceList.add(new StudentAttendanceAdapter.StudentAttendanceRecord("STU23003", "Rathana", "CS204 - Algorithms • BSCS 3-A", "Present: 110 Days • Absent: 8 Days • Late: 2 • Excused: 0", "91.6% Rate"));
        studentAttendanceList.add(new StudentAttendanceAdapter.StudentAttendanceRecord("STU23004", "Sokha", "CS101 - Intro to CS • BSCS 3-B", "Present: 114 Days • Absent: 4 Days • Late: 2 • Excused: 0", "95.0% Rate"));
        studentAttendanceList.add(new StudentAttendanceAdapter.StudentAttendanceRecord("STU23005", "Bopha", "IS201 - Enterprise Arch • BSIS 2-A", "Present: 98 Days • Absent: 18 Days • Late: 4 • Excused: 0", "81.6% Rate"));

        studentAttendanceAdapter = new StudentAttendanceAdapter(requireContext(), studentAttendanceList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Student Attendance Breakdown")
                    .setMessage("Student ID: " + item.studentId + "\n" +
                            "Student Name: " + item.studentName + "\n" +
                            "Course & Class: " + item.courseClass + "\n\n" +
                            "Attendance Log:\n" +
                            item.counts + "\n\n" +
                            "Overall Attendance Rate: " + item.rateBadge)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvStudentAttendanceRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudentAttendanceRoster.setAdapter(studentAttendanceAdapter);
    }

    // ==========================================
    // 4. ATTENDANCE REPORTS SUB-TAB
    // ==========================================
    private void bindReportsViews(View root) {
        btnExportAttendancePdf = root.findViewById(R.id.btnExportAttendancePdf);
        btnExportAttendanceExcel = root.findViewById(R.id.btnExportAttendanceExcel);

        View.OnClickListener exportListener = v -> {
            Intent intent = new Intent(requireContext(), GenerateReportActivity.class);
            intent.putExtra("selected_report_type", "Attendance");
            startActivity(intent);
        };

        if (btnExportAttendancePdf != null) btnExportAttendancePdf.setOnClickListener(exportListener);
        if (btnExportAttendanceExcel != null) btnExportAttendanceExcel.setOnClickListener(exportListener);
    }
}
