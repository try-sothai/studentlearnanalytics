package com.social7.studentlearnanalytics.student;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class StudentRecCardAdapter extends RecyclerView.Adapter<StudentRecCardAdapter.ViewHolder> {

    public static class StudentRecItem {
        public String id;
        public String courseId;
        public String courseCode;
        public String courseName;
        public String title;
        public String description;
        public String priorityBadge;
        public String statusBadge;

        public StudentRecItem(String id, String courseCode, String title, String description, String priorityBadge, String statusBadge) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.description = description;
            this.priorityBadge = priorityBadge;
            this.statusBadge = statusBadge;
        }
    }

    public interface OnRecCardClickListener {
        void onReviewTopicClick(StudentRecItem item);
        void onMarkCompletedClick(StudentRecItem item);
    }

    private final Context context;
    private final List<StudentRecItem> originalList;
    private final List<StudentRecItem> filteredList;
    private final OnRecCardClickListener listener;
    private String currentQuery = "";
    private String currentCategory = "All";

    public StudentRecCardAdapter(Context context, List<StudentRecItem> list, OnRecCardClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void refresh() {
        filter(currentQuery, currentCategory);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_recommendation_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentRecItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query, String category) {
        this.currentQuery = query != null ? query : "";
        this.currentCategory = category != null ? category : "All";
        filteredList.clear();
        String text = this.currentQuery.trim().toLowerCase();
        for (StudentRecItem item : originalList) {
            boolean matchesQuery = text.isEmpty() ||
                    (item.courseCode != null && item.courseCode.toLowerCase().contains(text)) ||
                    (item.courseName != null && item.courseName.toLowerCase().contains(text)) ||
                    (item.title != null && item.title.toLowerCase().contains(text)) ||
                    (item.description != null && item.description.toLowerCase().contains(text));

            boolean matchesCat = this.currentCategory.trim().isEmpty() || "All".equalsIgnoreCase(this.currentCategory) ||
                    (item.priorityBadge != null && item.priorityBadge.equalsIgnoreCase(this.currentCategory)) ||
                    (item.statusBadge != null && item.statusBadge.equalsIgnoreCase(this.currentCategory));

            if (matchesQuery && matchesCat) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvTitle, tvDescription, tvPriorityBadge, tvStatusBadge;
        private final MaterialButton btnReviewTopic, btnMarkCompleted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            btnReviewTopic = itemView.findViewById(R.id.btnReviewTopic);
            btnMarkCompleted = itemView.findViewById(R.id.btnMarkCompleted);
        }

        public void bind(StudentRecItem item, OnRecCardClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvTitle != null) tvTitle.setText(item.title);
            if (tvDescription != null) tvDescription.setText(item.description);

            if (tvPriorityBadge != null) {
                tvPriorityBadge.setText(item.priorityBadge);
                if (item.priorityBadge != null && item.priorityBadge.contains("High")) {
                    tvPriorityBadge.setTextColor(Color.parseColor("#EF4444"));
                } else if (item.priorityBadge != null && item.priorityBadge.contains("Medium")) {
                    tvPriorityBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvPriorityBadge.setTextColor(Color.parseColor("#10B981"));
                }
            }

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(item.statusBadge);
                if ("Completed".equalsIgnoreCase(item.statusBadge) || "Read".equalsIgnoreCase(item.statusBadge)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvStatusBadge.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            if (btnReviewTopic != null) {
                btnReviewTopic.setOnClickListener(v -> {
                    if (listener != null) listener.onReviewTopicClick(item);
                });
            }

            if (btnMarkCompleted != null) {
                btnMarkCompleted.setOnClickListener(v -> {
                    if (listener != null) listener.onMarkCompletedClick(item);
                });
            }
        }
    }
}
