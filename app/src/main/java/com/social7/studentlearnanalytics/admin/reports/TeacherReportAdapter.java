package com.social7.studentlearnanalytics.admin.reports;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class TeacherReportAdapter extends RecyclerView.Adapter<TeacherReportAdapter.ViewHolder> {

    public static class TeacherReportItem {
        public String teacherId;
        public String teacherName;
        public String department;
        public String courses;
        public String classes;
        public String teachingHours;
        public String rating;

        public TeacherReportItem(String teacherId, String teacherName, String department, String courses, String classes, String teachingHours, String rating) {
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.department = department;
            this.courses = courses;
            this.classes = classes;
            this.teachingHours = teachingHours;
            this.rating = rating;
        }
    }

    public interface OnTeacherReportClickListener {
        void onTeacherReportClick(TeacherReportItem item);
    }

    private final Context context;
    private final List<TeacherReportItem> originalList;
    private final List<TeacherReportItem> filteredList;
    private final OnTeacherReportClickListener listener;

    public TeacherReportAdapter(Context context, List<TeacherReportItem> list, OnTeacherReportClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_teacher_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherReportItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (TeacherReportItem item : originalList) {
            if (text.isEmpty() ||
                (item.teacherId != null && item.teacherId.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text)) ||
                (item.department != null && item.department.toLowerCase().contains(text)) ||
                (item.courses != null && item.courses.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTeacherId, tvTeacherName, tvPerformanceScore, tvTeacherDetails, tvAssignedCoursesClasses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeacherId = itemView.findViewById(R.id.tvTeacherId);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvPerformanceScore = itemView.findViewById(R.id.tvPerformanceScore);
            tvTeacherDetails = itemView.findViewById(R.id.tvTeacherDetails);
            tvAssignedCoursesClasses = itemView.findViewById(R.id.tvAssignedCoursesClasses);
        }

        public void bind(TeacherReportItem item, OnTeacherReportClickListener listener) {
            if (tvTeacherId != null) tvTeacherId.setText(item.teacherId);
            if (tvTeacherName != null) tvTeacherName.setText(item.teacherName);
            if (tvPerformanceScore != null) {
                tvPerformanceScore.setText(item.rating);
                tvPerformanceScore.setTextColor(Color.parseColor("#10B981"));
            }
            if (tvTeacherDetails != null) {
                tvTeacherDetails.setText(itemView.getContext().getString(R.string.dept_prefix) + item.department + " • " + item.teachingHours);
            }
            if (tvAssignedCoursesClasses != null) {
                tvAssignedCoursesClasses.setText(tvAssignedCoursesClasses.getContext().getString(R.string.courses) + item.courses + " • Classes: " + item.classes);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTeacherReportClick(item);
            });
        }
    }
}
