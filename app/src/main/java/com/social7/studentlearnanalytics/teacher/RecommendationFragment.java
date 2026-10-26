package com.social7.studentlearnanalytics.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

public class RecommendationFragment extends Fragment {

    public RecommendationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_recommendations,
                container,
                false
        );

        ImageView btnMenu = view.findViewById(R.id.btnTeacherMenuRec);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                }
            });
        }

        MaterialButton btnGenerate = view.findViewById(R.id.btnGenerateRecs);
        if (btnGenerate != null) {
            btnGenerate.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Generated latest AI recommendations for student!", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }
}
