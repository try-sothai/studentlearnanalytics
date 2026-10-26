package com.social7.studentlearnanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.social7.studentlearnanalytics.model.DashboardResponse;

import org.junit.Test;

public class OverallPerformanceUnitTest {

    private DashboardResponse.DashboardData parseData(String json) {
        DashboardResponse response = new Gson().fromJson(json, DashboardResponse.class);
        return response.getData();
    }

    @Test
    public void missingPerformanceFields_areFormattedAsNA() {
        DashboardResponse.DashboardData data = parseData("{\"data\":{}}");
        assertEquals("N/A", data.getOverallPerformanceFormatted());
        assertEquals("N/A", data.getAttendanceRateFormatted());
        assertEquals("N/A", data.getAssignmentScoreFormatted());
        assertEquals("N/A", data.getQuizScoreFormatted());
    }

    @Test
    public void validPerformanceMetrics_areFormattedWithPercentage() {
        DashboardResponse.DashboardData data = parseData(
                "{\"data\":{\"overall_performance\":85.5,\"attendance_rate\":92.0,\"assignment_score\":88,\"quiz_score\":78.4}}"
        );
        assertEquals("85.5%", data.getOverallPerformanceFormatted());
        assertEquals("92%", data.getAttendanceRateFormatted());
        assertEquals("88%", data.getAssignmentScoreFormatted());
        assertEquals("78.4%", data.getQuizScoreFormatted());
    }

    @Test
    public void nestedAnalyticsData_takesPrecedenceOverFlatData() {
        DashboardResponse.DashboardData data = parseData(
                "{\"data\":{\"overall_performance\":70,\"analytics\":{\"overall_performance\":88.2}}}"
        );
        assertEquals("88.2%", data.getOverallPerformanceFormatted());
    }

    @Test
    public void enrollmentSubjectsAndSemesters_areParsedCorrectly() {
        DashboardResponse.DashboardData data = parseData(
                "{\"data\":{\"enrollments\":[{\"id\":\"1\",\"final_score\":85.0,\"course\":{\"id\":\"101\",\"course_code\":\"CS101\",\"course_name\":\"Data Structures\"},\"semester\":{\"name\":\"Semester 1, 2025\"}}]}}"
        );
        assertNotNull(data.getEnrollments());
        assertEquals(1, data.getEnrollments().size());
        DashboardResponse.EnrollmentInfo info = data.getEnrollments().get(0);
        assertEquals(Float.valueOf(85.0f), info.finalScore);
        assertEquals("CS101", info.course.courseCode);
        assertEquals("Data Structures", info.course.courseName);
        assertNotNull(info.semester);
        assertEquals("Semester 1, 2025", info.semester.name);
    }

    @Test
    public void emptyEnrollments_isRepresentedAsEmptyList() {
        DashboardResponse.DashboardData data = parseData("{\"data\":{\"enrollments\":[]}}");
        assertNotNull(data.getEnrollments());
        assertEquals(0, data.getEnrollments().size());
    }

    @Test
    public void missingChartData_returnsNull() {
        DashboardResponse.DashboardData data = parseData("{\"data\":{}}");
        assertNull(data.getChartData());
        assertNull(data.getChartLabels());
    }

    @Test
    public void validChartData_parsesValuesAndLabels() {
        DashboardResponse.DashboardData data = parseData(
                "{\"data\":{\"chart_data\":[75.0,80.5,88.0],\"chart_labels\":[\"Jan\",\"Feb\",\"Mar\"]}}"
        );
        assertNotNull(data.getChartData());
        assertEquals(3, data.getChartData().size());
        assertEquals(Float.valueOf(75.0f), data.getChartData().get(0));
        assertEquals("Jan", data.getChartLabels().get(0));
    }
}
