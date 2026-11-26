package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentRecommendationFragment extends Fragment {

    private static final String TAG = "StudentRecFragment";

    private RecyclerView rvStudentRecommendationsList;
    private StudentRecCardAdapter adapter;
    private final List<StudentRecCardAdapter.StudentRecItem> recommendationList = new ArrayList<>();
    private EditText edtSearchStudentRecommendations;
    private TextView tvRecActiveCount, tvRecHighPriorityCount, tvRecTotalCount, tvRecCompletedCount, tvEmptyRecommendations;

    public StudentRecommendationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_recommendations, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.my_recommendations), "Personalized AI study recommendations"
            );
        }

        tvRecActiveCount = view.findViewById(R.id.tvRecActiveCount);
        tvRecHighPriorityCount = view.findViewById(R.id.tvRecHighPriorityCount);
        tvRecTotalCount = view.findViewById(R.id.tvRecTotalCount);
        tvRecCompletedCount = view.findViewById(R.id.tvRecCompletedCount);
        tvEmptyRecommendations = view.findViewById(R.id.tvEmptyRecommendations);

        edtSearchStudentRecommendations = view.findViewById(R.id.edtSearchStudentRecommendations);
        if (edtSearchStudentRecommendations != null) {
            edtSearchStudentRecommendations.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString(), "All");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rvStudentRecommendationsList = view.findViewById(R.id.rvStudentRecommendationsList);
        if (rvStudentRecommendationsList != null) {
            rvStudentRecommendationsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            setupAdapter();
        }

        loadStudentRecommendations();

        return view;
    }

    private void setupAdapter() {
        adapter = new StudentRecCardAdapter(requireContext(), recommendationList, new StudentRecCardAdapter.OnRecCardClickListener() {
            @Override
            public void onReviewTopicClick(StudentRecCardAdapter.StudentRecItem item) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.recommendation_details_action_plan)
                        .setMessage(getString(R.string.course_2f9b25) + item.courseCode + (item.courseName != null && !item.courseName.isEmpty() ? " - " + item.courseName : "") + "\n" +
                                item.title + "\n\n" +
                                "AI Diagnostic Explanation:\n" +
                                item.description + "\n\n" +
                                "Suggested Action Step:\n" +
                                "Review course materials, assessments, and practice exercises for " + item.courseCode + ".")
                        .setPositiveButton(R.string.open_course_details, (dialog, which) -> {
                            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
                            if (item.courseId != null && !item.courseId.isEmpty()) {
                                intent.putExtra("course_id", item.courseId);
                            }
                            intent.putExtra("course_code", item.courseCode);
                            intent.putExtra("course", (item.courseCode != null ? item.courseCode : "") +
                                    (item.courseName != null && !item.courseName.isEmpty() ? " - " + item.courseName : ""));
                            startActivity(intent);
                        })
                        .setNeutralButton("Full Action Plan", (dialog, which) ->
                                startActivity(new Intent(requireContext(), RecommendationDetailActivity.class)))
                        .setNegativeButton(R.string.close, null)
                        .show();
            }

            @Override
            public void onMarkCompletedClick(StudentRecCardAdapter.StudentRecItem item) {
                if (getContext() == null) return;
                JsonObject body = new JsonObject();
                body.addProperty("status", "completed");
                body.addProperty("is_read", true);

                ApiClient.getApiService(requireContext()).updateRecommendation(item.id, body)
                        .enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                if (!isAdded()) return;
                                if (response.isSuccessful()) {
                                    item.statusBadge = "Completed";
                                    if (adapter != null) adapter.refresh();
                                    updateSummaryMetrics();
                                    Toast.makeText(requireContext(), "Marked recommendation as completed!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Could not update recommendation (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Could not update recommendation status", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        if (rvStudentRecommendationsList != null) {
            rvStudentRecommendationsList.setAdapter(adapter);
        }
    }

    private void loadStudentRecommendations() {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(getContext()).getRecommendations().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonArray recArray = null;
                        if (root.has("data") && root.get("data").isJsonArray()) {
                            recArray = root.getAsJsonArray("data");
                        } else if (root.has("recommendations") && root.get("recommendations").isJsonArray()) {
                            recArray = root.getAsJsonArray("recommendations");
                        }

                        if (recArray != null && recArray.size() > 0) {
                            parseRecommendationArray(recArray);
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recommendations", e);
                    }
                }

                loadFromDashboard();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                loadFromDashboard();
            }
        });
    }

    private void loadFromDashboard() {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(getContext()).getStudentDashboard(null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        if (data.has("recommendations") && data.get("recommendations").isJsonArray()) {
                            JsonArray recs = data.getAsJsonArray("recommendations");
                            parseRecommendationArray(recs);
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing dashboard recommendations", e);
                    }
                }

                recommendationList.clear();
                if (adapter != null) adapter.refresh();
                updateSummaryMetrics();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                recommendationList.clear();
                if (adapter != null) adapter.refresh();
                updateSummaryMetrics();
            }
        });
    }

    private void parseRecommendationArray(JsonArray recArray) {
        recommendationList.clear();
        if (recArray != null) {
            for (JsonElement re : recArray) {
                if (!re.isJsonObject()) continue;
                JsonObject ro = re.getAsJsonObject();

                String id = ro.has("id") && !ro.get("id").isJsonNull() ? ro.get("id").getAsString() : "0";
                String courseId = ro.has("course_id") && !ro.get("course_id").isJsonNull() ? ro.get("course_id").getAsString() : "";
                String courseCode = "General";
                String courseName = "";
                if (ro.has("course") && ro.get("course").isJsonObject()) {
                    JsonObject co = ro.getAsJsonObject("course");
                    if (courseId.isEmpty() && co.has("id") && !co.get("id").isJsonNull()) {
                        courseId = co.get("id").getAsString();
                    }
                    if (co.has("course_code") && !co.get("course_code").isJsonNull()) {
                        courseCode = co.get("course_code").getAsString();
                    } else if (co.has("code") && !co.get("code").isJsonNull()) {
                        courseCode = co.get("code").getAsString();
                    }
                    if (co.has("course_name") && !co.get("course_name").isJsonNull()) {
                        courseName = co.get("course_name").getAsString();
                    } else if (co.has("name") && !co.get("name").isJsonNull()) {
                        courseName = co.get("name").getAsString();
                    }
                } else if (ro.has("course_code") && !ro.get("course_code").isJsonNull()) {
                    courseCode = ro.get("course_code").getAsString();
                }

                String title = ro.has("title") && !ro.get("title").isJsonNull()
                        ? ro.get("title").getAsString()
                        : (ro.has("recommendation_type") && !ro.get("recommendation_type").isJsonNull()
                        ? capitalize(ro.get("recommendation_type").getAsString()) + " Recommendation"
                        : "Study Recommendation");

                String desc = ro.has("recommendation_text") && !ro.get("recommendation_text").isJsonNull()
                        ? ro.get("recommendation_text").getAsString()
                        : (ro.has("description") && !ro.get("description").isJsonNull()
                        ? ro.get("description").getAsString()
                        : "Follow recommended learning path.");

                String priorityRaw = ro.has("priority") && !ro.get("priority").isJsonNull()
                        ? ro.get("priority").getAsString() : "medium";
                String priority = capitalize(priorityRaw) + " Priority";

                String statusRaw = ro.has("status") && !ro.get("status").isJsonNull()
                        ? ro.get("status").getAsString() : "";
                boolean isRead = ro.has("is_read") && !ro.get("is_read").isJsonNull() && ro.get("is_read").getAsBoolean();
                String status;
                if (!statusRaw.isEmpty()) {
                    status = capitalize(statusRaw);
                } else {
                    status = isRead ? "Completed" : "In Progress";
                }

                StudentRecCardAdapter.StudentRecItem item = new StudentRecCardAdapter.StudentRecItem(
                        id, courseCode, title, desc, priority, status
                );
                item.courseId = courseId;
                item.courseName = courseName;
                recommendationList.add(item);
            }
        }

        if (adapter != null) {
            adapter.refresh();
        }
        updateSummaryMetrics();
    }

    private void updateSummaryMetrics() {
        int total = recommendationList.size();
        int active = 0;
        int highPriority = 0;
        int completed = 0;

        for (StudentRecCardAdapter.StudentRecItem item : recommendationList) {
            boolean isDone = "Completed".equalsIgnoreCase(item.statusBadge) || "Read".equalsIgnoreCase(item.statusBadge);
            if (isDone) {
                completed++;
            } else {
                active++;
            }
            if (item.priorityBadge != null && item.priorityBadge.toLowerCase().contains("high")) {
                highPriority++;
            }
        }

        if (tvRecActiveCount != null) tvRecActiveCount.setText(active + " Active");
        if (tvRecHighPriorityCount != null) tvRecHighPriorityCount.setText(highPriority + " Urgent");
        if (tvRecTotalCount != null) tvRecTotalCount.setText(total + " Total");
        if (tvRecCompletedCount != null) tvRecCompletedCount.setText(completed + " Done");
        if (tvEmptyRecommendations != null) {
            tvEmptyRecommendations.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
