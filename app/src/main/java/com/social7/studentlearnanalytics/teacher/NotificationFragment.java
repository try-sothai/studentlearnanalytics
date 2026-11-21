package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private TextView tabAll, tabUnread, tabImportant;
    private RecyclerView rvTeacherNotifications;
    private NotificationAdapter adapter;
    private final List<Notification> allNotifications = new ArrayList<>();
    private final List<Notification> displayedNotifications = new ArrayList<>();
    private String currentFilter = "ALL";

    public NotificationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_notifications,
                container,
                false
        );

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherNotifications), getString(R.string.nav_notifications), "Academic alerts & updates"
            );
        }

        TextView btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllReadOnApi());
        }

        tabAll = view.findViewById(R.id.tabNotifAll);
        tabUnread = view.findViewById(R.id.tabNotifUnread);
        tabImportant = view.findViewById(R.id.tabNotifImportant);

        if (tabAll != null) tabAll.setOnClickListener(v -> setFilter("ALL"));
        if (tabUnread != null) tabUnread.setOnClickListener(v -> setFilter("UNREAD"));
        if (tabImportant != null) tabImportant.setOnClickListener(v -> setFilter("IMPORTANT"));

        rvTeacherNotifications = view.findViewById(R.id.rvTeacherNotifications);
        if (rvTeacherNotifications != null) {
            rvTeacherNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new NotificationAdapter(requireContext(), displayedNotifications, this::markSingleReadOnApi);
            rvTeacherNotifications.setAdapter(adapter);
        }

        fetchNotificationsFromApi();

        return view;
    }

    private void fetchNotificationsFromApi() {
        if (getContext() == null) return;
        ApiClient.getApiService(requireContext()).getNotifications().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                allNotifications.clear();
                if (response.isSuccessful() && response.body() != null && response.body().has("data") && response.body().get("data").isJsonArray()) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = getStr(obj, "id", "0");
                        String title = getStr(obj, "title", "Notification");
                        String msg = getStr(obj, "message", "");
                        String time = getStr(obj, "created_at", "Recent");
                        String type = getStr(obj, "type", "System");
                        boolean isRead = (obj.has("is_read") && !obj.get("is_read").isJsonNull() && obj.get("is_read").getAsBoolean())
                                || (obj.has("read_at") && !obj.get("read_at").isJsonNull());
                        boolean isImp = "high".equalsIgnoreCase(getStr(obj, "priority", "")) || "alert".equalsIgnoreCase(type);
                        allNotifications.add(new Notification(id, title, msg, time, type, "RECENT", isRead, isImp));
                    }
                }
                filterNotifications(currentFilter);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markSingleReadOnApi(Notification notification) {
        if (getContext() == null || notification == null || notification.getId() == null) return;
        ApiClient.getApiService(requireContext()).markNotificationRead(notification.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                notification.setRead(true);
                filterNotifications(currentFilter);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void markAllReadOnApi() {
        if (getContext() == null) return;
        ApiClient.getApiService(requireContext()).markAllNotificationsRead().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                for (Notification n : allNotifications) {
                    n.setRead(true);
                }
                filterNotifications(currentFilter);
                Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error marking notifications read", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabStyles();
        filterNotifications(filter);
    }

    private void updateTabStyles() {
        if (tabAll == null || tabUnread == null || tabImportant == null) return;

        tabAll.setBackgroundColor(Color.TRANSPARENT);
        tabUnread.setBackgroundColor(Color.TRANSPARENT);
        tabImportant.setBackgroundColor(Color.TRANSPARENT);

        tabAll.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tabUnread.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tabImportant.setTextColor(getResources().getColor(R.color.text_secondary, null));

        TextView activeTab = tabAll;
        if ("UNREAD".equals(currentFilter)) activeTab = tabUnread;
        else if ("IMPORTANT".equals(currentFilter)) activeTab = tabImportant;

        activeTab.setBackgroundResource(R.drawable.bg_notif_tab_active);
        activeTab.setTextColor(Color.WHITE);
    }

    private void filterNotifications(String filter) {
        displayedNotifications.clear();
        for (Notification n : allNotifications) {
            if ("UNREAD".equals(filter)) {
                if (!n.isRead()) displayedNotifications.add(n);
            } else if ("IMPORTANT".equals(filter)) {
                if (n.isImportant()) displayedNotifications.add(n);
            } else {
                displayedNotifications.add(n);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
