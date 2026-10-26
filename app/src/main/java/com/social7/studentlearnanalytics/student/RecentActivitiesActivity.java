package com.social7.studentlearnanalytics.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class RecentActivitiesActivity extends AppCompatActivity {

    private RecyclerView rvRecentActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_activities);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvRecentActivities = findViewById(R.id.rvRecentActivities);
        if (rvRecentActivities != null) {
            rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));
            List<ActivityItem> items = getSampleActivities();
            rvRecentActivities.setAdapter(new ActivityAdapter(items));
        }
    }

    private List<ActivityItem> getSampleActivities() {
        List<ActivityItem> list = new ArrayList<>();
        list.add(new ActivityItem("New Assignment Added", "Data Structures - Due May 25, 2024", "2 hours ago", R.drawable.ic_assignment, R.color.primary));
        list.add(new ActivityItem("Quiz Result Released", "Web Development - Score: 85/100", "5 hours ago", R.drawable.ic_analytics, R.color.warning_orange));
        list.add(new ActivityItem("Grade Updated", "Database Systems - Grade: A-", "1 day ago", R.drawable.ic_prediction, R.color.success_green));
        list.add(new ActivityItem("New Course Material", "Computer Networks - Chapter 4 Lecture Notes", "2 days ago", R.drawable.ic_book, R.color.primary));
        list.add(new ActivityItem("Attendance Recorded", "Software Engineering - Present", "3 days ago", R.drawable.ic_home, R.color.success_green));
        list.add(new ActivityItem("Exam Schedule Published", "Final Exam - Room 302, June 10", "4 days ago", R.drawable.ic_prediction, R.color.danger_red));
        return list;
    }

    public static class ActivityItem {
        public String title;
        public String description;
        public String time;
        public int iconResId;
        public int colorResId;

        public ActivityItem(String title, String description, String time, int iconResId, int colorResId) {
            this.title = title;
            this.description = description;
            this.time = time;
            this.iconResId = iconResId;
            this.colorResId = colorResId;
        }
    }

    private static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private final List<ActivityItem> items;

        public ActivityAdapter(List<ActivityItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActivityItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDescription.setText(item.description);
            holder.tvTime.setText(item.time);
            holder.imgActivityIcon.setImageResource(item.iconResId);
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvTime;
            ImageView imgActivityIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvTime = itemView.findViewById(R.id.tvTime);
                imgActivityIcon = itemView.findViewById(R.id.imgActivityIcon);
            }
        }
    }
}
