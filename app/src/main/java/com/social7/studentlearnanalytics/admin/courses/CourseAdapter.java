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
    private final List<Course> courseList;
    private final List<Course> filteredList;
    private final OnCourseClickListener listener;

    public CourseAdapter(Context context, List<Course> courseList, OnCourseClickListener listener) {
        this.context = context;
        this.courseList = courseList;
        this.filteredList = new ArrayList<>(courseList);
        this.listener = listener;
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(courseList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Course course : courseList) {
                if ((course.getName() != null && course.getName().toLowerCase().contains(lowerQuery)) ||
                    (course.getCode() != null && course.getCode().toLowerCase().contains(lowerQuery))) {
                    filteredList.add(course);
                }
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
        Course course = filteredList.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
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
                String creditsText = course.getCredits() + " Credits";
                tvCourseCredits.setText(creditsText);
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
