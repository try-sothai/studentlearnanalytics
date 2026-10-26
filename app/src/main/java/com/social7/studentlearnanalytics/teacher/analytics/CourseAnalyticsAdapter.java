package com.social7.studentlearnanalytics.teacher.analytics;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseAnalyticsAdapter extends RecyclerView.Adapter<CourseAnalyticsAdapter.ViewHolder> {

    public interface OnCourseAnalyticsClickListener {
        void onCourseClick(CourseAnalyticsItem item);
    }

    public static class CourseAnalyticsItem implements Serializable {
        public String id;
        public String code;
        public String name;
        public String instructor;
        public String studentsCount;
        public String semester;
        public int avgScore;
        public String status; // "Good", "Medium", "At Risk"
        public int iconBgRes;

        public CourseAnalyticsItem(String id, String code, String name, String instructor,
                                    String studentsCount, String semester, int avgScore,
                                    String status, int iconBgRes) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.instructor = instructor;
            this.studentsCount = studentsCount;
            this.semester = semester;
            this.avgScore = avgScore;
            this.status = status;
            this.iconBgRes = iconBgRes;
        }
    }

    private final Context context;
    private final List<CourseAnalyticsItem> originalList;
    private List<CourseAnalyticsItem> filteredList;
    private final OnCourseAnalyticsClickListener listener;

    public CourseAnalyticsAdapter(Context context, List<CourseAnalyticsItem> list, OnCourseAnalyticsClickListener listener) {
        this.context = context;
        this.originalList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void filter(String query, String statusFilter) {
        filteredList.clear();
        for (CourseAnalyticsItem item : originalList) {
            boolean matchesQuery = query == null || query.trim().isEmpty()
                    || item.code.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || item.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || item.instructor.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

            boolean matchesStatus = statusFilter == null || statusFilter.trim().isEmpty() || statusFilter.equalsIgnoreCase("All")
                    || item.status.equalsIgnoreCase(statusFilter);

            if (matchesQuery && matchesStatus) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_analytics_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseAnalyticsItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconCardContainer;
        ImageView imgCardCourseIcon;
        TextView tvCardCourseCodeName, tvCardInstructor, tvCardMetadata;
        TextView tvCardAvgScore, tvCardStatusBadge;
        ProgressBar progressBarAvgScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCardContainer = itemView.findViewById(R.id.iconCardContainer);
            imgCardCourseIcon = itemView.findViewById(R.id.imgCardCourseIcon);
            tvCardCourseCodeName = itemView.findViewById(R.id.tvCardCourseCodeName);
            tvCardInstructor = itemView.findViewById(R.id.tvCardInstructor);
            tvCardMetadata = itemView.findViewById(R.id.tvCardMetadata);
            tvCardAvgScore = itemView.findViewById(R.id.tvCardAvgScore);
            tvCardStatusBadge = itemView.findViewById(R.id.tvCardStatusBadge);
            progressBarAvgScore = itemView.findViewById(R.id.progressBarAvgScore);
        }

        public void bind(final CourseAnalyticsItem item, final OnCourseAnalyticsClickListener listener) {
            if (tvCardCourseCodeName != null) tvCardCourseCodeName.setText(item.code + " - " + item.name);
            if (tvCardInstructor != null) tvCardInstructor.setText("Instructor: " + item.instructor);
            if (tvCardMetadata != null) tvCardMetadata.setText(item.studentsCount + " • " + item.semester);
            if (tvCardAvgScore != null) tvCardAvgScore.setText(item.avgScore + "%");

            if (progressBarAvgScore != null) {
                progressBarAvgScore.setProgress(item.avgScore);
            }

            if (iconCardContainer != null && item.iconBgRes != 0) {
                iconCardContainer.setBackgroundResource(item.iconBgRes);
            }

            if (tvCardStatusBadge != null) {
                tvCardStatusBadge.setText(item.status);
                if ("Good".equalsIgnoreCase(item.status)) {
                    tvCardStatusBadge.setTextColor(Color.parseColor("#16B879"));
                    tvCardStatusBadge.setBackgroundResource(R.drawable.bg_status_good);
                } else if ("Medium".equalsIgnoreCase(item.status)) {
                    tvCardStatusBadge.setTextColor(Color.parseColor("#FF9B16"));
                    tvCardStatusBadge.setBackgroundResource(R.drawable.bg_status_attention);
                } else {
                    tvCardStatusBadge.setTextColor(Color.parseColor("#FF4055"));
                    tvCardStatusBadge.setBackgroundResource(R.drawable.bg_status_at_risk);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(item);
            });
        }
    }
}
