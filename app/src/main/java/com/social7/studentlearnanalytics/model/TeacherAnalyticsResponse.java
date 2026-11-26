package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TeacherAnalyticsResponse implements Serializable {

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

        @SerializedName("scope")
        private Scope scope;

        @SerializedName("summary")
        private TeacherDashboardSummaryResponse.Summary summary;

        @SerializedName("engagement")
        private List<EngagementItem> engagement = new ArrayList<>();

        public Scope getScope() {
            return scope;
        }

        public TeacherDashboardSummaryResponse.Summary getSummary() {
            return summary;
        }

        public List<EngagementItem> getEngagement() {
            return engagement != null ? engagement : new ArrayList<>();
        }
    }

    public static class Scope implements Serializable {
        @SerializedName("academic_year")
        private String academicYear;

        @SerializedName("semester")
        private String semester;

        @SerializedName("course")
        private String course;

        @SerializedName("class")
        private String className;

        @SerializedName("study_year")
        private Object studyYear;

        @SerializedName("year_level")
        private Object yearLevel;

        @SerializedName("year_level_code")
        private String yearLevelCode;

        public String getAcademicYear() { return academicYear; }
        public String getSemester() { return semester; }
        public String getCourse() { return course; }
        public String getClassName() { return className; }
        public String getStudyYear() {
            if (studyYear != null) {
                Integer lvl = com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(studyYear);
                return lvl != null ? ("Y" + lvl) : String.valueOf(studyYear);
            }
            return getYearLevelCode();
        }
        public Integer getYearLevel() {
            return com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(yearLevel != null ? yearLevel : studyYear);
        }
        public String getYearLevelCode() {
            if (yearLevelCode != null && !yearLevelCode.trim().isEmpty()) {
                return yearLevelCode.trim();
            }
            String code = com.social7.studentlearnanalytics.utils.YearLevelHelper.toCode(yearLevel != null ? yearLevel : studyYear);
            return code != null ? code : "";
        }
    }

    public static class EngagementItem implements Serializable {
        @SerializedName("activity")
        private String activity;

        @SerializedName("completed")
        private Double completed;

        @SerializedName("late")
        private Double late;

        @SerializedName("missed")
        private Double missed;

        @SerializedName("total_events")
        private int totalEvents;

        @SerializedName("has_data")
        private boolean hasData;

        public String getActivity() { return activity; }
        public Double getCompleted() { return completed; }
        public Double getLate() { return late; }
        public Double getMissed() { return missed; }
        public int getTotalEvents() { return totalEvents; }
        public boolean isHasData() { return hasData; }

        public float getCompletedFloat() {
            return completed != null ? completed.floatValue() : 0f;
        }

        public float getLateFloat() {
            return late != null ? late.floatValue() : 0f;
        }

        public float getMissedFloat() {
            return missed != null ? missed.floatValue() : 0f;
        }
    }
}
