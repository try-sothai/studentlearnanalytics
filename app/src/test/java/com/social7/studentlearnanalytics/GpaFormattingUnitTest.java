package com.social7.studentlearnanalytics;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.social7.studentlearnanalytics.model.DashboardResponse;

import org.junit.Test;

/**
 * Focused unit tests for Phase 7D.1 GPA formatting rules.
 *
 * Builds DashboardResponse objects from real API-shaped JSON via Gson, so no
 * private fields are touched. Missing GPA must never be rendered as a
 * fabricated 0.0, while a genuine numeric zero from the API is preserved.
 */
public class GpaFormattingUnitTest {

    private DashboardResponse.DashboardData dataWith(String json) {
        DashboardResponse response = new Gson().fromJson(json, DashboardResponse.class);
        return response.getData();
    }

    @Test
    public void missingGpa_isNotFormattedAsZero() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{}}");
        assertEquals("Unavailable", data.getGpaFormatted());
    }

    @Test
    public void numericZeroGpa_isPreserved() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{\"gpa\":0}}");
        assertEquals("0.00", data.getGpaFormatted());
    }

    @Test
    public void validGpa_isFormattedWithTwoDecimals() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{\"gpa\":3.25}}");
        assertEquals("3.25", data.getGpaFormatted());
    }

    @Test
    public void outOfRangeGpa_isReportedAsUnavailable() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{\"gpa\":4.5}}");
        assertEquals("Unavailable", data.getGpaFormatted());
    }

    @Test
    public void nonNumericGpa_isReportedAsUnavailable() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{\"gpa\":\"not-a-number\"}}");
        assertEquals("Unavailable", data.getGpaFormatted());
    }

    @Test
    public void emptyGpa_isReportedAsUnavailable() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{\"gpa\":\"  \"}}");
        assertEquals("Unavailable", data.getGpaFormatted());
    }

    @Test
    public void fallbackToNestedRecord_usesRecordValue() {
        DashboardResponse.DashboardData data = dataWith("{\"data\":{\"gpa_record\":{\"gpa\":2.5}}}");
        assertEquals("2.50", data.getGpaFormatted());
    }

    @Test
    public void rangeBoundaries_areAccepted() {
        DashboardResponse.DashboardData low = dataWith("{\"data\":{\"gpa\":0}}");
        assertEquals("0.00", low.getGpaFormatted());

        DashboardResponse.DashboardData high = dataWith("{\"data\":{\"gpa\":4}}");
        assertEquals("4.00", high.getGpaFormatted());
    }
}