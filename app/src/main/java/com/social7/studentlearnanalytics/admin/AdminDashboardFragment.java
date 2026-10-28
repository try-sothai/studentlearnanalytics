package com.social7.studentlearnanalytics.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "SLAS_ADMIN_DASH";

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
        fetchAdminDashboard(chart);

        setupCardClickListeners(view);

        return view;
    }

    private void fetchAdminDashboard(LineChart chart) {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getAdminDashboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        bindSummaryCounts(data);

                        List<Float> growthValues = new ArrayList<>();
                        List<String> growthLabels = new ArrayList<>();

                        if (data.has("user_growth_data") && data.get("user_growth_data").isJsonArray()) {
                            for (com.google.gson.JsonElement el : data.getAsJsonArray("user_growth_data")) {
                                growthValues.add(el.getAsFloat());
                            }
                        }
                        if (data.has("user_growth_labels") && data.get("user_growth_labels").isJsonArray()) {
                            for (com.google.gson.JsonElement el : data.getAsJsonArray("user_growth_labels")) {
                                growthLabels.add(el.getAsString());
                            }
                        }

                        setupGrowthChart(chart, growthValues, growthLabels);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing admin dashboard", e);
                        setupGrowthChart(chart, null, null);
                    }
                } else {
                    Log.d(TAG, "Admin dashboard HTTP code: " + response.code());
                    setupGrowthChart(chart, null, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.d(TAG, "Admin dashboard API failure: " + t.getMessage());
                setupGrowthChart(chart, null, null);
            }
        });
    }

    private void bindSummaryCounts(JsonObject data) {
        if (getView() == null || data == null) return;

        android.widget.TextView tvStudents = getView().findViewById(R.id.tvTotalStudentsCount);
        android.widget.TextView tvTeachers = getView().findViewById(R.id.tvTotalTeachersCount);
        android.widget.TextView tvCourses = getView().findViewById(R.id.tvTotalCoursesCount);
        android.widget.TextView tvActive = getView().findViewById(R.id.tvActiveUsersCount);

        if (tvStudents != null && data.has("total_students") && !data.get("total_students").isJsonNull()) {
            tvStudents.setText(data.get("total_students").getAsString());
        }
        if (tvTeachers != null && data.has("total_teachers") && !data.get("total_teachers").isJsonNull()) {
            tvTeachers.setText(data.get("total_teachers").getAsString());
        }
        if (tvCourses != null && data.has("total_courses") && !data.get("total_courses").isJsonNull()) {
            tvCourses.setText(data.get("total_courses").getAsString());
        }
        if (tvActive != null && data.has("total_departments") && !data.get("total_departments").isJsonNull()) {
            tvActive.setText(data.get("total_departments").getAsString());
        }
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

    private void setupGrowthChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            for (int i = 0; i < values.size(); i++) {
                entries.add(new Entry(i, values.get(i)));
            }
        } else {
            entries.add(new Entry(0, 100));
            entries.add(new Entry(1, 250));
            entries.add(new Entry(2, 400));
            entries.add(new Entry(3, 600));
            entries.add(new Entry(4, 900));
            entries.add(new Entry(5, 1234));
        }

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
        String[] xLabels = (labels != null && !labels.isEmpty())
                ? labels.toArray(new String[0])
                : new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        chart.animateX(1000);
        chart.invalidate();
    }
}
