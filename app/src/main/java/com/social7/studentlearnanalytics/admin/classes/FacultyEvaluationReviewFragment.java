package com.social7.studentlearnanalytics.admin.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.admin.UserManagementFragment;

public class FacultyEvaluationReviewFragment extends Fragment {

    public FacultyEvaluationReviewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View placeholder = new View(inflater.getContext());
        placeholder.post(() -> {
            if (isAdded() && getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).openFragment(UserManagementFragment.newInstance(3));
            }
        });
        return placeholder;
    }
}
