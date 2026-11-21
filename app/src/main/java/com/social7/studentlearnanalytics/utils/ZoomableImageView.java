package com.social7.studentlearnanalytics.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * ImageView supporting pinch-to-zoom, pan when zoomed, and double-tap zoom toggle
 * for inspecting student image submissions clearly inside SLAS.
 */
public class ZoomableImageView extends AppCompatImageView {

    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 5.0f;

    private final Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private float currentScale = 1.0f;
    private float baseScale = 1.0f;

    private final PointF lastTouch = new PointF();
    private boolean isDragging = false;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    public ZoomableImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (currentScale > 1.05f) {
                    resetZoom();
                } else {
                    zoomBy(2.5f, e.getX(), e.getY());
                }
                return true;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fitImageToView();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        post(this::fitImageToView);
    }

    public void resetZoom() {
        fitImageToView();
    }

    private void fitImageToView() {
        Drawable d = getDrawable();
        if (d == null) return;
        int vw = getWidth() - getPaddingLeft() - getPaddingRight();
        int vh = getHeight() - getPaddingTop() - getPaddingBottom();
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        if (vw <= 0 || vh <= 0 || dw <= 0 || dh <= 0) return;

        float scaleX = (float) vw / dw;
        float scaleY = (float) vh / dh;
        baseScale = Math.min(scaleX, scaleY);
        currentScale = 1.0f;

        float dx = (vw - dw * baseScale) * 0.5f;
        float dy = (vh - dh * baseScale) * 0.5f;

        matrix.reset();
        matrix.postScale(baseScale, baseScale);
        matrix.postTranslate(dx, dy);
        setImageMatrix(matrix);
    }

    private void zoomBy(float targetScaleFactor, float focusX, float focusY) {
        float newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, targetScaleFactor));
        float factor = newScale / currentScale;
        currentScale = newScale;
        matrix.postScale(factor, factor, focusX, focusY);
        constrainTranslation();
        setImageMatrix(matrix);
    }

    private void constrainTranslation() {
        Drawable d = getDrawable();
        if (d == null) return;
        int vw = getWidth() - getPaddingLeft() - getPaddingRight();
        int vh = getHeight() - getPaddingTop() - getPaddingBottom();
        if (vw <= 0 || vh <= 0) return;

        matrix.getValues(matrixValues);
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];
        float totalScale = matrixValues[Matrix.MSCALE_X];
        float contentW = d.getIntrinsicWidth() * totalScale;
        float contentH = d.getIntrinsicHeight() * totalScale;

        float fixX = 0f;
        if (contentW <= vw) {
            fixX = (vw - contentW) * 0.5f - transX;
        } else {
            float minX = vw - contentW;
            if (transX < minX) fixX = minX - transX;
            else if (transX > 0f) fixX = -transX;
        }

        float fixY = 0f;
        if (contentH <= vh) {
            fixY = (vh - contentH) * 0.5f - transY;
        } else {
            float minY = vh - contentH;
            if (transY < minY) fixY = minY - transY;
            else if (transY > 0f) fixY = -transY;
        }

        matrix.postTranslate(fixX, fixY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getDrawable() == null) {
            return super.onTouchEvent(event);
        }

        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouch.set(event.getX(), event.getY());
                isDragging = true;
                if (currentScale > 1.02f && getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && !scaleDetector.isInProgress() && currentScale > 1.02f) {
                    float dx = event.getX() - lastTouch.x;
                    float dy = event.getY() - lastTouch.y;
                    matrix.postTranslate(dx, dy);
                    constrainTranslation();
                    setImageMatrix(matrix);
                    lastTouch.set(event.getX(), event.getY());
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            float nextScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, currentScale * detector.getScaleFactor()));
            float appliedFactor = nextScale / currentScale;
            currentScale = nextScale;
            matrix.postScale(appliedFactor, appliedFactor, detector.getFocusX(), detector.getFocusY());
            constrainTranslation();
            setImageMatrix(matrix);
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(currentScale > 1.02f);
            }
            return true;
        }
    }
}
