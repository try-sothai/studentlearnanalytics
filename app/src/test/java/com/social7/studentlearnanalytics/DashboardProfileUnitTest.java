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
}
