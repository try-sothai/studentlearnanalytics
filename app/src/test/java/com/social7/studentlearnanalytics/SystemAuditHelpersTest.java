package com.social7.studentlearnanalytics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.utils.UiUtils;
import com.social7.studentlearnanalytics.utils.ValidationUtils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemAuditHelpersTest {

    @Test
    public void isValidEmail_validatesFormatsAccurately() {
        assertTrue(ValidationUtils.isValidEmail("admin@slas.edu.kh"));
        assertTrue(ValidationUtils.isValidEmail("  teacher.one+test@univ.org "));
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("   "));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail("user@domain"));
        assertFalse(ValidationUtils.isValidEmail("@domain.com"));
    }

    @Test
    public void calculateGrowthPercentage_handlesBaselineAndRounding() {
        assertEquals(0.0, UiUtils.calculateGrowthPercentage(100, 0), 0.001);
        assertEquals(0.0, UiUtils.calculateGrowthPercentage(100, -5), 0.001);
        assertEquals(25.0, UiUtils.calculateGrowthPercentage(125, 100), 0.001);
        assertEquals(-20.0, UiUtils.calculateGrowthPercentage(80, 100), 0.001);
        assertEquals(33.3, UiUtils.calculateGrowthPercentage(400, 300), 0.001);
    }

    @Test
    public void parseNotificationsJson_parsesLaravelPayloadsAndNormalizesCategories() {
        String payload = "{"
                + "\"data\": ["
                + "  {\"id\": 12, \"title\": \"Assignment Due\", \"message\": \"Submit Lab 1\", \"type\": \"academic\", \"created_at\": \"2026-11-24T09:15:00Z\", \"is_read\": false},"
                + "  {\"id\": 13, \"title\": \"Security Notice\", \"message\": \"New login\", \"category\": \"security\", \"time_ago\": \"5m ago\", \"read_at\": \"2026-11-24T09:10:00Z\", \"priority\": \"high\"},"
                + "  {\"id\": 14, \"title\": \"New Teacher\", \"message\": \"Created account\", \"type\": \"users\", \"is_read\": 1}"
                + "]"
                + "}";
        JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
        List<Notification> list = UiUtils.parseNotificationsJson(json);

        assertEquals(3, list.size());

        assertEquals("12", list.get(0).getId());
        assertEquals("Course", list.get(0).getCategory());
        assertFalse(list.get(0).isRead());
        assertEquals("2026-11-24 09:15", list.get(0).getTimeAgo());

        assertEquals("13", list.get(1).getId());
        assertEquals("Security", list.get(1).getCategory());
        assertTrue(list.get(1).isRead());
        assertTrue(list.get(1).isImportant());
        assertEquals("5m ago", list.get(1).getTimeAgo());

        assertEquals("14", list.get(2).getId());
        assertEquals("User", list.get(2).getCategory());
        assertTrue(list.get(2).isRead());
    }

    @Test
    public void parseNotificationsJson_handlesEmptyOrNullGracefully() {
        assertTrue(UiUtils.parseNotificationsJson(null).isEmpty());
        assertTrue(UiUtils.parseNotificationsJson(new JsonObject()).isEmpty());
        assertEquals("System", UiUtils.normalizeNotifCategory(null));
        assertEquals("System", UiUtils.normalizeNotifCategory("  "));
    }
}
