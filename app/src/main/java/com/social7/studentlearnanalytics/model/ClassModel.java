package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class ClassModel implements Serializable {
    private String id;
    private String name;
    private String code;
    private String department;
    private String academicYear;
    private String course;
    private String teacher;
    private String status;
    private int studentCount;

    public ClassModel() {
    }

    public ClassModel(String id, String name, String code, String department, String academicYear, String course, String teacher, String status, int studentCount) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.department = department;
        this.academicYear = academicYear;
        this.course = course;
        this.teacher = teacher;
        this.status = status;
        this.studentCount = studentCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
}
