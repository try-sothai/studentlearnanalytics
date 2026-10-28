package com.social7.studentlearnanalytics.student;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecentActivitiesActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_ACTIVITIES";
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
        }

        fetchRecentActivities();
    }

    private void fetchRecentActivities() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    List<ActivityItem> items = buildActivitiesFromDashboard(data);
                    bindActivities(items);
                } else {
                    Log.d(TAG, "Dashboard API code: " + response.code());
                    bindActivities(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "Dashboard API failed: " + t.getMessage());
                bindActivities(new ArrayList<>());
            }
        });
    }

    private List<ActivityItem> buildActivitiesFromDashboard(DashboardResponse.DashboardData data) {
        List<ActivityItem> list = new ArrayList<>();
        if (data == null) return list;

        List<DashboardResponse.EnrollmentInfo> enrollments = data.getEnrollments();
        if (enrollments != null && !enrollments.isEmpty()) {
            for (DashboardResponse.EnrollmentInfo info : enrollments) {
                if (info != null && info.course != null) {
                    String cName = info.course.courseName != null ? info.course.courseName : "Course";
                    String cCode = info.course.courseCode != null ? info.course.courseCode : "";
                    float score = info.finalScore != null ? info.finalScore : 0f;

                    list.add(new ActivityItem(
                            "Course Enrollment: " + cCode,
                            cName + " • Overall Score: " + Math.round(score) + "%",
                            "Current Semester",
                            R.drawable.ic_book,
                            R.color.primary
                    ));
                }
            }
        }

        String att = data.getAttendanceRateFormatted();
        if (!"N/A".equalsIgnoreCase(att)) {
            list.add(new ActivityItem(
                    "Attendance Record Updated",
                    "Attendance Rate: " + att,
                    "Recent Record",
                    R.drawable.ic_home,
                    R.color.success_green
            ));
        }

        String assign = data.getAssignmentScoreFormatted();
        if (!"N/A".equalsIgnoreCase(assign)) {
            list.add(new ActivityItem(
                    "Assignment Summary",
                    "Average Assignment Score: " + assign,
                    "Recent Submissions",
                    R.drawable.ic_assignment,
                    R.color.primary
            ));
        }

        String quiz = data.getQuizScoreFormatted();
        if (!"N/A".equalsIgnoreCase(quiz)) {
            list.add(new ActivityItem(
                    "Quiz Summary",
                    "Average Quiz Score: " + quiz,
                    "Recent Attempts",
                    R.drawable.ic_analytics,
                    R.color.warning_orange
            ));
        }

        String pred = data.getPredictedScoreFormatted();
        if (!"Not available yet".equalsIgnoreCase(pred)) {
            list.add(new ActivityItem(
                    "ML Score Prediction",
                    "Predicted Exam Score: " + pred,
                    "Latest Evaluation",
                    R.drawable.ic_prediction,
                    R.color.success_green
            ));
        }

        return list;
    }

    private void bindActivities(List<ActivityItem> items) {
        if (rvRecentActivities != null) {
            rvRecentActivities.setAdapter(new ActivityAdapter(items));
        }
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
