package com.social7.studentlearnanalytics.localization;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.social7.studentlearnanalytics.R;

/**
 * Applies Khmer-compatible Typeface to MPAndroidChart axes, legends, and labels
 * so Khmer text renders cleanly without missing glyphs or broken shaping.
 */
public final class ChartFontHelper {

    private ChartFontHelper() {
    }

    public static Typeface getChartTypeface(Context context) {
        if (context == null) return Typeface.DEFAULT;
        Typeface tf = ResourcesCompat.getFont(context, R.font.notosanskhmer_regular);
        return tf != null ? tf : Typeface.DEFAULT;
    }

    public static void applyKhmerFont(Context context, Chart<?> chart) {
        if (context == null || chart == null) return;
        Typeface tf = getChartTypeface(context);
        if (chart.getLegend() != null) {
            chart.getLegend().setTypeface(tf);
        }
        if (chart.getDescription() != null) {
            chart.getDescription().setTypeface(tf);
        }
        if (!(chart instanceof PieChart) && chart.getXAxis() != null) {
            chart.getXAxis().setTypeface(tf);
        }
        if (chart instanceof BarLineChartBase) {
            BarLineChartBase<?> bl = (BarLineChartBase<?>) chart;
            if (bl.getAxisLeft() != null) bl.getAxisLeft().setTypeface(tf);
            if (bl.getAxisRight() != null) bl.getAxisRight().setTypeface(tf);
        } else if (chart instanceof PieChart) {
            PieChart pc = (PieChart) chart;
            pc.setEntryLabelTypeface(tf);
            pc.setCenterTextTypeface(tf);
        }
    }
}
