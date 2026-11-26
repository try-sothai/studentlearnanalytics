package com.social7.studentlearnanalytics.admin.courses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;

import org.junit.Test;

public class CourseCategoryTest {

    @Test
    public void categoryItem_fromJson_parsesFieldsAndCountsGracefully() {
        JsonObject json = new JsonObject();
        json.addProperty("id", 14);
        json.addProperty("name", "Data Science");
        json.addProperty("code", "DS");
        json.add("description", com.google.gson.JsonNull.INSTANCE);
        json.addProperty("status", "active");
        json.addProperty("courses_count", 3);
        json.addProperty("classes_count", 1);
        json.addProperty("programs_count", 0);
        json.addProperty("teachers_count", 2);
        json.addProperty("students_count", 0);

        CourseCategoryAdapter.CategoryItem item = CourseCategoryAdapter.CategoryItem.fromJson(json);

        assertEquals("14", item.id);
        assertEquals("Data Science", item.name);
        assertEquals("DS", item.code);
        assertEquals("", item.description);
        assertTrue(item.isActive());
        assertEquals("Active", item.getDisplayStatus());
        assertEquals(3, item.courseCount);
        assertEquals(1, item.classCount);
        assertEquals(2, item.teacherCount);
        assertTrue(item.hasLinkedRecords());
        assertEquals("3 courses, 1 class, 2 teachers", item.buildDependencySummary());
    }

    @Test
    public void categoryItem_matchesQuery_filtersByNameCodeDescriptionAndStatus() {
        CourseCategoryAdapter.CategoryItem activeCat = new CourseCategoryAdapter.CategoryItem(
                "1", "Computer Science", "CS", "Software & AI courses", "active",
                2, 0, 0, 0, 0
        );
        CourseCategoryAdapter.CategoryItem inactiveCat = new CourseCategoryAdapter.CategoryItem(
                "2", "Business Administration", "BA", "", "inactive",
                0, 0, 0, 0, 0
        );

        assertTrue(activeCat.matchesQuery(""));
        assertTrue(activeCat.matchesQuery("computer"));
        assertTrue(activeCat.matchesQuery("cs"));
        assertTrue(activeCat.matchesQuery("software"));
        assertTrue(activeCat.matchesQuery("active"));
        assertFalse(activeCat.matchesQuery("business"));

        assertFalse(inactiveCat.hasLinkedRecords());
        assertFalse(inactiveCat.isActive());
        assertEquals("Inactive", inactiveCat.getDisplayStatus());
        assertTrue(inactiveCat.matchesQuery("inactive"));
        assertTrue(inactiveCat.matchesQuery("BA"));
    }
}
