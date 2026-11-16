package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.NotificationAdapter;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private static final String TAG = "SLAS_NOTIF";

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private TextView tvEmptyState, tvEmptyStateDesc;
    private TextView tabAll, tabUnread, tabImportant, btnMarkAllNotificationsRead;

    private final List<Notification> allNotifications = new ArrayList<>();
    private final List<Notification> displayedNotifications = new ArrayList<>();

    private String currentFilter = "ALL"; // ALL, UNREAD, IMPORTANT

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.activity_fragment_notifications,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.nav_notifications), "Stay updated with your academic activities"
            );
        }

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvEmptyStateDesc = view.findViewById(R.id.tvEmptyStateDesc);
        tabAll = view.findViewById(R.id.tabAll);
        tabUnread = view.findViewById(R.id.tabUnread);
        tabImportant = view.findViewById(R.id.tabImportant);
        btnMarkAllNotificationsRead = view.findViewById(R.id.btnMarkAllNotificationsRead);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new NotificationAdapter(getContext(), displayedNotifications, notification -> {
                navigateForNotification(notification);

                if (!notification.isRead()) {
                    notification.setRead(true);
                    updateTabsAndFilter();
                    if (notification.getId() != null && !notification.getId().isEmpty()) {
                        ApiClient.getApiService(requireContext()).markNotificationRead(notification.getId()).enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {}
                            @Override
                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                        });
                    }
                }
            });
            recyclerView.setAdapter(adapter);
        }

        if (tabAll != null) tabAll.setOnClickListener(v -> setFilter("ALL"));
        if (tabUnread != null) tabUnread.setOnClickListener(v -> setFilter("UNREAD"));
        if (tabImportant != null) tabImportant.setOnClickListener(v -> setFilter("IMPORTANT"));

        if (btnMarkAllNotificationsRead != null) {
            btnMarkAllNotificationsRead.setOnClickListener(v -> markAllRead());
        }

        fetchNotifications();
    }

    private void navigateForNotification(Notification notification) {
        if (getContext() == null || notification == null) return;
        String cat = notification.getCategory() != null ? notification.getCategory() : "";
        String title = notification.getTitle() != null ? notification.getTitle().toLowerCase() : "";
        String msg = notification.getMessage() != null ? notification.getMessage().toLowerCase() : "";

        if ("Recommendations".equalsIgnoreCase(cat) || title.contains("recommendation")) {
            startActivity(new Intent(requireContext(), RecommendationDetailActivity.class));
        } else if ("Security".equalsIgnoreCase(cat) || title.contains("risk") || title.contains("alert")) {
            startActivity(new Intent(requireContext(), RiskAnalysisActivity.class));
        } else if ("Report".equalsIgnoreCase(cat) || title.contains("report")) {
            startActivity(new Intent(requireContext(), LearningReportActivity.class));
        } else if ("Quiz".equalsIgnoreCase(cat) || title.contains("quiz") || msg.contains("quiz")) {
            startActivity(new Intent(requireContext(), QuizDetailActivity.class));
        } else if ("Exam".equalsIgnoreCase(cat) || title.contains("exam") || msg.contains("exam")) {
            startActivity(new Intent(requireContext(), ExamDetailActivity.class));
        } else if ("Attendance".equalsIgnoreCase(cat) || title.contains("attendance") || msg.contains("attendance")) {
            startActivity(new Intent(requireContext(), AttendanceDetailActivity.class));
        } else if ("Assignment".equalsIgnoreCase(cat) || "Homework".equalsIgnoreCase(cat)
                || "Grade".equalsIgnoreCase(cat) || "Academic".equalsIgnoreCase(cat)
                || title.contains("assignment") || title.contains("grade")) {
            startActivity(new Intent(requireContext(), AssessmentDetailActivity.class));
        } else if ("Course".equalsIgnoreCase(cat) || title.contains("course")) {
            startActivity(new Intent(requireContext(), CourseDetailActivity.class));
        }
    }

    private void markAllRead() {
        if (getContext() == null) return;
        for (Notification n : allNotifications) {
            n.setRead(true);
        }
        updateTabsAndFilter();

        ApiClient.getApiService(requireContext()).markAllNotificationsRead().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabsAndFilter();
    }

    private void updateTabsAndFilter() {
        if (tabAll == null || tabUnread == null || tabImportant == null) return;

        int purpleText = Color.parseColor("#5935F5");

        // Reset inactive styles
        tabAll.setBackgroundResource(R.drawable.bg_notif_filter_inactive);
        tabAll.setTextColor(purpleText);

        tabUnread.setBackgroundResource(R.drawable.bg_notif_filter_inactive);
        tabUnread.setTextColor(purpleText);

        tabImportant.setBackgroundResource(R.drawable.bg_notif_filter_inactive);
        tabImportant.setTextColor(purpleText);

        // Highlight active tab
        TextView activeTab = tabAll;
        if ("UNREAD".equals(currentFilter)) activeTab = tabUnread;
        else if ("IMPORTANT".equals(currentFilter)) activeTab = tabImportant;

        activeTab.setBackgroundResource(R.drawable.bg_notif_filter_active);
        activeTab.setTextColor(Color.WHITE);

        displayedNotifications.clear();
        int totalCount = allNotifications.size();
        int unreadCount = 0;
        int importantCount = 0;

        for (Notification n : allNotifications) {
            boolean isUnread = !n.isRead();
            String titleLower = n.getTitle() != null ? n.getTitle().toLowerCase() : "";
            String catLower = n.getCategory() != null ? n.getCategory().toLowerCase() : "";
            boolean isImportant = "security".equalsIgnoreCase(catLower)
                    || "recommendations".equalsIgnoreCase(catLower)
                    || titleLower.contains("alert")
                    || titleLower.contains("risk")
                    || titleLower.contains("exam")
                    || catLower.contains("exam");

            if (isUnread) unreadCount++;
            if (isImportant) importantCount++;

            if ("UNREAD".equals(currentFilter) && !isUnread) continue;
            if ("IMPORTANT".equals(currentFilter) && !isImportant) continue;

            displayedNotifications.add(n);
        }

        tabAll.setText(tabAll.getContext().getString(R.string.all_c49d0d) + totalCount + ")");
        tabUnread.setText("Unread (" + unreadCount + ")");
        tabImportant.setText("Important (" + importantCount + ")");

        if (btnMarkAllNotificationsRead != null) {
            btnMarkAllNotificationsRead.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
        }

        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).updateGlobalNotificationBadge(unreadCount);
        }

        if (adapter != null) adapter.notifyDataSetChanged();

        if (layoutEmptyState != null && progressBar != null) {
            if (displayedNotifications.isEmpty() && progressBar.getVisibility() == View.GONE) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                if (tvEmptyStateDesc != null) {
                    tvEmptyStateDesc.setText(getEmptyMessageForFilter(currentFilter));
                }
            } else {
                layoutEmptyState.setVisibility(View.GONE);
            }
        }
    }

    private String getEmptyMessageForFilter(String filter) {
        switch (filter) {
            case "UNREAD": return "You have no unread notifications.";
            case "IMPORTANT": return "You have no important notifications.";
            default: return "No notifications found.";
        }
    }

    private void fetchNotifications() {
        if (getContext() == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);

        ApiClient.getApiService(requireContext()).getNotifications().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getView() == null) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                allNotifications.clear();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        JsonArray array = null;
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            array = body.getAsJsonArray("data");
                        } else if (body.has("notifications") && body.get("notifications").isJsonArray()) {
                            array = body.getAsJsonArray("notifications");
                        }

                        if (array != null) {
                            for (JsonElement element : array) {
                                JsonObject obj = element.getAsJsonObject();
                                String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : "";
                                String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Notification";
                                String message = obj.has("message") && !obj.get("message").isJsonNull() ? obj.get("message").getAsString() : "";
                                String category = obj.has("category") && !obj.get("category").isJsonNull() ? obj.get("category").getAsString() : "System";
                                String dateGroup = obj.has("date_group") && !obj.get("date_group").isJsonNull() ? obj.get("date_group").getAsString() : "RECENT";
                                String timeAgo = obj.has("time_ago") && !obj.get("time_ago").isJsonNull() ? obj.get("time_ago").getAsString() : "just now";

                                boolean isRead = false;
                                if (obj.has("read_at") && !obj.get("read_at").isJsonNull()) isRead = true;
                                else if (obj.has("is_read") && !obj.get("is_read").isJsonNull()) isRead = obj.get("is_read").getAsBoolean();

                                allNotifications.add(new Notification(id, title, message, timeAgo, category, dateGroup, isRead));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing notifications", e);
                    }
                } else {
                    Log.d(TAG, "Notifications HTTP code: " + response.code());
                }

                updateTabsAndFilter();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (layoutEmptyState != null) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    if (tvEmptyStateDesc != null) {
                        tvEmptyStateDesc.setText("Unable to load notifications. Please check your connection.");
                    }
                }
                Log.d(TAG, "Notifications API call failed: " + t.getMessage());
            }
        });
    }
}
