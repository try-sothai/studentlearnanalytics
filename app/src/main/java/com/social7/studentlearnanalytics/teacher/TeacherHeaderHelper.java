package com.social7.studentlearnanalytics.teacher;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.storage.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherHeaderHelper {

    public static void setupHeader(Activity activity, View root, String title, String subtitle, boolean isDetailScreen) {
        if (root == null || activity == null) return;

        SessionManager sessionManager = new SessionManager(activity);

        ImageView btnMenu = root.findViewById(R.id.btnTeacherMenu);
        if (btnMenu != null) {
            if (isDetailScreen) {
                btnMenu.setImageResource(R.drawable.ic_back);
                btnMenu.setOnClickListener(v -> activity.onBackPressed());
            } else {
                btnMenu.setOnClickListener(v -> {
                    if (activity instanceof TeacherMainActivity) {
                        ((TeacherMainActivity) activity).openDrawer();
                    } else {
                        activity.onBackPressed();
                    }
                });
            }
        }

        TextView tvTitle = root.findViewById(R.id.tvHeaderTitle);
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }

        TextView tvSubtitle = root.findViewById(R.id.tvHeaderSubtitle);
        if (tvSubtitle != null && subtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        } else if (tvSubtitle != null) {
            tvSubtitle.setVisibility(View.GONE);
        }

        View notifBtn = root.findViewById(R.id.layoutTeacherNotifBtn);
        if (notifBtn != null) {
            notifBtn.setOnClickListener(v -> {
                if (activity instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) activity).openFragment(new NotificationFragment());
                } else {
                    // In a detail activity, we could start the TeacherMainActivity with a specific extra,
                    // but for simplicity, we'll just finish and let the user navigate from Main.
                    // Or launch TeacherMainActivity with intent flag.
                    Intent intent = new Intent(activity, TeacherMainActivity.class);
                    intent.putExtra("navigate_to", "notifications");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        View avatarBtn = root.findViewById(R.id.layoutTeacherAvatarBtn);
        if (avatarBtn != null) {
            avatarBtn.setOnClickListener(v -> {
                if (activity instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) activity).openFragment(new TeacherProfileOverviewFragment());
                } else {
                    Intent intent = new Intent(activity, TeacherMainActivity.class);
                    intent.putExtra("navigate_to", "profile");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        fetchUnreadNotificationCount(activity, root, sessionManager);
    }

    private static void fetchUnreadNotificationCount(Activity activity, View headerRoot, SessionManager sessionManager) {
        TextView tvBadge = headerRoot.findViewById(R.id.tvTeacherNotifBadge);
        if (tvBadge == null) return;

        ApiService apiService = ApiClient.getApiService(activity);
        String token = "Bearer " + sessionManager.getToken();

        apiService.getUnreadNotificationsCount(token).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        int count = response.body().get("data").getAsJsonObject().get("unread_count").getAsInt();
                        if (count > 0) {
                            tvBadge.setText(String.valueOf(count));
                            tvBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvBadge.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        tvBadge.setVisibility(View.GONE);
                    }
                } else {
                    tvBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                tvBadge.setVisibility(View.GONE);
            }
        });
    }
}
