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
    @POST("logout")
    Call<JsonObject> logout();

    @Headers({"Accept: application/json"})
    @GET("dashboard/student")
    Call<DashboardResponse> getStudentDashboard();

    @Headers({"Accept: application/json"})
    @GET("dashboard/student")
    Call<JsonObject> getStudentDashboard(
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("course_id") Integer courseId
    );

    @Headers({"Accept: application/json"})
    @GET("dashboard/student")
    Call<DashboardResponse> getStudentDashboardFiltered(
            @retrofit2.http.Query("semester_id") Long semesterId,
            @retrofit2.http.Query("course_id") Long courseId
    );

    @Headers({"Accept: application/json"})
    @GET("dashboard/student")
    Call<DashboardResponse> getStudentDashboardBySemester(
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("course_id") Integer courseId
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
    @GET("student/my-analytics")
    Call<LearningAnalyticsResponse> getLearningAnalyticsTrend(
            @retrofit2.http.Query("period_type") String periodType,
            @retrofit2.http.Query("semester_id") Long semesterId
    );

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
    @GET("recommendations")
    Call<JsonObject> getRecommendations(@retrofit2.http.Query("semester_id") Integer semesterId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("recommendations/{id}")
    Call<JsonObject> updateRecommendation(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("notifications")
    Call<JsonObject> getNotifications();

    @Headers({"Accept: application/json"})
    @GET("notifications/unread-count")
    Call<JsonObject> getUnreadNotificationsCount(@retrofit2.http.Header("Authorization") String token);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("notifications")
    Call<JsonObject> createNotification(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PATCH("notifications/{id}/read")
    Call<JsonObject> markNotificationRead(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @POST("notifications/read-all")
    Call<JsonObject> markAllNotificationsRead();

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("notifications/{id}")
    Call<JsonObject> deleteNotification(@retrofit2.http.Path("id") String id);

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
    @GET("student/attendance")
    Call<JsonObject> getStudentAttendanceByCourse(
            @retrofit2.http.Query("semester_id") Long semesterId,
            @retrofit2.http.Query("course_id") Long courseId
    );

    @Headers({"Accept: application/json"})
    @GET("student/assignments")
    Call<JsonObject> getStudentAssignments(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/assignments")
    Call<JsonObject> getStudentAssignmentsByCourse(
            @retrofit2.http.Query("semester_id") Long semesterId,
            @retrofit2.http.Query("course_id") Long courseId
    );

    @Headers({"Accept: application/json"})
    @GET("student/quizzes")
    Call<JsonObject> getStudentQuizzes(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/quizzes")
    Call<JsonObject> getStudentQuizzesByCourse(
            @retrofit2.http.Query("semester_id") Long semesterId,
            @retrofit2.http.Query("course_id") Long courseId
    );

    @Headers({"Accept: application/json"})
    @GET("student/exams")
    Call<JsonObject> getStudentExams(@retrofit2.http.Query("semester_id") Long semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/exams")
    Call<JsonObject> getStudentExamsByCourse(
            @retrofit2.http.Query("semester_id") Long semesterId,
            @retrofit2.http.Query("course_id") Long courseId
    );

    // ---- Student Module APIs (Phase 2-9) ----
    @Headers({"Accept: application/json"})
    @GET("student/module/dashboard")
    Call<JsonObject> getStudentModuleDashboard();

    @Headers({"Accept: application/json"})
    @GET("student/module/academic-journey")
    Call<JsonObject> getStudentAcademicJourney();

    @Headers({"Accept: application/json"})
    @GET("student/module/courses")
    Call<JsonObject> getStudentModuleCourses(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("study_year") Integer studyYear
    );

    @Headers({"Accept: application/json"})
    @GET("student/module/courses/{id}")
    Call<JsonObject> getStudentModuleCourseDetail(@retrofit2.http.Path("id") Integer id);

    @Headers({"Accept: application/json"})
    @GET("student/module/attendance")
    Call<JsonObject> getStudentModuleAttendanceOverview(@retrofit2.http.Query("semester_id") Integer semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/module/attendance/courses/{id}")
    Call<JsonObject> getStudentModuleAttendanceCourse(@retrofit2.http.Path("id") Integer id);

    @Headers({"Accept: application/json"})
    @GET("student/module/grades")
    Call<JsonObject> getStudentModuleGradesOverview(@retrofit2.http.Query("semester_id") Integer semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/module/grades/courses/{id}")
    Call<JsonObject> getStudentModuleGradesCourse(@retrofit2.http.Path("id") Integer id);

    @Headers({"Accept: application/json"})
    @GET("student/module/analytics")
    Call<JsonObject> getStudentModuleAnalytics(@retrofit2.http.Query("semester_id") Integer semesterId);

    @Headers({"Accept: application/json"})
    @GET("student/module/recommendations")
    Call<JsonObject> getStudentModuleRecommendations();

    @Headers({"Accept: application/json"})
    @GET("student/module/predictions-risk")
    Call<JsonObject> getStudentModulePredictionsAndRisk();

    @Headers({"Accept: application/json"})
    @GET("student/module/comparisons")
    Call<JsonObject> getStudentModuleComparisons();

    @Headers({"Accept: application/json"})
    @GET("student/module/gpa-overview")
    Call<JsonObject> getStudentGpaOverview();

    @Headers({"Accept: application/json"})
    @GET("student/module/gpa-history")
    Call<JsonObject> getStudentGpaHistory(@retrofit2.http.Query("study_year") Integer studyYear);

    @Headers({"Accept: application/json"})
    @GET("student/module/progression-status")
    Call<JsonObject> getStudentProgressionStatus();

    @Headers({"Accept: application/json"})
    @GET("student/module/retakes")
    Call<JsonObject> getStudentRetakes();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("student/module/retakes/enroll")
    Call<JsonObject> enrollStudentRetake(@Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("dashboard/teacher")
    Call<JsonObject> getTeacherDashboard();

    @Headers({"Accept: application/json"})
    @GET("dashboard/teacher")
    Call<JsonObject> getTeacherDashboardFiltered(
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("semester_id") Integer semesterId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/students")
    Call<JsonObject> getTeacherStudents(
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("risk_level") String riskLevel,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/students")
    Call<JsonObject> getTeacherStudents(
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("risk_level") String riskLevel,
            @retrofit2.http.Query("year_level") String yearLevel,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/students/{studentId}")
    Call<JsonObject> getTeacherStudentDetail(
            @retrofit2.http.Path("studentId") String studentId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/attendance-overview")
    Call<JsonObject> getTeacherAttendanceOverview(
            @retrofit2.http.Query("course_offering_id") Integer offeringId
    );

    @Headers({"Accept: application/json"})
    @POST("teacher/course-offerings/{id}/attendance")
    Call<JsonObject> submitTeacherAttendance(
            @retrofit2.http.Path("id") int offeringId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/assessments-overview")
    Call<JsonObject> getTeacherAssessmentsOverview(
            @retrofit2.http.Query("course_id") Integer courseId
    );

    @Headers({"Accept: application/json"})
    @POST("teacher/course-offerings/{id}/assignments")
    Call<JsonObject> createTeacherAssignment(
            @retrofit2.http.Path("id") int offeringId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.PUT("teacher/assignments/{assignmentId}")
    Call<JsonObject> updateTeacherAssignment(
            @retrofit2.http.Path("assignmentId") int assignmentId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/assignments/{assignmentId}")
    Call<JsonObject> deleteTeacherAssignment(
            @retrofit2.http.Path("assignmentId") int assignmentId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/submissions/{submissionId}")
    Call<JsonObject> getTeacherSubmissionDetail(
            @retrofit2.http.Path("submissionId") int submissionId
    );

    @retrofit2.http.Streaming
    @GET("teacher/submissions/{submissionId}/file")
    Call<okhttp3.ResponseBody> downloadTeacherSubmissionFile(
            @retrofit2.http.Path("submissionId") int submissionId,
            @retrofit2.http.Query("download") Integer download
    );

    @Headers({"Accept: application/json"})
    @GET("student/submissions")
    Call<JsonObject> getStudentSubmissions();

    @Headers({"Accept: application/json"})
    @GET("student/submissions/{submissionId}")
    Call<JsonObject> getStudentSubmissionDetail(
            @retrofit2.http.Path("submissionId") int submissionId
    );

    @retrofit2.http.Streaming
    @GET("student/submissions/{submissionId}/file")
    Call<okhttp3.ResponseBody> downloadStudentSubmissionFile(
            @retrofit2.http.Path("submissionId") int submissionId,
            @retrofit2.http.Query("download") Integer download
    );

    @Headers({"Accept: application/json"})
    @POST("teacher/submissions/{submissionId}/grade")
    Call<JsonObject> gradeTeacherSubmission(
            @retrofit2.http.Path("submissionId") int submissionId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @POST("teacher/course-offerings/{id}/quizzes")
    Call<JsonObject> createTeacherQuiz(
            @retrofit2.http.Path("id") int offeringId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @POST("teacher/quizzes/{quizId}/questions")
    Call<JsonObject> addTeacherQuizQuestion(
            @retrofit2.http.Path("quizId") int quizId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.PATCH("teacher/quizzes/{quizId}/publish")
    Call<JsonObject> publishTeacherQuiz(
            @retrofit2.http.Path("quizId") int quizId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/teaching-assignments")
    Call<JsonObject> getTeacherTeachingAssignments();

    @Headers({"Accept: application/json"})
    @GET("teacher/course-offerings/{id}/exams")
    Call<JsonObject> getTeacherOfferingExams(@retrofit2.http.Path("id") int offeringId);

    @Headers({"Accept: application/json"})
    @POST("teacher/course-offerings/{id}/exams")
    Call<JsonObject> createTeacherExam(
            @retrofit2.http.Path("id") int offeringId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.PUT("teacher/exams/{examId}")
    Call<JsonObject> updateTeacherExam(
            @retrofit2.http.Path("examId") int examId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/exams/{examId}")
    Call<JsonObject> deleteTeacherExam(@retrofit2.http.Path("examId") int examId);

    @Headers({"Accept: application/json"})
    @GET("teacher/exams/{examId}")
    Call<JsonObject> getTeacherExamDetail(@retrofit2.http.Path("examId") int examId);

    @Headers({"Accept: application/json"})
    @POST("teacher/exams/{examId}/scores")
    Call<JsonObject> storeTeacherExamScores(
            @retrofit2.http.Path("examId") int examId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.PATCH("teacher/exams/{examId}/publish")
    Call<JsonObject> publishTeacherExam(@retrofit2.http.Path("examId") int examId);

    @Headers({"Accept: application/json"})
    @retrofit2.http.PATCH("teacher/exams/{examId}/unpublish")
    Call<JsonObject> unpublishTeacherExam(@retrofit2.http.Path("examId") int examId);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/exams/{examId}")
    Call<JsonObject> deleteTeacherExamForce(
            @retrofit2.http.Path("examId") int examId,
            @retrofit2.http.Query("force") boolean force
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/quizzes/{quizId}")
    Call<JsonObject> getTeacherQuizDetail(@retrofit2.http.Path("quizId") int quizId);

    @Headers({"Accept: application/json"})
    @retrofit2.http.PUT("teacher/quizzes/{quizId}")
    Call<JsonObject> updateTeacherQuiz(
            @retrofit2.http.Path("quizId") int quizId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/quizzes/{quizId}")
    Call<JsonObject> deleteTeacherQuiz(@retrofit2.http.Path("quizId") int quizId);

    @Headers({"Accept: application/json"})
    @retrofit2.http.PUT("teacher/quiz-questions/{questionId}")
    Call<JsonObject> updateTeacherQuizQuestion(
            @retrofit2.http.Path("questionId") int questionId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/quiz-questions/{questionId}")
    Call<JsonObject> deleteTeacherQuizQuestion(@retrofit2.http.Path("questionId") int questionId);

    @Headers({"Accept: application/json"})
    @POST("teacher/quiz-questions/{questionId}/duplicate")
    Call<JsonObject> duplicateTeacherQuizQuestion(@retrofit2.http.Path("questionId") int questionId);

    @Headers({"Accept: application/json"})
    @retrofit2.http.PUT("teacher/quizzes/{quizId}/questions/reorder")
    Call<JsonObject> reorderTeacherQuizQuestions(
            @retrofit2.http.Path("quizId") int quizId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/quizzes/{quizId}/attempts")
    Call<JsonObject> getTeacherQuizAttempts(@retrofit2.http.Path("quizId") int quizId);

    @Headers({"Accept: application/json"})
    @GET("teacher/quiz-attempts/{attemptId}")
    Call<JsonObject> getTeacherQuizAttemptDetail(@retrofit2.http.Path("attemptId") int attemptId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PATCH("teacher/quiz-answers/{answerId}/grade")
    Call<JsonObject> gradeTeacherQuizAnswer(
            @retrofit2.http.Path("answerId") int answerId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/quiz-attempts/{attemptId}/grade")
    Call<JsonObject> gradeTeacherQuizAttempt(
            @retrofit2.http.Path("attemptId") int attemptId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.PATCH("teacher/quizzes/{quizId}/unpublish")
    Call<JsonObject> unpublishTeacherQuiz(@retrofit2.http.Path("quizId") int quizId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/quizzes/{quizId}/reopen")
    Call<JsonObject> reopenTeacherQuiz(
            @retrofit2.http.Path("quizId") int quizId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/quizzes/{quizId}/students")
    Call<JsonObject> getTeacherQuizStudents(@retrofit2.http.Path("quizId") int quizId);

    @Headers({"Accept: application/json"})
    @GET("teacher/quizzes/{quizId}/overrides")
    Call<JsonObject> getTeacherQuizOverrides(@retrofit2.http.Path("quizId") int quizId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/quizzes/{quizId}/overrides")
    Call<JsonObject> grantTeacherQuizOverride(
            @retrofit2.http.Path("quizId") int quizId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/quizzes/{quizId}/overrides/{studentId}")
    Call<JsonObject> revokeTeacherQuizOverride(
            @retrofit2.http.Path("quizId") int quizId,
            @retrofit2.http.Path("studentId") int studentId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/quizzes/{quizId}/audit-logs")
    Call<JsonObject> getTeacherQuizAuditLogs(@retrofit2.http.Path("quizId") int quizId);

    // -------------------------------------------------------------------------
    // Universal Reopen, Retake, Late Submission, History & Audit Logs
    // -------------------------------------------------------------------------

    @Headers({"Accept: application/json"})
    @GET("teacher/assessments/{assessmentType}/{assessmentId}/reopen-status")
    Call<JsonObject> getTeacherAssessmentReopenStatus(
            @retrofit2.http.Path("assessmentType") String assessmentType,
            @retrofit2.http.Path("assessmentId") int assessmentId,
            @retrofit2.http.Query("student_id") Integer studentId
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/assessments/reopen")
    Call<JsonObject> reopenTeacherAssessment(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/assessments/extend-deadline")
    Call<JsonObject> extendTeacherAssessmentDeadline(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/assessments/grant-attempts")
    Call<JsonObject> grantTeacherAssessmentAttempts(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("teacher/assessments/revoke-reopen")
    Call<JsonObject> revokeTeacherAssessmentReopen(@Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teacher/assessments/{assessmentType}/{assessmentId}/overrides/{studentId}")
    Call<JsonObject> deleteTeacherAssessmentOverride(
            @retrofit2.http.Path("assessmentType") String assessmentType,
            @retrofit2.http.Path("assessmentId") int assessmentId,
            @retrofit2.http.Path("studentId") int studentId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/assessments/{assessmentType}/{assessmentId}/submission-history")
    Call<JsonObject> getTeacherAssessmentSubmissionHistory(
            @retrofit2.http.Path("assessmentType") String assessmentType,
            @retrofit2.http.Path("assessmentId") int assessmentId,
            @retrofit2.http.Query("student_id") Integer studentId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/assessments/quiz/{assessmentId}/attempt-history")
    Call<JsonObject> getTeacherQuizAttemptHistory(
            @retrofit2.http.Path("assessmentId") int assessmentId,
            @retrofit2.http.Query("student_id") Integer studentId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/assessments/audit-logs")
    Call<JsonObject> getTeacherAssessmentAuditLogs(
            @retrofit2.http.QueryMap java.util.Map<String, String> filters
    );

    @Headers({"Accept: application/json"})
    @GET("student/assessments/{assessmentType}/{assessmentId}/reopen-status")
    Call<JsonObject> getStudentAssessmentReopenStatus(
            @retrofit2.http.Path("assessmentType") String assessmentType,
            @retrofit2.http.Path("assessmentId") int assessmentId
    );

    @Headers({"Accept: application/json"})
    @GET("student/assessments/{assessmentType}/{assessmentId}/submission-history")
    Call<JsonObject> getStudentAssessmentSubmissionHistory(
            @retrofit2.http.Path("assessmentType") String assessmentType,
            @retrofit2.http.Path("assessmentId") int assessmentId
    );

    @Headers({"Accept: application/json"})
    @GET("student/assessments/quiz/{assessmentId}/attempt-history")
    Call<JsonObject> getStudentQuizAttemptHistory(
            @retrofit2.http.Path("assessmentId") int assessmentId
    );

    @Headers({"Accept: application/json"})
    @GET("student/quizzes/{id}")
    Call<JsonObject> getStudentQuizDetail(@retrofit2.http.Path("id") String quizId);

    @Headers({"Accept: application/json"})
    @POST("student/quizzes/{id}/start")
    Call<JsonObject> startStudentQuiz(@retrofit2.http.Path("id") String quizId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("student/quiz-attempts/{attemptId}/answers")
    Call<JsonObject> saveStudentQuizAnswers(
            @retrofit2.http.Path("attemptId") String attemptId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("student/quiz-attempts/{attemptId}/submit")
    Call<JsonObject> submitStudentQuiz(
            @retrofit2.http.Path("attemptId") String attemptId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @GET("student/quiz-attempts/{attemptId}/review")
    Call<JsonObject> reviewStudentQuizAttempt(@retrofit2.http.Path("attemptId") String attemptId);

    @Headers({"Accept: application/json"})
    @POST("teacher/course-offerings/{id}/scores")
    Call<JsonObject> submitTeacherScore(
            @retrofit2.http.Path("id") int offeringId,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/risk-predictions")
    Call<JsonObject> getTeacherRiskPredictions();

    @Headers({"Accept: application/json"})
    @POST("teacher/recommendations")
    Call<JsonObject> createTeacherRecommendation(
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.PATCH("teacher/recommendations/{id}")
    Call<JsonObject> updateTeacherRecommendation(
            @retrofit2.http.Path("id") int id,
            @retrofit2.http.Body JsonObject body
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/dashboard")
    Call<com.social7.studentlearnanalytics.model.TeacherDashboardSummaryResponse> getTeacherDashboardSummary(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/filters")
    Call<com.social7.studentlearnanalytics.model.TeacherFiltersResponse> getTeacherFilters(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId
    );

    @Headers({"Accept: application/json"})
    @GET("teacher/course-offerings")
    Call<JsonObject> getTeacherCourseOfferings();

    @Headers({"Accept: application/json"})
    @GET("teacher/analytics")
    Call<com.social7.studentlearnanalytics.model.TeacherAnalyticsResponse> getTeacherAnalytics(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("class_id") Integer classId,
            @retrofit2.http.Query("program_id") Integer programId,
            @retrofit2.http.Query("study_year") Integer studyYear
    );

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

    @GET("admin/analytics/enrollment")
    Call<JsonObject> getAdminAnalyticsEnrollment();

    @GET("admin/analytics/enrollment")
    Call<JsonObject> getAdminAnalyticsEnrollment(@retrofit2.http.Query("academic_year_id") Integer academicYearId);

    @GET("admin/analytics/performance")
    Call<JsonObject> getAdminAnalyticsPerformance();

    @GET("admin/analytics/performance")
    Call<JsonObject> getAdminAnalyticsPerformance(@retrofit2.http.Query("academic_year_id") Integer academicYearId);

    @GET("admin/analytics/attendance")
    Call<JsonObject> getAdminAnalyticsAttendance();

    @GET("admin/analytics/attendance")
    Call<JsonObject> getAdminAnalyticsAttendance(@retrofit2.http.Query("academic_year_id") Integer academicYearId);

    @GET("admin/analytics/student-risk")
    Call<JsonObject> getAdminAnalyticsStudentRisk();

    @GET("admin/analytics/student-risk")
    Call<JsonObject> getAdminAnalyticsStudentRisk(@retrofit2.http.Query("academic_year_id") Integer academicYearId);

    @GET("admin/analytics/model-evaluation")
    Call<JsonObject> getAdminModelEvaluation();

    @Headers({"Accept: application/json"})
    @GET("incidents")
    Call<JsonObject> getIncidents(
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("priority") String priority,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json"})
    @POST("incidents")
    Call<JsonObject> createIncident(@retrofit2.http.Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.PUT("incidents/{id}")
    Call<JsonObject> updateIncident(
            @retrofit2.http.Path("id") long id,
            @retrofit2.http.Body JsonObject body
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
    @GET("admin/users")
    Call<JsonObject> getUsers(
            @retrofit2.http.Query("role") String role,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("department") String department,
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @Headers({"Accept: application/json"})
    @GET("admin/users")
    Call<JsonObject> getUsers(
            @retrofit2.http.Query("role") String role,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("department") String department,
            @retrofit2.http.Query("year_level") String yearLevel,
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

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/students/{student}/reset-password")
    Call<JsonObject> resetStudentPassword(@retrofit2.http.Path("student") String studentIdOrCode, @Body JsonObject body);

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

    @GET("admin/students/imports/{id}/rows")
    Call<JsonObject> getStudentImportRows(
            @retrofit2.http.Path("id") int id,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @GET("admin/students/imports/{id}/error-report")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> downloadImportErrorReport(@retrofit2.http.Path("id") int id);

    @GET("admin/students/imports/{id}/credentials")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> downloadImportCredentials(@retrofit2.http.Path("id") int id);

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

    @Headers({"Accept: application/json"})
    @GET("courses")
    Call<JsonObject> getCoursesCatalogFiltered(
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("program_id") Integer programId,
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId
    );

    @Headers({"Accept: application/json"})
    @GET("courses")
    Call<JsonObject> getCoursesCatalogFiltered(
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("program_id") Integer programId,
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("year_level") String yearLevel
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PATCH("courses/{id}/status")
    Call<JsonObject> updateCourseStatus(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("courses")
    Call<JsonObject> createCourse(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("courses/{id}")
    Call<JsonObject> updateCourse(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("courses/{id}")
    Call<JsonObject> deleteCourse(@retrofit2.http.Path("id") String id);

    @GET("classes")
    Call<JsonObject> getClassesCatalog();

    @Headers({"Accept: application/json"})
    @GET("classes")
    Call<JsonObject> getClassesCatalogFiltered(
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("year_level") String yearLevel,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("classes")
    Call<JsonObject> createClass(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("classes/{id}")
    Call<JsonObject> updateClass(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("classes/{id}")
    Call<JsonObject> deleteClass(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET("admin/users/{id}")
    Call<JsonObject> getUserDetails(@retrofit2.http.Path("id") String userId);

    @Headers({"Accept: application/json"})
    @GET("admin/users/{id}")
    Call<JsonObject> getUser(@retrofit2.http.Path("id") String userId);

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
    @GET("admin/users/{id}/notes")
    Call<JsonObject> getStudentNotes(@retrofit2.http.Path("id") String userId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/users/{id}/notes")
    Call<JsonObject> createStudentNote(@retrofit2.http.Path("id") String userId, @Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("admin/users/{id}/notes/{noteId}")
    Call<JsonObject> updateStudentNote(@retrofit2.http.Path("id") String userId, @retrofit2.http.Path("noteId") long noteId, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("admin/users/{id}/notes/{noteId}")
    Call<JsonObject> deleteStudentNote(@retrofit2.http.Path("id") String userId, @retrofit2.http.Path("noteId") long noteId);

    @Headers({"Accept: application/json"})
    @GET("students")
    Call<JsonObject> getStudents();

    @Headers({"Accept: application/json"})
    @GET("teachers")
    Call<JsonObject> getTeachers();

    @Headers({"Accept: application/json"})
    @GET("teachers")
    Call<JsonObject> getTeachersPaged(
            @retrofit2.http.Query("status") String status,
            @retrofit2.http.Query("department") String department,
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @Headers({"Accept: application/json"})
    @GET("teachers/{id}")
    Call<JsonObject> getTeacher(@retrofit2.http.Path("id") String teacherId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/teachers/{id}/provision-account")
    Call<JsonObject> provisionTeacherAccount(@retrofit2.http.Path("id") String teacherDbId, @Body JsonObject body);

    @GET("admin/teachers/import/template")
    @retrofit2.http.Streaming
    Call<okhttp3.ResponseBody> downloadTeacherImportTemplate();

    @retrofit2.http.Multipart
    @Headers({"Accept: application/json"})
    @POST("admin/teachers/import")
    Call<JsonObject> importTeachers(@retrofit2.http.Part okhttp3.MultipartBody.Part file);

    @Headers({"Accept: application/json"})
    @GET("admin/teachers/lookups")
    Call<JsonObject> getTeacherLookups(@retrofit2.http.Query("search") String search);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/office-locations")
    Call<JsonObject> createOfficeLocation(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/specializations")
    Call<JsonObject> createSpecialization(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("teachers/{id}")
    Call<JsonObject> updateTeacher(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("teachers/{id}")
    Call<JsonObject> deleteTeacher(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET("admins")
    Call<JsonObject> getAdmins();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admins")
    Call<JsonObject> createAdmin(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("admins/{id}")
    Call<JsonObject> updateAdmin(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("admins/{id}")
    Call<JsonObject> deleteAdmin(@retrofit2.http.Path("id") String id);

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
    @retrofit2.http.Multipart
    @POST("assignment-submissions")
    Call<JsonObject> submitAssignmentMultipart(
            @retrofit2.http.Part("assignment_id") okhttp3.RequestBody assignmentId,
            @retrofit2.http.Part("file_path") okhttp3.RequestBody filePath,
            @retrofit2.http.Part okhttp3.MultipartBody.Part submissionFile
    );

    @Headers({"Accept: application/json"})
    @retrofit2.http.Multipart
    @POST("assignment-submissions")
    Call<JsonObject> submitAssignmentMultipartWithText(
            @retrofit2.http.Part("assignment_id") okhttp3.RequestBody assignmentId,
            @retrofit2.http.Part("file_path") okhttp3.RequestBody filePath,
            @retrofit2.http.Part("submission_text") okhttp3.RequestBody submissionText,
            @retrofit2.http.Part okhttp3.MultipartBody.Part submissionFile
    );

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

    @Headers({"Accept: application/json"})
    @GET("admin/class-enrollments")
    Call<JsonObject> getClassEnrollments(
            @retrofit2.http.Query("class_id") Integer classId,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json"})
    @GET("admin/class-enrollments/lookups")
    Call<JsonObject> getClassEnrollmentLookups();

    @Headers({"Accept: application/json"})
    @GET("admin/class-enrollments/classes/{classId}/students")
    Call<JsonObject> getClassEligibleStudents(
            @retrofit2.http.Path("classId") int classId,
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("program_id") Integer programId,
            @retrofit2.http.Query("year_level") Integer yearLevel,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/class-enrollments/classes/{classId}/bulk-enroll")
    Call<JsonObject> bulkEnrollStudents(
            @retrofit2.http.Path("classId") int classId,
            @Body JsonObject body
    );

    @retrofit2.http.Multipart
    @Headers({"Accept: application/json"})
    @POST("admin/class-enrollments/import/preview")
    Call<JsonObject> previewClassEnrollmentImport(@retrofit2.http.Part okhttp3.MultipartBody.Part file);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/class-enrollments/import/confirm")
    Call<JsonObject> confirmClassEnrollmentImport(@Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/lookups")
    Call<JsonObject> getReportsAnalyticsLookups();

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/overview")
    Call<JsonObject> getReportsAnalyticsOverview(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId
    );

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/overview")
    Call<JsonObject> getReportsAnalyticsOverview(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("year_level") String yearLevel
    );

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/attendance")
    Call<JsonObject> getReportsAnalyticsAttendance(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("class_id") Integer classId
    );

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/attendance")
    Call<JsonObject> getReportsAnalyticsAttendance(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("class_id") Integer classId,
            @retrofit2.http.Query("year_level") String yearLevel
    );

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/teachers")
    Call<JsonObject> getReportsAnalyticsTeachers(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("search") String search,
            @retrofit2.http.Query("workload_status") String workloadStatus,
            @retrofit2.http.Query("sort_by") String sortBy
    );

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/courses")
    Call<JsonObject> getReportsAnalyticsCourses(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("course_id") Integer courseId
    );

    @Headers({"Accept: application/json"})
    @GET("admin/reports-analytics/courses")
    Call<JsonObject> getReportsAnalyticsCourses(
            @retrofit2.http.Query("academic_year_id") Integer academicYearId,
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("department_id") Integer departmentId,
            @retrofit2.http.Query("course_id") Integer courseId,
            @retrofit2.http.Query("year_level") String yearLevel
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("admin/reports-analytics/generate")
    Call<JsonObject> generateAdminReport(@Body JsonObject body);

    @retrofit2.http.Streaming
    @Headers({"Accept: */*", "Content-Type: application/json"})
    @POST("admin/reports-analytics/export")
    Call<okhttp3.ResponseBody> exportAdminReport(@Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("admin/academic-structure")
    Call<JsonObject> getAdminAcademicStructure();

    @Headers({"Accept: application/json"})
    @GET("classes/{id}")
    Call<JsonObject> getClassDetail(@retrofit2.http.Path("id") String classId);

    @Headers({"Accept: application/json"})
    @GET("buildings")
    Call<JsonObject> getBuildings();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("buildings")
    Call<JsonObject> createBuilding(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("buildings/{id}")
    Call<JsonObject> updateBuilding(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("buildings/{id}")
    Call<JsonObject> deleteBuilding(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET("floors")
    Call<JsonObject> getFloors(@retrofit2.http.Query("building_id") Integer buildingId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("floors")
    Call<JsonObject> createFloor(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("floors/{id}")
    Call<JsonObject> updateFloor(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("floors/{id}")
    Call<JsonObject> deleteFloor(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET("class-schedules")
    Call<JsonObject> getClassSchedules(
            @retrofit2.http.Query("class_id") Integer classId,
            @retrofit2.http.Query("day_of_week") String dayOfWeek,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json"})
    @GET("class-schedules")
    Call<JsonObject> getClassSchedules(
            @retrofit2.http.Query("class_id") Integer classId,
            @retrofit2.http.Query("day_of_week") String dayOfWeek,
            @retrofit2.http.Query("year_level") String yearLevel,
            @retrofit2.http.Query("search") String search
    );

    @Headers({"Accept: application/json"})
    @GET("my-schedules")
    Call<JsonObject> getMySchedules(@retrofit2.http.Query("semester_id") Integer semesterId);

    @Headers({"Accept: application/json"})
    @GET("my-schedules")
    Call<JsonObject> getMySchedules(
            @retrofit2.http.Query("semester_id") Integer semesterId,
            @retrofit2.http.Query("year_level") String yearLevel
    );

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("class-schedules")
    Call<JsonObject> createClassSchedule(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("class-schedules/{id}")
    Call<JsonObject> updateClassSchedule(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("class-schedules/{id}")
    Call<JsonObject> deleteClassSchedule(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET("academic-years")
    Call<JsonObject> getAcademicYears();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("academic-years")
    Call<JsonObject> createAcademicYear(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("academic-years/{id}")
    Call<JsonObject> updateAcademicYear(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("semesters")
    Call<JsonObject> getSemesters();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("semesters")
    Call<JsonObject> createSemester(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("semesters/{id}")
    Call<JsonObject> updateSemester(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("departments")
    Call<JsonObject> createDepartment(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("departments/{id}")
    Call<JsonObject> updateDepartment(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("departments/{id}")
    Call<JsonObject> deleteDepartment(@retrofit2.http.Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET("departments/{id}/programs")
    Call<JsonObject> getProgramsByDepartment(@retrofit2.http.Path("id") int departmentId);

    @Headers({"Accept: application/json"})
    @GET("programs")
    Call<JsonObject> getPrograms(@retrofit2.http.Query("department_id") Integer departmentId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("programs")
    Call<JsonObject> createProgram(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("programs/{id}")
    Call<JsonObject> updateProgram(@retrofit2.http.Path("id") int id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("programs/{id}")
    Call<JsonObject> deleteProgram(@retrofit2.http.Path("id") int id);

    @Headers({"Accept: application/json"})
    @GET("programs/{id}/courses")
    Call<JsonObject> getProgramCourses(@retrofit2.http.Path("id") int programId);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("courses/{id}/majors")
    Call<JsonObject> assignCourseMajors(@retrofit2.http.Path("id") int courseId, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @GET("course-categories")
    Call<JsonObject> getCourseCategories();

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("course-categories")
    Call<JsonObject> createCourseCategory(@Body JsonObject body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @retrofit2.http.PUT("course-categories/{id}")
    Call<JsonObject> updateCourseCategory(@retrofit2.http.Path("id") String id, @Body JsonObject body);

    @Headers({"Accept: application/json"})
    @retrofit2.http.DELETE("course-categories/{id}")
    Call<JsonObject> deleteCourseCategory(@retrofit2.http.Path("id") String id);
}
