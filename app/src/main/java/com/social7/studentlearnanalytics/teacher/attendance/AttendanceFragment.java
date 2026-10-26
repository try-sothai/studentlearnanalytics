package com.social7.studentlearnanalytics.teacher.attendance;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import java.util.ArrayList;
import java.util.List;

public class AttendanceFragment extends Fragment {

    private AttendanceAdapter adapter;
    private List<AttendanceAdapter.AttendanceRecordItem> recordList;

    public AttendanceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_attendance,
                container,
                false
        );

        ImageView btnMenu = view.findViewById(R.id.btnTeacherMenuAttendance);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                }
            });
        }

        PieChart pieChart = view.findViewById(R.id.pieChartAttendance);
        if (pieChart != null) {
            setupPieChart(pieChart);
        }

        RecyclerView rvRecords = view.findViewById(R.id.rvAttendanceRecords);
        if (rvRecords != null) {
            rvRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
            recordList = getSampleAttendance();
            adapter = new AttendanceAdapter(requireContext(), recordList);
            rvRecords.setAdapter(adapter);
        }

        MaterialButton btnAdd = view.findViewById(R.id.btnAddAttendanceRecord);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showAddAttendanceDialog());
        }

        return view;
    }

    private void setupPieChart(PieChart chart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(88f, "Present"));
        entries.add(new PieEntry(12f, "Absent"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#10B981"), Color.parseColor("#EF4444")});
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setHoleRadius(65f);
        chart.setTransparentCircleRadius(70f);
        chart.setCenterText("88%");
        chart.setCenterTextColor(Color.parseColor("#1D1F2A"));
        chart.setCenterTextSize(22f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    private void showAddAttendanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Attendance Session");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText edtDate = new EditText(requireContext());
        edtDate.setHint("Date (e.g. May 12, 2024)");
        layout.addView(edtDate);

        final EditText edtStatus = new EditText(requireContext());
        edtStatus.setHint("Status (Present / Absent)");
        layout.addView(edtStatus);

        builder.setView(layout);

        builder.setPositiveButton("Record", (dialog, which) -> {
            String date = edtDate.getText().toString().trim();
            String status = edtStatus.getText().toString().trim();

            if (date.isEmpty()) date = "May 12, 2024";
            if (status.isEmpty()) status = "Present";

            if (recordList != null && adapter != null) {
                recordList.add(0, new AttendanceAdapter.AttendanceRecordItem(date, status));
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Attendance recorded", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private List<AttendanceAdapter.AttendanceRecordItem> getSampleAttendance() {
        List<AttendanceAdapter.AttendanceRecordItem> list = new ArrayList<>();
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 01, 2024", "Present"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 03, 2024", "Present"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 06, 2024", "Absent"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 08, 2024", "Present"));
        list.add(new AttendanceAdapter.AttendanceRecordItem("May 10, 2024", "Present"));
        return list;
    }
}
