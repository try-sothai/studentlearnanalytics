package com.social7.studentlearnanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper;

import org.junit.Test;

public class FormValidationHelperUnitTest {

    @Test
    public void personName_requiresAtLeastOneOfKhmerOrEnglish() {
        assertTrue(PersonNameDisplayHelper.hasAtLeastOneName("សុខ សុធា", ""));
        assertTrue(PersonNameDisplayHelper.hasAtLeastOneName("", "Try Sothai"));
        assertTrue(PersonNameDisplayHelper.hasAtLeastOneName("សុខ សុធា", "Try Sothai"));

        assertFalse(PersonNameDisplayHelper.hasAtLeastOneName("", ""));
        assertFalse(PersonNameDisplayHelper.hasAtLeastOneName("   \t  ", "   "));
        assertFalse(PersonNameDisplayHelper.hasAtLeastOneName(null, null));
    }

    @Test
    public void personName_resolvesDisplayNameConsistentOrder() {
        assertEquals("Try Sothai", PersonNameDisplayHelper.resolveDisplayName("Try Sothai", "សុខ សុធា", "Fallback"));
        assertEquals("សុខ សុធា", PersonNameDisplayHelper.resolveDisplayName("", "សុខ សុធា", "Fallback"));
        assertEquals("សុខ សុធា", PersonNameDisplayHelper.resolveDisplayName("   ", "សុខ សុធា", "Fallback"));
        assertEquals("Fallback", PersonNameDisplayHelper.resolveDisplayName("", "", "Fallback"));
        assertEquals("", PersonNameDisplayHelper.resolveDisplayName(null, null, null));
    }

    @Test
    public void isoDateValidation_acceptsValidAndRejectsInvalidCalendarDates() {
        assertTrue(DatePickerHelper.isValidIsoDate("2004-02-29"));
        assertTrue(DatePickerHelper.isValidIsoDate("2026-11-21"));
        assertTrue(DatePickerHelper.isValidIsoDate(" 1998-07-05 "));

        assertFalse(DatePickerHelper.isValidIsoDate("2025-02-29"));
        assertFalse(DatePickerHelper.isValidIsoDate("2026-04-31"));
        assertFalse(DatePickerHelper.isValidIsoDate("2026-13-01"));
        assertFalse(DatePickerHelper.isValidIsoDate("21-11-2026"));
        assertFalse(DatePickerHelper.isValidIsoDate("2026/11/21"));
        assertFalse(DatePickerHelper.isValidIsoDate(""));
        assertFalse(DatePickerHelper.isValidIsoDate(null));
    }
}
