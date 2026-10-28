package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private static final String TAG = "SLAS_NOTIF";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.activity_fragment_notifications,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof StudentMainActivity) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_home);
                    }
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        View icon1 = view.findViewById(R.id.icon1);
        if (icon1 != null && icon1.getParent() != null && icon1.getParent().getParent() != null) {
            View card = (View) icon1.getParent().getParent();
            card.setOnClickListener(v -> startActivity(new Intent(requireContext(), RiskAnalysisActivity.class)));
        }

        View icon2 = view.findViewById(R.id.icon2);
        if (icon2 != null && icon2.getParent() != null && icon2.getParent().getParent() != null) {
            View card = (View) icon2.getParent().getParent();
            card.setOnClickListener(v -> startActivity(new Intent(requireContext(), RecommendationDetailActivity.class)));
        }

        View icon3 = view.findViewById(R.id.icon3);
        if (icon3 != null && icon3.getParent() != null && icon3.getParent().getParent() != null) {
            View card = (View) icon3.getParent().getParent();
            card.setOnClickListener(v -> startActivity(new Intent(requireContext(), QuizDetailActivity.class)));
        }

        View icon4 = view.findViewById(R.id.icon4);
        if (icon4 != null && icon4.getParent() != null && icon4.getParent().getParent() != null) {
            View card = (View) icon4.getParent().getParent();
            card.setOnClickListener(v -> startActivity(new Intent(requireContext(), ExamDetailActivity.class)));
        }

        View icon5 = view.findViewById(R.id.icon5);
        if (icon5 != null && icon5.getParent() != null && icon5.getParent().getParent() != null) {
            View card = (View) icon5.getParent().getParent();
            card.setOnClickListener(v -> startActivity(new Intent(requireContext(), LearningReportActivity.class)));
        }

        fetchNotifications();
    }

    private void fetchNotifications() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getNotifications().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        com.google.gson.JsonArray array = null;
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            array = body.getAsJsonArray("data");
                        } else if (body.has("notifications") && body.get("notifications").isJsonArray()) {
                            array = body.getAsJsonArray("notifications");
                        }

                        if (array != null && array.size() > 0) {
                            Log.d(TAG, "Loaded " + array.size() + " notifications from API");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing notifications", e);
                    }
                } else {
                    Log.d(TAG, "Notifications HTTP code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d(TAG, "Notifications API call failed: " + t.getMessage());
            }
        });
    }
}
