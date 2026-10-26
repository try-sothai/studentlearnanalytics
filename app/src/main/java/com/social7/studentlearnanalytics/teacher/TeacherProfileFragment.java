package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class TeacherProfileFragment extends Fragment {

    private SessionManager sessionManager;

    public TeacherProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_profile,
                container,
                false
        );

        sessionManager = new SessionManager(requireContext());

        ImageView btnMenu = view.findViewById(R.id.btnTeacherMenuProfile);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                }
            });
        }

        View btnOptChangePassword = view.findViewById(R.id.btnOptChangePassword);
        if (btnOptChangePassword != null) {
            btnOptChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        View btnOptPersonalInfo = view.findViewById(R.id.btnOptPersonalInfo);
        if (btnOptPersonalInfo != null) {
            btnOptPersonalInfo.setOnClickListener(v -> Toast.makeText(requireContext(), "Mr. Chan - Computer Science Department", Toast.LENGTH_SHORT).show());
        }

        Button btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        return view;
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    if (getActivity() instanceof TeacherMainActivity) {
                        ((TeacherMainActivity) getActivity()).logout();
                    } else {
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
