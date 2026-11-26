package com.social7.studentlearnanalytics.utils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class YearLevelHelperUnitTest {

    @Test
    public void parseAcceptsValidRepresentationsAndKhmer() {
        assertEquals(Integer.valueOf(1), YearLevelHelper.parse(1));
        assertEquals(Integer.valueOf(2), YearLevelHelper.parse("2"));
        assertEquals(Integer.valueOf(3), YearLevelHelper.parse("Y3"));
        assertEquals(Integer.valueOf(4), YearLevelHelper.parse("y4"));
        assertEquals(Integer.valueOf(2), YearLevelHelper.parse("Year 2"));
        assertEquals(Integer.valueOf(2), YearLevelHelper.parse("2.0"));
        assertEquals(Integer.valueOf(3), YearLevelHelper.parse(3.0));
        assertEquals(Integer.valueOf(2), YearLevelHelper.parse("Y2 — Year 2"));
        assertEquals(Integer.valueOf(4), YearLevelHelper.parse("Year 4 — Fourth Year"));
        assertEquals(Integer.valueOf(1), YearLevelHelper.parse("ឆ្នាំទី១"));
        assertEquals(Integer.valueOf(3), YearLevelHelper.parse("ឆ្នាំទី៣"));
    }

    @Test
    public void parseRejectsOutOfRangeAndAcademicYears() {
        assertNull(YearLevelHelper.parse(null));
        assertNull(YearLevelHelper.parse(""));
        assertNull(YearLevelHelper.parse("Unassigned"));
        assertNull(YearLevelHelper.parse(0));
        assertNull(YearLevelHelper.parse(5));
        assertNull(YearLevelHelper.parse(-1));
        assertNull(YearLevelHelper.parse("Y5"));
        assertNull(YearLevelHelper.parse("Year 5"));
        assertNull(YearLevelHelper.parse("Year 10"));
        assertNull(YearLevelHelper.parse("2.5"));
        assertNull(YearLevelHelper.parse(5.0));
        assertNull(YearLevelHelper.parse("2024-2025"));
        assertNull(YearLevelHelper.parse("2025-2026"));
        assertNull(YearLevelHelper.parse("2026–2027"));
    }

    @Test
    public void formattingAndStorageHelpersProduceCanonicalValues() {
        assertTrue(YearLevelHelper.isValid("Y1"));
        assertFalse(YearLevelHelper.isValid("Y5"));
        assertFalse(YearLevelHelper.isValid("2026-2027"));

        assertEquals("Y1", YearLevelHelper.toCode(1));
        assertEquals("Y4", YearLevelHelper.toCode("Year 4"));
        assertNull(YearLevelHelper.toCode("2026-2027"));

        assertEquals("2", YearLevelHelper.toStorageString("Y2"));
        assertEquals("3", YearLevelHelper.toStorageString("Year 3"));
        assertNull(YearLevelHelper.toStorageString(5));

        assertEquals("Y1 — Year 1", YearLevelHelper.toFullLabel(null, 1));
        assertEquals("Y4 — Year 4", YearLevelHelper.toFullLabel(null, "Y4"));
        assertEquals("Unassigned", YearLevelHelper.toFullLabel(null, null));

        assertEquals("Y3", YearLevelHelper.toApiFilterParam("Year 3"));
        assertEquals(Arrays.asList(1, 2, 3, 4), YearLevelHelper.canonicalLevels());
    }

    @Test
    public void spinnerIndexHandlesFormAndFilterModes() {
        assertEquals(0, YearLevelHelper.spinnerIndexFor(1, false));
        assertEquals(3, YearLevelHelper.spinnerIndexFor("Y4", false));
        assertEquals(0, YearLevelHelper.spinnerIndexFor(null, false));

        assertEquals(1, YearLevelHelper.spinnerIndexFor(1, true));
        assertEquals(4, YearLevelHelper.spinnerIndexFor("Year 4", true));
        assertEquals(0, YearLevelHelper.spinnerIndexFor("2026-2027", true));
    }
}
