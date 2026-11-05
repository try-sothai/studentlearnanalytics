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
    @GET("dashboard/student")
    Call<DashboardResponse> getStudentDashboardFiltered(
            @retrofit2.http.Query("semester_id") Long semesterId,
            @retrofit2.http.Query("course_id") Long courseId
    );

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
    @GET("notifications/unread-count")
    Call<JsonObject> getUnreadNotificationsCount(@retrofit2.http.Header("Authorization") String token);

    @Headers({"Accept: application/json"})
    @GET("profile")
    Call<JsonObject> getProfile();

    @Headers({"Accept: application/json"})
    @GET("performance-records")
    Call<JsonObject> getPerformanceRecords(@retrofit2.http.Query("course_id") String courseId);

    @Headers({"Accept: application/json"})
    @GET("student/attendance")
    Call<JsonObject> getStudentAttendance(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/assignments")
    Call<JsonObject> getStudentAssignments(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/quizzes")
    Call<JsonObject> getStudentQuizzes(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/exams")
    Call<JsonObject> getStudentExams(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("dashboard/teacher")
    Call<JsonObject> getTeacherDashboard();

    @Headers({"Accept: application/json"})
    @GET("dashboard/admin")
    Call<JsonObject> getAdminDashboard();

    @Headers({"Accept: application/json"})
    @GET("admin/dashboard/overview")
    Call<JsonObject> getAdminDashboardOverview();

    @Headers({"Accept: application/json"})
    @GET("admin/dashboard/user-growth")
    Call<JsonObject> getAdminUserGrowth();

    @Headers({"Accept: application/json"})
    @GET("admin/dashboard/system-status")
    Call<JsonObject> getAdminSystemStatus();

    @Headers({"Accept: application/json"})
    @GET("admin/dashboard/statistics")
    Call<JsonObject> getAdminStatistics();

    @Headers({"Accept: application/json"})
    @GET("admin/dashboard/annual-analytics")
    Call<JsonObject> getAnnualAnalytics(
            @retrofit2.http.Query("academic_year_id") String academicYearId,
            @retrofit2.http.Query("compare_year_id") String compareYearId
    );

    @Headers({"Accept: application/json"})
    @GET("admin/users")
    Call<JsonObject> getUsers(
            @retrofit2.http.Query("role") String role,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @Headers({"Accept: application/json"})
    @GET("admin/users/statistics")
    Call<JsonObject> getUserStatistics();

    // ---- Student Bulk Import & Management ----
    @GET("admin/students/lookups")
    Call<JsonObject> getStudentLookups();

    @POST("admin/students")
    Call<JsonObject> addStudentUniversity(@Body JsonObject body);

    @GET("admin/students/import/template")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> downloadImportTemplateXlsx();

    @GET("admin/students/import/template-csv")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> downloadImportTemplateCsv();

    @retrofit2.http.Multipart
    @POST("admin/students/import/preview")
    Call<JsonObject> uploadImportPreview(
            @retrofit2.http.Part okhttp3.MultipartBody.Part file
    );

    @POST("admin/students/import/{id}/confirm")
    Call<JsonObject> confirmStudentImport(@retrofit2.http.Path("id") int id);

    @GET("admin/students/import/{id}/status")
    Call<JsonObject> getStudentImportStatus(@retrofit2.http.Path("id") int id);

    @POST("admin/students/import/{id}/cancel")
    Call<JsonObject> cancelStudentImport(@retrofit2.http.Path("id") int id);

    @GET("admin/students/imports")
    Call<JsonObject> getStudentImports(
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @GET("admin/students/imports/{id}/errors")
    Call<JsonObject> getStudentImportErrors(
            @retrofit2.http.Path("id") int id,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @GET("admin/students/imports/{id}/error-report")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> downloadImportErrorReport(@retrofit2.http.Path("id") int id);

    @POST("admin/students/export")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> exportStudents(@Body JsonObject filters);

    // ---- Faculty Evaluators ----
    @GET("faculty-evaluators/statistics")
    Call<JsonObject> getFacultyStatistics();

    @GET("faculty-evaluators")
    Call<JsonObject> getFacultyEvaluators(
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage);

    @GET("faculty-evaluators/{id}/courses")
    Call<JsonObject> getFacultyCourses(@retrofit2.http.Path("id") int id);

    @GET("faculty-evaluators/{id}/classes")
    Call<JsonObject> getFacultyClasses(@retrofit2.http.Path("id") int id);

    @POST("faculty-evaluators/{id}/assign-courses")
    Call<JsonObject> assignFacultyCourses(@retrofit2.http.Path("id") int id, @Body JsonObject body);

    @POST("faculty-evaluators/{id}/assign-classes")
    Call<JsonObject> assignFacultyClasses(@retrofit2.http.Path("id") int id, @Body JsonObject body);

    @retrofit2.http.HTTP(method = "DELETE", path = "faculty-evaluators/{id}/assignments", hasBody = true)
    Call<JsonObject> removeFacultyAssignments(@retrofit2.http.Path("id") int id, @Body JsonObject body);

    @POST("faculty-evaluators/{id}/reviews")
    Call<JsonObject> submitFacultyReview(@retrofit2.http.Path("id") int id, @Body JsonObject body);

    @GET("courses")
    Call<JsonObject> getCoursesCatalog();

    @GET("classes")
    Call<JsonObject> getClassesCatalog();

    @Headers({"Accept: application/json"})
    @GET("admin/users/{id}")
    Call<JsonObject> getUserDetails(@retrofit2.http.Path("id") String userId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/users")
    Call<JsonObject> createUser(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("admin/users/{id}")
    Call<JsonObject> updateUser(@retrofit2.http.Path("id") String userId, @Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PATCH("admin/users/{id}/status")
    Call<JsonObject> updateUserStatus(@retrofit2.http.Path("id") String userId, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("admin/users/{id}")
    Call<JsonObject> deleteUser(@retrofit2.http.Path("id") String userId);

    @Headers({"Accept: application/json"})
    @GET("students")
    Call<JsonObject> getStudents();

    @Headers({"Accept: application/json"})
    @GET("teachers")
    Call<JsonObject> getTeachers();

    @Headers({"Accept: application/json"})
    @GET("admins")
    Call<JsonObject> getAdmins();

    @Headers({"Accept: application/json"})
    @GET("departments")
    Call<JsonObject> getDepartments();

    @Headers({"Accept: application/json"})
    @GET("classes")
    Call<JsonObject> getClasses();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("profile")
    Call<JsonObject> updateProfile(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("change-password")
    Call<JsonObject> changePassword(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("students")
    Call<JsonObject> createStudent(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teachers")
    Call<JsonObject> createTeacher(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("assignments")
    Call<JsonObject> createAssignment(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("assignment-submissions")
    Call<JsonObject> submitAssignment(@Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("quizzes/{id}")
    Call<JsonObject> getQuizDetails(@retrofit2.http.Path("id") String quizId);

    @Headers({"Accept: application/json"})
    @GET("assignments/{id}")
    Call<JsonObject> getAssignmentDetails(@retrofit2.http.Path("id") String assignmentId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("quiz-attempts")
    Call<JsonObject> startQuizAttempt(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("quiz-answers")
    Call<JsonObject> submitQuizAnswer(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("quiz-attempts/{id}")
    Call<JsonObject> updateQuizAttempt(@retrofit2.http.Path("id") String attemptId, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("assignment-submissions")
    Call<JsonObject> getAssignmentSubmissions(
            @retrofit2.http.Query("assignment_id") String assignmentId,
            @retrofit2.http.Query("student_id") String studentId
    );
}