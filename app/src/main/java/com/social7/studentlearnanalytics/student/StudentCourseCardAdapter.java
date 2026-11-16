package com.social7.studentlearnanalytics.student;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class StudentCourseCardAdapter extends RecyclerView.Adapter<StudentCourseCardAdapter.ViewHolder> {

    public static class StudentCourseItem {
        public String id;
        public String code;
        public String name;
        public String instructor;
        public String credits;
        public String category;
        public int progressPct;
        public String room;
        public String schedule;
        public String department;
        public String semester;
        public String status;
        public String grade;

        public StudentCourseItem(String id, String code, String name, String instructor, String credits, String category, int progressPct) {
            this(id, code, name, instructor, credits, category, progressPct, "", "", "", "", "Active", "");
        }

        public StudentCourseItem(String id, String code, String name, String instructor, String credits,
                                 String category, int progressPct, String room, String schedule,
                                 String department, String semester, String status, String grade) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.instructor = instructor;
            this.credits = credits;
            this.category = category;
            this.progressPct = progressPct;
            this.room = room != null ? room : "";
            this.schedule = schedule != null ? schedule : "";
            this.department = department != null ? department : "";
            this.semester = semester != null ? semester : "";
            this.status = status != null ? status : "Active";
            this.grade = grade != null ? grade : "";
        }
    }

    public interface OnCourseClickListener {
        void onCourseClick(StudentCourseItem item);
    }

    private final Context context;
    private final List<StudentCourseItem> originalList;
    private final List<StudentCourseItem> filteredList;
    private final OnCourseClickListener listener;
    private String currentQuery = "";

    public StudentCourseCardAdapter(Context context, List<StudentCourseItem> list, OnCourseClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_course_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentCourseItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void refresh() {
        filter(currentQuery);
    }

    public void filter(String query) {
        currentQuery = query != null ? query.trim().toLowerCase() : "";
        filteredList.clear();
        for (StudentCourseItem item : originalList) {
            if (currentQuery.isEmpty() ||
                (item.code != null && item.code.toLowerCase().contains(currentQuery)) ||
                (item.name != null && item.name.toLowerCase().contains(currentQuery)) ||
                (item.instructor != null && item.instructor.toLowerCase().contains(currentQuery))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvCategoryBadge, tvCredits, tvCourseName, tvInstructorName, tvProgressPct;
        private final ProgressBar progressBarCourse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvInstructorName = itemView.findViewById(R.id.tvInstructorName);
            tvProgressPct = itemView.findViewById(R.id.tvProgressPct);
            progressBarCourse = itemView.findViewById(R.id.progressBarCourse);
        }

        public void bind(StudentCourseItem item, OnCourseClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.code);
            if (tvCategoryBadge != null) tvCategoryBadge.setText(item.category);
            if (tvCredits != null) tvCredits.setText(item.credits);
            if (tvCourseName != null) tvCourseName.setText(item.name);
            if (tvInstructorName != null) tvInstructorName.setText(tvInstructorName.getContext().getString(R.string.instructor) + item.instructor);
            if (tvProgressPct != null) {
                if (item.grade != null && !item.grade.isEmpty()) {
                    tvProgressPct.setText(item.progressPct + "% (" + item.grade + ")");
                } else {
                    tvProgressPct.setText(item.progressPct + "%");
                }
            }

            if (progressBarCourse != null) {
                progressBarCourse.setProgress(Math.max(0, Math.min(100, item.progressPct)));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(item);
            });
        }
    }
}
