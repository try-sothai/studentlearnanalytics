package com.social7.studentlearnanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.model.DashboardResponse;

import org.junit.Test;

public class StudentGpaAcademicHistoryUnitTest {

    private final Gson gson = new Gson();

    @Test
    public void gpaHistoryPayload_preservesDistinctSemestersAndPendingNaDisplay() {
        String gpaHistoryJson = "{"
                + "\"success\":true,"
                + "\"data\":{"
                + "\"student\":{\"program_name\":\"Information Technology\",\"faculty_code\":\"FOSAT\"},"
                + "\"current_period\":{\"study_year\":1,\"year_level_name\":\"Year 1\",\"semester_name\":\"Semester 1\",\"academic_year_name\":\"2026-2027\"},"
                + "\"cumulative_gpa_display\":\"N/A\","
                + "\"completed_credits\":0,"
                + "\"years\":["
                + "  {\"study_year\":1,\"semesters\":["
                + "    {\"study_year\":1,\"semester_id\":3,\"semester_number\":1,\"semester_name\":\"Semester 1\",\"academic_year_name\":\"2026-2027\",\"state\":\"Current\",\"semester_average_display\":\"N/A\",\"semester_gpa_display\":\"N/A\",\"courses\":[{\"course_name\":\"C++\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"},{\"course_name\":\"Data Communication and Networking\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"},{\"course_name\":\"Multimedia and Graphic Design\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"}]},"
                + "    {\"study_year\":1,\"semester_id\":4,\"semester_number\":2,\"semester_name\":\"Semester 2\",\"academic_year_name\":\"2026-2027\",\"state\":\"Planned\",\"semester_average_display\":\"N/A\",\"semester_gpa_display\":\"N/A\",\"courses\":[{\"course_name\":\".NET Technology\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"},{\"course_name\":\"System Analytics and Design\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"}]}"
                + "  ]},"
                + "  {\"study_year\":2,\"semesters\":["
                + "    {\"study_year\":2,\"semester_id\":9,\"semester_number\":1,\"semester_name\":\"Semester 1\",\"academic_year_name\":\"2027-2028\",\"state\":\"Planned\",\"semester_average_display\":\"N/A\",\"semester_gpa_display\":\"N/A\",\"courses\":[{\"course_name\":\"Web App Development\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"},{\"course_name\":\"Web App Framework\",\"score_display\":\"N/A\",\"grade_display\":\"N/A\"}]}"
                + "  ]},"
                + "  {\"study_year\":3,\"semesters\":[],\"empty_state\":\"No curriculum or semester calendar is configured for Year 3 yet.\"},"
                + "  {\"study_year\":4,\"semesters\":[],\"empty_state\":\"No curriculum or semester calendar is configured for Year 4 yet.\"}"
                + "]"
                + "}"
                + "}";

        JsonObject root = JsonParser.parseString(gpaHistoryJson).getAsJsonObject();
        JsonObject data = root.getAsJsonObject("data");
        JsonArray years = data.getAsJsonArray("years");
        assertEquals(4, years.size());

        JsonArray y1Semesters = years.get(0).getAsJsonObject().getAsJsonArray("semesters");
        assertEquals(2, y1Semesters.size());
        assertEquals(1, y1Semesters.get(0).getAsJsonObject().get("semester_number").getAsInt());
        assertEquals("Semester 1", y1Semesters.get(0).getAsJsonObject().get("semester_name").getAsString());
        assertEquals(3, y1Semesters.get(0).getAsJsonObject().getAsJsonArray("courses").size());
        assertEquals(2, y1Semesters.get(1).getAsJsonObject().get("semester_number").getAsInt());
        assertEquals("Semester 2", y1Semesters.get(1).getAsJsonObject().get("semester_name").getAsString());
        assertEquals(2, y1Semesters.get(1).getAsJsonObject().getAsJsonArray("courses").size());

        JsonArray y2Semesters = years.get(1).getAsJsonObject().getAsJsonArray("semesters");
        assertEquals(1, y2Semesters.size());
        assertEquals("Semester 1", y2Semesters.get(0).getAsJsonObject().get("semester_name").getAsString());
        assertEquals("2027-2028", y2Semesters.get(0).getAsJsonObject().get("academic_year_name").getAsString());
        assertEquals("N/A", y2Semesters.get(0).getAsJsonObject().get("semester_gpa_display").getAsString());
    }

    @Test
    public void weightedBreakdownPayload_parsesPart7ExampleAndPendingNaState() {
        String breakdownJson = "{"
                + "\"weighting_policy\":{\"attendance\":10,\"coursework\":40,\"midterm\":15,\"final\":35},"
                + "\"groups\":["
                + "  {\"key\":\"attendance\",\"label\":\"Attendance\",\"raw_display\":\"26.00 / 30\",\"weighted_display\":\"8.67 / 10\",\"has_score\":true,\"components\":[{\"label\":\"Attendance\",\"type\":\"attendance\",\"raw_display\":\"26/30\",\"has_score\":true}]},"
                + "  {\"key\":\"coursework\",\"label\":\"Coursework / Quiz\",\"raw_display\":\"82.83 / 100\",\"weighted_display\":\"33.13 / 40\",\"has_score\":true,\"components\":[{\"label\":\"Homework 1\",\"type\":\"homework\",\"raw_display\":\"8.5/10\",\"has_score\":true},{\"label\":\"Assignment 1\",\"type\":\"assignment\",\"raw_display\":\"9/10\",\"has_score\":true},{\"label\":\"Quiz 1\",\"type\":\"quiz\",\"raw_display\":\"8/10\",\"has_score\":true},{\"label\":\"Quiz 2\",\"type\":\"quiz\",\"raw_display\":\"10/10\",\"has_score\":true}]},"
                + "  {\"key\":\"midterm\",\"label\":\"Midterm Exam\",\"raw_display\":\"66.00 / 100\",\"weighted_display\":\"9.90 / 15\",\"has_score\":true,\"components\":[{\"label\":\"Midterm Exam\",\"type\":\"midterm\",\"raw_display\":\"25.5/30\",\"has_score\":true}]},"
                + "  {\"key\":\"final\",\"label\":\"Final Exam\",\"raw_display\":\"61.00 / 100\",\"weighted_display\":\"21.35 / 35\",\"has_score\":true,\"components\":[{\"label\":\"Final Exam\",\"type\":\"final\",\"raw_display\":\"26.4/30\",\"has_score\":true}]}"
                + "],"
                + "\"computed_total\":73.05,"
                + "\"total_display\":\"73.05 / 100\","
                + "\"letter_grade\":\"B-\","
                + "\"is_finalized\":true"
                + "}";
        JsonObject wb = JsonParser.parseString(breakdownJson).getAsJsonObject();
        assertEquals("73.05 / 100", wb.get("total_display").getAsString());
        assertEquals("B-", wb.get("letter_grade").getAsString());
        JsonArray groups = wb.getAsJsonArray("groups");
        assertEquals(4, groups.size());
        assertEquals("8.67 / 10", groups.get(0).getAsJsonObject().get("weighted_display").getAsString());
        assertEquals("33.13 / 40", groups.get(1).getAsJsonObject().get("weighted_display").getAsString());
        assertEquals(4, groups.get(1).getAsJsonObject().getAsJsonArray("components").size());
        assertEquals("9.90 / 15", groups.get(2).getAsJsonObject().get("weighted_display").getAsString());
        assertEquals("21.35 / 35", groups.get(3).getAsJsonObject().get("weighted_display").getAsString());

        String pendingJson = "{\"groups\":[{\"key\":\"attendance\",\"label\":\"Attendance\",\"raw_display\":\"N/A\",\"weighted_display\":\"N/A / 10\",\"has_score\":false}],\"total_display\":\"N/A\",\"letter_grade\":\"N/A\",\"is_finalized\":false}";
        JsonObject pending = JsonParser.parseString(pendingJson).getAsJsonObject();
        assertEquals("N/A", pending.get("total_display").getAsString());
        assertEquals("N/A", pending.get("letter_grade").getAsString());
    }

    @Test
    public void dashboardGpaCard_prefersCurrentSemesterGpaAndDisplaysUnavailableWhenNull() {
        String incompleteSemesterJson = "{"
                + "\"status\":\"success\","
                + "\"data\":{"
                + "\"gpa\":null,"
                + "\"gpa_summary\":{"
                + "\"current_semester_gpa\":null,"
                + "\"semester_gpa\":null,"
                + "\"cumulative_gpa\":null,"
                + "\"academic_standing\":null"
                + "}"
                + "}"
                + "}";

        DashboardResponse incompleteResp = gson.fromJson(incompleteSemesterJson, DashboardResponse.class);
        assertNotNull(incompleteResp.getData());
        assertEquals("Unavailable", incompleteResp.getData().getGpaFormatted());

        String completedSemesterJson = "{"
                + "\"status\":\"success\","
                + "\"data\":{"
                + "\"gpa\":3.45,"
                + "\"gpa_summary\":{"
                + "\"current_semester_gpa\":3.45,"
                + "\"semester_gpa\":3.45,"
                + "\"semester_average\":83.50,"
                + "\"cumulative_gpa\":3.20,"
                + "\"academic_standing\":\"Good Standing\""
                + "}"
                + "}"
                + "}";

        DashboardResponse completedResp = gson.fromJson(completedSemesterJson, DashboardResponse.class);
        assertNotNull(completedResp.getData());
        assertEquals("3.45", completedResp.getData().getGpaFormatted());
        assertEquals(Float.valueOf(83.50f), completedResp.getData().getGpaSummary().semesterAverage);
    }
}
