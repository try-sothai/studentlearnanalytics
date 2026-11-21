package com.social7.studentlearnanalytics.teacher;

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
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.student.StudentListAdapter;
import com.social7.studentlearnanalytics.teacher.student.StudentReportActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentListFragment extends Fragment {

    private StudentListAdapter adapter;
    private String selectedCategory = "All";
    private String currentSearchQuery = "";
    private Integer selectedCourseId = null;

    private TextView tvSelectedCourseName;
    private TextView tvSummaryCourseName;
    private TextView tvSummaryTeacherName;
    private TextView tvSummaryDepartmentName;
    private TextView tvSummarySemester;
    private TextView tvSummaryTotalStudents;

    private MaterialButton btnFilterAll;
    private MaterialButton btnFilterAtRisk;
    private MaterialButton btnFilterGood;
    private MaterialButton btnFilterNeedAttention;
    private LinearLayout btnFilterDropdown;

    private TextView tvPaginationInfo;
    private TextView btnPagePrev, btnPage1, btnPage2, btnPage3, tvPageEllipsis, btnPage5, btnPageNext;

    private int currentPage = 1;
    private final int pageSize = 8;
    private List<Student> currentCourseAllStudents = new ArrayList<>();
    private final List<CourseOption> availableCourses = new ArrayList<>();

    private static class CourseOption {
        final Integer id;
        final String code;
        final String name;

        CourseOption(Integer id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }
    }

    public StudentListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_students,
                container,
                false
        );

        if (getArguments() != null && getArguments().containsKey("course_id")) {
            int argCourseId = getArguments().getInt("course_id", -1);
            if (argCourseId > 0) {
                selectedCourseId = argCourseId;
            }
        }

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherStudents), getString(R.string.nav_students), "Students in my class"
            );
        }

        tvSelectedCourseName = view.findViewById(R.id.tvSelectedCourseName);
        tvSummaryCourseName = view.findViewById(R.id.tvSummaryCourseName);
        tvSummaryTeacherName = view.findViewById(R.id.tvSummaryTeacherName);
        tvSummaryDepartmentName = view.findViewById(R.id.tvSummaryDepartmentName);
        tvSummarySemester = view.findViewById(R.id.tvSummarySemester);
        tvSummaryTotalStudents = view.findViewById(R.id.tvSummaryTotalStudents);

        LinearLayout btnCourseSelector = view.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterAtRisk = view.findViewById(R.id.btnFilterAtRisk);
        btnFilterGood = view.findViewById(R.id.btnFilterGood);
        btnFilterNeedAttention = view.findViewById(R.id.btnFilterNeedAttention);
        btnFilterDropdown = view.findViewById(R.id.btnFilterDropdown);

        if (btnFilterAll != null) btnFilterAll.setOnClickListener(v -> setFilterCategory("All"));
        if (btnFilterAtRisk != null) btnFilterAtRisk.setOnClickListener(v -> setFilterCategory("At Risk"));
        if (btnFilterGood != null) btnFilterGood.setOnClickListener(v -> setFilterCategory("Good"));
        if (btnFilterNeedAttention != null) btnFilterNeedAttention.setOnClickListener(v -> setFilterCategory("Need Attention"));
        if (btnFilterDropdown != null) btnFilterDropdown.setOnClickListener(this::showFilterDropdown);

        EditText edtSearch = view.findViewById(R.id.edtSearchStudents);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    applyFilterAndPagination();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        tvPaginationInfo = view.findViewById(R.id.tvPaginationInfo);
        btnPagePrev = view.findViewById(R.id.btnPagePrev);
        btnPage1 = view.findViewById(R.id.btnPage1);
        btnPage2 = view.findViewById(R.id.btnPage2);
        btnPage3 = view.findViewById(R.id.btnPage3);
        tvPageEllipsis = view.findViewById(R.id.tvPageEllipsis);
        btnPage5 = view.findViewById(R.id.btnPage5);
        btnPageNext = view.findViewById(R.id.btnPageNext);

        if (btnPage1 != null) btnPage1.setOnClickListener(v -> setCurrentPage(1));
        if (btnPage2 != null) btnPage2.setOnClickListener(v -> setCurrentPage(2));
        if (btnPage3 != null) btnPage3.setOnClickListener(v -> setCurrentPage(3));
        if (btnPage5 != null) btnPage5.setOnClickListener(v -> setCurrentPage(Math.min(5, getMaxPageCount())));
        if (btnPagePrev != null) btnPagePrev.setOnClickListener(v -> {
            if (currentPage > 1) setCurrentPage(currentPage - 1);
        });
        if (btnPageNext != null) btnPageNext.setOnClickListener(v -> {
            int maxPages = getMaxPageCount();
            if (currentPage < maxPages) setCurrentPage(currentPage + 1);
        });

        RecyclerView rvStudentList = view.findViewById(R.id.rvStudentList);
        if (rvStudentList != null) {
            rvStudentList.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new StudentListAdapter(requireContext(), new ArrayList<>(), student -> {
                Intent intent = new Intent(requireContext(), StudentReportActivity.class);
                intent.putExtra("student", student);
                startActivity(intent);
            });
            rvStudentList.setAdapter(adapter);
        }

        loadStudentsFromApi();

        return view;
    }

    private void showCourseDropdown(View anchor) {
        if (getContext() == null) return;
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "All Assigned Courses");
        for (int i = 0; i < availableCourses.size(); i++) {
            CourseOption opt = availableCourses.get(i);
            popup.getMenu().add(0, i + 1, i + 1, opt.code + " - " + opt.name);
        }

        popup.setOnMenuItemClickListener(item -> {
            int idx = item.getItemId();
            if (idx == 0) {
                selectedCourseId = null;
                if (tvSelectedCourseName != null) tvSelectedCourseName.setText("All Assigned Courses");
                if (tvSummaryCourseName != null) tvSummaryCourseName.setText("All Assigned Courses");
            } else if (idx - 1 < availableCourses.size()) {
                CourseOption opt = availableCourses.get(idx - 1);
                selectedCourseId = opt.id;
                String title = opt.code + " - " + opt.name;
                if (tvSelectedCourseName != null) tvSelectedCourseName.setText(title);
                if (tvSummaryCourseName != null) tvSummaryCourseName.setText(title);
            }
            loadStudentsFromApi();
            return true;
        });
        popup.show();
    }

    private void showFilterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "All Categories");
        popup.getMenu().add(0, 2, 1, "At Risk Only");
        popup.getMenu().add(0, 3, 2, "Good Only");
        popup.getMenu().add(0, 4, 3, "Need Attention Only");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) setFilterCategory("All");
            else if (item.getItemId() == 2) setFilterCategory("At Risk");
            else if (item.getItemId() == 3) setFilterCategory("Good");
            else if (item.getItemId() == 4) setFilterCategory("Need Attention");
            return true;
        });
        popup.show();
    }

    private void loadStudentsFromApi() {
        if (getContext() == null) return;
        this.currentPage = 1;

        ApiClient.getApiService(requireContext()).getTeacherStudents(selectedCourseId, null, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        currentCourseAllStudents.clear();

                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            JsonObject data = response.body().getAsJsonObject("data");

                            if (data.has("teacher") && data.get("teacher").isJsonObject()) {
                                JsonObject t = data.getAsJsonObject("teacher");
                                String tName = getStr(t, "name", "Teacher");
                                String tDept = getStr(t, "department", "Academic Department");
                                if (tvSummaryTeacherName != null) tvSummaryTeacherName.setText("Teacher: " + tName);
                                if (tvSummaryDepartmentName != null) tvSummaryDepartmentName.setText(tDept);
                            }

                            if (data.has("courses") && data.get("courses").isJsonArray()) {
                                availableCourses.clear();
                                for (JsonElement cel : data.getAsJsonArray("courses")) {
                                    if (!cel.isJsonObject()) continue;
                                    JsonObject co = cel.getAsJsonObject();
                                    int cid = getInt(co, "id");
                                    String cCode = getStr(co, "code", "COURSE");
                                    String cName = getStr(co, "name", "Course");
                                    availableCourses.add(new CourseOption(cid, cCode, cName));
                                }
                                if (selectedCourseId == null) {
                                    if (tvSelectedCourseName != null) tvSelectedCourseName.setText("All Assigned Courses");
                                    if (tvSummaryCourseName != null) tvSummaryCourseName.setText("All Assigned Courses");
                                }
                            }

                            if (data.has("students") && data.get("students").isJsonArray()) {
                                JsonArray arr = data.getAsJsonArray("students");
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String code = getStr(obj, "student_code", "STU");
                                    String name = getStr(obj, "name", "Student");
                                    String email = getStr(obj, "email", "");
                                    String dept = getStr(obj, "department", "Academic Department");
                                    String prog = getStr(obj, "program", "Program");
                                    String year = getStr(obj, "year_level", "Year 1");
                                    double gpa = getDouble(obj, "gpa");
                                    String risk = getStr(obj, "risk_level", "Good");
                                    double pred = getDouble(obj, "predicted_score");
                                    String courseCode = getStr(obj, "course_code", prog);

                                    String bg = "At Risk".equalsIgnoreCase(risk) ? "#FFE4E6"
                                            : ("Need Attention".equalsIgnoreCase(risk) ? "#FEF3C7" : "#EAF0FF");
                                    String fg = "At Risk".equalsIgnoreCase(risk) ? "#E11D48"
                                            : ("Need Attention".equalsIgnoreCase(risk) ? "#D97706" : "#2563FF");

                                    Student st = new Student(code, name, email, dept, prog, year, gpa, risk, pred, bg, fg, courseCode);
                                    long dbId = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsLong() : 0L;
                                    long uId = obj.has("user_id") && !obj.get("user_id").isJsonNull() ? obj.get("user_id").getAsLong() : 0L;
                                    if (dbId > 0) st.setDbId(dbId);
                                    if (uId > 0) st.setUserId(uId);
                                    st.setClassName(getStr(obj, "class_name", "N/A"));
                                    st.setAttendanceRate(getInt(obj, "attendance_rate"));
                                    currentCourseAllStudents.add(st);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Unable to load students (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }

                        bindStudentSummaryAndFilters();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        currentCourseAllStudents.clear();
                        Toast.makeText(getContext(), "Network error loading students", Toast.LENGTH_SHORT).show();
                        bindStudentSummaryAndFilters();
                    }
                });
    }

    private void bindStudentSummaryAndFilters() {
        int totalCount = currentCourseAllStudents.size();
        int atRiskCount = 0;
        int goodCount = 0;
        int attentionCount = 0;

        for (Student s : currentCourseAllStudents) {
            String risk = s.getRiskLevel();
            if ("At Risk".equalsIgnoreCase(risk)) atRiskCount++;
            else if ("Need Attention".equalsIgnoreCase(risk)) attentionCount++;
            else goodCount++;
        }

        if (tvSummaryTotalStudents != null) tvSummaryTotalStudents.setText(String.valueOf(totalCount));

        if (btnFilterAll != null) btnFilterAll.setText(btnFilterAll.getContext().getString(R.string.all_c49d0d) + totalCount + ")");
        if (btnFilterAtRisk != null) btnFilterAtRisk.setText("At Risk (" + atRiskCount + ")");
        if (btnFilterGood != null) btnFilterGood.setText(btnFilterGood.getContext().getString(R.string.good) + goodCount + ")");
        if (btnFilterNeedAttention != null) btnFilterNeedAttention.setText("Need Attention (" + attentionCount + ")");

        applyFilterAndPagination();
    }

    private void setFilterCategory(String category) {
        this.selectedCategory = category;
        this.currentPage = 1;
        updateFilterButtonStyles();
        applyFilterAndPagination();
    }

    private void updateFilterButtonStyles() {
        int primaryBlue = Color.parseColor("#2563FF");
        int white = Color.WHITE;

        if (btnFilterAll != null) {
            if ("All".equalsIgnoreCase(selectedCategory)) {
                btnFilterAll.setBackgroundColor(primaryBlue);
                btnFilterAll.setTextColor(white);
            } else {
                btnFilterAll.setBackgroundColor(Color.parseColor("#EAF0FF"));
                btnFilterAll.setTextColor(primaryBlue);
            }
        }

        if (btnFilterAtRisk != null) {
            if ("At Risk".equalsIgnoreCase(selectedCategory)) {
                btnFilterAtRisk.setBackgroundColor(primaryBlue);
                btnFilterAtRisk.setTextColor(white);
            } else {
                btnFilterAtRisk.setBackgroundColor(Color.parseColor("#FFEBEF"));
                btnFilterAtRisk.setTextColor(Color.parseColor("#FF4055"));
            }
        }

        if (btnFilterGood != null) {
            if ("Good".equalsIgnoreCase(selectedCategory)) {
                btnFilterGood.setBackgroundColor(primaryBlue);
                btnFilterGood.setTextColor(white);
            } else {
                btnFilterGood.setBackgroundColor(Color.parseColor("#E8F8F1"));
                btnFilterGood.setTextColor(Color.parseColor("#16B879"));
            }
        }

        if (btnFilterNeedAttention != null) {
            if ("Need Attention".equalsIgnoreCase(selectedCategory)) {
                btnFilterNeedAttention.setBackgroundColor(primaryBlue);
                btnFilterNeedAttention.setTextColor(white);
            } else {
                btnFilterNeedAttention.setBackgroundColor(Color.parseColor("#FFF5E6"));
                btnFilterNeedAttention.setTextColor(Color.parseColor("#FF9B16"));
            }
        }
    }

    private void setCurrentPage(int page) {
        this.currentPage = page;
        applyFilterAndPagination();
    }

    private int getMaxPageCount() {
        List<Student> filtered = getFilteredList();
        if (filtered.isEmpty()) return 1;
        return (int) Math.ceil((double) filtered.size() / pageSize);
    }

    private List<Student> getFilteredList() {
        List<Student> filtered = new ArrayList<>();
        String q = currentSearchQuery != null ? currentSearchQuery.trim().toLowerCase(Locale.ROOT) : "";
        for (Student s : currentCourseAllStudents) {
            if (s == null) continue;
            boolean matchesQuery = q.isEmpty()
                    || (s.getName() != null && s.getName().toLowerCase(Locale.ROOT).contains(q))
                    || (s.getId() != null && s.getId().toLowerCase(Locale.ROOT).contains(q))
                    || (s.getProgram() != null && s.getProgram().toLowerCase(Locale.ROOT).contains(q));

            boolean matchesCategory = true;
            if (!"All".equalsIgnoreCase(selectedCategory)) {
                if ("At Risk".equalsIgnoreCase(selectedCategory)) {
                    matchesCategory = "At Risk".equalsIgnoreCase(s.getRiskLevel());
                } else if ("Good".equalsIgnoreCase(selectedCategory)) {
                    matchesCategory = "Good".equalsIgnoreCase(s.getRiskLevel());
                } else if ("Need Attention".equalsIgnoreCase(selectedCategory)) {
                    matchesCategory = "Need Attention".equalsIgnoreCase(s.getRiskLevel());
                }
            }

            if (matchesQuery && matchesCategory) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    private void applyFilterAndPagination() {
        List<Student> filtered = getFilteredList();
        int totalFiltered = filtered.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalFiltered / pageSize));

        if (currentPage > maxPages) {
            currentPage = maxPages;
        }

        int startIdx = (currentPage - 1) * pageSize;
        int endIdx = Math.min(startIdx + pageSize, totalFiltered);

        List<Student> pageSlice = new ArrayList<>();
        if (startIdx < totalFiltered) {
            pageSlice = filtered.subList(startIdx, endIdx);
        }

        if (adapter != null) {
            adapter.setStudents(pageSlice);
        }

        if (tvPaginationInfo != null) {
            if (totalFiltered == 0) {
                tvPaginationInfo.setText("Showing 0 of 0 students");
            } else {
                tvPaginationInfo.setText(String.format(Locale.US, "Showing %d–%d of %d students", startIdx + 1, endIdx, totalFiltered));
            }
        }

        updatePaginationUI(maxPages);
    }

    private void updatePaginationUI(int maxPages) {
        updatePageButtonStyle(btnPage1, 1);
        updatePageButtonStyle(btnPage2, 2);
        updatePageButtonStyle(btnPage3, 3);
        updatePageButtonStyle(btnPage5, maxPages >= 4 ? maxPages : 5);
        if (btnPage5 != null && maxPages >= 4) {
            btnPage5.setText(String.valueOf(maxPages));
        }

        if (btnPage2 != null) btnPage2.setVisibility(maxPages >= 2 ? View.VISIBLE : View.GONE);
        if (btnPage3 != null) btnPage3.setVisibility(maxPages >= 3 ? View.VISIBLE : View.GONE);
        if (tvPageEllipsis != null) tvPageEllipsis.setVisibility(maxPages >= 5 ? View.VISIBLE : View.GONE);
        if (btnPage5 != null) btnPage5.setVisibility(maxPages >= 4 ? View.VISIBLE : View.GONE);
    }

    private void updatePageButtonStyle(TextView btn, int pageNum) {
        if (btn == null) return;
        if (pageNum == currentPage) {
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_pagination_button_active);
        } else {
            btn.setTextColor(Color.parseColor("#10165C"));
            btn.setBackgroundResource(R.drawable.bg_pagination_button_inactive);
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

    private int getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0.0;
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
