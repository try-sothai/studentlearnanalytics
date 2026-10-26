package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GradesResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<GradeItem> data;

    public String getMessage() {
        return message;
    }

    public List<GradeItem> getData() {
        return data;
    }

    public static class GradeItem {
        @SerializedName("id")
        public Object id;

        @SerializedName("student_id")
        public Object studentId;

        @SerializedName("course_id")
        public Object courseId;

        @SerializedName("semester_id")
        public Object semesterId;

        @SerializedName("assignment_score")
        public Float assignmentScore;

        @SerializedName("quiz_score")
        public Float quizScore;

        @SerializedName("attendance_score")
        public Float attendanceScore;

        @SerializedName("homework_score")
        public Float homeworkScore;

        @SerializedName("midterm_score")
        public Float midtermScore;

        @SerializedName("final_exam_score")
        public Float finalExamScore;

        @SerializedName("total_score")
        public Float totalScore;

        @SerializedName("letter_grade")
        public String letterGrade;

        @SerializedName("grade_point")
        public Float gradePoint;

        @SerializedName("status")
        public String status;

        @SerializedName("course")
        public DashboardResponse.CourseModelInfo course;

        @SerializedName("semester")
        public DashboardResponse.SemesterInfo semester;

        public float calculateCalculatedTotal() {
            if (totalScore != null) return totalScore;

            float att = attendanceScore != null ? attendanceScore : 0f;
            float assign = assignmentScore != null ? assignmentScore : 0f;
            float quiz = quizScore != null ? quizScore : 0f;
            float hw = homeworkScore != null ? homeworkScore : quiz; // Fallback
            float midterm = midtermScore != null ? midtermScore : 0f;
            float finalExam = finalExamScore != null ? finalExamScore : 0f;

            return (att * 0.10f) + (assign * 0.20f) + (quiz * 0.10f) + (hw * 0.10f) + (midterm * 0.15f) + (finalExam * 0.35f);
        }
    }
}
