package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.users.AccountSecurityActivity;
import com.social7.studentlearnanalytics.admin.users.NotificationSettingsActivity;
import com.social7.studentlearnanalytics.admin.users.ProfileSettingsActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class SystemSettingsFragment extends Fragment {

    private SessionManager sessionManager;

    public SystemSettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_system_settings,
                container,
                false
        );

        sessionManager = new SessionManager(requireContext());

        setupClickListeners(view);

        return view;
    }

    private void setupClickListeners(View view) {
        View btnMenu = view.findViewById(R.id.btnSettingsMenu);
        View cardProfile = view.findViewById(R.id.cardProfileSettings);
        View cardSecurity = view.findViewById(R.id.cardSecuritySettings);
        View cardNotification = view.findViewById(R.id.cardNotificationSettings);
        View cardAppearance = view.findViewById(R.id.cardAppearanceSettings);
        View cardConfig = view.findViewById(R.id.cardSystemConfig);
        View cardUserRoles = view.findViewById(R.id.cardUserRoles);
        View cardBackup = view.findViewById(R.id.cardDatabaseBackup);
        View cardAbout = view.findViewById(R.id.cardAboutSystem);
        View cardLogout = view.findViewById(R.id.cardLogout);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileSettingsActivity.class);
                startActivity(intent);
            });
        }

        if (cardSecurity != null) {
            cardSecurity.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AccountSecurityActivity.class);
                startActivity(intent);
            });
        }

        if (cardNotification != null) {
            cardNotification.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), NotificationSettingsActivity.class);
                startActivity(intent);
            });
        }

        if (cardAppearance != null) {
            cardAppearance.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Appearance settings: Theme, Language, Display Options", Toast.LENGTH_SHORT).show()
            );
        }

        if (cardConfig != null) {
            cardConfig.setOnClickListener(v ->
                    Toast.makeText(getContext(), "System Configuration & Preferences", Toast.LENGTH_SHORT).show()
            );
        }

        if (cardUserRoles != null) {
            cardUserRoles.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new UserManagementFragment());
                }
            });
        }

        if (cardBackup != null) {
            cardBackup.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Database Backup & Data Options", Toast.LENGTH_SHORT).show()
            );
        }

        if (cardAbout != null) {
            cardAbout.setOnClickListener(v ->
                    Toast.makeText(getContext(), "SLAS Admin Panel v1.0.0 — Student Learning Analytics System", Toast.LENGTH_LONG).show()
            );
        }

        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from the admin panel?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
