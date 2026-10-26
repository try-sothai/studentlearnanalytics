package com.social7.studentlearnanalytics.network;

import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.model.GpaRecordsResponse;
import com.social7.studentlearnanalytics.model.LoginRequest;
import com.social7.studentlearnanalytics.model.LoginResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Headers({"Accept: application/json"})
    @GET("dashboard/student")
    Call<DashboardResponse> getStudentDashboard();

    @Headers({"Accept: application/json"})
    @GET("gpa-records")
    Call<GpaRecordsResponse> getGpaRecords();

    @Headers({"Accept: application/json"})
    @GET("grades")
    Call<com.social7.studentlearnanalytics.model.GradesResponse> getGrades();

    @Headers({"Accept: application/json"})
    @GET("courses")
    Call<List<Course>> getCourses();

    @Headers({"Accept: application/json"})
    @GET("learning-analytics")
    Call<JsonObject> getLearningAnalytics();

    @Headers({"Accept: application/json"})
    @GET("predictions")
    Call<JsonObject> getPredictions();

    @Headers({"Accept: application/json"})
    @GET("risk-assessments")
    Call<JsonObject> getRiskAssessments();

    @Headers({"Accept: application/json"})
    @GET("recommendations")
    Call<JsonObject> getRecommendations();

    @Headers({"Accept: application/json"})
    @GET("notifications")
    Call<JsonObject> getNotifications();

    @Headers({"Accept: application/json"})
    @GET("profile")
    Call<JsonObject> getProfile();

    @Headers({"Accept: application/json"})
    @GET("performance-records")
    Call<JsonObject> getPerformanceRecords(@retrofit2.http.Query("course_id") String courseId);

    @Headers({"Accept: application/json"})
    @GET("student/attendance")
    Call<JsonObject> getStudentAttendance();

    @Headers({"Accept: application/json"})
    @GET("student/assignments")
    Call<JsonObject> getStudentAssignments();

    @Headers({"Accept: application/json"})
    @GET("student/quizzes")
    Call<JsonObject> getStudentQuizzes();

    @Headers({"Accept: application/json"})
    @GET("student/exams")
    Call<JsonObject> getStudentExams();
}