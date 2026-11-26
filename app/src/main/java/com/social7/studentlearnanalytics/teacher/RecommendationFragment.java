package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationFragment extends Fragment {

    private TextView tabRecOverview, tabRecStudent, tabRecPriority, tabRecIntervention, tabRecHistory;
    private View secRecOverviewContainer, secRecStudentContainer, secRecPriorityContainer, secRecInterventionContainer, secRecHistoryContainer;

    private PieChart chartRecommendationPriorityPie;
    private MaterialButton btnGenerateRecs;
    private TextView tvRecTotalCount, tvRecHighCount, tvRecMedCount, tvRecLowCount, tvRecStudentsCount, tvRecCompletedCount;

    private RecyclerView rvStudentRecsList;
    private StudentRecommendationAdapter studentRecAdapter;
    private final List<StudentRecommendationAdapter.StudentRecommendationItem> studentRecList = new ArrayList<>();
    private EditText edtSearchStudentRecs;

    private RecyclerView rvPriorityRecsList;
    private StudentRecommendationAdapter priorityRecAdapter;
    private final List<StudentRecommendationAdapter.StudentRecommendationItem> priorityRecList = new ArrayList<>();

    private RecyclerView rvInterventionPlansList;
    private InterventionPlanAdapter interventionAdapter;
    private final List<InterventionPlanAdapter.InterventionPlanItem> interventionList = new ArrayList<>();
    private MaterialButton btnCreateInterventionPlan;

    private RecyclerView rvRecommendationHistoryList;
    private RecommendationHistoryAdapter historyAdapter;
    private final List<RecommendationHistoryAdapter.RecommendationHistoryItem> historyList = new ArrayList<>();
    private EditText edtSearchRecHistory;

    private int selectedSubTab = 0;
    private int defaultStudentDbId = 0;

    public RecommendationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.recommendations_management), "Academic guidance & student support plans"
            );
        }

        tabRecOverview = view.findViewById(R.id.tabRecOverview);
        tabRecStudent = view.findViewById(R.id.tabRecStudent);
        tabRecPriority = view.findViewById(R.id.tabRecPriority);
        tabRecIntervention = view.findViewById(R.id.tabRecIntervention);
        tabRecHistory = view.findViewById(R.id.tabRecHistory);

        secRecOverviewContainer = view.findViewById(R.id.secRecOverviewContainer);
        secRecStudentContainer = view.findViewById(R.id.secRecStudentContainer);
        secRecPriorityContainer = view.findViewById(R.id.secRecPriorityContainer);
        secRecInterventionContainer = view.findViewById(R.id.secRecInterventionContainer);
        secRecHistoryContainer = view.findViewById(R.id.secRecHistoryContainer);

        bindOverviewViews(view);
        bindStudentRecsViews(view);
        bindPriorityRecsViews(view);
        bindInterventionViews(view);
        bindHistoryViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchRecommendationsFromApi();

        return view;
    }

    private void setupSubTabListeners() {
        if (tabRecOverview != null) tabRecOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabRecStudent != null) tabRecStudent.setOnClickListener(v -> selectSubTab(1));
        if (tabRecPriority != null) tabRecPriority.setOnClickListener(v -> selectSubTab(2));
        if (tabRecIntervention != null) tabRecIntervention.setOnClickListener(v -> selectSubTab(3));
        if (tabRecHistory != null) tabRecHistory.setOnClickListener(v -> selectSubTab(4));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secRecOverviewContainer != null) secRecOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secRecStudentContainer != null) secRecStudentContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secRecPriorityContainer != null) secRecPriorityContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secRecInterventionContainer != null) secRecInterventionContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secRecHistoryContainer != null) secRecHistoryContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabRecOverview, tabRecStudent, tabRecPriority, tabRecIntervention, tabRecHistory};
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

    private void fetchRecommendationsFromApi() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherRiskPredictions().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                studentRecList.clear();
                priorityRecList.clear();
                interventionList.clear();
                historyList.clear();

                int highCnt = 0, medCnt = 0, lowCnt = 0;

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");

                    if (data.has("at_risk_students") && data.get("at_risk_students").isJsonArray()) {
                        JsonArray riskArr = data.getAsJsonArray("at_risk_students");
                        for (JsonElement el : riskArr) {
                            if (el.isJsonObject()) {
                                JsonObject ro = el.getAsJsonObject();
                                if (defaultStudentDbId == 0 && ro.has("student_id") && !ro.get("student_id").isJsonNull()) {
                                    defaultStudentDbId = ro.get("student_id").getAsInt();
                                }
                            }
                        }
                    }

                    if (data.has("recommendations") && data.get("recommendations").isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray("recommendations");
                        for (JsonElement el : arr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject o = el.getAsJsonObject();
                            String code = getStr(o, "student_code", "STU");
                            String name = getStr(o, "student_name", "Student");
                            String cCode = getStr(o, "course_code", "COURSE");
                            String title = getStr(o, "title", "Recommendation");
                            String msg = getStr(o, "message", "");
                            String prio = getStr(o, "priority", "Medium");
                            String status = getStr(o, "status", "Pending");
                            String date = getStr(o, "date", "-");

                            if ("High".equalsIgnoreCase(prio)) highCnt++;
                            else if ("Low".equalsIgnoreCase(prio)) lowCnt++;
                            else medCnt++;

                            StudentRecommendationAdapter.StudentRecommendationItem sr = new StudentRecommendationAdapter.StudentRecommendationItem(
                                    code, name, cCode, title, msg, prio + " Priority"
                            );
                            studentRecList.add(sr);
                            if ("High".equalsIgnoreCase(prio)) {
                                priorityRecList.add(sr);
                            }

                            interventionList.add(new InterventionPlanAdapter.InterventionPlanItem(
                                    code, name, "Support Plan: " + title, msg + " • " + date, status
                            ));

                            if ("Completed".equalsIgnoreCase(status)) {
                                historyList.add(new RecommendationHistoryAdapter.RecommendationHistoryItem(
                                        code, name, title, "Completed: " + date + " • Priority: " + prio, "Completed"
                                ));
                            }
                        }
                    }
                }

                bindOverviewPie(highCnt, medCnt, lowCnt);
                int totalRecs = highCnt + medCnt + lowCnt;
                if (tvRecTotalCount != null) tvRecTotalCount.setText(totalRecs + " Active");
                if (tvRecHighCount != null) tvRecHighCount.setText(highCnt + " Urgent");
                if (tvRecMedCount != null) tvRecMedCount.setText(medCnt + " Active");
                if (tvRecLowCount != null) tvRecLowCount.setText(lowCnt + " Advisory");
                if (tvRecStudentsCount != null) tvRecStudentsCount.setText(studentRecList.size() + " Students");
                if (tvRecCompletedCount != null) tvRecCompletedCount.setText(historyList.size() + " Plans");
                if (studentRecAdapter != null) studentRecAdapter.filter("");
                if (priorityRecAdapter != null) priorityRecAdapter.notifyDataSetChanged();
                if (interventionAdapter != null) interventionAdapter.notifyDataSetChanged();
                if (historyAdapter != null) historyAdapter.filter("");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                bindOverviewPie(0, 0, 0);
                if (tvRecTotalCount != null) tvRecTotalCount.setText("0 Active");
                if (tvRecHighCount != null) tvRecHighCount.setText("0 Urgent");
                if (tvRecMedCount != null) tvRecMedCount.setText("0 Active");
                if (tvRecLowCount != null) tvRecLowCount.setText("0 Advisory");
                if (tvRecStudentsCount != null) tvRecStudentsCount.setText("0 Students");
                if (tvRecCompletedCount != null) tvRecCompletedCount.setText("0 Plans");
                Toast.makeText(getContext(), "Network error loading recommendations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOverviewViews(View root) {
        chartRecommendationPriorityPie = root.findViewById(R.id.chartRecommendationPriorityPie);
        btnGenerateRecs = root.findViewById(R.id.btnGenerateRecs);
        tvRecTotalCount = root.findViewById(R.id.tvRecTotalCount);
        tvRecHighCount = root.findViewById(R.id.tvRecHighCount);
        tvRecMedCount = root.findViewById(R.id.tvRecMedCount);
        tvRecLowCount = root.findViewById(R.id.tvRecLowCount);
        tvRecStudentsCount = root.findViewById(R.id.tvRecStudentsCount);
        tvRecCompletedCount = root.findViewById(R.id.tvRecCompletedCount);

        if (chartRecommendationPriorityPie != null) {
            chartRecommendationPriorityPie.setNoDataText("Loading recommendations...");
        }

        if (btnGenerateRecs != null) {
            btnGenerateRecs.setOnClickListener(v -> showCreateInterventionPlanDialog());
        }
    }

    private void bindOverviewPie(int high, int med, int low) {
        if (chartRecommendationPriorityPie == null) return;
        int tot = high + med + low;
        if (tot == 0) {
            chartRecommendationPriorityPie.clear();
            chartRecommendationPriorityPie.setNoDataText("No academic recommendations recorded yet");
            chartRecommendationPriorityPie.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (high > 0) {
            entries.add(new PieEntry(high, "High Priority"));
            colors.add(Color.parseColor("#EF4444"));
        }
        if (med > 0) {
            entries.add(new PieEntry(med, "Medium Priority"));
            colors.add(Color.parseColor("#F59E0B"));
        }
        if (low > 0) {
            entries.add(new PieEntry(low, "Low Priority"));
            colors.add(Color.parseColor("#10B981"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        chartRecommendationPriorityPie.setData(pieData);
        chartRecommendationPriorityPie.setDrawHoleEnabled(true);
        chartRecommendationPriorityPie.setHoleColor(Color.WHITE);
        chartRecommendationPriorityPie.setHoleRadius(65f);
        chartRecommendationPriorityPie.setTransparentCircleRadius(70f);
        chartRecommendationPriorityPie.setCenterText(tot + "\nRecs");
        chartRecommendationPriorityPie.setCenterTextColor(Color.parseColor("#10165C"));
        chartRecommendationPriorityPie.setCenterTextSize(18f);
        chartRecommendationPriorityPie.getDescription().setEnabled(false);
        chartRecommendationPriorityPie.getLegend().setEnabled(false);
        chartRecommendationPriorityPie.animateY(600);
        ChartFontHelper.applyKhmerFont(chartRecommendationPriorityPie.getContext(), chartRecommendationPriorityPie);
        chartRecommendationPriorityPie.invalidate();
    }

    private void bindStudentRecsViews(View root) {
        rvStudentRecsList = root.findViewById(R.id.rvStudentRecsList);
        edtSearchStudentRecs = root.findViewById(R.id.edtSearchStudentRecs);

        if (rvStudentRecsList != null && getContext() != null) {
            studentRecAdapter = new StudentRecommendationAdapter(requireContext(), studentRecList, this::showStudentRecommendationDetailDialog);
            rvStudentRecsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvStudentRecsList.setAdapter(studentRecAdapter);
        }

        if (edtSearchStudentRecs != null) {
            edtSearchStudentRecs.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (studentRecAdapter != null) {
                        studentRecAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void showStudentRecommendationDetailDialog(StudentRecommendationAdapter.StudentRecommendationItem item) {
        if (!isAdded() || getContext() == null) return;

        String details = "Student Name: " + item.studentName + " (" + item.studentId + ")\n" +
                "Course: " + item.courseClass + "\n" +
                "Priority Status: " + item.priorityBadge + "\n\n" +
                "Recommendation:\n" +
                item.recommendationTitle + "\n\n" +
                item.academicStats;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.student_recommendation_detail)
                .setMessage(details)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void bindPriorityRecsViews(View root) {
        rvPriorityRecsList = root.findViewById(R.id.rvPriorityRecsList);
        if (rvPriorityRecsList != null && getContext() != null) {
            priorityRecAdapter = new StudentRecommendationAdapter(requireContext(), priorityRecList, this::showStudentRecommendationDetailDialog);
            rvPriorityRecsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvPriorityRecsList.setAdapter(priorityRecAdapter);
        }
    }

    private void bindInterventionViews(View root) {
        rvInterventionPlansList = root.findViewById(R.id.rvInterventionPlansList);
        btnCreateInterventionPlan = root.findViewById(R.id.btnCreateInterventionPlan);

        if (rvInterventionPlansList != null && getContext() != null) {
            interventionAdapter = new InterventionPlanAdapter(requireContext(), interventionList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.intervention_plan_status)
                        .setMessage(getString(R.string.student_name_4f287a) + item.studentName + " (" + item.studentId + ")\n" +
                                item.activityTitle + "\n" +
                                item.timeline + "\n\n" +
                                "Current Status: " + item.statusBadge)
                        .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvInterventionPlansList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvInterventionPlansList.setAdapter(interventionAdapter);
        }

        if (btnCreateInterventionPlan != null) {
            btnCreateInterventionPlan.setOnClickListener(v -> showCreateInterventionPlanDialog());
        }
    }

    private void showCreateInterventionPlanDialog() {
        if (getContext() == null) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.create_academic_support_plan);

        android.widget.LinearLayout container = new android.widget.LinearLayout(requireContext());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputLayout tilStudent = new TextInputLayout(requireContext());
        tilStudent.setHint("Student Database ID");
        TextInputEditText edtStudent = new TextInputEditText(tilStudent.getContext());
        if (defaultStudentDbId > 0) edtStudent.setText(String.valueOf(defaultStudentDbId));
        edtStudent.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        tilStudent.addView(edtStudent);
        container.addView(tilStudent);

        TextInputLayout tilActivity = new TextInputLayout(requireContext());
        tilActivity.setHint("Support Plan Title");
        TextInputEditText edtActivity = new TextInputEditText(tilActivity.getContext());
        tilActivity.addView(edtActivity);
        container.addView(tilActivity);

        builder.setView(container);

        builder.setPositiveButton(R.string.create_plan_4c7cd7, (dialog, which) -> {
            String sidStr = edtStudent.getText() != null ? edtStudent.getText().toString().trim() : "";
            String act = edtActivity.getText() != null ? edtActivity.getText().toString().trim() : "";
            if (sidStr.isEmpty() || act.isEmpty()) {
                Toast.makeText(requireContext(), "Student ID and support plan title are required", Toast.LENGTH_SHORT).show();
                return;
            }
            int sid;
            try {
                sid = Integer.parseInt(sidStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid student ID", Toast.LENGTH_SHORT).show();
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("student_id", sid);
            body.addProperty("title", act);
            body.addProperty("message", "Teacher academic support plan");
            body.addProperty("priority", "high");

            ApiClient.getApiService(requireContext()).createTeacherRecommendation(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded() || getContext() == null) return;
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.academic_support_plan_created_successfully, Toast.LENGTH_SHORT).show();
                        fetchRecommendationsFromApi();
                    } else {
                        Toast.makeText(requireContext(), "Failed to create support plan (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(requireContext(), "Network error creating support plan", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void bindHistoryViews(View root) {
        rvRecommendationHistoryList = root.findViewById(R.id.rvRecommendationHistoryList);
        edtSearchRecHistory = root.findViewById(R.id.edtSearchRecHistory);

        if (rvRecommendationHistoryList != null && getContext() != null) {
            historyAdapter = new RecommendationHistoryAdapter(requireContext(), historyList, item -> {});
            rvRecommendationHistoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvRecommendationHistoryList.setAdapter(historyAdapter);
        }

        if (edtSearchRecHistory != null) {
            edtSearchRecHistory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (historyAdapter != null) {
                        historyAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
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
}
