package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;

public class StudentSettingsFragment extends Fragment {

    public StudentSettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_settings, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.nav_settings), "Manage your account and application preferences"
            );
        }

        View cardPassword = view.findViewById(R.id.cardStudentChangePassword);
        if (cardPassword != null) {
            cardPassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        View cardNotif = view.findViewById(R.id.cardStudentNotifPref);
        if (cardNotif != null) {
            cardNotif.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.notification_preferences)
                        .setMessage("All notification alerts (Assignments, Quizzes, Grades, and System Announcements) are active for your student account.")
                        .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        View cardAppearance = view.findViewById(R.id.cardStudentAppearance);
        if (cardAppearance != null) {
            cardAppearance.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), com.social7.studentlearnanalytics.admin.AdminAppearanceActivity.class);
                startActivity(intent);
            });
        }

        View cardAbout = view.findViewById(R.id.cardStudentAbout);
        if (cardAbout != null) {
            cardAbout.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("About SLAS Student Portal")
                        .setMessage("Student Learning Analytics System (SLAS)\n" +
                                "Version: 2.4.0\n" +
                                "Build: Production\n\n" +
                                "Provides students with learning analytics, performance predictions, personalized recommendations, and course management.")
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        return view;
    }
}
