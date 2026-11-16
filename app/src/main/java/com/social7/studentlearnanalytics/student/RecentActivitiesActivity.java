package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecentActivitiesActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_ACTIVITIES";
    private RecyclerView rvRecentActivities;
    private TextView tvEmptyActivities;
    private TextView chipActAll, chipActAssignments, chipActQuizzes, chipActGrades, chipActAttendance, chipActCourses;
    private final List<ActivityItem> allItems = new ArrayList<>();
    private final List<ActivityItem> filteredItems = new ArrayList<>();
    private ActivityAdapter adapter;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_activities);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEmptyActivities = findViewById(R.id.tvEmptyActivities);
        rvRecentActivities = findViewById(R.id.rvRecentActivities);
        if (rvRecentActivities != null) {
            rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ActivityAdapter(filteredItems, this::onActivityItemClick);
            rvRecentActivities.setAdapter(adapter);
        }

        chipActAll = findViewById(R.id.chipActAll);
        chipActAssignments = findViewById(R.id.chipActAssignments);
        chipActQuizzes = findViewById(R.id.chipActQuizzes);
        chipActGrades = findViewById(R.id.chipActGrades);
        chipActAttendance = findViewById(R.id.chipActAttendance);
        chipActCourses = findViewById(R.id.chipActCourses);

        setupCategoryChips();
        fetchRecentActivities();
    }

    private void setupCategoryChips() {
        TextView[] chips = {chipActAll, chipActAssignments, chipActQuizzes, chipActGrades, chipActAttendance, chipActCourses};
        String[] categories = {"All", "Assignments", "Quizzes", "Grades", "Attendance", "Courses"};

        for (int i = 0; i < chips.length; i++) {
            final TextView chip = chips[i];
            final String cat = categories[i];
            if (chip != null) {
                chip.setOnClickListener(v -> {
                    selectedCategory = cat;
                    for (TextView c : chips) {
                        if (c != null) {
                            c.setBackgroundResource(R.drawable.bg_filter_inactive);
                            c.setTextColor(Color.parseColor("#64748B"));
                        }
                    }
                    chip.setBackgroundResource(R.drawable.bg_filter_active);
                    chip.setTextColor(Color.WHITE);
                    applyFilter();
                });
            }
        }
    }

    private void applyFilter() {
        filteredItems.clear();
        for (ActivityItem item : allItems) {
            if ("All".equalsIgnoreCase(selectedCategory)
                    || (item.category != null && item.category.equalsIgnoreCase(selectedCategory))) {
                filteredItems.add(item);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (tvEmptyActivities != null) {
            tvEmptyActivities.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void onActivityItemClick(ActivityItem item) {
        if (item == null) return;
        String cat = item.category != null ? item.category : "";
        String type = item.type != null ? item.type : "";
        if ("Assignments".equalsIgnoreCase(cat) || "assignment".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, AssessmentDetailActivity.class));
        } else if ("Quizzes".equalsIgnoreCase(cat) || "quiz".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, QuizDetailActivity.class));
        } else if ("Grades".equalsIgnoreCase(cat) || "exam".equalsIgnoreCase(type)) {
            if ("exam".equalsIgnoreCase(type)) {
                startActivity(new Intent(this, ExamDetailActivity.class));
            } else {
                startActivity(new Intent(this, AssessmentDetailActivity.class));
            }
        } else if ("Attendance".equalsIgnoreCase(cat) || "attendance".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, AttendanceDetailActivity.class));
        } else if ("Courses".equalsIgnoreCase(cat) || "course".equalsIgnoreCase(type)) {
            Intent intent = new Intent(this, CourseDetailActivity.class);
            if (item.referenceId != null) intent.putExtra("course_id", String.valueOf(item.referenceId));
            if (item.courseCode != null) intent.putExtra("course_code", item.courseCode);
            if (item.courseName != null) intent.putExtra("course", (item.courseCode != null ? item.courseCode + " - " : "") + item.courseName);
            startActivity(intent);
        }
    }

    private void fetchRecentActivities() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                allItems.clear();
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    allItems.addAll(buildActivitiesFromDashboard(data));
                } else {
                    Log.d(TAG, "Dashboard API code: " + response.code());
                }
                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "Dashboard API failed: " + t.getMessage());
                allItems.clear();
                applyFilter();
            }
        });
    }

    private List<ActivityItem> buildActivitiesFromDashboard(DashboardResponse.DashboardData data) {
        List<ActivityItem> list = new ArrayList<>();
        if (data == null) return list;

        List<DashboardResponse.RecentActivityInfo> realActivities = data.getRecentActivities();
        if (realActivities != null && !realActivities.isEmpty()) {
            for (DashboardResponse.RecentActivityInfo act : realActivities) {
                if (act == null) continue;
                int iconRes = R.drawable.ic_book;
                int colorRes = R.color.primary;
                String cat = act.category != null ? act.category : "Courses";
                if ("Assignments".equalsIgnoreCase(cat)) {
                    iconRes = R.drawable.ic_assignment;
                    colorRes = R.color.primary;
                } else if ("Quizzes".equalsIgnoreCase(cat)) {
                    iconRes = R.drawable.ic_analytics;
                    colorRes = R.color.warning_orange;
                } else if ("Grades".equalsIgnoreCase(cat)) {
                    iconRes = R.drawable.ic_prediction;
                    colorRes = R.color.success_green;
                } else if ("Attendance".equalsIgnoreCase(cat)) {
                    iconRes = R.drawable.ic_home;
                    colorRes = R.color.success_green;
                }

                String timeDisplay = act.timeAgo != null && !act.timeAgo.isEmpty()
                        ? act.timeAgo
                        : (act.timestamp != null ? act.timestamp : "");
                if (act.statusBadge != null && !act.statusBadge.isEmpty()) {
                    timeDisplay = act.statusBadge + (timeDisplay.isEmpty() ? "" : " • " + timeDisplay);
                }

                ActivityItem item = new ActivityItem(
                        act.title != null ? act.title : "Academic Activity",
                        act.description != null ? act.description : "",
                        timeDisplay,
                        iconRes,
                        colorRes
                );
                item.category = cat;
                item.type = act.type;
                item.courseCode = act.courseCode;
                item.courseName = act.courseName;
                item.referenceId = act.referenceId;
                list.add(item);
            }
            return list;
        }

        List<DashboardResponse.EnrollmentInfo> enrollments = data.getEnrollments();
        if (enrollments != null && !enrollments.isEmpty()) {
            for (DashboardResponse.EnrollmentInfo info : enrollments) {
                if (info != null && info.course != null) {
                    String cName = info.course.courseName != null ? info.course.courseName : "Course";
                    String cCode = info.course.courseCode != null ? info.course.courseCode : "";
                    String scorePart = info.finalScore != null ? (" • Score: " + Math.round(info.finalScore) + "%") : "";

                    ActivityItem item = new ActivityItem(
                            "Enrolled in " + cCode,
                            cName + scorePart,
                            "Current Semester",
                            R.drawable.ic_book,
                            R.color.primary
                    );
                    item.category = "Courses";
                    item.type = "course";
                    item.courseCode = cCode;
                    item.courseName = cName;
                    list.add(item);
                }
            }
        }

        return list;
    }

    public static class ActivityItem {
        public String title;
        public String description;
        public String time;
        public int iconResId;
        public int colorResId;
        public String category;
        public String type;
        public String courseCode;
        public String courseName;
        public Integer referenceId;

        public ActivityItem(String title, String description, String time, int iconResId, int colorResId) {
            this.title = title;
            this.description = description;
            this.time = time;
            this.iconResId = iconResId;
            this.colorResId = colorResId;
        }
    }

    public interface OnActivityClickListener {
        void onClick(ActivityItem item);
    }

    private static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private final List<ActivityItem> items;
        private final OnActivityClickListener listener;

        public ActivityAdapter(List<ActivityItem> items, OnActivityClickListener listener) {
            this.items = items;
            this.listener = listener;
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
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
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
