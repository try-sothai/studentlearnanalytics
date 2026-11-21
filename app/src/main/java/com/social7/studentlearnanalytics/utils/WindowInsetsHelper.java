package com.social7.studentlearnanalytics.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;

/**
 * Centralized WindowInsets handler for Android 10-17 (including targetSdk 35+ edge-to-edge).
 * Attaches directly to android.R.id.content so DrawerLayout and standard Activities both
 * reliably receive status bar, navigation bar, and IME insets without double-padding.
 */
public final class WindowInsetsHelper {

    private WindowInsetsHelper() {
    }

    public static void applyToActivity(@Nullable Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        if (window == null) return;

        View decorView = window.getDecorView();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        final ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;
        final View root = content.getChildAt(0);
        if (root == null) return;

        if (Boolean.TRUE.equals(content.getTag(R.id.tag_slas_insets_applied))) {
            return;
        }
        content.setTag(R.id.tag_slas_insets_applied, Boolean.TRUE);

        // Paint the status bar inset strip with SLAS primary purple/indigo behind the status bar icons.
        content.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary));
        root.setFitsSystemWindows(false);

        final int cLeft = content.getPaddingLeft();
        final int cTop = content.getPaddingTop();
        final int cRight = content.getPaddingRight();
        final int cBottom = content.getPaddingBottom();

        if (root instanceof DrawerLayout) {
            final DrawerLayout drawer = (DrawerLayout) root;
            final View mainContent = drawer.getChildCount() > 0 ? drawer.getChildAt(0) : null;
            final View drawerPanel = drawer.getChildCount() > 1 ? drawer.getChildAt(1) : null;
            if (mainContent != null) mainContent.setFitsSystemWindows(false);
            if (drawerPanel != null) drawerPanel.setFitsSystemWindows(false);

            final BottomNavigationView bottomNav = mainContent != null ? findBottomNav(mainContent) : null;
            final int navBottom = bottomNav != null ? bottomNav.getPaddingBottom() : 0;
            final int dTop = drawerPanel != null ? drawerPanel.getPaddingTop() : 0;
            final int dBottom = drawerPanel != null ? drawerPanel.getPaddingBottom() : 0;
            final int mBottom = mainContent != null ? mainContent.getPaddingBottom() : 0;

            ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                int extraIme = Math.max(0, ime.bottom - bars.bottom);

                // Push DrawerLayout below the status bar so content never overlaps system icons,
                // while keeping BottomNavigationView flush at the bottom with internal nav-bar padding.
                v.setPadding(cLeft + bars.left, cTop + bars.top, cRight + bars.right, cBottom);

                if (bottomNav != null) {
                    bottomNav.setPadding(
                            bottomNav.getPaddingLeft(),
                            bottomNav.getPaddingTop(),
                            bottomNav.getPaddingRight(),
                            navBottom + bars.bottom
                    );
                }
                if (mainContent != null) {
                    mainContent.setPadding(
                            mainContent.getPaddingLeft(),
                            mainContent.getPaddingTop(),
                            mainContent.getPaddingRight(),
                            mBottom + extraIme
                    );
                }
                if (drawerPanel != null) {
                    drawerPanel.setPadding(
                            drawerPanel.getPaddingLeft(),
                            dTop,
                            drawerPanel.getPaddingRight(),
                            dBottom + bars.bottom
                    );
                }
                return WindowInsetsCompat.CONSUMED;
            });
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottomInset = Math.max(bars.bottom, ime.bottom);
                v.setPadding(cLeft + bars.left, cTop + bars.top, cRight + bars.right, cBottom + bottomInset);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        ViewCompat.requestApplyInsets(content);
    }

    @Nullable
    private static BottomNavigationView findBottomNav(@NonNull View view) {
        if (view instanceof BottomNavigationView) {
            return (BottomNavigationView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                BottomNavigationView found = findBottomNav(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
