package com.social7.studentlearnanalytics;

import com.google.gson.Gson;
import com.social7.studentlearnanalytics.model.DashboardResponse;

import org.junit.Test;

import static org.junit.Assert.*;

public class DashboardProfileUnitTest {

    private final Gson gson = new Gson();

    @Test
    public void testStudentProfileParsingWithMajor() {
        String json = "{\n" +
                "  \"data\": {\n" +
                "    \"student\": {\n" +
                "      \"first_name\": \"Sok\",\n" +
                "      \"last_name\": \"Dara\",\n" +
                "      \"student_code\": \"STD001\",\n" +
                "      \"major\": \"Information Technology (IT)\",\n" +
                "      \"year_level\": 3,\n" +
                "      \"user\": {\n" +
                "        \"name\": \"Sok Dara\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        DashboardResponse response = gson.fromJson(json, DashboardResponse.class);
        assertNotNull(response);
        DashboardResponse.DashboardData data = response.getData();
        assertNotNull(data);

        assertEquals("Sok Dara", data.getStudentName());
        assertEquals("STD001", data.getStudentId());
        assertEquals("Information Technology (IT)", data.getMajorOrDepartment());
        assertEquals("Year 3", data.getYearLevelFormatted());
    }

    @Test
    public void testStudentProfileParsingWithDepartmentFallback() {
        String json = "{\n" +
                "  \"data\": {\n" +
                "    \"student\": {\n" +
                "      \"first_name\": \"Vibol\",\n" +
                "      \"last_name\": \"Chan\",\n" +
                "      \"student_id\": \"STD002\",\n" +
                "      \"department\": {\n" +
                "        \"name\": \"Computer Science\"\n" +
                "      },\n" +
                "      \"year_level\": \"2\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        DashboardResponse response = gson.fromJson(json, DashboardResponse.class);
        assertNotNull(response);
        DashboardResponse.DashboardData data = response.getData();

        assertEquals("Vibol Chan", data.getStudentName());
        assertEquals("STD002", data.getStudentId());
        assertEquals("Computer Science", data.getMajorOrDepartment());
        assertEquals("Year 2", data.getYearLevelFormatted());
    }

    @Test
    public void testStudentProfileParsingWithMissingMajorAndDepartment() {
        String json = "{\n" +
                "  \"data\": {\n" +
                "    \"student_name\": \"Test Student\",\n" +
                "    \"student_id\": \"STD003\"\n" +
                "  }\n" +
                "}";

        DashboardResponse response = gson.fromJson(json, DashboardResponse.class);
        assertNotNull(response);
        DashboardResponse.DashboardData data = response.getData();

        assertEquals("Test Student", data.getStudentName());
        assertEquals("STD003", data.getStudentId());
        assertEquals("", data.getMajorOrDepartment());
        assertEquals("Year 1", data.getYearLevelFormatted());
    }

    @Test
    public void testYearLevelFormatting() {
        DashboardResponse.StudentInfo student = new DashboardResponse.StudentInfo();

        assertEquals("Year 1", student.getYearLevelFormatted());
    }

    @Test
    public void testCanonicalCodesAndAcademicYearSeparation() {
        String json = "{\n" +
                "  \"data\": {\n" +
                "    \"student\": {\n" +
                "      \"student_code\": \"STD004\",\n" +
                "      \"year_level\": \"Y4\",\n" +
                "      \"year_level_code\": \"Y4\",\n" +
                "      \"academic_year\": \"2026-2027\",\n" +
                "      \"user\": { \"name\": \"Dara\" }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        DashboardResponse response = gson.fromJson(json, DashboardResponse.class);
        DashboardResponse.DashboardData data = response.getData();

        assertEquals("Year 4", data.getYearLevelFormatted());
        assertEquals("Y4", data.getYearLevelCode());
        assertEquals("2026-2027", data.getAcademicYear());
        assertEquals(Integer.valueOf(4), data.getStudent().getYearLevelNumber());
    }

    @Test
    public void testAcademicYearNeverConfusedWithYearLevel() {
        String json = "{\n" +
                "  \"data\": {\n" +
                "    \"student\": {\n" +
                "      \"student_code\": \"STD005\",\n" +
                "      \"year_level\": \"2026-2027\",\n" +
                "      \"academic_year\": \"2026-2027\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        DashboardResponse response = gson.fromJson(json, DashboardResponse.class);
        DashboardResponse.DashboardData data = response.getData();

        assertEquals("Year 1", data.getYearLevelFormatted());
        assertNull(data.getStudent().getYearLevelNumber());
        assertEquals("2026-2027", data.getAcademicYear());
    }
}
