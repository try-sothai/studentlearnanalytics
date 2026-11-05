package com.social7.studentlearnanalytics.teacher;

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

public class StudentRecommendationAdapter extends RecyclerView.Adapter<StudentRecommendationAdapter.ViewHolder> {

    public static class StudentRecommendationItem {
        public String studentId;
        public String studentName;
        public String courseClass;
        public String recommendationTitle;
        public String academicStats;
        public String priorityBadge;

        public StudentRecommendationItem(String studentId, String studentName, String courseClass, String recommendationTitle, String academicStats, String priorityBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseClass = courseClass;
            this.recommendationTitle = recommendationTitle;
            this.academicStats = academicStats;
            this.priorityBadge = priorityBadge;
        }
    }

    public interface OnRecommendationClickListener {
        void onRecommendationClick(StudentRecommendationItem item);
    }

    private final Context context;
    private final List<StudentRecommendationItem> originalList;
    private final List<StudentRecommendationItem> filteredList;
    private final OnRecommendationClickListener listener;

    public StudentRecommendationAdapter(Context context, List<StudentRecommendationItem> list, OnRecommendationClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_student_recommendation_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentRecommendationItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (StudentRecommendationItem item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.courseClass != null && item.courseClass.toLowerCase().contains(text)) ||
                (item.recommendationTitle != null && item.recommendationTitle.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvCourseClass, tvRecommendationTitle, tvAcademicStats, tvPriorityBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvCourseClass = itemView.findViewById(R.id.tvCourseClass);
            tvRecommendationTitle = itemView.findViewById(R.id.tvRecommendationTitle);
            tvAcademicStats = itemView.findViewById(R.id.tvAcademicStats);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
        }

        public void bind(StudentRecommendationItem item, OnRecommendationClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvCourseClass != null) tvCourseClass.setText(item.courseClass);
            if (tvRecommendationTitle != null) tvRecommendationTitle.setText(item.recommendationTitle);
            if (tvAcademicStats != null) tvAcademicStats.setText(item.academicStats);

            if (tvPriorityBadge != null) {
                tvPriorityBadge.setText(item.priorityBadge);
                if (item.priorityBadge.contains("High")) {
                    tvPriorityBadge.setTextColor(Color.parseColor("#EF4444"));
                } else if (item.priorityBadge.contains("Medium")) {
                    tvPriorityBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvPriorityBadge.setTextColor(Color.parseColor("#10B981"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRecommendationClick(item);
            });
        }
    }
}
