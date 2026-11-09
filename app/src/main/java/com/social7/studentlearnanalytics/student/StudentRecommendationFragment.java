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
                    view,
                    "My Recommendations",
                    "Personalized AI study recommendations"
            );
        }

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
                        .setTitle("Recommendation Details & Action Plan")
                        .setMessage("Course: " + item.courseCode + "\n" +
                                item.title + "\n\n" +
                                "AI Diagnostic Explanation:\n" +
                                item.description + "\n\n" +
                                "Suggested Action Step:\n" +
                                "Access online learning resources and review materials for " + item.courseCode)
                        .setPositiveButton("Open Course Materials", (dialog, which) -> {
                            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
                            intent.putExtra("course_code", item.courseCode);
                            startActivity(intent);
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }

            @Override
            public void onMarkCompletedClick(StudentRecCardAdapter.StudentRecItem item) {
                if (getContext() == null) return;
                JsonObject body = new JsonObject();
                body.addProperty("status", "completed");

                ApiClient.getApiService(requireContext()).updateRecommendation(item.id, body)
                        .enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                if (!isAdded()) return;
                                item.statusBadge = "Completed";
                                if (adapter != null) adapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), "Marked recommendation as completed!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                if (!isAdded()) return;
                                item.statusBadge = "Completed";
                                if (adapter != null) adapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), "Updated recommendation status", Toast.LENGTH_SHORT).show();
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
                            recommendationList.clear();
                            for (JsonElement re : recArray) {
                                if (!re.isJsonObject()) continue;
                                JsonObject ro = re.getAsJsonObject();

                                String id = ro.has("id") ? ro.get("id").getAsString() : "0";
                                String courseCode = "CS101";
                                if (ro.has("course") && ro.get("course").isJsonObject()) {
                                    JsonObject co = ro.getAsJsonObject("course");
                                    if (co.has("code") && !co.get("code").isJsonNull()) courseCode = co.get("code").getAsString();
                                } else if (ro.has("course_code") && !ro.get("course_code").isJsonNull()) {
                                    courseCode = ro.get("course_code").getAsString();
                                }

                                String title = ro.has("title") && !ro.get("title").isJsonNull()
                                        ? ro.get("title").getAsString()
                                        : (ro.has("recommendation_type") ? capitalize(ro.get("recommendation_type").getAsString()) : "Study Recommendation");

                                String desc = ro.has("recommendation_text") && !ro.get("recommendation_text").isJsonNull()
                                        ? ro.get("recommendation_text").getAsString()
                                        : (ro.has("description") ? ro.get("description").getAsString() : "Follow recommended learning path.");

                                String priority = ro.has("priority") && !ro.get("priority").isJsonNull()
                                        ? capitalize(ro.get("priority").getAsString()) + " Priority" : "High Priority";

                                String status = ro.has("status") && !ro.get("status").isJsonNull()
                                        ? capitalize(ro.get("status").getAsString()) : "In Progress";

                                recommendationList.add(new StudentRecCardAdapter.StudentRecItem(
                                        id, courseCode, title, desc, priority, status
                                ));
                            }

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recommendations", e);
                    }
                }

                // If empty from recommendations endpoint, fetch from dashboard
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
                            if (recs.size() > 0) {
                                recommendationList.clear();
                                for (JsonElement re : recs) {
                                    if (!re.isJsonObject()) continue;
                                    JsonObject ro = re.getAsJsonObject();

                                    String id = ro.has("id") ? ro.get("id").getAsString() : "0";
                                    String courseCode = "CS101";
                                    if (ro.has("course") && ro.get("course").isJsonObject()) {
                                        JsonObject co = ro.getAsJsonObject("course");
                                        if (co.has("code") && !co.get("code").isJsonNull()) courseCode = co.get("code").getAsString();
                                    }

                                    String title = ro.has("title") && !ro.get("title").isJsonNull()
                                            ? ro.get("title").getAsString()
                                            : "Study Recommendation";

                                    String desc = ro.has("recommendation_text") && !ro.get("recommendation_text").isJsonNull()
                                            ? ro.get("recommendation_text").getAsString()
                                            : "Targeted remedial activities to improve your academic performance.";

                                    String priority = ro.has("priority") && !ro.get("priority").isJsonNull()
                                            ? capitalize(ro.get("priority").getAsString()) + " Priority" : "High Priority";

                                    String status = ro.has("status") && !ro.get("status").isJsonNull()
                                            ? capitalize(ro.get("status").getAsString()) : "In Progress";

                                    recommendationList.add(new StudentRecCardAdapter.StudentRecItem(
                                            id, courseCode, title, desc, priority, status
                                    ));
                                }

                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing dashboard recommendations", e);
                    }
                }

                loadFallbackRecommendations();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                loadFallbackRecommendations();
            }
        });
    }

    private void loadFallbackRecommendations() {
        recommendationList.clear();
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("1", "CS101", "Review Weak Topic: Linked Lists & Pointers", "Reviewing Chapter 4 practice exercises can increase your predicted score by +15%.", "High Priority", "In Progress"));
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("2", "IT402", "Improve Attendance Rate in Web Development", "Attending the next 3 consecutive lectures will prevent academic risk alerts.", "High Priority", "In Progress"));
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("3", "CS204", "Practice Exercises for Recursion & Sorting", "Completing additional coding exercises will boost your final grade.", "Medium Priority", "In Progress"));
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("4", "IS201", "Midterm Review: Database Architecture", "Review study guide for Module 3 to maintain your current GPA.", "Low Priority", "In Progress"));

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
