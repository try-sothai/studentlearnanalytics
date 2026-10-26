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
import com.social7.studentlearnanalytics.teacher.student.AtRiskStudentAdapter;
import com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredictionRiskFragment extends Fragment {

    private AtRiskStudentAdapter adapter;
    private String selectedCategory = "All";
    private String searchQuery = "";

    private TextView tvSelectedCourseName;
    private TextView tvTotalAtRiskCount;
    private TextView tvTotalAtRiskSubtext;
    private TextView tvCountLowGpa, tvCountLowScores, tvCountHighAbsence, tvCountMultipleIssues;

    private MaterialButton btnFilterAllRisk;
    private MaterialButton btnFilterLowGpa;
    private MaterialButton btnFilterLowScores;
    private MaterialButton btnFilterHighAbsence;
    private MaterialButton btnFilterMultipleIssues;
    private LinearLayout btnFilterDropdown;

    public PredictionRiskFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_prediction_risk,
                container,
                false
        );

        ImageView btnBack = view.findViewById(R.id.btnTeacherPredictionBack);
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
        LinearLayout btnCourseSelector = view.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        tvTotalAtRiskCount = view.findViewById(R.id.tvTotalAtRiskCount);
        tvTotalAtRiskSubtext = view.findViewById(R.id.tvTotalAtRiskSubtext);
        tvCountLowGpa = view.findViewById(R.id.tvCountLowGpa);
        tvCountLowScores = view.findViewById(R.id.tvCountLowScores);
        tvCountHighAbsence = view.findViewById(R.id.tvCountHighAbsence);
        tvCountMultipleIssues = view.findViewById(R.id.tvCountMultipleIssues);

        btnFilterAllRisk = view.findViewById(R.id.btnFilterAllRisk);
        btnFilterLowGpa = view.findViewById(R.id.btnFilterLowGpa);
        btnFilterLowScores = view.findViewById(R.id.btnFilterLowScores);
        btnFilterHighAbsence = view.findViewById(R.id.btnFilterHighAbsence);
        btnFilterMultipleIssues = view.findViewById(R.id.btnFilterMultipleIssues);
        btnFilterDropdown = view.findViewById(R.id.btnFilterDropdown);

        if (btnFilterAllRisk != null) btnFilterAllRisk.setOnClickListener(v -> setFilterCategory("All"));
        if (btnFilterLowGpa != null) btnFilterLowGpa.setOnClickListener(v -> setFilterCategory("Low GPA"));
        if (btnFilterLowScores != null) btnFilterLowScores.setOnClickListener(v -> setFilterCategory("Low Scores"));
        if (btnFilterHighAbsence != null) btnFilterHighAbsence.setOnClickListener(v -> setFilterCategory("High Absence"));
        if (btnFilterMultipleIssues != null) btnFilterMultipleIssues.setOnClickListener(v -> setFilterCategory("Multiple Issues"));
        if (btnFilterDropdown != null) btnFilterDropdown.setOnClickListener(this::showFilterDropdown);

        EditText edtSearch = view.findViewById(R.id.edtSearchAtRisk);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (adapter != null) {
                        adapter.filter(searchQuery, selectedCategory);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        RecyclerView rvAtRisk = view.findViewById(R.id.rvAtRiskStudents);
        if (rvAtRisk != null) {
            rvAtRisk.setLayoutManager(new LinearLayoutManager(requireContext()));
            List<AtRiskStudentAdapter.AtRiskStudent> list = getSampleAtRiskStudents("CS101");
            adapter = new AtRiskStudentAdapter(requireContext(), list, student -> {
                Student s = new Student(
                        student.id,
                        student.name,
                        student.name.toLowerCase() + "@university.edu",
                        "Computer Science",
                        student.program,
                        student.year,
                        Double.parseDouble(student.gpa),
                        student.riskBadge,
                        58.0,
                        student.avatarBgColor,
                        student.avatarTextColor,
                        "CS101"
                );
                Intent intent = new Intent(requireContext(), StudentDetailActivity.class);
                intent.putExtra("student", s);
                startActivity(intent);
            });
            rvAtRisk.setAdapter(adapter);
        }

        loadCourseRiskData("CS101");

        return view;
    }

    private void showCourseDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "CS101 - Data Structures");
        popup.getMenu().add(0, 2, 1, "CS102 - Web Development");
        popup.getMenu().add(0, 3, 2, "CS201 - Database Systems");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                loadCourseRiskData("CS101");
            } else if (item.getItemId() == 2) {
                loadCourseRiskData("CS102");
            } else if (item.getItemId() == 3) {
                loadCourseRiskData("CS201");
            }
            return true;
        });
        popup.show();
    }

    private void showFilterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "All Risk Categories");
        popup.getMenu().add(0, 2, 1, "Low GPA");
        popup.getMenu().add(0, 3, 2, "Low Scores");
        popup.getMenu().add(0, 4, 3, "High Absence");
        popup.getMenu().add(0, 5, 4, "Multiple Issues");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) setFilterCategory("All");
            else if (item.getItemId() == 2) setFilterCategory("Low GPA");
            else if (item.getItemId() == 3) setFilterCategory("Low Scores");
            else if (item.getItemId() == 4) setFilterCategory("High Absence");
            else if (item.getItemId() == 5) setFilterCategory("Multiple Issues");
            return true;
        });
        popup.show();
    }

    private void loadCourseRiskData(String courseCode) {
        String courseTitle;
        if ("CS102".equalsIgnoreCase(courseCode)) {
            courseTitle = "CS102 - Web Development";
        } else if ("CS201".equalsIgnoreCase(courseCode)) {
            courseTitle = "CS201 - Database Systems";
        } else {
            courseTitle = "CS101 - Data Structures";
        }

        if (tvSelectedCourseName != null) tvSelectedCourseName.setText(courseTitle);

        List<AtRiskStudentAdapter.AtRiskStudent> list = getSampleAtRiskStudents(courseCode);
        if (adapter != null) {
            adapter.setStudents(list);
            adapter.filter(searchQuery, selectedCategory);
        }

        if ("CS101".equalsIgnoreCase(courseCode)) {
            if (tvTotalAtRiskCount != null) tvTotalAtRiskCount.setText("5");
            if (tvTotalAtRiskSubtext != null) tvTotalAtRiskSubtext.setText("out of 35 students (14.3%)");
            if (tvCountLowGpa != null) tvCountLowGpa.setText("4");
            if (tvCountLowScores != null) tvCountLowScores.setText("3");
            if (tvCountHighAbsence != null) tvCountHighAbsence.setText("2");
            if (tvCountMultipleIssues != null) tvCountMultipleIssues.setText("1");
        } else {
            if (tvTotalAtRiskCount != null) tvTotalAtRiskCount.setText("3");
            if (tvTotalAtRiskSubtext != null) tvTotalAtRiskSubtext.setText("out of 28 students (10.7%)");
            if (tvCountLowGpa != null) tvCountLowGpa.setText("2");
            if (tvCountLowScores != null) tvCountLowScores.setText("2");
            if (tvCountHighAbsence != null) tvCountHighAbsence.setText("1");
            if (tvCountMultipleIssues != null) tvCountMultipleIssues.setText("1");
        }
    }

    private void setFilterCategory(String category) {
        this.selectedCategory = category;
        updateFilterButtonStyles();
        if (adapter != null) {
            adapter.filter(searchQuery, selectedCategory);
        }
    }

    private void updateFilterButtonStyles() {
        int primaryBlue = Color.parseColor("#2563FF");
        int white = Color.WHITE;

        if (btnFilterAllRisk != null) {
            if ("All".equalsIgnoreCase(selectedCategory)) {
                btnFilterAllRisk.setBackgroundColor(primaryBlue);
                btnFilterAllRisk.setTextColor(white);
            } else {
                btnFilterAllRisk.setBackgroundColor(Color.parseColor("#EAF0FF"));
                btnFilterAllRisk.setTextColor(primaryBlue);
            }
        }

        if (btnFilterLowGpa != null) {
            if ("Low GPA".equalsIgnoreCase(selectedCategory)) {
                btnFilterLowGpa.setBackgroundColor(primaryBlue);
                btnFilterLowGpa.setTextColor(white);
            } else {
                btnFilterLowGpa.setBackgroundColor(Color.parseColor("#FFEBEF"));
                btnFilterLowGpa.setTextColor(Color.parseColor("#FF4055"));
            }
        }

        if (btnFilterLowScores != null) {
            if ("Low Scores".equalsIgnoreCase(selectedCategory)) {
                btnFilterLowScores.setBackgroundColor(primaryBlue);
                btnFilterLowScores.setTextColor(white);
            } else {
                btnFilterLowScores.setBackgroundColor(Color.parseColor("#FFF5E6"));
                btnFilterLowScores.setTextColor(Color.parseColor("#FF9B16"));
            }
        }

        if (btnFilterHighAbsence != null) {
            if ("High Absence".equalsIgnoreCase(selectedCategory)) {
                btnFilterHighAbsence.setBackgroundColor(primaryBlue);
                btnFilterHighAbsence.setTextColor(white);
            } else {
                btnFilterHighAbsence.setBackgroundColor(Color.parseColor("#EAF0FF"));
                btnFilterHighAbsence.setTextColor(primaryBlue);
            }
        }

        if (btnFilterMultipleIssues != null) {
            if ("Multiple Issues".equalsIgnoreCase(selectedCategory)) {
                btnFilterMultipleIssues.setBackgroundColor(primaryBlue);
                btnFilterMultipleIssues.setTextColor(white);
            } else {
                btnFilterMultipleIssues.setBackgroundColor(Color.parseColor("#F3E8FF"));
                btnFilterMultipleIssues.setTextColor(Color.parseColor("#7C3AED"));
            }
        }
    }

    private List<AtRiskStudentAdapter.AtRiskStudent> getSampleAtRiskStudents(String courseCode) {
        List<AtRiskStudentAdapter.AtRiskStudent> list = new ArrayList<>();

        // Student 1: Dara
        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023002", "Dara", "Data Science", "2nd Year", "High Risk",
                "2.10", "58%", "12%",
                "• Low GPA", "• Low assignment scores", "• High absence rate",
                "#FFE4E6", "#FF4055",
                Arrays.asList("Low GPA", "Low Scores", "High Absence", "Multiple Issues")
        ));

        // Student 2: Rathana
        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023003", "Rathana", "Web Development", "3rd Year", "High Risk",
                "2.75", "62%", "10%",
                "• Low assignment scores", "• Irregular class attendance", "• Declining performance",
                "#FEF3C7", "#D97706",
                Arrays.asList("Low Scores", "High Absence")
        ));

        // Student 3: Sreyneang
        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023005", "Sreyneang", "Artificial Intelligence", "3rd Year", "High Risk",
                "2.30", "60%", "14%",
                "• Low GPA", "• High absence rate", "• Missed assignments",
                "#FFE4E6", "#FF4055",
                Arrays.asList("Low GPA", "Low Scores", "High Absence")
        ));

        // Student 4: Chenda
        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023006", "Chenda", "Data Structures", "2nd Year", "At Risk",
                "2.10", "65%", "8%",
                "• Low GPA", "• Low quiz scores", "• Needs additional support",
                "#F3E8FF", "#7C3AED",
                Arrays.asList("Low GPA", "Low Scores")
        ));

        // Student 5: Kimsan
        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023007", "Kimsan", "Database Systems", "3rd Year", "At Risk",
                "2.95", "68%", "9%",
                "• Low assignment scores", "• Irregular attendance", "• Needs study guidance",
                "#FFEDD5", "#D97706",
                Arrays.asList("Low GPA", "Low Scores")
        ));

        return list;
    }
}
