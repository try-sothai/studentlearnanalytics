package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.NotificationAdapter;
import com.social7.studentlearnanalytics.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private TextView tabAll, tabUnread, tabImportant;
    private RecyclerView rvTeacherNotifications;
    private NotificationAdapter adapter;
    private List<Notification> allNotifications = new ArrayList<>();
    private List<Notification> displayedNotifications = new ArrayList<>();
    private String currentFilter = "ALL"; // ALL, UNREAD, IMPORTANT

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
            btnMarkAllRead.setOnClickListener(v -> {
                for (Notification n : allNotifications) {
                    n.setRead(true);
                }
                filterNotifications(currentFilter);
                Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
            });
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
            loadSampleNotifications();
            adapter = new NotificationAdapter(requireContext(), displayedNotifications, notification -> {
                notification.setRead(!notification.isRead());
                filterNotifications(currentFilter);
                Toast.makeText(requireContext(), notification.isRead() ? "Marked as read" : "Marked as unread", Toast.LENGTH_SHORT).show();
            });
            rvTeacherNotifications.setAdapter(adapter);
        }

        return view;
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

    private void loadSampleNotifications() {
        allNotifications.clear();
        // Assignments
        allNotifications.add(new Notification("1", "Student Assignment Submitted", "Sophea submitted Assignment 1 for Data Structures (IT-4A).", "10 mins ago", "Course", "TODAY", false, true));
        allNotifications.add(new Notification("2", "Assignment Deadline Approaching", "Assignment 2 for Network Administration closes in 24 hours.", "2 hours ago", "Course", "TODAY", false, false));
        // Quizzes
        allNotifications.add(new Notification("3", "Quiz Results Ready", "Quiz 1 evaluation completed for CS101. Average score: 84.5%.", "Yesterday", "Report", "YESTERDAY", true, true));
        // Homework
        allNotifications.add(new Notification("4", "New Homework Submission", "Dara submitted Homework 3 for Database Systems.", "Yesterday", "Course", "YESTERDAY", false, false));
        // Classes & Students
        allNotifications.add(new Notification("5", "At-Risk Student Alert", "Student Dara has dropped below 70% in performance and requires intervention.", "2 days ago", "Security", "EARLIER", false, true));
        // Academic Schedule
        allNotifications.add(new Notification("6", "Upcoming Class Schedule", "Lecture for IT-4A starts in 30 minutes in Room B-204.", "2 days ago", "System", "EARLIER", true, false));
        // System
        allNotifications.add(new Notification("7", "System Maintenance Notice", "Scheduled server maintenance tonight at 12:00 AM.", "3 days ago", "System", "EARLIER", true, false));

        filterNotifications(currentFilter);
    }
}
