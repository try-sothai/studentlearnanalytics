package com.social7.studentlearnanalytics.utils;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
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
 * Confines the Status Bar background color strictly to the system Status Bar top inset rect
 * while applying clean insets to android.R.id.content without duplicate top padding.
 */
public final class WindowInsetsHelper {

    private WindowInsetsHelper() {
    }

    @SuppressWarnings("deprecation")
    public static void applyToActivity(@Nullable Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        if (window == null) return;

        WindowCompat.setDecorFitsSystemWindows(window, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        View decorView = window.getDecorView();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
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

        final StatusBarInsetDrawable bgDrawable = new StatusBarInsetDrawable(
                ContextCompat.getColor(activity, R.color.primary),
                ContextCompat.getColor(activity, R.color.app_background),
                ContextCompat.getColor(activity, R.color.card_white)
        );
        content.setBackground(bgDrawable);
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
                Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

                int statusTop = Math.max(0, statusBars.top);
                int navBottomInset = Math.max(0, navBars.bottom);
                int extraIme = Math.max(0, ime.bottom - navBottomInset);

                bgDrawable.setInsets(statusTop, 0);
                v.setPadding(cLeft + systemBars.left, cTop + statusTop, cRight + systemBars.right, cBottom);

                if (bottomNav != null) {
                    bottomNav.setPadding(
                            bottomNav.getPaddingLeft(),
                            bottomNav.getPaddingTop(),
                            bottomNav.getPaddingRight(),
                            navBottom + navBottomInset
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
                            dBottom + navBottomInset
                    );
                }
                return WindowInsetsCompat.CONSUMED;
            });
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
                Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

                int statusTop = Math.max(0, statusBars.top);
                int bottomInset = Math.max(0, Math.max(navBars.bottom, ime.bottom));

                bgDrawable.setInsets(statusTop, bottomInset);
                v.setPadding(
                        cLeft + systemBars.left,
                        cTop + statusTop,
                        cRight + systemBars.right,
                        cBottom + bottomInset
                );
                return WindowInsetsCompat.CONSUMED;
            });
        }

        ViewCompat.requestApplyInsets(content);
    }

    public static int computeStatusBarRectHeight(int statusTopInset, int totalHeight) {
        if (totalHeight <= 0 || statusTopInset <= 0) return 0;
        return Math.min(statusTopInset, totalHeight);
    }

    public static int computeBottomInsetRectHeight(int bottomInset, int clampedTopHeight, int totalHeight) {
        if (totalHeight <= 0 || bottomInset <= 0) return 0;
        int remaining = Math.max(0, totalHeight - Math.max(0, clampedTopHeight));
        return Math.min(bottomInset, remaining);
    }

    private static final class StatusBarInsetDrawable extends Drawable {
        private final Paint statusBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint navBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int topInsetPx = 0;
        private int bottomInsetPx = 0;

        StatusBarInsetDrawable(@ColorInt int statusBarColor, @ColorInt int bodyColor, @ColorInt int navBarColor) {
            statusBarPaint.setColor(statusBarColor);
            bodyPaint.setColor(bodyColor);
            navBarPaint.setColor(navBarColor);
        }

        void setInsets(int topPx, int bottomPx) {
            int nextTop = Math.max(0, topPx);
            int nextBottom = Math.max(0, bottomPx);
            if (nextTop != topInsetPx || nextBottom != bottomInsetPx) {
                topInsetPx = nextTop;
                bottomInsetPx = nextBottom;
                invalidateSelf();
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            if (bounds.isEmpty()) return;

            int height = bounds.height();
            int clampedTop = computeStatusBarRectHeight(topInsetPx, height);
            int clampedBottom = computeBottomInsetRectHeight(bottomInsetPx, clampedTop, height);

            canvas.drawRect(
                    bounds.left,
                    bounds.top + clampedTop,
                    bounds.right,
                    bounds.bottom - clampedBottom,
                    bodyPaint
            );

            if (clampedTop > 0) {
                canvas.drawRect(
                        bounds.left,
                        bounds.top,
                        bounds.right,
                        bounds.top + clampedTop,
                        statusBarPaint
                );
            }

            if (clampedBottom > 0) {
                canvas.drawRect(
                        bounds.left,
                        bounds.bottom - clampedBottom,
                        bounds.right,
                        bounds.bottom,
                        navBarPaint
                );
            }
        }

        @Override
        public void setAlpha(int alpha) {
            statusBarPaint.setAlpha(alpha);
            bodyPaint.setAlpha(alpha);
            navBarPaint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            statusBarPaint.setColorFilter(colorFilter);
            bodyPaint.setColorFilter(colorFilter);
            navBarPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        @SuppressWarnings("deprecation")
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
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
