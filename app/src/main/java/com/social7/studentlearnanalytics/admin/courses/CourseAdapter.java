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
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final Context context;
    private final List<Course> masterList;
    private final List<Course> filteredList;
    private final List<Course> displayedList;
    private final OnCourseClickListener listener;

    private String currentQuery = "";
    private String currentStatusFilter = "ALL";
    private int currentDepartmentId = -1;
    private String currentDepartmentName = "";
    private int currentProgramId = -1;
    private String currentProgramName = "";
    private int currentAcademicYearId = -1;
    private String currentAcademicYearName = "";
    private int currentSemesterId = -1;
    private String currentSemesterName = "";

    private int currentPage = 1;
    private int itemsPerPage = 8;

    public CourseAdapter(Context context, List<Course> courseList, OnCourseClickListener listener) {
        this.context = context;
        this.masterList = courseList != null ? new ArrayList<>(courseList) : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.masterList);
        this.displayedList = new ArrayList<>();
        this.listener = listener;
        applyFilters();
    }

    public void updateData(List<Course> newList) {
        masterList.clear();
        if (newList != null) {
            masterList.addAll(newList);
        }
        applyFilters();
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        this.currentPage = 1;
        applyFilters();
    }

    public void filterByStatus(String status) {
        this.currentStatusFilter = status != null ? status.trim().toUpperCase(Locale.ROOT) : "ALL";
        this.currentPage = 1;
        applyFilters();
    }

    public void filterByDepartment(int deptId, String deptName) {
        this.currentDepartmentId = deptId;
        this.currentDepartmentName = deptName != null ? deptName.trim().toLowerCase(Locale.ROOT) : "";
        this.currentPage = 1;
        applyFilters();
    }

    public void filterByDepartment(String dept) {
        this.currentDepartmentId = -1;
        this.currentDepartmentName = (dept == null || "all".equalsIgnoreCase(dept)) ? "" : dept.trim().toLowerCase(Locale.ROOT);
        this.currentPage = 1;
        applyFilters();
    }

    public void filterByProgram(int programId, String programName) {
        this.currentProgramId = programId;
        this.currentProgramName = programName != null ? programName.trim().toLowerCase(Locale.ROOT) : "";
        this.currentPage = 1;
        applyFilters();
    }

    public void filterByAcademicYear(int ayId, String ayName) {
        this.currentAcademicYearId = ayId;
        this.currentAcademicYearName = ayName != null ? ayName.trim().toLowerCase(Locale.ROOT) : "";
        this.currentPage = 1;
        applyFilters();
    }

    public void filterBySemester(int semId, String semName) {
        this.currentSemesterId = semId;
        this.currentSemesterName = semName != null ? semName.trim().toLowerCase(Locale.ROOT) : "";
        this.currentPage = 1;
        applyFilters();
    }

    public void clearAllFilters() {
        this.currentQuery = "";
        this.currentStatusFilter = "ALL";
        this.currentDepartmentId = -1;
        this.currentDepartmentName = "";
        this.currentProgramId = -1;
        this.currentProgramName = "";
        this.currentAcademicYearId = -1;
        this.currentAcademicYearName = "";
        this.currentSemesterId = -1;
        this.currentSemesterName = "";
        this.currentPage = 1;
        applyFilters();
    }

    public void setPage(int page, int perPage) {
        this.currentPage = Math.max(1, page);
        this.itemsPerPage = Math.max(1, perPage);
        rebuildPageSlice();
    }

    private void applyFilters() {
        filteredList.clear();
        for (Course course : masterList) {
            boolean matchesQuery = currentQuery.isEmpty()
                    || containsIgnoreCase(course.getName(), currentQuery)
                    || containsIgnoreCase(course.getCode(), currentQuery)
                    || containsIgnoreCase(course.getDepartment(), currentQuery)
                    || containsIgnoreCase(course.getDepartmentCode(), currentQuery)
                    || containsIgnoreCase(course.getProgramName(), currentQuery)
                    || containsIgnoreCase(course.getProgramCode(), currentQuery)
                    || containsIgnoreCase(course.getTeacher(), currentQuery)
                    || containsIgnoreCase(course.getDescription(), currentQuery);

            boolean matchesStatus = currentStatusFilter.equals("ALL")
                    || (course.getStatus() != null && course.getStatus().equalsIgnoreCase(currentStatusFilter));

            boolean matchesDept = true;
            if (currentDepartmentId > 0) {
                matchesDept = course.getDepartmentId() == currentDepartmentId
                        || (!currentDepartmentName.isEmpty() && containsIgnoreCase(course.getDepartment(), currentDepartmentName));
            } else if (!currentDepartmentName.isEmpty()) {
                matchesDept = containsIgnoreCase(course.getDepartment(), currentDepartmentName);
            }

            boolean matchesProg = true;
            if (currentProgramId > 0) {
                matchesProg = course.getProgramId() == currentProgramId
                        || (!currentProgramName.isEmpty() && containsIgnoreCase(course.getProgramName(), currentProgramName));
            } else if (!currentProgramName.isEmpty()) {
                matchesProg = containsIgnoreCase(course.getProgramName(), currentProgramName)
                        || containsIgnoreCase(course.getProgramCode(), currentProgramName);
            }

            boolean matchesAy = true;
            if (currentAcademicYearId > 0) {
                matchesAy = course.getAcademicYearId() == currentAcademicYearId
                        || (!currentAcademicYearName.isEmpty() && containsIgnoreCase(course.getAcademicYear(), currentAcademicYearName))
                        || course.getAcademicYear().isEmpty();
            } else if (!currentAcademicYearName.isEmpty()) {
                matchesAy = containsIgnoreCase(course.getAcademicYear(), currentAcademicYearName);
            }

            boolean matchesSem = true;
            if (currentSemesterId > 0) {
                matchesSem = course.getSemesterId() == currentSemesterId
                        || (!currentSemesterName.isEmpty() && containsIgnoreCase(course.getSemester(), currentSemesterName));
            } else if (!currentSemesterName.isEmpty()) {
                matchesSem = containsIgnoreCase(course.getSemester(), currentSemesterName);
            }

            if (matchesQuery && matchesStatus && matchesDept && matchesProg && matchesAy && matchesSem) {
                filteredList.add(course);
            }
        }
        rebuildPageSlice();
    }

    private void rebuildPageSlice() {
        displayedList.clear();
        int total = filteredList.size();
        int maxPage = Math.max(1, (int) Math.ceil((double) total / itemsPerPage));
        if (currentPage > maxPage) currentPage = maxPage;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, total);
        if (start >= 0 && start < end) {
            displayedList.addAll(filteredList.subList(start, end));
        }
        notifyDataSetChanged();
    }

    private boolean containsIgnoreCase(String src, String query) {
        return src != null && src.toLowerCase(Locale.ROOT).contains(query);
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

    public int getFilteredCount() {
        return filteredList.size();
    }

    public List<Course> getFilteredList() {
        return filteredList;
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
                if (!course.getDepartment().isEmpty()) {
                    details.append(" • ").append(course.getDepartment());
                }
                if (!course.getProgramCode().isEmpty()) {
                    details.append(" (").append(course.getProgramCode()).append(")");
                } else if (!course.getProgramName().isEmpty()) {
                    details.append(" • ").append(course.getProgramName());
                }
                if (!course.getTeacher().isEmpty()) {
                    details.append("\nTeacher: ").append(course.getTeacher());
                }
                if (!course.getAcademicYear().isEmpty() || !course.getSemester().isEmpty()) {
                    details.append(" • ");
                    if (!course.getAcademicYear().isEmpty()) {
                        details.append(course.getAcademicYear());
                    }
                    if (!course.getSemester().isEmpty()) {
                        if (!course.getAcademicYear().isEmpty()) details.append(" ");
                        details.append("(").append(course.getSemester()).append(")");
                    }
                }
                tvCourseCredits.setText(details.toString());
            }

            if (tvCourseStatus != null) {
                String rawStatus = course.getStatus();
                String displayStatus = (rawStatus != null && !rawStatus.isEmpty()) ? rawStatus : "Active";
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