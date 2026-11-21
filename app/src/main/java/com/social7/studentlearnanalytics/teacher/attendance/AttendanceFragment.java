package com.social7.studentlearnanalytics.teacher.attendance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.ReportsFragment;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceFragment extends Fragment {

    private TextView tabAttOverview, tabAttTake, tabAttRecords, tabAttStudent, tabAttReports;
    private View secAttOverviewContainer, secAttTakeContainer, secAttRecordsContainer, secAttStudentContainer, secAttReportsContainer;

    private PieChart pieChartAttendance;

    private RecyclerView rvTakeAttendanceRoster;
    private TakeAttendanceAdapter takeAttendanceAdapter;
    private final List<TakeAttendanceAdapter.StudentAttendanceItem> takeAttendanceList = new ArrayList<>();
    private MaterialButton btnTakeAttDatePicker, btnMarkAllPresent, btnSaveAttendance;
    private TextView tvTakeCountPresent, tvTakeCountAbsent, tvTakeCountLate, tvTakeCountExcused;
    private EditText edtSearchTakeAttendance;

    private AttendanceAdapter recordsAdapter;
    private RecyclerView rvAttendanceRecords;
    private final List<AttendanceAdapter.AttendanceRecordItem> recordList = new ArrayList<>();
    private EditText edtSearchAttendanceRecords;

    private RecyclerView rvStudentAttendanceRoster;
    private StudentAttendanceAdapter studentAttendanceAdapter;
    private final List<StudentAttendanceAdapter.StudentAttendanceRecord> studentAttendanceList = new ArrayList<>();
    private EditText edtSearchStudentAttendance;

    private MaterialButton btnExportAttendancePdf, btnExportAttendanceExcel;

    private int selectedSubTab = 0;
    private int currentOfferingId = 0;
    private String selectedIsoDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

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

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.attendance_management), "Manage daily attendance, roster, and attendance analytics"
            );
        }

        tabAttOverview = view.findViewById(R.id.tabAttOverview);
        tabAttTake = view.findViewById(R.id.tabAttTake);
        tabAttRecords = view.findViewById(R.id.tabAttRecords);
        tabAttStudent = view.findViewById(R.id.tabAttStudent);
        tabAttReports = view.findViewById(R.id.tabAttReports);

        secAttOverviewContainer = view.findViewById(R.id.secAttOverviewContainer);
        secAttTakeContainer = view.findViewById(R.id.secAttTakeContainer);
        secAttRecordsContainer = view.findViewById(R.id.secAttRecordsContainer);
        secAttStudentContainer = view.findViewById(R.id.secAttStudentContainer);
        secAttReportsContainer = view.findViewById(R.id.secAttReportsContainer);

        bindOverviewViews(view);
        bindTakeAttendanceViews(view);
        bindRecordsViews(view);
        bindStudentAttendanceViews(view);
        bindReportsViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchAttendanceOverview();

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

    private void fetchAttendanceOverview() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherAttendanceOverview(currentOfferingId > 0 ? currentOfferingId : null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            JsonObject data = response.body().getAsJsonObject("data");
                            if (data.has("selected_offering_id") && !data.get("selected_offering_id").isJsonNull()) {
                                currentOfferingId = data.get("selected_offering_id").getAsInt();
                            }

                            bindOverviewChart(data.has("summary") && data.get("summary").isJsonObject()
                                    ? data.getAsJsonObject("summary") : null);
                            bindRoster(data.has("roster") && data.get("roster").isJsonArray()
                                    ? data.getAsJsonArray("roster") : null);
                            bindSessions(data.has("sessions") && data.get("sessions").isJsonArray()
                                    ? data.getAsJsonArray("sessions") : null);
                            bindStudentBreakdown(data.has("student_breakdown") && data.get("student_breakdown").isJsonArray()
                                    ? data.getAsJsonArray("student_breakdown") : null);
                        } else {
                            Toast.makeText(getContext(), "Unable to load attendance data", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "Network error loading attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindOverviewViews(View root) {
        pieChartAttendance = root.findViewById(R.id.pieChartAttendance);
        if (pieChartAttendance != null) {
            pieChartAttendance.setNoDataText("Loading attendance overview...");
        }
    }

    private void bindOverviewChart(JsonObject summary) {
        if (pieChartAttendance == null) return;
        if (summary == null) {
            pieChartAttendance.clear();
            pieChartAttendance.setNoDataText("No attendance records available");
            pieChartAttendance.invalidate();
            return;
        }

        int present = getInt(summary, "present");
        int absent = getInt(summary, "absent");
        int late = getInt(summary, "late");
        int excused = getInt(summary, "excused");
        int total = present + absent + late + excused;
        float rate = summary.has("rate") && !summary.get("rate").isJsonNull() ? summary.get("rate").getAsFloat() : 0f;

        if (total == 0) {
            pieChartAttendance.clear();
            pieChartAttendance.setNoDataText("No attendance sessions recorded yet");
            pieChartAttendance.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (present > 0) {
            entries.add(new PieEntry(present, "Present"));
            colors.add(Color.parseColor("#10B981"));
        }
        if (absent > 0) {
            entries.add(new PieEntry(absent, "Absent"));
            colors.add(Color.parseColor("#EF4444"));
        }
        if (late > 0) {
            entries.add(new PieEntry(late, "Late"));
            colors.add(Color.parseColor("#F59E0B"));
        }
        if (excused > 0) {
            entries.add(new PieEntry(excused, "Excused"));
            colors.add(Color.parseColor("#9333EA"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChartAttendance.setData(pieData);
        pieChartAttendance.setDrawHoleEnabled(true);
        pieChartAttendance.setHoleColor(Color.WHITE);
        pieChartAttendance.setHoleRadius(65f);
        pieChartAttendance.setTransparentCircleRadius(70f);
        pieChartAttendance.setCenterText(String.format(Locale.US, "%.1f%%", rate));
        pieChartAttendance.setCenterTextColor(Color.parseColor("#10165C"));
        pieChartAttendance.setCenterTextSize(22f);
        pieChartAttendance.getDescription().setEnabled(false);
        pieChartAttendance.getLegend().setEnabled(false);
        pieChartAttendance.animateY(600);
        ChartFontHelper.applyKhmerFont(pieChartAttendance.getContext(), pieChartAttendance);
        pieChartAttendance.invalidate();
    }

    private void bindTakeAttendanceViews(View root) {
        btnTakeAttDatePicker = root.findViewById(R.id.btnTakeAttDatePicker);
        btnMarkAllPresent = root.findViewById(R.id.btnMarkAllPresent);
        btnSaveAttendance = root.findViewById(R.id.btnSaveAttendance);
        rvTakeAttendanceRoster = root.findViewById(R.id.rvTakeAttendanceRoster);

        tvTakeCountPresent = root.findViewById(R.id.tvTakeCountPresent);
        tvTakeCountAbsent = root.findViewById(R.id.tvTakeCountAbsent);
        tvTakeCountLate = root.findViewById(R.id.tvTakeCountLate);
        tvTakeCountExcused = root.findViewById(R.id.tvTakeCountExcused);

        if (rvTakeAttendanceRoster != null && getContext() != null) {
            takeAttendanceAdapter = new TakeAttendanceAdapter(requireContext(), takeAttendanceList, this::updateTakeCounters);
            rvTakeAttendanceRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvTakeAttendanceRoster.setAdapter(takeAttendanceAdapter);
        }

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
            btnTakeAttDatePicker.setText(selectedIsoDate);
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
                if (currentOfferingId <= 0 || takeAttendanceList.isEmpty()) {
                    Toast.makeText(requireContext(), "No enrolled students available to record attendance.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirm_attendance_submission)
                        .setMessage("Submit attendance record on " + selectedIsoDate + "?")
                        .setPositiveButton(R.string.submit_attendance, (dialog, which) -> submitAttendanceToApi())
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
        }
    }

    private void bindRoster(JsonArray arr) {
        takeAttendanceList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String code = getStr(obj, "student_code", "STU");
                String name = getStr(obj, "name", "Student");
                String st = getStr(obj, "status", "P");
                TakeAttendanceAdapter.StudentAttendanceItem item = new TakeAttendanceAdapter.StudentAttendanceItem(code, name, st);
                item.dbId = getInt(obj, "student_id");
                takeAttendanceList.add(item);
            }
        }
        if (takeAttendanceAdapter != null) {
            takeAttendanceAdapter.filter("");
        }
        updateTakeCounters();
    }

    private void submitAttendanceToApi() {
        if (getContext() == null || currentOfferingId <= 0) return;

        JsonObject body = new JsonObject();
        body.addProperty("attendance_date", selectedIsoDate);
        JsonArray records = new JsonArray();
        for (TakeAttendanceAdapter.StudentAttendanceItem item : takeAttendanceAdapter.getItems()) {
            if (item.dbId <= 0) continue;
            JsonObject rec = new JsonObject();
            rec.addProperty("student_id", item.dbId);
            rec.addProperty("status", item.status);
            records.add(rec);
        }
        body.add("records", records);

        ApiClient.getApiService(requireContext()).submitTeacherAttendance(currentOfferingId, body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), R.string.class_attendance_saved_successfully, Toast.LENGTH_LONG).show();
                            fetchAttendanceOverview();
                            selectSubTab(2);
                        } else {
                            Toast.makeText(requireContext(), "Failed to save attendance (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(requireContext(), "Network error saving attendance", Toast.LENGTH_SHORT).show();
                    }
                });
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
            selectedIsoDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (btnTakeAttDatePicker != null) {
                btnTakeAttDatePicker.setText(selectedIsoDate);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void bindRecordsViews(View root) {
        rvAttendanceRecords = root.findViewById(R.id.rvAttendanceRecords);
        edtSearchAttendanceRecords = root.findViewById(R.id.edtSearchAttendanceRecords);

        if (rvAttendanceRecords != null && getContext() != null) {
            rvAttendanceRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
            recordsAdapter = new AttendanceAdapter(requireContext(), recordList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.attendance_session_detail)
                        .setMessage(getString(R.string.date_7ae482) + item.date + "\n" +
                                "Session Status: " + item.status)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvAttendanceRecords.setAdapter(recordsAdapter);
        }

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
            btnAdd.setOnClickListener(v -> selectSubTab(1));
        }
    }

    private void bindSessions(JsonArray arr) {
        recordList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String date = getStr(obj, "date", "-");
                String summary = getStr(obj, "summary", getStr(obj, "status", "Completed"));
                recordList.add(new AttendanceAdapter.AttendanceRecordItem(date, summary));
            }
        }
        if (recordsAdapter != null) {
            recordsAdapter.filter("");
        }
    }

    private void bindStudentAttendanceViews(View root) {
        rvStudentAttendanceRoster = root.findViewById(R.id.rvStudentAttendanceRoster);
        edtSearchStudentAttendance = root.findViewById(R.id.edtSearchStudentAttendance);

        if (rvStudentAttendanceRoster != null && getContext() != null) {
            studentAttendanceAdapter = new StudentAttendanceAdapter(requireContext(), studentAttendanceList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.student_attendance_breakdown)
                        .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                                "Student Name: " + item.studentName + "\n" +
                                "Course & Class: " + item.courseClass + "\n\n" +
                                "Attendance Log:\n" +
                                item.counts + "\n\n" +
                                "Overall Attendance Rate: " + item.rateBadge)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvStudentAttendanceRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvStudentAttendanceRoster.setAdapter(studentAttendanceAdapter);
        }

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

    private void bindStudentBreakdown(JsonArray arr) {
        studentAttendanceList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String code = getStr(obj, "student_code", "STU");
                String name = getStr(obj, "name", "Student");
                String cls = getStr(obj, "class_name", "Class");
                int p = getInt(obj, "present");
                int a = getInt(obj, "absent");
                int l = getInt(obj, "late");
                int rate = getInt(obj, "rate");
                String counts = "Present: " + p + " • Absent: " + a + " • Late: " + l;
                String badge = rate + "% Rate";
                studentAttendanceList.add(new StudentAttendanceAdapter.StudentAttendanceRecord(code, name, cls, counts, badge));
            }
        }
        if (studentAttendanceAdapter != null) {
            studentAttendanceAdapter.filter("");
        }
    }

    private void bindReportsViews(View root) {
        btnExportAttendancePdf = root.findViewById(R.id.btnExportAttendancePdf);
        btnExportAttendanceExcel = root.findViewById(R.id.btnExportAttendanceExcel);

        View.OnClickListener openTeacherReports = v -> {
            if (getActivity() instanceof TeacherMainActivity) {
                ((TeacherMainActivity) getActivity()).openFragment(new ReportsFragment());
            }
        };

        if (btnExportAttendancePdf != null) btnExportAttendancePdf.setOnClickListener(openTeacherReports);
        if (btnExportAttendanceExcel != null) btnExportAttendanceExcel.setOnClickListener(openTeacherReports);
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private int getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }
}
