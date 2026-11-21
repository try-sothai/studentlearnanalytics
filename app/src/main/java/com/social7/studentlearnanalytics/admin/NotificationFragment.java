package com.social7.studentlearnanalytics.admin;

import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.NotificationAdapter;
import com.social7.studentlearnanalytics.admin.users.NotificationSettingsActivity;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

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
        setupData();
        setupFilterChips();
        setupSearchAndFilter(view);
        setupOverflowMenu(view);

        return view;
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

    private void setupData() {
        masterNotificationList.clear();

        // TODAY Group
        masterNotificationList.add(new Notification("N001", "New Teacher Registration", "Ms. Dany has registered as a new teacher and is pending approval.", "10:30 AM", "User", "TODAY", false));
        masterNotificationList.add(new Notification("N002", "New Course Created", "CS102 - Data Structures has been created by Mr. Vanna.", "09:15 AM", "Course", "TODAY", false));
        masterNotificationList.add(new Notification("N003", "Report Ready", "Monthly academic report for Semester 1 is ready to view.", "08:45 AM", "Report", "TODAY", false));

        // YESTERDAY Group
        masterNotificationList.add(new Notification("N004", "System Announcement", "The academic calendar for 2024 - 2025 has been published.", "04:00 PM", "System", "YESTERDAY", true));
        masterNotificationList.add(new Notification("N005", "Suspicious Login Attempt", "A suspicious login attempt was detected from a new device.", "02:15 PM", "Security", "YESTERDAY", true));
        masterNotificationList.add(new Notification("N006", "New Student Registration", "New students have registered and are pending approval.", "11:00 AM", "User", "YESTERDAY", true));

        // THIS WEEK Group
        masterNotificationList.add(new Notification("N007", "Class Enrollment Update", "CS101 has 5 new enrollments.", "Oct 25", "Academic", "THIS WEEK", true));

        filterAndRefreshNotifications();
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
        new MaterialAlertDialogBuilder(getContext())
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
        PopupMenu popup = new PopupMenu(getContext(), anchor);
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
                Intent intent = new Intent(getContext(), NotificationSettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void markAllAsRead() {
        for (Notification n : masterNotificationList) {
            n.setRead(true);
        }
        filterAndRefreshNotifications();
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteReadConfirmationDialog() {
        if (!isAdded() || getContext() == null) return;
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.delete_read_notifications)
                .setMessage(R.string.are_you_sure_you_want_to_delete_all_read_n)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    List<Notification> toRemove = new ArrayList<>();
                    for (Notification n : masterNotificationList) {
                        if (n.isRead()) {
                            toRemove.add(n);
                        }
                    }
                    masterNotificationList.removeAll(toRemove);
                    filterAndRefreshNotifications();
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Read notifications deleted", Toast.LENGTH_SHORT).show();
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
        displayedNotificationList.clear();

        for (Notification n : masterNotificationList) {
            boolean matchesCategory = true;

            if ("UNREAD".equalsIgnoreCase(currentCategoryFilter)) {
                matchesCategory = !n.isRead();
            } else if (!"ALL".equalsIgnoreCase(currentCategoryFilter)) {
                matchesCategory = currentCategoryFilter.equalsIgnoreCase(n.getCategory());
            }

            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                String q = currentSearchQuery.toLowerCase();
                boolean inTitle = n.getTitle() != null && n.getTitle().toLowerCase().contains(q);
                boolean inMsg = n.getMessage() != null && n.getMessage().toLowerCase().contains(q);
                boolean inCat = n.getCategory() != null && n.getCategory().toLowerCase().contains(q);
                matchesSearch = inTitle || inMsg || inCat;
            }

            if (matchesCategory && matchesSearch) {
                displayedNotificationList.add(n);
            }
        }

        // Update Unread Badge Count
        int unreadCount = getUnreadCount();
        if (tvNotifBadgeCount != null) {
            tvNotifBadgeCount.setText(String.valueOf(unreadCount));
            tvNotifBadgeCount.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
        }

        // Update Category Chip Counts
        if (pillNotifAll != null) pillNotifAll.setText(pillNotifAll.getContext().getString(R.string.all_c49d0d) + masterNotificationList.size() + ")");
        if (pillNotifUnread != null) pillNotifUnread.setText("Unread (" + unreadCount + ")");

        // Refresh List & Empty State
        if (displayedNotificationList.isEmpty()) {
            if (rvNotifications != null) rvNotifications.setVisibility(View.GONE);
            if (layoutNotifEmptyState != null) layoutNotifEmptyState.setVisibility(View.VISIBLE);

            if (!currentSearchQuery.isEmpty() || !"ALL".equalsIgnoreCase(currentCategoryFilter)) {
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

            if (!isAdded() || getContext() == null) return;
            adapter = new NotificationAdapter(getContext(), displayedNotificationList, notification -> {
                notification.setRead(true);
                filterAndRefreshNotifications();

                if (!isAdded() || getContext() == null) return;
                Intent intent = new Intent(getContext(), NotificationDetailActivity.class);
                intent.putExtra("notification", notification);
                startActivity(intent);
            });

            if (rvNotifications != null) {
                rvNotifications.setAdapter(adapter);
            }
        }
    }
}
