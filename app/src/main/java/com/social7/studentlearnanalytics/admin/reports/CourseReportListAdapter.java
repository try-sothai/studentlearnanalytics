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

public class CourseReportListAdapter extends RecyclerView.Adapter<CourseReportListAdapter.ViewHolder> {

    public static class CourseReportItem {
        public String courseCode;
        public String courseName;
        public String department;
        public String metricDetail;
        public String status;

        public CourseReportItem(String courseCode, String courseName, String department, String metricDetail, String status) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.department = department;
            this.metricDetail = metricDetail;
            this.status = status;
        }
    }

    public interface OnCourseClickListener {
        void onCourseClick(CourseReportItem item);
    }

    private final Context context;
    private final List<CourseReportItem> originalList;
    private final List<CourseReportItem> filteredList;
    private final OnCourseClickListener listener;

    public CourseReportListAdapter(Context context, List<CourseReportItem> list, OnCourseClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_course_report_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseReportItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (CourseReportItem item : originalList) {
            if (text.isEmpty() ||
                (item.courseCode != null && item.courseCode.toLowerCase().contains(text)) ||
                (item.courseName != null && item.courseName.toLowerCase().contains(text)) ||
                (item.department != null && item.department.toLowerCase().contains(text)) ||
                (item.metricDetail != null && item.metricDetail.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvCourseName, tvDepartment, tvCourseMetric, tvCourseStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvCourseMetric = itemView.findViewById(R.id.tvCourseMetric);
            tvCourseStatus = itemView.findViewById(R.id.tvCourseStatus);
        }

        public void bind(CourseReportItem item, OnCourseClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvCourseName != null) tvCourseName.setText(item.courseName);
            if (tvDepartment != null) tvDepartment.setText(item.department);
            if (tvCourseMetric != null) tvCourseMetric.setText(item.metricDetail);
            if (tvCourseStatus != null) {
                tvCourseStatus.setText(item.status);
                if ("Active".equalsIgnoreCase(item.status) || "High Pass Rate".equalsIgnoreCase(item.status) || "Completed".equalsIgnoreCase(item.status)) {
                    tvCourseStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Incomplete".equalsIgnoreCase(item.status) || "Needs Review".equalsIgnoreCase(item.status)) {
                    tvCourseStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvCourseStatus.setTextColor(Color.parseColor("#155DFC"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(item);
            });
        }
    }
}
