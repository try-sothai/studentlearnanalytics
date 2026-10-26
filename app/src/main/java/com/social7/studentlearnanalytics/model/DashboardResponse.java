package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
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

        @SerializedName("overall_performance")
        private Object overallPerformance;

        @SerializedName("attendance_rate")
        private Object attendanceRate;

        @SerializedName("assignment_score")
        private Object assignmentScore;

        @SerializedName("quiz_score")
        private Object quizScore;

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

        public Object getGpaValue() {
            if (gpa != null) return gpa;
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

        public String getDepartment() {
            if (student != null && student.getDepartmentName() != null) {
                return student.getDepartmentName();
            }
            return "";
        }

        public String getYearLevel() {
            if (student != null) {
                return student.getYearLevel();
            }
            return "1";
        }

        public String getOverallPerformanceFormatted() {
            Object obj = (analytics != null && analytics.overallScore != null) ? analytics.overallScore : overallPerformance;
            return formatPercentage(obj);
        }

        public String getAttendanceRateFormatted() {
            Object obj = (analytics != null && analytics.attendanceRate != null) ? analytics.attendanceRate : attendanceRate;
            return formatPercentage(obj);
        }

        public String getAssignmentScoreFormatted() {
            Object obj = (analytics != null && analytics.assignmentAvg != null) ? analytics.assignmentAvg : assignmentScore;
            return formatPercentage(obj);
        }

        public String getQuizScoreFormatted() {
            Object obj = (analytics != null && analytics.quizAvg != null) ? analytics.quizAvg : quizScore;
            return formatPercentage(obj);
        }

        public String getExamScoreFormatted() {
            Object obj = (analytics != null && analytics.examAvg != null) ? analytics.examAvg : null;
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
        private Object gpa;
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

        @SerializedName("user")
        private UserInfo user;

        @SerializedName("department")
        private DepartmentInfo department;

        @SerializedName("year_level")
        private String yearLevel;

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

        public String getDepartmentName() {
            return department != null && department.name != null ? department.name : "";
        }

        public String getYearLevel() {
            return yearLevel != null ? yearLevel : "1";
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
        private String name;
    }

    public static class AnalyticsInfo {
        @SerializedName("overall_performance")
        private Object overallScore;

        @SerializedName("attendance_rate")
        private Object attendanceRate;

        @SerializedName("assignment_average")
        private Object assignmentAvg;

        @SerializedName("quiz_average")
        private Object quizAvg;

        @SerializedName("exam_average")
        public Object examAvg;

        @SerializedName("gpa")
        private Object gpa;

        @SerializedName("predicted_score")
        private Object predictedScore;
    }

    public static class RiskAssessmentInfo {
        @SerializedName("risk_level")
        private String riskLevel;
    }

    public static class EnrollmentInfo {
        @SerializedName("id")
        public String id;

        @SerializedName("status")
        public String status;

        @SerializedName("final_score")
        public Float finalScore;

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
    }

    public static class PredictionInfo {
        @SerializedName("predicted_score")
        public Object predictedScore;
    }
}

