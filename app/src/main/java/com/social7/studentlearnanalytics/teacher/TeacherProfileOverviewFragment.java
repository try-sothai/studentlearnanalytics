package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherProfileOverviewFragment extends Fragment {

    private TextView tvOverviewAvatar, tvOverviewFullName, tvOverviewEmail, tvOverviewPhone, tvOverviewGender, tvOverviewDob;
    private TextView tvOverviewTeacherId, tvOverviewDepartment, tvOverviewQualifications, tvOverviewPositionDept;
    private MaterialButton btnEditProfile, btnChangePassword;
    private SessionManager sessionManager;

    public TeacherProfileOverviewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_teacher_profile_overview, container, false);

        if (getContext() != null) {
            sessionManager = new SessionManager(requireContext());
        }

        initViews(view);
        loadProfileData();
        setupListeners(view);

        return view;
    }

    private void initViews(View view) {
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherProfileOverview), getString(R.string.teacher_profile), ""
            );
        }

        tvOverviewAvatar = view.findViewById(R.id.tvOverviewAvatar);
        tvOverviewFullName = view.findViewById(R.id.tvOverviewFullName);
        tvOverviewEmail = view.findViewById(R.id.tvOverviewEmail);
        tvOverviewPhone = view.findViewById(R.id.tvOverviewPhone);
        tvOverviewGender = view.findViewById(R.id.tvOverviewGender);
        tvOverviewDob = view.findViewById(R.id.tvOverviewDob);

        tvOverviewTeacherId = view.findViewById(R.id.tvOverviewTeacherId);
        tvOverviewDepartment = view.findViewById(R.id.tvOverviewDepartment);
        tvOverviewQualifications = view.findViewById(R.id.tvOverviewQualifications);
        tvOverviewPositionDept = view.findViewById(R.id.tvOverviewPositionDept);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
    }

    private void loadProfileData() {
        String name = sessionManager != null && sessionManager.getUserName() != null ? sessionManager.getUserName() : "";
        if (!name.isEmpty()) {
            if (tvOverviewAvatar != null) tvOverviewAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase(Locale.ROOT));
            if (tvOverviewFullName != null) tvOverviewFullName.setText(name);
        }

        if (getContext() == null) return;
        ApiClient.getApiService(requireContext()).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject userObj = root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data") : root;
                    if (userObj.has("user") && userObj.get("user").isJsonObject()) {
                        userObj = userObj.getAsJsonObject("user");
                    }

                    String fullName = getStr(userObj, "name", name);
                    String email = getStr(userObj, "email", "-");
                    String phone = getStr(userObj, "phone", "-");

                    JsonObject teacherObj = userObj.has("teacher") && userObj.get("teacher").isJsonObject()
                            ? userObj.getAsJsonObject("teacher") : null;

                    String tCode = teacherObj != null ? getStr(teacherObj, "teacher_code", "-") : "-";
                    String gender = teacherObj != null ? getStr(teacherObj, "gender", "-") : "-";
                    String dob = teacherObj != null ? getStr(teacherObj, "date_of_birth", "-") : "-";
                    String spec = teacherObj != null ? getStr(teacherObj, "specialization", "-") : "-";
                    String pos = teacherObj != null ? getStr(teacherObj, "position", "Lecturer") : "Lecturer";
                    if (teacherObj != null && phone.equals("-")) {
                        phone = getStr(teacherObj, "phone", "-");
                    }

                    String dept = "Academic Faculty";
                    if (teacherObj != null && teacherObj.has("department") && teacherObj.get("department").isJsonObject()) {
                        dept = getStr(teacherObj.getAsJsonObject("department"), "name", dept);
                    }

                    if (!fullName.isEmpty() && tvOverviewAvatar != null) {
                        tvOverviewAvatar.setText(String.valueOf(fullName.charAt(0)).toUpperCase(Locale.ROOT));
                    }
                    if (tvOverviewFullName != null) tvOverviewFullName.setText(fullName);
                    if (tvOverviewEmail != null) tvOverviewEmail.setText(email);
                    if (tvOverviewPhone != null) tvOverviewPhone.setText(phone);
                    if (tvOverviewGender != null) tvOverviewGender.setText(gender);
                    if (tvOverviewDob != null) tvOverviewDob.setText(dob);
                    if (tvOverviewTeacherId != null) tvOverviewTeacherId.setText("ID: " + tCode);
                    if (tvOverviewDepartment != null) tvOverviewDepartment.setText(dept);
                    if (tvOverviewQualifications != null) tvOverviewQualifications.setText(spec);
                    if (tvOverviewPositionDept != null) tvOverviewPositionDept.setText(pos + " • " + dept);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners(View view) {
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new TeacherProfileFragment());
                }
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            });
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
