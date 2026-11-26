package com.social7.studentlearnanalytics.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.NotificationAdapter;
import com.social7.studentlearnanalytics.admin.users.NotificationSettingsActivity;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private LinearLayout layoutNotifEmptyState;
    private TextView tvEmptyStateTitle, tvEmptyStateDesc, tvNotifBadgeCount;
    private MaterialButton btnResetNotifSearch;
    private EditText etSearchNotification;

    private TextView pillNotifAll, pillNotifUnread, pillNotifSystem, pillNotifUser, pillNotifCourse;

    private final List<Notification> masterNotificationList = new ArrayList<>();
    private final List<Notification> displayedNotificationList = new ArrayList<>();

    private String currentCategoryFilter = "ALL"; // "ALL", "UNREAD", "System", "User", "Course"
    private String currentSearchQuery = "";
    private boolean lastRequestFailed = false;

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadNotificationsFromApi();
                }
            }
    );

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
                R.layout.fragment_admin_notifications,
                container,
                false
        );

        initViews(view);
        setupFilterChips();
        setupSearchAndFilter(view);
        setupOverflowMenu(view);
        loadNotificationsFromApi();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && getContext() != null) {
            loadNotificationsFromApi();
        }
    }

    private void initViews(View view) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        layoutNotifEmptyState = view.findViewById(R.id.layoutNotifEmptyState);
        tvEmptyStateTitle = view.findViewById(R.id.tvEmptyStateTitle);
        tvEmptyStateDesc = view.findViewById(R.id.tvEmptyStateDesc);
        tvNotifBadgeCount = view.findViewById(R.id.tvNotifBadgeCount);
        btnResetNotifSearch = view.findViewById(R.id.btnResetNotifSearch);
        etSearchNotification = view.findViewById(R.id.etSearchNotification);

        pillNotifAll = view.findViewById(R.id.pillNotifAll);
        pillNotifUnread = view.findViewById(R.id.pillNotifUnread);
        pillNotifSystem = view.findViewById(R.id.pillNotifSystem);
        pillNotifUser = view.findViewById(R.id.pillNotifUser);
        pillNotifCourse = view.findViewById(R.id.pillNotifCourse);

        View btnMenu = view.findViewById(R.id.btnNotifMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (rvNotifications != null) {
            rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (btnResetNotifSearch != null) {
            btnResetNotifSearch.setOnClickListener(v -> {
                if (etSearchNotification != null) etSearchNotification.setText("");
                selectFilterChip("ALL");
            });
        }
    }

    private void loadNotificationsFromApi() {
        if (!isAdded() || getContext() == null) return;
        lastRequestFailed = false;

        ApiClient.getApiService(requireContext()).getNotifications().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                masterNotificationList.clear();
                if (response.isSuccessful() && response.body() != null) {
                    masterNotificationList.addAll(UiUtils.parseNotificationsJson(response.body()));
                } else {
                    lastRequestFailed = true;
                }
                filterAndRefreshNotifications();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                lastRequestFailed = true;
                masterNotificationList.clear();
                filterAndRefreshNotifications();
            }
        });
    }

    private void setupFilterChips() {
        if (pillNotifAll != null) pillNotifAll.setOnClickListener(v -> selectFilterChip("ALL"));
        if (pillNotifUnread != null) pillNotifUnread.setOnClickListener(v -> selectFilterChip("UNREAD"));
        if (pillNotifSystem != null) pillNotifSystem.setOnClickListener(v -> selectFilterChip("System"));
        if (pillNotifUser != null) pillNotifUser.setOnClickListener(v -> selectFilterChip("User"));
        if (pillNotifCourse != null) pillNotifCourse.setOnClickListener(v -> selectFilterChip("Course"));
    }

    private void selectFilterChip(String category) {
        currentCategoryFilter = category;

        updateChipStyle(pillNotifAll, "ALL".equalsIgnoreCase(category));
        updateChipStyle(pillNotifUnread, "UNREAD".equalsIgnoreCase(category));
        updateChipStyle(pillNotifSystem, "System".equalsIgnoreCase(category));
        updateChipStyle(pillNotifUser, "User".equalsIgnoreCase(category));
        updateChipStyle(pillNotifCourse, "Course".equalsIgnoreCase(category));

        if ("ALL".equalsIgnoreCase(category)) {
            UiUtils.scrollToTab(pillNotifAll);
        } else if ("UNREAD".equalsIgnoreCase(category)) {
            UiUtils.scrollToTab(pillNotifUnread);
        } else if ("System".equalsIgnoreCase(category)) {
            UiUtils.scrollToTab(pillNotifSystem);
        } else if ("User".equalsIgnoreCase(category)) {
            UiUtils.scrollToTab(pillNotifUser);
        } else if ("Course".equalsIgnoreCase(category)) {
            UiUtils.scrollToTab(pillNotifCourse);
        }

        filterAndRefreshNotifications();
    }

    private void updateChipStyle(TextView pill, boolean isActive) {
        if (pill == null) return;
        pill.setBackgroundResource(isActive ? R.drawable.bg_notif_chip_active : R.drawable.bg_notif_chip_inactive);
        pill.setTextColor(isActive ? Color.WHITE : Color.parseColor("#081B88"));
    }

    private void setupSearchAndFilter(View view) {
        if (etSearchNotification != null) {
            etSearchNotification.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s != null ? s.toString().trim() : "";
                    filterAndRefreshNotifications();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        View btnFilter = view.findViewById(R.id.btnFilterNotifications);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterOptionsDialog());
        }
    }

    private void showFilterOptionsDialog() {
        if (!isAdded() || getContext() == null) return;
        String[] options = {"All Categories", "User Notifications", "Course Notifications", "System Notifications", "Unread Only"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.filter_notifications)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: selectFilterChip("ALL"); break;
                        case 1: selectFilterChip("User"); break;
                        case 2: selectFilterChip("Course"); break;
                        case 3: selectFilterChip("System"); break;
                        case 4: selectFilterChip("UNREAD"); break;
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupOverflowMenu(View view) {
        ImageView btnOverflow = view.findViewById(R.id.btnNotifOverflow);
        if (btnOverflow != null) {
            btnOverflow.setOnClickListener(this::showOverflowPopupMenu);
        }
    }

    private void showOverflowPopupMenu(View anchor) {
        if (!isAdded() || getContext() == null) return;
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Mark All as Read");
        popup.getMenu().add(0, 2, 1, "Delete Read");
        popup.getMenu().add(0, 3, 2, "Notification Settings");

        int unreadCount = getUnreadCount();
        int readCount = masterNotificationList.size() - unreadCount;

        if (unreadCount == 0) {
            popup.getMenu().findItem(1).setEnabled(false);
        }

        if (readCount == 0) {
            popup.getMenu().findItem(2).setEnabled(false);
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                markAllAsRead();
                return true;
            } else if (id == 2) {
                showDeleteReadConfirmationDialog();
                return true;
            } else if (id == 3) {
                if (!isAdded() || getContext() == null) return false;
                Intent intent = new Intent(requireContext(), NotificationSettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void markAllAsRead() {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(requireContext()).markAllNotificationsRead().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    for (Notification n : masterNotificationList) {
                        n.setRead(true);
                    }
                    filterAndRefreshNotifications();
                    Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to mark all as read", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteReadConfirmationDialog() {
        if (!isAdded() || getContext() == null) return;
        List<Notification> readItems = new ArrayList<>();
        for (Notification n : masterNotificationList) {
            if (n.isRead() && n.getId() != null && !n.getId().isEmpty()) {
                readItems.add(n);
            }
        }
        if (readItems.isEmpty()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_read_notifications)
                .setMessage(R.string.are_you_sure_you_want_to_delete_all_read_n)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    AtomicInteger remaining = new AtomicInteger(readItems.size());
                    for (Notification n : readItems) {
                        ApiClient.getApiService(requireContext()).deleteNotification(n.getId())
                                .enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                        if (response.isSuccessful()) {
                                            masterNotificationList.remove(n);
                                        }
                                        if (remaining.decrementAndGet() == 0 && isAdded() && getContext() != null) {
                                            filterAndRefreshNotifications();
                                            Toast.makeText(getContext(), "Read notifications deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                        if (remaining.decrementAndGet() == 0 && isAdded() && getContext() != null) {
                                            filterAndRefreshNotifications();
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private int getUnreadCount() {
        int count = 0;
        for (Notification n : masterNotificationList) {
            if (!n.isRead()) count++;
        }
        return count;
    }

    private void filterAndRefreshNotifications() {
        if (!isAdded() || getContext() == null) return;
        displayedNotificationList.clear();

        int systemCount = 0;
        int userCount = 0;
        int courseCount = 0;

        for (Notification n : masterNotificationList) {
            String cat = n.getCategory() != null ? n.getCategory() : "System";
            if ("System".equalsIgnoreCase(cat) || "Security".equalsIgnoreCase(cat)) systemCount++;
            if ("User".equalsIgnoreCase(cat)) userCount++;
            if ("Course".equalsIgnoreCase(cat) || "Academic".equalsIgnoreCase(cat)) courseCount++;

            boolean matchesCategory = true;
            if ("UNREAD".equalsIgnoreCase(currentCategoryFilter)) {
                matchesCategory = !n.isRead();
            } else if (!"ALL".equalsIgnoreCase(currentCategoryFilter)) {
                matchesCategory = currentCategoryFilter.equalsIgnoreCase(cat);
            }

            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                String q = currentSearchQuery.toLowerCase();
                boolean inTitle = n.getTitle() != null && n.getTitle().toLowerCase().contains(q);
                boolean inMsg = n.getMessage() != null && n.getMessage().toLowerCase().contains(q);
                boolean inCat = cat.toLowerCase().contains(q);
                matchesSearch = inTitle || inMsg || inCat;
            }

            if (matchesCategory && matchesSearch) {
                displayedNotificationList.add(n);
            }
        }

        int unreadCount = getUnreadCount();
        if (tvNotifBadgeCount != null) {
            tvNotifBadgeCount.setText(String.valueOf(unreadCount));
            tvNotifBadgeCount.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
        }

        if (pillNotifAll != null) pillNotifAll.setText(pillNotifAll.getContext().getString(R.string.all_c49d0d) + masterNotificationList.size() + ")");
        if (pillNotifUnread != null) pillNotifUnread.setText("Unread (" + unreadCount + ")");
        if (pillNotifSystem != null) pillNotifSystem.setText("System (" + systemCount + ")");
        if (pillNotifUser != null) pillNotifUser.setText("User (" + userCount + ")");
        if (pillNotifCourse != null) pillNotifCourse.setText("Course (" + courseCount + ")");

        if (displayedNotificationList.isEmpty()) {
            if (rvNotifications != null) rvNotifications.setVisibility(View.GONE);
            if (layoutNotifEmptyState != null) layoutNotifEmptyState.setVisibility(View.VISIBLE);

            if (lastRequestFailed) {
                if (tvEmptyStateTitle != null) tvEmptyStateTitle.setText("Unable to Load Notifications");
                if (tvEmptyStateDesc != null) tvEmptyStateDesc.setText("Please check your network connection and try again.");
                if (btnResetNotifSearch != null) btnResetNotifSearch.setVisibility(View.GONE);
            } else if (!currentSearchQuery.isEmpty() || !"ALL".equalsIgnoreCase(currentCategoryFilter)) {
                if (tvEmptyStateTitle != null) tvEmptyStateTitle.setText("No Matching Notifications");
                if (tvEmptyStateDesc != null) tvEmptyStateDesc.setText("No notifications found matching your search or filter criteria.");
                if (btnResetNotifSearch != null) btnResetNotifSearch.setVisibility(View.VISIBLE);
            } else {
                if (tvEmptyStateTitle != null) tvEmptyStateTitle.setText(R.string.no_notifications);
                if (tvEmptyStateDesc != null) tvEmptyStateDesc.setText("You're all caught up! There are no notifications at the moment.");
                if (btnResetNotifSearch != null) btnResetNotifSearch.setVisibility(View.GONE);
            }
        } else {
            if (rvNotifications != null) rvNotifications.setVisibility(View.VISIBLE);
            if (layoutNotifEmptyState != null) layoutNotifEmptyState.setVisibility(View.GONE);

            adapter = new NotificationAdapter(getContext(), displayedNotificationList, notification -> {
                if (!notification.isRead() && notification.getId() != null && !notification.getId().isEmpty()) {
                    notification.setRead(true);
                    filterAndRefreshNotifications();
                    ApiClient.getApiService(requireContext()).markNotificationRead(notification.getId())
                            .enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {}
                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                            });
                }

                if (!isAdded() || getContext() == null) return;
                Intent intent = new Intent(getContext(), NotificationDetailActivity.class);
                intent.putExtra("notification", notification);
                detailLauncher.launch(intent);
            });

            if (rvNotifications != null) {
                rvNotifications.setAdapter(adapter);
            }
        }
    }
}
