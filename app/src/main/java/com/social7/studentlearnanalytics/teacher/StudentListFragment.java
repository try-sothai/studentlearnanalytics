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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity;
import com.social7.studentlearnanalytics.teacher.student.StudentReportActivity;
import com.social7.studentlearnanalytics.teacher.student.StudentListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentListFragment extends Fragment {

    private StudentListAdapter adapter;
    private String selectedCategory = "All";
    private String currentSearchQuery = "";

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

        ImageView btnBack = view.findViewById(R.id.btnTeacherStudentsBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
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
        if (btnPage5 != null) btnPage5.setOnClickListener(v -> setCurrentPage(5));
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

        loadCourseData("CS101");

        return view;
    }

    private void showCourseDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "CS101 - Data Structures");
        popup.getMenu().add(0, 2, 1, "CS102 - Web Development");
        popup.getMenu().add(0, 3, 2, "CS201 - Database Systems");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                loadCourseData("CS101");
            } else if (item.getItemId() == 2) {
                loadCourseData("CS102");
            } else if (item.getItemId() == 3) {
                loadCourseData("CS201");
            }
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

    private void loadCourseData(String courseCode) {
        this.currentPage = 1;

        String courseTitle;
        if ("CS102".equalsIgnoreCase(courseCode)) {
            courseTitle = "CS102 - Web Development";
        } else if ("CS201".equalsIgnoreCase(courseCode)) {
            courseTitle = "CS201 - Database Systems";
        } else {
            courseTitle = "CS101 - Data Structures";
        }

        if (tvSelectedCourseName != null) tvSelectedCourseName.setText(courseTitle);
        if (tvSummaryCourseName != null) tvSummaryCourseName.setText(courseTitle);
        if (tvSummaryTeacherName != null) tvSummaryTeacherName.setText("Teacher: Mr. Chan");
        if (tvSummaryDepartmentName != null) tvSummaryDepartmentName.setText("Computer Science Dept.");
        if (tvSummarySemester != null) tvSummarySemester.setText("Semester 1, 2024");

        if (getContext() != null) {
            com.social7.studentlearnanalytics.network.ApiClient.getApiService(requireContext()).getStudents().enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            com.google.gson.JsonArray dataArray = response.body().has("data") && response.body().get("data").isJsonArray()
                                    ? response.body().getAsJsonArray("data") : null;
                            if (dataArray != null && !dataArray.isEmpty()) {
                                List<Student> apiStudents = new ArrayList<>();
                                for (com.google.gson.JsonElement el : dataArray) {
                                    if (!el.isJsonObject()) continue;
                                    com.google.gson.JsonObject obj = el.getAsJsonObject();
                                    String id = obj.has("student_code") && !obj.get("student_code").isJsonNull()
                                            ? obj.get("student_code").getAsString()
                                            : (obj.has("id") ? obj.get("id").getAsString() : "STU");
                                    String name = obj.has("first_name") && !obj.get("first_name").isJsonNull()
                                            ? obj.get("first_name").getAsString() + " " + (obj.has("last_name") ? obj.get("last_name").getAsString() : "")
                                            : "Student";
                                    apiStudents.add(new Student(id, name.trim(), id.toLowerCase() + "@university.edu", "Computer Science", courseCode, "3rd Year", 3.25, "Good", 76.5, "#EAF0FF", "#2563FF", courseCode));
                                }
                                if (!apiStudents.isEmpty()) {
                                    currentCourseAllStudents = apiStudents;
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("StudentList", "Error parsing students API", e);
                        }
                    }
                    bindStudentSummaryAndFilters();
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    bindStudentSummaryAndFilters();
                }
            });
        } else {
            currentCourseAllStudents = getStudentsForCourse(courseCode);
            bindStudentSummaryAndFilters();
        }
    }

    private void bindStudentSummaryAndFilters() {
        if (currentCourseAllStudents.isEmpty()) {
            currentCourseAllStudents = getStudentsForCourse("CS101");
        }
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

        if (btnFilterAll != null) btnFilterAll.setText("All (" + totalCount + ")");
        if (btnFilterAtRisk != null) btnFilterAtRisk.setText("At Risk (" + atRiskCount + ")");
        if (btnFilterGood != null) btnFilterGood.setText("Good (" + goodCount + ")");
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
        for (Student s : currentCourseAllStudents) {
            boolean matchesQuery = currentSearchQuery.isEmpty()
                    || s.getName().toLowerCase(Locale.ROOT).contains(currentSearchQuery.toLowerCase(Locale.ROOT))
                    || s.getId().toLowerCase(Locale.ROOT).contains(currentSearchQuery.toLowerCase(Locale.ROOT))
                    || s.getProgram().toLowerCase(Locale.ROOT).contains(currentSearchQuery.toLowerCase(Locale.ROOT));

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
        updatePageButtonStyle(btnPage5, 5);

        if (btnPage2 != null) btnPage2.setVisibility(maxPages >= 2 ? View.VISIBLE : View.GONE);
        if (btnPage3 != null) btnPage3.setVisibility(maxPages >= 3 ? View.VISIBLE : View.GONE);
        if (tvPageEllipsis != null) tvPageEllipsis.setVisibility(maxPages >= 5 ? View.VISIBLE : View.GONE);
        if (btnPage5 != null) btnPage5.setVisibility(maxPages >= 5 ? View.VISIBLE : View.GONE);
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

    private List<Student> getStudentsForCourse(String courseCode) {
        List<Student> list = new ArrayList<>();

        if ("CS102".equalsIgnoreCase(courseCode)) {
            // Sample CS102 students
            list.add(new Student("STU023020", "Rathana Web", "rathana.w@university.edu", "Computer Science", "Web Development", "3rd Year", 3.10, "Good", 80.0, "#EAF0FF", "#2563FF", "CS102"));
            list.add(new Student("STU023021", "Sokha Web", "sokha.w@university.edu", "Computer Science", "Web Development", "2nd Year", 2.20, "At Risk", 55.0, "#FFE4E6", "#E11D48", "CS102"));
            list.add(new Student("STU023022", "Borith Web", "borith.w@university.edu", "Computer Science", "Web Development", "3rd Year", 2.80, "Need Attention", 68.0, "#FEF3C7", "#D97706", "CS102"));
            for (int i = 4; i <= 28; i++) {
                String risk = (i % 5 == 0) ? "At Risk" : (i % 4 == 0) ? "Need Attention" : "Good";
                list.add(new Student("STU0230" + (20 + i), "Student " + i, "student" + i + "@university.edu", "Computer Science", "Web Development", "2nd Year", 3.0, risk, 75.0, "#EAF0FF", "#2563FF", "CS102"));
            }
            return list;
        } else if ("CS201".equalsIgnoreCase(courseCode)) {
            // Sample CS201 students
            for (int i = 1; i <= 30; i++) {
                String risk = (i % 6 == 0) ? "At Risk" : (i % 5 == 0) ? "Need Attention" : "Good";
                list.add(new Student("STU0231" + (10 + i), "DB Student " + i, "db" + i + "@university.edu", "Computer Science", "Database Systems", "3rd Year", 3.1, risk, 78.0, "#EAF0FF", "#2563FF", "CS201"));
            }
            return list;
        }

        // Exact 8 students for CS101 Page 1:
        list.add(new Student("STU023001", "Sophea", "sophea@university.edu", "Computer Science", "Computer Science", "3rd Year", 3.25, "Good", 76.5, "#EAF0FF", "#2563FF", "CS101"));
        list.add(new Student("STU023002", "Dara", "dara@university.edu", "Computer Science", "Data Science", "2nd Year", 2.10, "At Risk", 52.3, "#FFE4E6", "#E11D48", "CS101"));
        list.add(new Student("STU023003", "Rathana", "rathana@university.edu", "Computer Science", "Web Development", "3rd Year", 2.75, "Need Attention", 65.4, "#FEF3C7", "#D97706", "CS101"));
        list.add(new Student("STU023004", "Vannak", "vannak@university.edu", "Computer Science", "Computer Networks", "4th Year", 3.40, "Good", 82.1, "#EAF0FF", "#2563FF", "CS101"));
        list.add(new Student("STU023005", "Sreyneang", "sreyneang@university.edu", "Computer Science", "Artificial Intelligence", "3rd Year", 2.30, "At Risk", 54.1, "#FFE4E6", "#E11D48", "CS101"));
        list.add(new Student("STU023006", "Chenda", "chenda@university.edu", "Computer Science", "Data Structures", "2nd Year", 2.10, "Good", 74.0, "#F3E8FF", "#7C3AED", "CS101"));
        list.add(new Student("STU023007", "Kimsan", "kimsan@university.edu", "Computer Science", "Database Systems", "3rd Year", 2.95, "Need Attention", 71.0, "#FFEDD5", "#D97706", "CS101"));
        list.add(new Student("STU023008", "Visal", "visal@university.edu", "Computer Science", "Software Engineering", "4th Year", 3.60, "Good", 88.5, "#D1FAE5", "#059669", "CS101"));

        // Remaining 27 students for CS101 to reach total 35:
        // CS101 breakdown: 5 At Risk (Dara, Sreyneang + 3 more), 6 Need Attention (Rathana, Kimsan + 4 more), 24 Good (Sophea, Vannak, Chenda, Visal + 20 more)
        list.add(new Student("STU023009", "Sokha", "sokha@university.edu", "Computer Science", "Computer Science", "2nd Year", 2.15, "At Risk", 51.0, "#FFE4E6", "#E11D48", "CS101"));
        list.add(new Student("STU023010", "Borith", "borith@university.edu", "Computer Science", "Data Science", "3rd Year", 2.25, "At Risk", 53.0, "#FFE4E6", "#E11D48", "CS101"));
        list.add(new Student("STU023011", "Chanthea", "chanthea@university.edu", "Computer Science", "Web Development", "2nd Year", 2.05, "At Risk", 49.5, "#FFE4E6", "#E11D48", "CS101"));

        list.add(new Student("STU023012", "Makara", "makara@university.edu", "Computer Science", "Computer Networks", "3rd Year", 2.85, "Need Attention", 67.0, "#FEF3C7", "#D97706", "CS101"));
        list.add(new Student("STU023013", "Narith", "narith@university.edu", "Computer Science", "Artificial Intelligence", "4th Year", 2.90, "Need Attention", 69.0, "#FEF3C7", "#D97706", "CS101"));
        list.add(new Student("STU023014", "Piseth", "piseth@university.edu", "Computer Science", "Data Structures", "3rd Year", 2.80, "Need Attention", 66.5, "#FEF3C7", "#D97706", "CS101"));
        list.add(new Student("STU023015", "Sambath", "sambath@university.edu", "Computer Science", "Database Systems", "2nd Year", 2.70, "Need Attention", 64.0, "#FEF3C7", "#D97706", "CS101"));

        for (int i = 16; i <= 35; i++) {
            list.add(new Student("STU0230" + i, "Student " + i, "student" + i + "@university.edu", "Computer Science", "Software Engineering", "3rd Year", 3.30, "Good", 79.0, "#EAF0FF", "#2563FF", "CS101"));
        }

        return list;
    }
}
