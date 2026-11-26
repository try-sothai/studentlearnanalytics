package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.users.ProfileSettingsActivity;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.utils.UiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvHeaderName, tvHeaderEmail, tvHeaderBadge;
    private TextView tvMetricLabel1, tvMetricValue1, tvAccountStatus;
    private TextView tvFullName, tvEmail, tvRole, tvLastUpdated;

    public AdminProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_admin_profile,
                container,
                false
        );

        sessionManager = new SessionManager(requireContext());

        tvHeaderName = view.findViewById(R.id.tvAdminProfileHeaderName);
        tvHeaderEmail = view.findViewById(R.id.tvAdminProfileHeaderEmail);
        tvHeaderBadge = view.findViewById(R.id.tvAdminProfileHeaderBadge);
        tvMetricLabel1 = view.findViewById(R.id.tvAdminProfileMetricLabel1);
        tvMetricValue1 = view.findViewById(R.id.tvAdminProfileMetricValue1);
        tvAccountStatus = view.findViewById(R.id.tvAdminProfileAccountStatus);
        tvFullName = view.findViewById(R.id.tvAdminProfileFullName);
        tvEmail = view.findViewById(R.id.tvAdminProfileEmail);
        tvRole = view.findViewById(R.id.tvAdminProfileRole);
        tvLastUpdated = view.findViewById(R.id.tvAdminProfileLastUpdated);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnChangePassword = view.findViewById(R.id.btnProfileChangePassword);
        Button btnLogout = view.findViewById(R.id.btnProfileLogout);

        bindFromSession();
        loadProfileAndStats();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileSettingsActivity.class);
                startActivity(intent);
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && getContext() != null) {
            loadProfileAndStats();
        }
    }

    private void bindFromSession() {
        if (sessionManager == null) return;
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole();
        if (name == null || name.trim().isEmpty()) name = getString(R.string.system_administrator);
        if (email == null || email.trim().isEmpty()) email = "—";
        if (role == null || role.trim().isEmpty()) role = getString(R.string.administrator);

        if (tvHeaderName != null) tvHeaderName.setText(name);
        if (tvHeaderEmail != null) tvHeaderEmail.setText(email);
        if (tvHeaderBadge != null) tvHeaderBadge.setText(role);
        if (tvFullName != null) tvFullName.setText(name);
        if (tvEmail != null) tvEmail.setText(email);
        if (tvRole != null) tvRole.setText(role);
        if (tvMetricLabel1 != null) tvMetricLabel1.setText("Total Users");
    }

    private void loadProfileAndStats() {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(requireContext()).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject user = root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data") : root;
                    if (user.has("user") && user.get("user").isJsonObject()) {
                        user = user.getAsJsonObject("user");
                    }
                    String name = UiUtils.optString(user, "name", sessionManager.getUserName());
                    String email = UiUtils.optString(user, "email", sessionManager.getUserEmail());
                    String role = UiUtils.optString(user, "role", getString(R.string.administrator));
                    String status = UiUtils.optString(user, "status", "Active");
                    String updatedAt = UiUtils.optString(user, "updated_at", UiUtils.optString(user, "created_at", "—"));
                    if (updatedAt.length() > 16) updatedAt = updatedAt.substring(0, 16).replace('T', ' ');

                    if (!name.isEmpty()) {
                        sessionManager.updateUserProfile(name, email);
                    }
                    if (tvHeaderName != null) tvHeaderName.setText(name.isEmpty() ? getString(R.string.system_administrator) : name);
                    if (tvHeaderEmail != null) tvHeaderEmail.setText(email.isEmpty() ? "—" : email);
                    if (tvHeaderBadge != null) tvHeaderBadge.setText(role.isEmpty() ? getString(R.string.system_administrator) : role);
                    if (tvFullName != null) tvFullName.setText(name.isEmpty() ? getString(R.string.system_administrator) : name);
                    if (tvEmail != null) tvEmail.setText(email.isEmpty() ? "—" : email);
                    if (tvRole != null) tvRole.setText(role.isEmpty() ? getString(R.string.administrator) : role);
                    if (tvAccountStatus != null) {
                        tvAccountStatus.setText("inactive".equalsIgnoreCase(status) ? getString(R.string.status_inactive) : getString(R.string.status_active));
                    }
                    if (tvLastUpdated != null) tvLastUpdated.setText(updatedAt);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });

        ApiClient.getApiService(requireContext()).getAdminStatistics().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    int userCount = UiUtils.optInt(data, "user_count", 0);
                    if (tvMetricValue1 != null) tvMetricValue1.setText(String.valueOf(userCount));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage("Are you sure you want to logout from the admin panel?")
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    if (getContext() != null) {
                        ApiClient.getApiService(requireContext()).logout().enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {}
                            @Override
                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                        });
                    }
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
