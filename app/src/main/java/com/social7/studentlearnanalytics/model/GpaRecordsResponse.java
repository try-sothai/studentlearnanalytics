package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GpaRecordsResponse {
    @SerializedName("data")
    private List<GpaRecord> data;

    public List<GpaRecord> getRecords() { return data; }

    public static class GpaRecord {
        @SerializedName("gpa") private Object gpa;
        @SerializedName("academic_standing") private String academicStanding;
        @SerializedName("semester") private Semester semester;

        public Object getGpa() { return gpa; }
        public String getAcademicStanding() { return academicStanding; }
        public Semester getSemester() { return semester; }
    }

    public static class Semester {
        @SerializedName("name") private String name;
        public String getName() { return name; }
    }
}