package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

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
                    view.findViewById(R.id.headerTeacherProfileOverview),
                    "Teacher Profile",
                    ""
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
        String name = sessionManager != null && sessionManager.getUserName() != null && !sessionManager.getUserName().trim().isEmpty() ? sessionManager.getUserName() : "Mr. Chan";
        String email = name.toLowerCase().replaceAll("[^a-z]", "") + "@univ.edu";
        if (email.startsWith("@")) email = "chan@univ.edu";

        String initial = !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "T";

        if (tvOverviewAvatar != null) tvOverviewAvatar.setText(initial);
        if (tvOverviewFullName != null) tvOverviewFullName.setText(name);
        if (tvOverviewEmail != null) tvOverviewEmail.setText(email);
        if (tvOverviewPhone != null) tvOverviewPhone.setText("+855 12 345 678");
        if (tvOverviewGender != null) tvOverviewGender.setText("Male");
        if (tvOverviewDob != null) tvOverviewDob.setText("Mar 10, 1990");

        if (tvOverviewTeacherId != null) tvOverviewTeacherId.setText("ID: TCH-001");
        if (tvOverviewDepartment != null) tvOverviewDepartment.setText("College of Computer Studies");
        if (tvOverviewQualifications != null) tvOverviewQualifications.setText("M.Sc. Computer Science & Software Engineering");
        if (tvOverviewPositionDept != null) tvOverviewPositionDept.setText("Senior Lecturer • Computer Science Dept");
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
}
