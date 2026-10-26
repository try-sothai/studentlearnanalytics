package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.reports.CourseReportActivity;
import com.social7.studentlearnanalytics.admin.reports.StudentReportActivity;

public class ReportsFragment extends Fragment {

    private MaterialButton btnTabOverview, btnTabStudentsReport, btnTabTeachersReport, btnTabCoursesReport;

    public ReportsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_admin_reports,
                container,
                false
        );

        btnTabOverview = view.findViewById(R.id.btnTabOverview);
        btnTabStudentsReport = view.findViewById(R.id.btnTabStudentsReport);
        btnTabTeachersReport = view.findViewById(R.id.btnTabTeachersReport);
        btnTabCoursesReport = view.findViewById(R.id.btnTabCoursesReport);

        View btnMenu = view.findViewById(R.id.btnReportsMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        setupTopTabs();
        setupCategoryRows(view);

        return view;
    }

    private void setupTopTabs() {
        if (btnTabOverview != null) {
            btnTabOverview.setOnClickListener(v -> selectTab("Overview"));
        }
        if (btnTabStudentsReport != null) {
            btnTabStudentsReport.setOnClickListener(v -> {
                selectTab("Students");
                Intent intent = new Intent(requireContext(), StudentReportActivity.class);
                startActivity(intent);
            });
        }
        if (btnTabTeachersReport != null) {
            btnTabTeachersReport.setOnClickListener(v -> {
                selectTab("Teachers");
                Toast.makeText(getContext(), "Opening Teacher Reports...", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnTabCoursesReport != null) {
            btnTabCoursesReport.setOnClickListener(v -> {
                selectTab("Courses");
                Intent intent = new Intent(requireContext(), CourseReportActivity.class);
                startActivity(intent);
            });
        }
    }

    private void selectTab(String tabName) {
        int primaryColor = Color.parseColor("#3155F5");
        int secondaryColor = Color.parseColor("#64748B");
        int whiteColor = Color.WHITE;

        if (btnTabOverview != null) {
            boolean active = "Overview".equalsIgnoreCase(tabName);
            btnTabOverview.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabOverview.setTextColor(active ? whiteColor : secondaryColor);
        }
        if (btnTabStudentsReport != null) {
            boolean active = "Students".equalsIgnoreCase(tabName);
            btnTabStudentsReport.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabStudentsReport.setTextColor(active ? whiteColor : secondaryColor);
        }
        if (btnTabTeachersReport != null) {
            boolean active = "Teachers".equalsIgnoreCase(tabName);
            btnTabTeachersReport.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabTeachersReport.setTextColor(active ? whiteColor : secondaryColor);
        }
        if (btnTabCoursesReport != null) {
            boolean active = "Courses".equalsIgnoreCase(tabName);
            btnTabCoursesReport.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabCoursesReport.setTextColor(active ? whiteColor : secondaryColor);
        }
    }

    private void setupCategoryRows(View view) {
        View rowStudent = view.findViewById(R.id.rowStudentReports);
        View rowTeacher = view.findViewById(R.id.rowTeacherReports);
        View rowCourse = view.findViewById(R.id.rowCourseReports);
        View rowAcademic = view.findViewById(R.id.rowAcademicReports);
        View rowActivity = view.findViewById(R.id.rowActivityReports);

        if (rowStudent != null) {
            rowStudent.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), StudentReportActivity.class);
                startActivity(intent);
            });
        }
        if (rowTeacher != null) {
            rowTeacher.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Opening Teacher Reports...", Toast.LENGTH_SHORT).show()
            );
        }
        if (rowCourse != null) {
            rowCourse.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CourseReportActivity.class);
                startActivity(intent);
            });
        }
        if (rowAcademic != null) {
            rowAcademic.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Opening Academic Reports...", Toast.LENGTH_SHORT).show()
            );
        }
        if (rowActivity != null) {
            rowActivity.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Opening Activity Reports...", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
