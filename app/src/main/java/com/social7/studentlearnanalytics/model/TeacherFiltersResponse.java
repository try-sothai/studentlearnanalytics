package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TeacherFiltersResponse implements Serializable {

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

        @SerializedName("generations")
        private List<FilterItem> generations = new ArrayList<>();

        @SerializedName("programs")
        private List<ProgramItem> programs = new ArrayList<>();

        @SerializedName("academic_years")
        private List<AcademicYearItem> academicYears = new ArrayList<>();

        @SerializedName("study_years")
        private List<StudyYearItem> studyYears = new ArrayList<>();

        @SerializedName("semesters")
        private List<SemesterItem> semesters = new ArrayList<>();

        @SerializedName("courses")
        private List<CourseItem> courses = new ArrayList<>();

        @SerializedName("classes")
        private List<ClassItem> classes = new ArrayList<>();

        public List<FilterItem> getGenerations() {
            return generations != null ? generations : new ArrayList<>();
        }

        public List<ProgramItem> getPrograms() {
            return programs != null ? programs : new ArrayList<>();
        }

        public List<AcademicYearItem> getAcademicYears() {
            return academicYears != null ? academicYears : new ArrayList<>();
        }

        public List<StudyYearItem> getStudyYears() {
            return studyYears != null ? studyYears : new ArrayList<>();
        }

        public List<SemesterItem> getSemesters() {
            return semesters != null ? semesters : new ArrayList<>();
        }

        public List<CourseItem> getCourses() {
            return courses != null ? courses : new ArrayList<>();
        }

        public List<ClassItem> getClasses() {
            return classes != null ? classes : new ArrayList<>();
        }
    }

    public static class FilterItem implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name != null ? name : ""; }
    }

    public static class ProgramItem implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        public int getId() { return id; }
        public String getCode() { return code; }
        public String getName() { return name; }
        @Override public String toString() { return (code != null ? code : name); }
    }

    public static class AcademicYearItem implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("status")
        private String status;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        @Override public String toString() { return name != null ? name : ""; }
    }

    public static class StudyYearItem implements Serializable {
        @SerializedName("year")
        private Object year;

        @SerializedName("year_level")
        private Object yearLevel;

        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        public int getYear() {
            Integer parsed = YearLevelHelper.parse(yearLevel != null ? yearLevel : year);
            return parsed != null ? parsed : 0;
        }

        public String getCode() {
            if (code != null && !code.trim().isEmpty()) {
                return code.trim();
            }
            int y = getYear();
            return y > 0 ? YearLevelHelper.toCode(y) : "";
        }

        public String getName() { return name; }
        @Override public String toString() {
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
            int y = getYear();
            return y > 0 ? ("Y" + y + " — Year " + y) : "";
        }
    }

    public static class SemesterItem implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("academic_year_id")
        private int academicYearId;

        @SerializedName("name")
        private String name;

        @SerializedName("semester_number")
        private int semesterNumber;

        public int getId() { return id; }
        public int getAcademicYearId() { return academicYearId; }
        public String getName() { return name; }
        public int getSemesterNumber() { return semesterNumber; }
        @Override public String toString() { return name != null ? name : ""; }
    }

    public static class CourseItem implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        public int getId() { return id; }
        public String getCode() { return code; }
        public String getName() { return name; }
        @Override public String toString() { return (code != null ? code : name); }
    }

    public static class ClassItem implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        @SerializedName("year_level")
        private Object yearLevel;

        @SerializedName("year_level_code")
        private String yearLevelCode;

        @SerializedName("academic_year")
        private String academicYear;

        public int getId() { return id; }
        public String getCode() { return code; }
        public String getName() { return name; }
        public Integer getYearLevel() { return YearLevelHelper.parse(yearLevel); }
        public String getYearLevelCode() {
            if (yearLevelCode != null && !yearLevelCode.trim().isEmpty()) {
                return yearLevelCode.trim();
            }
            String c = YearLevelHelper.toCode(yearLevel);
            return c != null ? c : "";
        }
        public String getAcademicYear() { return academicYear != null ? academicYear : ""; }
        @Override public String toString() { return (name != null ? name : code); }
    }
}
