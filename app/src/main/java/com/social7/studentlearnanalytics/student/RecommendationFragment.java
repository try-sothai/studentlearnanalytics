package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationFragment extends Fragment {

    private static final String TAG = "SLAS_RECOMMENDATION";
    private SessionManager sessionManager;

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

        sessionManager = new SessionManager(requireContext());

        setupListeners(view);
        fetchRecommendations();

        return view;
    }

    private void setupListeners(View view) {
        View btnMenu = view.findViewById(R.id.btnTeacherMenuRec);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        View btnGenerateRecs = view.findViewById(R.id.btnGenerateRecs);
        if (btnGenerateRecs != null) {
            btnGenerateRecs.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Generating new AI recommendations...", Toast.LENGTH_SHORT).show()
            );
        }

        // Setup card click listeners
        View parentLayout = view.findViewById(R.id.btnGenerateRecs) != null ? 
                (View) view.findViewById(R.id.btnGenerateRecs).getParent() : null;

        if (parentLayout instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parentLayout;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof com.google.android.material.card.MaterialCardView) {
                    child.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), RecommendationDetailActivity.class);
                        startActivity(intent);
                    });
                }
            }
        }
    }

    private void fetchRecommendations() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getRecommendations().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        com.google.gson.JsonArray dataArray = null;
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            dataArray = body.getAsJsonArray("data");
                        } else if (body.has("recommendations") && body.get("recommendations").isJsonArray()) {
                            dataArray = body.getAsJsonArray("recommendations");
                        }

                        if (dataArray != null && !dataArray.isEmpty()) {
                            Log.d(TAG, "Loaded " + dataArray.size() + " recommendations from API");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recommendations", e);
                    }
                } else {
                    Log.d(TAG, "Recommendations HTTP code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d(TAG, "Recommendations API failed: " + t.getMessage());
            }
        });
    }
}
