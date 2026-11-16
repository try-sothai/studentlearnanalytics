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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentCoursesFragment extends Fragment {

    private static final String TAG = "StudentCoursesFragment";

    private ImageView btnSearch;
    private View layoutSearchBox, cardSemesterSelector;
    private EditText edtSearchCourses;
    private TextView tvSemesterText, tvStatCourseCount, tvStatGpa, tvStatAverage, tvStatCompleted, tvEmptyCourses;
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

        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.my_courses), getString(R.string.manage_and_track_your_enrolled_courses)
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
                        updateEmptyState();
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
        tvEmptyCourses = view.findViewById(R.id.tvEmptyCourses);

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
            intent.putExtra("course_department", item.department);
            intent.putExtra("course_grade", item.grade);

            int credits = 3;
            try {
                if (item.credits != null) {
                    String num = item.credits.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) credits = Integer.parseInt(num);
                }
            } catch (Exception ignored) {}

            String semLabel = !item.semester.isEmpty()
                    ? item.semester
                    : (tvSemesterText != null ? tvSemesterText.getText().toString() : "Current Semester");
            String roomLabel = !item.room.isEmpty() ? item.room : "Room TBA";
            String scheduleLabel = !item.schedule.isEmpty() ? item.schedule : "Schedule TBA";

            Course c = new Course(
                    item.id,
                    item.code,
                    item.name,
                    item.instructor,
                    semLabel,
                    credits,
                    roomLabel,
                    scheduleLabel,
                    0,
                    item.progressPct,
                    item.status
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

                        courseList.clear();
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
                                            String sName = so.has("name") && !so.get("name").isJsonNull()
                                                    ? so.get("name").getAsString()
                                                    : (so.has("semester_name") && !so.get("semester_name").isJsonNull()
                                                    ? so.get("semester_name").getAsString() : "Semester " + sId);
                                            if (sId > 0) availableSemesters.add(new SemesterOption(sId, sName));
                                        }
                                    }
                                    if (currentSemesterId == null && tvSemesterText != null && !availableSemesters.isEmpty()) {
                                        tvSemesterText.setText(tvSemesterText.getContext().getString(R.string.all_semesters_c20da3) + availableSemesters.get(0).name + ")");
                                    }
                                }

                                float totalScore = 0f;
                                int scoredCourses = 0;
                                int completedCredits = 0;
                                int totalCredits = 0;

                                if (data.has("enrollments") && data.get("enrollments").isJsonArray()) {
                                    JsonArray enrollments = data.getAsJsonArray("enrollments");
                                    for (JsonElement el : enrollments) {
                                        if (!el.isJsonObject()) continue;
                                        JsonObject eo = el.getAsJsonObject();

                                        String courseId = eo.has("course_id") && !eo.get("course_id").isJsonNull()
                                                ? eo.get("course_id").getAsString() : "0";
                                        boolean hasFinalScore = eo.has("final_score") && !eo.get("final_score").isJsonNull();
                                        int score = hasFinalScore ? Math.round(eo.get("final_score").getAsFloat()) : 0;
                                        String grade = eo.has("grade") && !eo.get("grade").isJsonNull()
                                                ? eo.get("grade").getAsString() : "";
                                        String status = eo.has("status") && !eo.get("status").isJsonNull()
                                                ? eo.get("status").getAsString() : "enrolled";

                                        String code = "CRS" + courseId;
                                        String title = "Course";
                                        String instructor = "Faculty Lecturer";
                                        int creditNum = 3;
                                        String category = "Core Course";
                                        String room = "";
                                        String schedule = "";
                                        String department = "";
                                        String semesterName = "";

                                        if (eo.has("semester") && eo.get("semester").isJsonObject()) {
                                            JsonObject semObj = eo.getAsJsonObject("semester");
                                            if (semObj.has("name") && !semObj.get("name").isJsonNull()) {
                                                semesterName = semObj.get("name").getAsString();
                                            } else if (semObj.has("semester_name") && !semObj.get("semester_name").isJsonNull()) {
                                                semesterName = semObj.get("semester_name").getAsString();
                                            }
                                        }

                                        if (eo.has("course") && eo.get("course").isJsonObject()) {
                                            JsonObject co = eo.getAsJsonObject("course");
                                            if (co.has("id") && !co.get("id").isJsonNull()) {
                                                courseId = co.get("id").getAsString();
                                            }
                                            if (co.has("course_code") && !co.get("course_code").isJsonNull()) {
                                                code = co.get("course_code").getAsString();
                                            } else if (co.has("code") && !co.get("code").isJsonNull()) {
                                                code = co.get("code").getAsString();
                                            }
                                            if (co.has("course_name") && !co.get("course_name").isJsonNull()) {
                                                title = co.get("course_name").getAsString();
                                            } else if (co.has("name") && !co.get("name").isJsonNull()) {
                                                title = co.get("name").getAsString();
                                            }
                                            if (co.has("credit") && !co.get("credit").isJsonNull()) {
                                                creditNum = co.get("credit").getAsInt();
                                            } else if (co.has("credits") && !co.get("credits").isJsonNull()) {
                                                creditNum = co.get("credits").getAsInt();
                                            }
                                            if (co.has("course_type") && !co.get("course_type").isJsonNull()) {
                                                category = co.get("course_type").getAsString();
                                            }
                                            if (co.has("room") && !co.get("room").isJsonNull()) {
                                                room = co.get("room").getAsString();
                                            }
                                            String day = co.has("day_of_week") && !co.get("day_of_week").isJsonNull()
                                                    ? co.get("day_of_week").getAsString() : "";
                                            String start = co.has("start_time") && !co.get("start_time").isJsonNull()
                                                    ? co.get("start_time").getAsString() : "";
                                            String end = co.has("end_time") && !co.get("end_time").isJsonNull()
                                                    ? co.get("end_time").getAsString() : "";
                                            if (!day.isEmpty() || !start.isEmpty()) {
                                                schedule = day + (!start.isEmpty() ? " " + start + (!end.isEmpty() ? "–" + end : "") : "");
                                                schedule = schedule.trim();
                                            }

                                            if (co.has("department") && co.get("department").isJsonObject()) {
                                                JsonObject deptObj = co.getAsJsonObject("department");
                                                if (deptObj.has("department_name") && !deptObj.get("department_name").isJsonNull()) {
                                                    department = deptObj.get("department_name").getAsString();
                                                } else if (deptObj.has("name") && !deptObj.get("name").isJsonNull()) {
                                                    department = deptObj.get("name").getAsString();
                                                }
                                            }

                                            if (co.has("teacher") && co.get("teacher").isJsonObject()) {
                                                JsonObject to = co.getAsJsonObject("teacher");
                                                if (to.has("full_name") && !to.get("full_name").isJsonNull()) {
                                                    instructor = to.get("full_name").getAsString();
                                                } else if (to.has("name") && !to.get("name").isJsonNull()) {
                                                    instructor = to.get("name").getAsString();
                                                } else if (to.has("first_name") && !to.get("first_name").isJsonNull()) {
                                                    String fn = to.get("first_name").getAsString();
                                                    String ln = to.has("last_name") && !to.get("last_name").isJsonNull()
                                                            ? to.get("last_name").getAsString() : "";
                                                    instructor = (fn + " " + ln).trim();
                                                } else if (to.has("user") && to.get("user").isJsonObject()) {
                                                    JsonObject uo = to.getAsJsonObject("user");
                                                    if (uo.has("name") && !uo.get("name").isJsonNull()) {
                                                        instructor = uo.get("name").getAsString();
                                                    }
                                                }
                                            }
                                        }

                                        String displayStatus = "completed".equalsIgnoreCase(status) ? "Completed" : "Active";
                                        courseList.add(new StudentCourseCardAdapter.StudentCourseItem(
                                                courseId, code, title, instructor,
                                                creditNum + " Credits", category, score,
                                                room, schedule, department, semesterName, displayStatus, grade
                                        ));

                                        totalCredits += creditNum;
                                        if (hasFinalScore && score > 0) {
                                            totalScore += score;
                                            scoredCourses++;
                                        }
                                        if ("completed".equalsIgnoreCase(status) || (hasFinalScore && score >= 50)) {
                                            completedCredits += creditNum;
                                        }
                                    }
                                }

                                // Update Stats
                                if (tvStatCourseCount != null) {
                                    tvStatCourseCount.setText(courseList.size() + (courseList.size() == 1 ? " Course" : " Courses"));
                                }
                                if (tvStatCompleted != null) {
                                    tvStatCompleted.setText(completedCredits + " / " + totalCredits + " Credits");
                                }
                                if (tvStatAverage != null) {
                                    if (scoredCourses > 0) {
                                        float avg = totalScore / scoredCourses;
                                        tvStatAverage.setText(String.format(Locale.US, "%.1f%%", avg));
                                    } else {
                                        tvStatAverage.setText("N/A");
                                    }
                                }
                                if (tvStatGpa != null) {
                                    if (data.has("gpa") && !data.get("gpa").isJsonNull()) {
                                        tvStatGpa.setText(String.format(Locale.US, "%.2f", data.get("gpa").getAsDouble()));
                                    } else if (data.has("gpa_summary") && data.get("gpa_summary").isJsonObject()) {
                                        JsonObject gpaSum = data.getAsJsonObject("gpa_summary");
                                        if (gpaSum.has("cumulative_gpa") && !gpaSum.get("cumulative_gpa").isJsonNull()) {
                                            tvStatGpa.setText(String.format(Locale.US, "%.2f", gpaSum.get("cumulative_gpa").getAsDouble()));
                                        } else {
                                            tvStatGpa.setText("N/A");
                                        }
                                    } else {
                                        tvStatGpa.setText("N/A");
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing student courses", e);
                            }
                        }

                        if (adapter != null) {
                            adapter.refresh();
                        }
                        updateEmptyState();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        courseList.clear();
                        if (adapter != null) {
                            adapter.refresh();
                        }
                        updateEmptyState();
                    }
                });
    }

    private void updateEmptyState() {
        if (tvEmptyCourses == null || rvStudentCoursesList == null || adapter == null) return;
        if (adapter.getItemCount() == 0) {
            tvEmptyCourses.setVisibility(View.VISIBLE);
            rvStudentCoursesList.setVisibility(View.GONE);
        } else {
            tvEmptyCourses.setVisibility(View.GONE);
            rvStudentCoursesList.setVisibility(View.VISIBLE);
        }
    }
}
