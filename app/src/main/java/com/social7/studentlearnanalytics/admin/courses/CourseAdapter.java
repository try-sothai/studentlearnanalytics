package com.social7.studentlearnanalytics.admin.courses;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final Context context;
    private final List<Course> masterList;
    private final List<Course> displayedList;
    private final OnCourseClickListener listener;

    private String currentQuery = "";
    private String currentStatusFilter = "ALL";
    private String currentDepartmentFilter = "ALL";

    public CourseAdapter(Context context, List<Course> courseList, OnCourseClickListener listener) {
        this.context = context;
        this.masterList = courseList != null ? courseList : new ArrayList<>();
        this.displayedList = new ArrayList<>(this.masterList);
        this.listener = listener;
    }

    public void updateData(List<Course> newList) {
        masterList.clear();
        if (newList != null) {
            masterList.addAll(newList);
        }
        applyFilters();
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query.trim().toLowerCase() : "";
        applyFilters();
    }

    public void filterByStatus(String status) {
        this.currentStatusFilter = status != null ? status.trim().toUpperCase() : "ALL";
        applyFilters();
    }

    public void filterByDepartment(String dept) {
        this.currentDepartmentFilter = dept != null ? dept.trim().toLowerCase() : "all";
        applyFilters();
    }

    private void applyFilters() {
        displayedList.clear();
        for (Course course : masterList) {
            boolean matchesQuery = currentQuery.isEmpty() ||
                    (course.getName() != null && course.getName().toLowerCase().contains(currentQuery)) ||
                    (course.getCode() != null && course.getCode().toLowerCase().contains(currentQuery)) ||
                    (course.getDepartment() != null && course.getDepartment().toLowerCase().contains(currentQuery)) ||
                    (course.getTeacher() != null && course.getTeacher().toLowerCase().contains(currentQuery));

            boolean matchesStatus = currentStatusFilter.equals("ALL") ||
                    (course.getStatus() != null && course.getStatus().equalsIgnoreCase(currentStatusFilter));

            boolean matchesDept = currentDepartmentFilter.equals("all") ||
                    (course.getDepartment() != null && course.getDepartment().toLowerCase().contains(currentDepartmentFilter));

            if (matchesQuery && matchesStatus && matchesDept) {
                displayedList.add(course);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = displayedList.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    public List<Course> getDisplayedList() {
        return displayedList;
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseCode, tvCourseName, tvCourseCredits, tvCourseStatus;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvCourseCredits = itemView.findViewById(R.id.tvCourseCredits);
            tvCourseStatus = itemView.findViewById(R.id.tvCourseStatus);
        }

        public void bind(final Course course, final OnCourseClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(course.getCode());
            if (tvCourseName != null) tvCourseName.setText(course.getName());

            if (tvCourseCredits != null) {
                StringBuilder details = new StringBuilder();
                details.append(course.getCredits()).append(" Credits");
                if (course.getDepartment() != null && !course.getDepartment().isEmpty()) {
                    details.append(" • ").append(course.getDepartment());
                }
                if (course.getTeacher() != null && !course.getTeacher().isEmpty()) {
                    details.append(" • ").append(course.getTeacher());
                }
                tvCourseCredits.setText(details.toString());
            }

            if (tvCourseStatus != null) {
                String rawStatus = course.getStatus();
                String displayStatus = (rawStatus != null) ? rawStatus : "Active";
                tvCourseStatus.setText(displayStatus);

                if ("Active".equalsIgnoreCase(displayStatus)) {
                    tvCourseStatus.setTextColor(Color.parseColor("#16A34A"));
                    tvCourseStatus.setBackgroundResource(R.drawable.bg_badge_core);
                } else {
                    tvCourseStatus.setTextColor(Color.parseColor("#EF4444"));
                    tvCourseStatus.setBackgroundResource(R.drawable.bg_badge_credit);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(course);
            });
        }
    }
}
