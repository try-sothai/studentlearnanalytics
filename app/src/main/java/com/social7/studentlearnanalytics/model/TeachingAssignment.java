package com.social7.studentlearnanalytics.model;

import com.google.gson.JsonObject;

public class TeachingAssignment {
    private int courseOfferingId;
    private int academicYearId;
    private String academicYearName;
    private int semesterId;
    private String semesterName;
    private int classId;
    private String className;
    private String classCode;
    private int courseId;
    private String courseName;
    private String courseCode;
    private int studentCount;
    private String status;

    public static TeachingAssignment fromJson(JsonObject obj) {
        TeachingAssignment item = new TeachingAssignment();
        item.courseOfferingId = optInt(obj, "course_offering_id", 0);
        item.academicYearId = optInt(obj, "academic_year_id", 0);
        item.academicYearName = optStr(obj, "academic_year_name", "Academic Year");
        item.semesterId = optInt(obj, "semester_id", 0);
        item.semesterName = optStr(obj, "semester_name", "Semester");
        item.classId = optInt(obj, "class_id", 0);
        item.className = optStr(obj, "class_name", "Class");
        item.classCode = optStr(obj, "class_code", "");
        item.courseId = optInt(obj, "course_id", 0);
        item.courseName = optStr(obj, "course_name", "Course");
        item.courseCode = optStr(obj, "course_code", "");
        item.studentCount = optInt(obj, "student_count", 0);
        item.status = optStr(obj, "status", "active");
        return item;
    }

    private static int optInt(JsonObject o, String key, int def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        try { return o.get(key).getAsInt(); } catch (Exception e) { return def; }
    }

    private static String optStr(JsonObject o, String key, String def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        try {
            String v = o.get(key).getAsString();
            return (v != null && !v.trim().isEmpty()) ? v.trim() : def;
        } catch (Exception e) { return def; }
    }

    public int getCourseOfferingId() { return courseOfferingId; }
    public int getAcademicYearId() { return academicYearId; }
    public String getAcademicYearName() { return academicYearName; }
    public int getSemesterId() { return semesterId; }
    public String getSemesterName() { return semesterName; }
    public int getClassId() { return classId; }
    public String getClassName() { return className; }
    public String getClassCode() { return classCode; }
    public int getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public int getStudentCount() { return studentCount; }
    public String getStatus() { return status; }

    public String getCourseDisplayLabel() {
        if (courseCode != null && !courseCode.isEmpty()) {
            return courseCode + " – " + courseName;
        }
        return courseName;
    }
}
