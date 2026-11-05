package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TeacherManagementFragment extends Fragment {

    public TeacherManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        if (getActivity() instanceof AdminMainActivity) {
            ((AdminMainActivity) getActivity()).openFragment(UserManagementFragment.newInstance(1));
        }
        return new View(requireContext());
    }
}
