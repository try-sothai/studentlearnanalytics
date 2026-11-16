package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TeacherDashboardSummaryResponse implements Serializable {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data implements Serializable {

        @SerializedName("teacher")
        private TeacherInfo teacher;

        @SerializedName("summary")
        private Summary summary;

        public TeacherInfo getTeacher() {
            return teacher;
        }

        public Summary getSummary() {
            return summary;
        }
    }

    public static class TeacherInfo implements Serializable {

        @SerializedName("id")
        private int id;

        @SerializedName("teacher_code")
        private String teacherCode;

        @SerializedName("name")
        private String name;

        @SerializedName("department")
        private String department;

        @SerializedName("email")
        private String email;

        public int getId() {
            return id;
        }

        public String getTeacherCode() {
            return teacherCode;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class Summary implements Serializable {

        @SerializedName("total_students")
        private int totalStudents;

        @SerializedName("my_courses")
        private int myCourses;

        @SerializedName("at_risk_students")
        private int atRiskStudents;

        @SerializedName("pending_scores")
        private int pendingScores;

        public int getTotalStudents() {
            return totalStudents;
        }

        public int getMyCourses() {
            return myCourses;
        }

        public int getAtRiskStudents() {
            return atRiskStudents;
        }

        public int getPendingScores() {
            return pendingScores;
        }
    }
}
