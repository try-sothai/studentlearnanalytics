package com.social7.studentlearnanalytics.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WindowInsetsHelperUnitTest {

    @Test
    public void computeStatusBarRectHeight_confinesColorStrictlyToStatusBarInset() {
        assertEquals(84, WindowInsetsHelper.computeStatusBarRectHeight(84, 2400));
        assertEquals(0, WindowInsetsHelper.computeStatusBarRectHeight(0, 2400));
        assertEquals(0, WindowInsetsHelper.computeStatusBarRectHeight(-12, 2400));
        assertEquals(50, WindowInsetsHelper.computeStatusBarRectHeight(120, 50));
    }

    @Test
    public void computeBottomInsetRectHeight_neverOverlapsTopStatusBarInset() {
        assertEquals(48, WindowInsetsHelper.computeBottomInsetRectHeight(48, 84, 2400));
        assertEquals(0, WindowInsetsHelper.computeBottomInsetRectHeight(0, 84, 2400));
        assertEquals(16, WindowInsetsHelper.computeBottomInsetRectHeight(48, 84, 100));
        assertEquals(0, WindowInsetsHelper.computeBottomInsetRectHeight(48, 100, 100));
    }
}
