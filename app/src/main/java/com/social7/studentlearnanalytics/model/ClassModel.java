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
    private int departmentId;
    private int academicYearId;
    private int buildingId;
    private int floorId;
    private int yearLevel = 1;
    private int capacity = 40;
    private String buildingName;
    private String floorName;
    private String locationLabel;

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

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }

    public int getBuildingId() { return buildingId; }
    public void setBuildingId(int buildingId) { this.buildingId = buildingId; }

    public int getFloorId() { return floorId; }
    public void setFloorId(int floorId) { this.floorId = floorId; }

    public int getYearLevel() { return yearLevel; }
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getFloorName() { return floorName; }
    public void setFloorName(String floorName) { this.floorName = floorName; }

    public String getLocationLabel() { return locationLabel; }
    public void setLocationLabel(String locationLabel) { this.locationLabel = locationLabel; }
}
