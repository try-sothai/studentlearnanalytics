package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;
import java.util.List;

public class DashboardResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private DashboardData data;

    public DashboardData getData() {
        if (data == null) {
            data = new DashboardData();
        }
        return data;
    }

    public static class DashboardData {
        @SerializedName("student_name")
        private String studentName;

        @SerializedName("student_id")
        private String studentId;

        @SerializedName("academic_year")
        private String academicYear;

        @SerializedName("overall_performance")
        private Object overallPerformance;

        @SerializedName("attendance_rate")
        private Object attendanceRate;

        @SerializedName("assignment_score")
        private Object assignmentScore;

        @SerializedName("quiz_score")
        private Object quizScore;

        @SerializedName("exam_score")
        private Object examScore;

        @SerializedName("gpa")
        private Object gpa;

        @SerializedName("gpa_record")
        private GpaRecordInfo gpaRecord;

        @SerializedName("predicted_score")
        private Object predictedScore;

        @SerializedName("risk_level")
        private String riskLevel;

        @SerializedName("chart_data")
        private List<Float> chartData;

        @SerializedName("chart_labels")
        private List<String> chartLabels;

        @SerializedName("prediction_chart_data")
        private List<Float> predictionChartData;

        @SerializedName("prediction_chart_labels")
        private List<String> predictionChartLabels;

        // Nested objects from Laravel DashboardController::student
        @SerializedName("student")
        private StudentInfo student;

        @SerializedName("analytics")
        private AnalyticsInfo analytics;

        @SerializedName("risk_assessment")
        private RiskAssessmentInfo riskAssessment;

        @SerializedName("enrollments")
        private List<EnrollmentInfo> enrollments;

        @SerializedName("predictions")
        private List<PredictionInfo> predictions;

        @SerializedName("semesters")
        public List<SemesterInfo> semesters;

        @SerializedName("unread_notifications_count")
        public Integer unreadNotificationsCount;

        @SerializedName("attendance_summary")
        public AttendanceSummaryInfo attendanceSummary;

        @SerializedName("assignments_summary")
        public AssignmentsSummaryInfo assignmentsSummary;

        @SerializedName("quizzes_summary")
        public QuizzesSummaryInfo quizzesSummary;

        @SerializedName("exams_summary")
        public ExamsSummaryInfo examsSummary;

        @SerializedName("gpa_summary")
        public GpaSummaryInfo gpaSummary;

        @SerializedName("recent_activities")
        public List<RecentActivityInfo> recentActivities;

        public List<SemesterInfo> getSemesters() {
            return semesters;
        }

        public int getUnreadNotificationsCount() {
            return unreadNotificationsCount != null ? unreadNotificationsCount : 0;
        }

        public AttendanceSummaryInfo getAttendanceSummary() {
            return attendanceSummary;
        }

        public AssignmentsSummaryInfo getAssignmentsSummary() {
            return assignmentsSummary;
        }

        public QuizzesSummaryInfo getQuizzesSummary() {
            return quizzesSummary;
        }

        public ExamsSummaryInfo getExamsSummary() {
            return examsSummary;
        }

        public GpaSummaryInfo getGpaSummary() {
            return gpaSummary;
        }

        public List<RecentActivityInfo> getRecentActivities() {
            return recentActivities;
        }

        public AnalyticsInfo getAnalytics() {
            return analytics;
        }

        public RiskAssessmentInfo getRiskAssessment() {
            return riskAssessment;
        }

        public List<PredictionInfo> getPredictions() {
            return predictions;
        }

        public Double getPredictionConfidence() {
            if (predictions != null && !predictions.isEmpty() && predictions.get(0).confidenceScore != null) {
                try {
                    return Double.parseDouble(String.valueOf(predictions.get(0).confidenceScore).trim());
                } catch (Exception ignored) {}
            }
            return null;
        }

        public String getModelVersion() {
            if (predictions != null && !predictions.isEmpty() && predictions.get(0).modelVersion != null) {
                return predictions.get(0).modelVersion;
            }
            return null;
        }

        public Object getGpaValue() {
            if (gpa != null) return gpa;
            if (gpaSummary != null && gpaSummary.currentSemesterGpa != null) return gpaSummary.currentSemesterGpa;
            if (gpaSummary != null && gpaSummary.semesterGpa != null) return gpaSummary.semesterGpa;
            if (gpaSummary != null && gpaSummary.cumulativeGpa != null) return gpaSummary.cumulativeGpa;
            return gpaRecord != null ? gpaRecord.gpa : null;
        }

        public StudentInfo getStudent() {
            return student;
        }

        public String getStudentName() {
            if (student != null) {
                String name = student.getDisplayName();
                if (!name.isEmpty()) return name;
            }
            return studentName != null ? studentName : "";
        }

        public String getStudentId() {
            if (student != null && student.getStudentCode() != null && !student.getStudentCode().isEmpty()) {
                return student.getStudentCode();
            }
            return studentId != null ? studentId : "";
        }

        public String getMajorOrDepartment() {
            if (student != null) {
                String m = student.getMajorOrDepartment();
                if (!m.isEmpty()) return m;
            }
            return "";
        }

        public String getDepartment() {
            return getMajorOrDepartment();
        }

        public String getYearLevelFormatted() {
            if (student != null) {
                return student.getYearLevelFormatted();
            }
            return "Year 1";
        }

        public String getYearLevel() {
            return getYearLevelFormatted();
        }

        public String getYearLevelCode() {
            return student != null ? student.getYearLevelCode() : "";
        }

        public String getYearLevelLabel() {
            return student != null ? student.getYearLevelLabel() : getYearLevelFormatted();
        }

        public String getAcademicYear() {
            if (student != null && !student.getAcademicYear().isEmpty()) {
                return student.getAcademicYear();
            }
            return academicYear != null ? academicYear.trim() : "";
        }

        public String getOverallPerformanceFormatted() {
            Object obj = (analytics != null && analytics.overallScore != null) ? analytics.overallScore : overallPerformance;
            return formatPercentage(obj);
        }

        public String getAttendanceRateFormatted() {
            Object obj = (analytics != null && analytics.attendanceRate != null) ? analytics.attendanceRate : attendanceRate;
            if (obj == null && attendanceSummary != null && attendanceSummary.percentage != null) {
                obj = attendanceSummary.percentage;
            }
            return formatPercentage(obj);
        }

        public String getAssignmentScoreFormatted() {
            Object obj = (analytics != null && analytics.assignmentAvg != null) ? analytics.assignmentAvg : assignmentScore;
            if (obj == null && assignmentsSummary != null && assignmentsSummary.average != null) {
                obj = assignmentsSummary.average;
            }
            return formatPercentage(obj);
        }

        public String getQuizScoreFormatted() {
            Object obj = (analytics != null && analytics.quizAvg != null) ? analytics.quizAvg : quizScore;
            if (obj == null && quizzesSummary != null && quizzesSummary.average != null) {
                obj = quizzesSummary.average;
            }
            return formatPercentage(obj);
        }

        public String getExamScoreFormatted() {
            Object obj = (analytics != null && analytics.examAvg != null) ? analytics.examAvg : examScore;
            if (obj == null && examsSummary != null && examsSummary.average != null) {
                obj = examsSummary.average;
            }
            return formatPercentage(obj);
        }

        public String getGpaFormatted() {
            Object value = getGpaValue();
            if (value == null && analytics != null) value = analytics.gpa;
            if (value == null) return "Unavailable";
            String raw = String.valueOf(value).trim();
            if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) return "Unavailable";
            try {
                double parsed = Double.parseDouble(raw);
                if (Double.isNaN(parsed) || Double.isInfinite(parsed) || parsed < 0 || parsed > 4) {
                    return "Unavailable";
                }
                return String.format(java.util.Locale.US, "%.2f", parsed);
            } catch (NumberFormatException ignored) {
                return "Unavailable";
            }
        }

        public String getPredictedScoreFormatted() {
            Object scoreObj = null;
            if (predictions != null && !predictions.isEmpty() && predictions.get(0).predictedScore != null) {
                scoreObj = predictions.get(0).predictedScore;
            } else if (analytics != null && analytics.predictedScore != null) {
                scoreObj = analytics.predictedScore;
            } else if (predictedScore != null) {
                scoreObj = predictedScore;
            }

            if (scoreObj == null) {
                return "Not available yet";
            }
            String raw = String.valueOf(scoreObj).trim();
            if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) {
                return "Not available yet";
            }
            try {
                double d = Double.parseDouble(raw);
                if (d == (long) d) {
                    return ((long) d) + " / 100";
                } else {
                    return String.format(java.util.Locale.US, "%.1f / 100", d);
                }
            } catch (NumberFormatException ignored) {
                return raw;
            }
        }

        public String getRiskLevel() {
            if (riskAssessment != null && riskAssessment.riskLevel != null) {
                String r = riskAssessment.riskLevel.trim();
                if (!r.isEmpty() && !"UNKNOWN".equalsIgnoreCase(r)) {
                    return r.toUpperCase();
                }
            }
            if (riskLevel != null) {
                String r = riskLevel.trim();
                if (!r.isEmpty() && !"UNKNOWN".equalsIgnoreCase(r)) {
                    return r.toUpperCase();
                }
            }
            return "Not available yet";
        }

        public Double getRiskScoreVal() {
            if (riskAssessment != null && riskAssessment.riskScore != null) {
                try {
                    return Double.parseDouble(String.valueOf(riskAssessment.riskScore).trim());
                } catch (Exception ignored) {}
            }
            return null;
        }

        public String getRiskScoreFormatted() {
            Double score = getRiskScoreVal();
            if (score != null) {
                return String.format(java.util.Locale.US, "%.0f / 100", score);
            }
            return null;
        }

        public List<Float> getChartData() {
            return chartData;
        }

        public List<String> getChartLabels() {
            return chartLabels;
        }

        public List<Float> getPredictionChartData() {
            return predictionChartData;
        }

        public List<String> getPredictionChartLabels() {
            return predictionChartLabels;
        }

        public List<EnrollmentInfo> getEnrollments() {
            return enrollments;
        }

        private String formatPercentage(Object obj) {
            if (obj == null) return "N/A";
            String val = String.valueOf(obj).trim();
            if (val.isEmpty() || "null".equalsIgnoreCase(val)) return "N/A";
            try {
                double d = Double.parseDouble(val);
                if (d == (long) d) {
                    return ((long) d) + "%";
                } else {
                    return String.format(java.util.Locale.US, "%.1f%%", d);
                }
            } catch (NumberFormatException ignored) {
                return val;
            }
        }

        private String formatValue(Object obj, String defaultVal) {
            if (obj == null) return defaultVal;
            String val = String.valueOf(obj).trim();
            if (val.isEmpty() || "null".equalsIgnoreCase(val)) return defaultVal;
            if (obj instanceof Number) {
                double d = ((Number) obj).doubleValue();
                if (d == (long) d) {
                    return String.valueOf((long) d);
                } else {
                    return String.format(java.util.Locale.US, "%.1f", d);
                }
            }
            return val;
        }
    }

    public static class GpaRecordInfo {
        @SerializedName("gpa")
        public Object gpa;

        @SerializedName("total_credits")
        public Float totalCredits;

        @SerializedName("earned_credits")
        public Float earnedCredits;

        @SerializedName("academic_standing")
        public String academicStanding;
    }

    public static class StudentInfo {
        @SerializedName("first_name")
        private String firstName;

        @SerializedName("last_name")
        private String lastName;

        @SerializedName("student_code")
        private String studentCode;

        @SerializedName("student_id")
        private String studentId;

        @SerializedName("academic_year")
        private String academicYear;

        @SerializedName("major")
        private String major;

        @SerializedName("user")
        private UserInfo user;

        @SerializedName("department")
        private DepartmentInfo department;

        @SerializedName("year_level")
        private Object yearLevel;

        @SerializedName("year_level_code")
        private String yearLevelCode;

        @SerializedName("year_level_label")
        private String yearLevelLabel;

        @SerializedName("semester")
        private Object semester;

        public String getDisplayName() {
            if (user != null && user.name != null && !user.name.isEmpty()) {
                return user.name;
            }
            String full = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
            return full;
        }

        public String getStudentCode() {
            if (studentCode != null && !studentCode.trim().isEmpty()) {
                return studentCode.trim();
            }
            if (studentId != null && !studentId.trim().isEmpty()) {
                return studentId.trim();
            }
            return "";
        }

        public String getMajorOrDepartment() {
            if (major != null && !major.trim().isEmpty()) {
                return major.trim();
            }
            if (department != null && department.name != null && !department.name.trim().isEmpty()) {
                return department.name.trim();
            }
            return "";
        }

        public String getDepartmentName() {
            return getMajorOrDepartment();
        }

        public String getYearLevelFormatted() {
            Integer lvl = YearLevelHelper.parse(yearLevel);
            if (lvl != null) {
                return "Year " + lvl;
            }
            return "Year 1";
        }

        public String getYearLevel() {
            return getYearLevelFormatted();
        }

        public Integer getYearLevelNumber() {
            return YearLevelHelper.parse(yearLevel);
        }

        public String getYearLevelCode() {
            if (yearLevelCode != null && !yearLevelCode.trim().isEmpty()) {
                return yearLevelCode.trim();
            }
            String code = YearLevelHelper.toCode(yearLevel);
            return code != null ? code : "";
        }

        public String getYearLevelLabel() {
            if (yearLevelLabel != null && !yearLevelLabel.trim().isEmpty()) {
                return yearLevelLabel.trim();
            }
            return getYearLevelFormatted();
        }

        public String getAcademicYear() {
            return academicYear != null ? academicYear.trim() : "";
        }
    }

    public static class UserInfo {
        @SerializedName("name")
        public String name;

        @SerializedName("email")
        public String email;
    }

    public static class DepartmentInfo {
        @SerializedName("name")
        public String name;
    }

    public static class AnalyticsInfo {
        @SerializedName("overall_performance")
        public Object overallScore;

        @SerializedName("attendance_rate")
        public Object attendanceRate;

        @SerializedName("assignment_average")
        public Object assignmentAvg;

        @SerializedName("quiz_average")
        public Object quizAvg;

        @SerializedName("exam_average")
        public Object examAvg;

        @SerializedName("gpa")
        public Object gpa;

        @SerializedName("predicted_score")
        public Object predictedScore;

        @SerializedName("learning_trend")
        public String learningTrend;

        @SerializedName("performance_change")
        public Float performanceChange;
    }

    public static class RiskAssessmentInfo {
        @SerializedName("risk_level")
        public String riskLevel;

        @SerializedName("risk_score")
        public Object riskScore;

        @SerializedName("risk_factors")
        public List<String> riskFactors;

        @SerializedName("status")
        public String status;

        @SerializedName("assessment_date")
        public String assessmentDate;
    }

    public static class AttendanceSummaryInfo {
        @SerializedName("total")
        public Integer total;

        @SerializedName("present")
        public Integer present;

        @SerializedName("absent")
        public Integer absent;

        @SerializedName("late")
        public Integer late;

        @SerializedName("excused")
        public Integer excused;

        @SerializedName("percentage")
        public Float percentage;
    }

    public static class AssignmentsSummaryInfo {
        @SerializedName("total")
        public Integer total;

        @SerializedName("completed")
        public Integer completed;

        @SerializedName("graded")
        public Integer graded;

        @SerializedName("pending")
        public Integer pending;

        @SerializedName("average")
        public Float average;
    }

    public static class QuizzesSummaryInfo {
        @SerializedName("total")
        public Integer total;

        @SerializedName("completed")
        public Integer completed;

        @SerializedName("upcoming")
        public Integer upcoming;

        @SerializedName("average")
        public Float average;
    }

    public static class ExamsSummaryInfo {
        @SerializedName("total")
        public Integer total;

        @SerializedName("completed")
        public Integer completed;

        @SerializedName("upcoming")
        public Integer upcoming;

        @SerializedName("average")
        public Float average;
    }

    public static class GpaSummaryInfo {
        @SerializedName("current_semester_gpa")
        public Float currentSemesterGpa;

        @SerializedName("semester_gpa")
        public Float semesterGpa;

        @SerializedName("semester_average")
        public Float semesterAverage;

        @SerializedName("cumulative_gpa")
        public Float cumulativeGpa;

        @SerializedName("completed_credits")
        public Float completedCredits;

        @SerializedName("semester_credits")
        public Float semesterCredits;

        @SerializedName("academic_standing")
        public String academicStanding;
    }

    public static class RecentActivityInfo {
        @SerializedName("id")
        public String id;

        @SerializedName("type")
        public String type;

        @SerializedName("category")
        public String category;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("course_name")
        public String courseName;

        @SerializedName("course_code")
        public String courseCode;

        @SerializedName("reference_id")
        public Integer referenceId;

        @SerializedName("status_badge")
        public String statusBadge;

        @SerializedName("timestamp")
        public String timestamp;

        @SerializedName("time_ago")
        public String timeAgo;

        @SerializedName("date_group")
        public String dateGroup;
    }

    public static class EnrollmentInfo {
        @SerializedName("id")
        public String id;

        @SerializedName("status")
        public String status;

        @SerializedName("final_score")
        public Float finalScore;

        @SerializedName("grade")
        public String grade;

        @SerializedName("course")
        public CourseModelInfo course;

        @SerializedName("semester")
        public SemesterInfo semester;
    }

    public static class CourseModelInfo {
        @SerializedName("id")
        public String id;

        @SerializedName("course_code")
        public String courseCode;

        @SerializedName("course_name")
        public String courseName;

        @SerializedName("credit")
        public Float credit;

        @SerializedName("teacher")
        public TeacherInfo teacher;
    }

    public static class TeacherInfo {
        @SerializedName("user")
        public UserInfo user;
    }

    public static class SemesterInfo {
        @SerializedName("id")
        public Object id;

        @SerializedName("name")
        public String name;

        @SerializedName("academic_year_id")
        public Object academicYearId;

        public String getDisplayName() {
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
            Long idVal = getIdAsLong();
            return idVal != null ? ("Semester " + idVal) : "Semester";
        }

        public Long getIdAsLong() {
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
            if (id != null) {
                try {
                    return (long) Double.parseDouble(String.valueOf(id).trim());
                } catch (Exception ignored) {}
            }
            return null;
        }
    }

    public static class PredictionInfo {
        @SerializedName("predicted_score")
        public Object predictedScore;

        @SerializedName("confidence_score")
        public Object confidenceScore;

        @SerializedName("model_version")
        public String modelVersion;

        @SerializedName("prediction_type")
        public String predictionType;

        @SerializedName("prediction_date")
        public String predictionDate;

        @SerializedName("notes")
        public String notes;
    }
}
