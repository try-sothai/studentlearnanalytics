package com.social7.studentlearnanalytics.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

        ImageView btnMenu = view.findViewById(R.id.btnTeacherMenuNotif);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                }
            });
        }

        RecyclerView rvNotif = view.findViewById(R.id.rvTeacherNotifications);
        if (rvNotif != null) {
            rvNotif.setLayoutManager(new LinearLayoutManager(requireContext()));
            List<Notification> list = getSampleNotifications();
            NotificationAdapter adapter = new NotificationAdapter(requireContext(), list, notification -> {
                notification.setRead(true);
            });
            rvNotif.setAdapter(adapter);
        }

        return view;
    }

    private List<Notification> getSampleNotifications() {
        List<Notification> list = new ArrayList<>();
        list.add(new Notification("1", "New Student Enrollment", "Sophea has been enrolled in CS101 Data Structures.", "2 hours ago", "General", false));
        list.add(new Notification("2", "At-Risk Student Alert", "Dara's risk level is now High. Please review performance.", "4 hours ago", "Risk", false));
        list.add(new Notification("3", "Scores Updated", "Quiz scores have been updated for CS101 class.", "1 day ago", "Grade", true));
        list.add(new Notification("4", "New Recommendation", "New recommendations generated for 18 at-risk students.", "1 day ago", "Recommendation", true));
        return list;
    }
}
