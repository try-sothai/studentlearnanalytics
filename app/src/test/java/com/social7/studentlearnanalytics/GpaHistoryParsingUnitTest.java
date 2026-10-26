package com.social7.studentlearnanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.social7.studentlearnanalytics.model.GpaRecordsResponse;
import org.junit.Test;

/**
 * Phase 7D.2 focused tests for GPA history parsing (Phase 7D.1 fixes applied
 * to the GPA history pipeline). Mirrors the Activity's formatGpa acceptance
 * rules: missing values stay null (never fabricated as 0.0), and non-finite
 * or out-of-range values are rejected rather than plotted as zero.
 */
public class GpaHistoryParsingUnitTest {

    private GpaRecordsResponse parse(String json) {
        return new Gson().fromJson(json, GpaRecordsResponse.class);
    }

    /** Same acceptance rules as GpaProgressActivity.formatGpa. */
    private String formatGpa(Object value) {
        if (value == null) return null;
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) return null;
        try {
            double gpa = Double.parseDouble(raw);
            if (Double.isNaN(gpa) || Double.isInfinite(gpa) || gpa < 0 || gpa > 4) return null;
            return String.format(java.util.Locale.US, "%.2f", gpa);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Test
    public void validHistoryRecords_areParsedWithSemesterAndStanding() {
        GpaRecordsResponse response = parse(
                "{\"data\":[{\"gpa\":3.5,\"academic_standing\":\"Good\",\"semester\":{\"name\":\"Fall 2025\"}}]}");
        assertEquals(1, response.getRecords().size());
        GpaRecordsResponse.GpaRecord record = response.getRecords().get(0);
        assertEquals("3.50", formatGpa(record.getGpa()));
        assertEquals("Good", record.getAcademicStanding());
        assertEquals("Fall 2025", record.getSemester().getName());
    }

    @Test
    public void emptyHistory_isRepresentedAsEmptyList_notZero() {
        GpaRecordsResponse response = parse("{\"data\":[]}");
        assertTrue(response.getRecords().isEmpty());
    }

    @Test
    public void missingHistoryField_isRepresentedAsNullList() {
        GpaRecordsResponse response = parse("{}");
        assertNull(response.getRecords());
    }

    @Test
    public void missingRecordGpa_isNotFormattedAsZero() {
        GpaRecordsResponse response = parse("{\"data\":[{}]}");
        assertNull(formatGpa(response.getRecords().get(0).getGpa()));
    }

    @Test
    public void nonNumericHistoryGpa_isRejected() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":\"N/A\"}]}");
        assertNull(formatGpa(response.getRecords().get(0).getGpa()));
    }

    @Test
    public void stringNaNHistoryGpa_isRejected() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":\"NaN\"}]}");
        assertNull(formatGpa(response.getRecords().get(0).getGpa()));
    }

    @Test
    public void infiniteHistoryGpa_isRejected() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":\"Infinity\"}]}");
        assertNull(formatGpa(response.getRecords().get(0).getGpa()));
    }

    @Test
    public void belowZeroHistoryGpa_isRejected() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":-1.5}]}");
        assertNull(formatGpa(response.getRecords().get(0).getGpa()));
    }

    @Test
    public void aboveFourHistoryGpa_isRejected() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":4.01}]}");
        assertNull(formatGpa(response.getRecords().get(0).getGpa()));
    }

    @Test
    public void invalidRecords_areSkippedWhenFindingLatestFallback() {
        // Mirrors latestHistoryGpa(): skip invalid/missing values, return the
        // most recent valid one, and never fabricate 0.0.
        GpaRecordsResponse response = parse(
                "{\"data\":[{\"gpa\":\"NaN\"},{},{\"gpa\":2.75},{\"gpa\":900}]}");
        java.util.List<GpaRecordsResponse.GpaRecord> records = response.getRecords();
        String latest = null;
        for (int i = records.size() - 1; i >= 0; i--) {
            String value = formatGpa(records.get(i).getGpa());
            if (value != null) { latest = value; break; }
        }
        assertEquals("2.75", latest);
    }

    @Test
    public void allInvalidHistoryYieldsNoFallback_notZero() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":\"NaN\"},{\"gpa\":-2},{\"gpa\":99}]}");
        String latest = null;
        for (GpaRecordsResponse.GpaRecord record : response.getRecords()) {
            String value = formatGpa(record.getGpa());
            if (value != null) { latest = value; break; }
        }
        assertNull(latest);
    }

    @Test
    public void missingSemester_rendersPlaceholderLabel() {
        GpaRecordsResponse response = parse("{\"data\":[{\"gpa\":3.0}]}");
        GpaRecordsResponse.GpaRecord record = response.getRecords().get(0);
        GpaRecordsResponse.Semester semester = record.getSemester();
        String label = semester == null || semester.getName() == null || semester.getName().trim().isEmpty()
                ? "Semester unavailable" : semester.getName().trim();
        assertEquals("Semester unavailable", label);
    }
}