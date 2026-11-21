package com.social7.studentlearnanalytics.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
        default void onStudentsClick(Course course) { onCourseClick(course); }
        default void onScheduleClick(Course course) { onCourseClick(course); }
        default void onAnalyticsClick(Course course) { onCourseClick(course); }
        default void onMoreClick(Course course, View anchor) { onCourseClick(course); }
        default void onMetricStudentsClick(Course course) { onStudentsClick(course); }
        default void onMetricAtRiskClick(Course course) { onCourseClick(course); }
        default void onMetricPendingScoresClick(Course course) { onCourseClick(course); }
    }

    private final Context context;
    private final List<Course> originalList;
    private List<Course> filteredList;
    private final OnCourseClickListener listener;

    public CourseAdapter(Context context, List<Course> courseList, OnCourseClickListener listener) {
        this.context = context;
        this.originalList = courseList != null ? new ArrayList<>(courseList) : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void setCourses(List<Course> newCourses) {
        this.originalList.clear();
        if (newCourses != null) {
            this.originalList.addAll(newCourses);
        }
        this.filteredList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    public void filter(String query, String semester, String department, String sortOption, boolean isArchivedTab) {
        filteredList.clear();
        String qLower = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        String deptLower = department != null ? department.trim().toLowerCase(Locale.ROOT) : "";
        for (Course course : originalList) {
            if (course == null) continue;
            String code = course.getCode() != null ? course.getCode() : "";
            String name = course.getName() != null ? course.getName() : "";
            String dept = course.getDepartment() != null ? course.getDepartment() : "";
            String sem = course.getSemester() != null ? course.getSemester() : "";
            String status = course.getStatus() != null ? course.getStatus() : "";

            boolean matchesQuery = qLower.isEmpty()
                    || code.toLowerCase(Locale.ROOT).contains(qLower)
                    || name.toLowerCase(Locale.ROOT).contains(qLower)
                    || dept.toLowerCase(Locale.ROOT).contains(qLower);

            boolean matchesSemester = semester == null || semester.trim().isEmpty() || semester.equalsIgnoreCase("All")
                    || sem.equalsIgnoreCase(semester);

            boolean matchesDept = deptLower.isEmpty() || deptLower.contains("all")
                    || dept.toLowerCase(Locale.ROOT).contains(deptLower);

            boolean matchesTab = isArchivedTab ? "Archived".equalsIgnoreCase(status) : !"Archived".equalsIgnoreCase(status);

            if (matchesQuery && matchesSemester && matchesDept && matchesTab) {
                filteredList.add(course);
            }
        }

        if (sortOption != null) {
            if (sortOption.contains("Name")) {
                Collections.sort(filteredList, (c1, c2) -> {
                    String n1 = c1.getName() != null ? c1.getName() : "";
                    String n2 = c2.getName() != null ? c2.getName() : "";
                    return n1.compareToIgnoreCase(n2);
                });
            } else if (sortOption.contains("Students")) {
                Collections.sort(filteredList, (c1, c2) -> Integer.compare(c2.getStudentCount(), c1.getStudentCount()));
            } else {
                Collections.sort(filteredList, (c1, c2) -> {
                    String code1 = c1.getCode() != null ? c1.getCode() : "";
                    String code2 = c2.getCode() != null ? c2.getCode() : "";
                    return code1.compareToIgnoreCase(code2);
                });
            }
        }

        notifyDataSetChanged();
    }

    public void filter(String query, String semester) {
        filter(query, semester, "All", "Code", false);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = filteredList.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView imgCourseIcon;
        TextView tvCourseCodeName, tvTeacherName, tvDepartment, tvCourseMetadata, tvStatusBadge;
        TextView tvQuickStudentsLabel;
        LinearLayout btnQuickStudents, btnQuickSchedule, btnQuickAnalytics, btnQuickMore;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            imgCourseIcon = itemView.findViewById(R.id.imgCourseIcon);
            tvCourseCodeName = itemView.findViewById(R.id.tvCourseCodeName);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvCourseMetadata = itemView.findViewById(R.id.tvCourseMetadata);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            tvQuickStudentsLabel = itemView.findViewById(R.id.tvQuickStudentsLabel);
            btnQuickStudents = itemView.findViewById(R.id.btnQuickStudents);
            btnQuickSchedule = itemView.findViewById(R.id.btnQuickSchedule);
            btnQuickAnalytics = itemView.findViewById(R.id.btnQuickAnalytics);
            btnQuickMore = itemView.findViewById(R.id.btnQuickMore);
        }

        public void bind(final Course course, final OnCourseClickListener listener) {
            if (tvCourseCodeName != null) tvCourseCodeName.setText(course.getCode() + " - " + course.getName());
            if (tvTeacherName != null) tvTeacherName.setText(tvTeacherName.getContext().getString(R.string.teacher) + course.getTeacher());
            if (tvDepartment != null) tvDepartment.setText(course.getDepartment());

            if (tvCourseMetadata != null) {
                tvCourseMetadata.setText(course.getStudentCount() + " students • " + course.getSchedule() + " • " + course.getRoom());
            }

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(course.getStatus());
            }

            if (tvQuickStudentsLabel != null) {
                tvQuickStudentsLabel.setText(tvQuickStudentsLabel.getContext().getString(R.string.students) + course.getStudentCount() + ")");
            }

            if (iconContainer != null && course.getIconBgRes() != 0) {
                iconContainer.setBackgroundResource(course.getIconBgRes());
            }

            if (imgCourseIcon != null && course.getIconRes() != 0) {
                imgCourseIcon.setImageResource(course.getIconRes());
            }

            if (btnQuickStudents != null) {
                btnQuickStudents.setOnClickListener(v -> {
                    if (listener != null) listener.onStudentsClick(course);
                });
            }

            if (btnQuickSchedule != null) {
                btnQuickSchedule.setOnClickListener(v -> {
                    if (listener != null) listener.onScheduleClick(course);
                });
            }

            if (btnQuickAnalytics != null) {
                btnQuickAnalytics.setOnClickListener(v -> {
                    if (listener != null) listener.onAnalyticsClick(course);
                });
            }

            if (btnQuickMore != null) {
                btnQuickMore.setOnClickListener(v -> {
                    if (listener != null) listener.onMoreClick(course, v);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(course);
            });
        }
    }
}
