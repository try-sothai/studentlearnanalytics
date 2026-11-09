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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentCoursesFragment extends Fragment {

    private static final String TAG = "StudentCoursesFragment";

    private ImageView btnSearch;
    private View layoutSearchBox, cardSemesterSelector;
    private EditText edtSearchCourses;
    private TextView tvSemesterText, tvStatCourseCount, tvStatGpa, tvStatAverage, tvStatCompleted;
    private RecyclerView rvStudentCoursesList;
    private StudentCourseCardAdapter adapter;
    private final List<StudentCourseCardAdapter.StudentCourseItem> courseList = new ArrayList<>();
    private final List<SemesterOption> availableSemesters = new ArrayList<>();
    private Integer currentSemesterId = null;

    private static class SemesterOption {
        int id;
        String name;
        SemesterOption(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public StudentCoursesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_courses, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view,
                    "My Courses",
                    "Manage and track your enrolled courses"
            );
        }

        btnSearch = view.findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setVisibility(View.VISIBLE);
        }
        layoutSearchBox = view.findViewById(R.id.layoutSearchBox);
        edtSearchCourses = view.findViewById(R.id.edtSearchCourses);

        if (btnSearch != null && layoutSearchBox != null) {
            btnSearch.setOnClickListener(v -> {
                if (layoutSearchBox.getVisibility() == View.VISIBLE) {
                    layoutSearchBox.setVisibility(View.GONE);
                } else {
                    layoutSearchBox.setVisibility(View.VISIBLE);
                    if (edtSearchCourses != null) edtSearchCourses.requestFocus();
                }
            });
        }

        if (edtSearchCourses != null) {
            edtSearchCourses.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        tvSemesterText = view.findViewById(R.id.tvSemesterText);
        cardSemesterSelector = view.findViewById(R.id.cardSemesterSelector);
        if (cardSemesterSelector != null) {
            cardSemesterSelector.setOnClickListener(this::showSemesterDropdown);
        }

        tvStatCourseCount = view.findViewById(R.id.tvStatCourseCount);
        tvStatGpa = view.findViewById(R.id.tvStatGpa);
        tvStatAverage = view.findViewById(R.id.tvStatAverage);
        tvStatCompleted = view.findViewById(R.id.tvStatCompleted);

        rvStudentCoursesList = view.findViewById(R.id.rvStudentCoursesList);
        if (rvStudentCoursesList != null) {
            rvStudentCoursesList.setLayoutManager(new LinearLayoutManager(requireContext()));
            setupAdapter();
        }

        loadStudentCourses();

        return view;
    }

    private void setupAdapter() {
        adapter = new StudentCourseCardAdapter(requireContext(), courseList, item -> {
            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
            intent.putExtra("course_id", item.id);
            intent.putExtra("course_code", item.code);
            intent.putExtra("course_title", item.name);
            intent.putExtra("instructor", item.instructor);

            int credits = 3;
            try {
                if (item.credits != null) {
                    String num = item.credits.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) credits = Integer.parseInt(num);
                }
            } catch (Exception ignored) {}

            Course c = new Course(
                    item.id,
                    item.code,
                    item.name,
                    item.instructor,
                    tvSemesterText != null ? tvSemesterText.getText().toString() : "Current Semester",
                    credits,
                    "Room A101",
                    "Mon, Wed 08:00–09:30",
                    35,
                    item.progressPct,
                    "Active"
            );
            intent.putExtra("course", c);
            startActivity(intent);
        });

        if (rvStudentCoursesList != null) {
            rvStudentCoursesList.setAdapter(adapter);
        }
    }

    private void showSemesterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "All Semesters");

        for (int i = 0; i < availableSemesters.size(); i++) {
            SemesterOption sem = availableSemesters.get(i);
            popup.getMenu().add(0, sem.id, i + 1, sem.name);
        }

        popup.setOnMenuItemClickListener(item -> {
            if (tvSemesterText != null) {
                tvSemesterText.setText(item.getTitle());
            }
            if (item.getItemId() == 0) {
                currentSemesterId = null;
            } else {
                currentSemesterId = item.getItemId();
            }
            loadStudentCourses();
            return true;
        });
        popup.show();
    }

    private void loadStudentCourses() {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(getContext()).getStudentDashboard(currentSemesterId, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JsonObject root = response.body();
                                JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                        ? root.getAsJsonObject("data") : root;

                                // Parse Semesters
                                if (data.has("semesters") && data.get("semesters").isJsonArray()) {
                                    availableSemesters.clear();
                                    JsonArray semArray = data.getAsJsonArray("semesters");
                                    for (JsonElement se : semArray) {
                                        if (se.isJsonObject()) {
                                            JsonObject so = se.getAsJsonObject();
                                            int sId = so.has("id") ? so.get("id").getAsInt() : 0;
                                            String sName = so.has("name") ? so.get("name").getAsString() : "Semester";
                                            if (sId > 0) availableSemesters.add(new SemesterOption(sId, sName));
                                        }
                                    }
                                }

                                // Parse Enrollments
                                courseList.clear();
                                float totalScore = 0f;
                                int scoredCourses = 0;
                                int completedCount = 0;

                                if (data.has("enrollments") && data.get("enrollments").isJsonArray()) {
                                    JsonArray enrollments = data.getAsJsonArray("enrollments");
                                    for (JsonElement el : enrollments) {
                                        if (!el.isJsonObject()) continue;
                                        JsonObject eo = el.getAsJsonObject();

                                        String courseId = eo.has("course_id") ? eo.get("course_id").getAsString() : "0";
                                        int score = eo.has("final_score") && !eo.get("final_score").isJsonNull() ? eo.get("final_score").getAsInt() : 80;
                                        String status = eo.has("status") && !eo.get("status").isJsonNull() ? eo.get("status").getAsString() : "enrolled";

                                        String code = "CRS" + courseId;
                                        String title = "Course";
                                        String instructor = "Faculty Lecturer";
                                        String credits = "3 Credits";
                                        String category = "Core Course";

                                        if (eo.has("course") && eo.get("course").isJsonObject()) {
                                            JsonObject co = eo.getAsJsonObject("course");
                                            if (co.has("id")) courseId = co.get("id").getAsString();
                                            if (co.has("code") && !co.get("code").isJsonNull()) code = co.get("code").getAsString();
                                            if (co.has("name") && !co.get("name").isJsonNull()) title = co.get("name").getAsString();
                                            if (co.has("credits") && !co.get("credits").isJsonNull()) credits = co.get("credits").getAsString() + " Credits";
                                            if (co.has("course_type") && !co.get("course_type").isJsonNull()) category = co.get("course_type").getAsString();

                                            if (co.has("teacher") && co.get("teacher").isJsonObject()) {
                                                JsonObject to = co.getAsJsonObject("teacher");
                                                if (to.has("name") && !to.get("name").isJsonNull()) instructor = to.get("name").getAsString();
                                                else if (to.has("user") && to.get("user").isJsonObject()) {
                                                    JsonObject uo = to.getAsJsonObject("user");
                                                    if (uo.has("name") && !uo.get("name").isJsonNull()) instructor = uo.get("name").getAsString();
                                                }
                                            }
                                        }

                                        courseList.add(new StudentCourseCardAdapter.StudentCourseItem(
                                                courseId, code, title, instructor, credits, category, score
                                        ));

                                        totalScore += score;
                                        scoredCourses++;
                                        if ("completed".equalsIgnoreCase(status) || score >= 50) {
                                            completedCount++;
                                        }
                                    }
                                }

                                // Update Stats
                                if (tvStatCourseCount != null) {
                                    tvStatCourseCount.setText(String.valueOf(courseList.size()));
                                }
                                if (tvStatCompleted != null) {
                                    tvStatCompleted.setText(String.valueOf(completedCount));
                                }
                                if (tvStatAverage != null) {
                                    float avg = scoredCourses > 0 ? (totalScore / scoredCourses) : 0f;
                                    tvStatAverage.setText(Math.round(avg) + "%");
                                }
                                if (tvStatGpa != null) {
                                    if (data.has("gpa") && !data.get("gpa").isJsonNull()) {
                                        tvStatGpa.setText(String.format(java.util.Locale.US, "%.2f", data.get("gpa").getAsDouble()));
                                    } else {
                                        tvStatGpa.setText("3.50");
                                    }
                                }

                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing student dashboard", e);
                            }
                        }

                        if (courseList.isEmpty()) {
                            loadFallbackCourses();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loadFallbackCourses();
                    }
                });
    }

    private void loadFallbackCourses() {
        courseList.clear();
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("1", "CS101", "Introduction to Computer Science", "Prof. Emily Johnson", "3 Credits", "Core Course", 85));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("2", "IT402", "Web Development Systems & APIs", "Prof. Alan Santos", "3 Credits", "Major Elective", 70));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("3", "CS204", "Software Engineering Architecture", "Prof. Michael Chen", "4 Credits", "Core Course", 92));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("4", "IS201", "Database Systems Design", "Prof. Robert Davis", "3 Credits", "Major Elective", 78));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("5", "MA101", "Calculus I for Computing", "Prof. Sarah Miller", "3 Credits", "General Core", 88));

        if (tvStatCourseCount != null) tvStatCourseCount.setText(String.valueOf(courseList.size()));
        if (tvStatCompleted != null) tvStatCompleted.setText("5");
        if (tvStatAverage != null) tvStatAverage.setText("83%");
        if (tvStatGpa != null) tvStatGpa.setText("3.55");

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
