package com.social7.studentlearnanalytics.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {

    public AdminDashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_admin_dashboard,
                container,
                false
        );

        LineChart chart = view.findViewById(R.id.adminGrowthChart);
        setupGrowthChart(chart);

        setupCardClickListeners(view);

        return view;
    }

    private void setupCardClickListeners(View view) {
        View btnMenu = view.findViewById(R.id.btnAdminMenu);
        View btnNotif = view.findViewById(R.id.btnNotificationBell);
        View cardAvatar = view.findViewById(R.id.cardAvatar);
        View cardStudents = view.findViewById(R.id.cardDashStudents);
        View cardTeachers = view.findViewById(R.id.cardDashTeachers);
        View cardCourses = view.findViewById(R.id.cardDashCourses);
        View cardActiveUsers = view.findViewById(R.id.cardDashActiveUsers);
        View cardStatus = view.findViewById(R.id.cardDashSystemStatus);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new NotificationFragment());
                }
            });
        }

        if (cardAvatar != null) {
            cardAvatar.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new AdminProfileFragment());
                }
            });
        }

        if (cardStudents != null) {
            cardStudents.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_users);
                }
            });
        }

        if (cardTeachers != null) {
            cardTeachers.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_users);
                }
            });
        }

        if (cardCourses != null) {
            cardCourses.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_courses);
                }
            });
        }

        if (cardActiveUsers != null) {
            cardActiveUsers.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_users);
                }
            });
        }

        if (cardStatus != null) {
            cardStatus.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new SystemStatisticsFragment());
                }
            });
        }
    }

    private void setupGrowthChart(LineChart chart) {
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 100));
        entries.add(new Entry(1, 250));
        entries.add(new Entry(2, 400));
        entries.add(new Entry(3, 600));
        entries.add(new Entry(4, 900));
        entries.add(new Entry(5, 1234));

        LineDataSet dataSet = new LineDataSet(entries, "User Growth");
        dataSet.setColor(Color.parseColor("#5B3DF5"));
        dataSet.setCircleColor(Color.parseColor("#5B3DF5"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#EFEBFF"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"}));
        xAxis.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        chart.animateX(1000);
        chart.invalidate();
    }
}
