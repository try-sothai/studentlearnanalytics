package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Student implements Serializable {
    private String id;
    private String studentCode;
    private Long dbId;
    private String name;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String department;
    private String program;
    private String year;
    private String academicYear;
    private String enrolledCoursesCount;
    private double gpa;
    private String riskLevel;
    private double predictedScore;
    private String status;
    private String advisor;
    private String createdAt;
    private String lastLogin;
    private int attendanceRate;
    private int assignmentScore;
    private int quizScore;
    private int avatarRes;
    private String avatarBgColor;
    private String avatarTextColor;
    private String courseCode;

    public Student(String studentCode, String name, String email, String department, String program, String year, double gpa, String riskLevel, double predictedScore, String avatarBgColor, String avatarTextColor, String courseCode) {
        this.id = studentCode != null ? studentCode : "STD001";
        this.studentCode = this.id;
        this.name = name;
        this.email = email;
        this.phone = "+855 12 345 678";
        this.dob = "Jan 15, 2003";
        this.gender = "Female";
        this.department = department;
        this.program = program;
        this.year = year;
        this.academicYear = "2024–2025";
        this.enrolledCoursesCount = "6 Courses";
        this.gpa = gpa;
        this.riskLevel = riskLevel;
        this.predictedScore = predictedScore;
        this.avatarBgColor = avatarBgColor != null ? avatarBgColor : "#EAF0FF";
        this.avatarTextColor = avatarTextColor != null ? avatarTextColor : "#2563FF";
        this.courseCode = courseCode != null ? courseCode : "CS101";
        this.status = "Active";
        this.advisor = "Mr. Chan";
        this.createdAt = "Sep 01, 2023";
        this.lastLogin = "May 29, 2024, 10:30 AM";
        this.attendanceRate = 88;
        this.assignmentScore = 80;
        this.quizScore = 78;
    }

    public Student(String studentCode, String name, String email, String department, String year, double gpa, String riskLevel, double predictedScore) {
        this(studentCode, name, email, department, department, year, gpa, riskLevel, predictedScore, "#EAF0FF", "#2563FF", "CS101");
    }

    public Student(String studentCode, String name, String email, String status) {
        this(studentCode, name, email, "Computer Science", "Bachelor of Computer Science", "Year 3", 3.25, "Good", 85.0, "#EAF0FF", "#2563FF", "CS101");
        this.status = status != null ? status : "Active";
    }

    public Student(String studentCode, String name, double gpa, String riskLevel) {
        this(studentCode, name, name.toLowerCase().replace(" ", "") + "@univ.edu", "Computer Science", "Year 3", gpa, riskLevel, 75.0);
    }

    public String getId() { return studentCode != null ? studentCode : (id != null ? id : "STD001"); }
    public String getStudentCode() { return getId(); }
    public Long getDbId() { return dbId; }
    public void setDbId(Long dbId) { this.dbId = dbId; }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone != null ? phone : "+855 12 345 678"; }
    public String getDob() { return dob != null ? dob : "Jan 15, 2003"; }
    public String getGender() { return gender != null ? gender : "Female"; }
    public String getDepartment() { return department; }
    public String getProgram() { return program != null ? program : (department != null ? department : "Bachelor of Computer Science"); }
    public String getYear() { return year; }
    public String getAcademicYear() { return academicYear != null ? academicYear : "2024–2025"; }
    public String getEnrolledCoursesCount() { return enrolledCoursesCount != null ? enrolledCoursesCount : "6 Courses"; }
    public double getGpa() { return gpa; }
    public String getRiskLevel() { return riskLevel; }
    public double getPredictedScore() { return predictedScore; }
    public String getStatus() { return status != null ? status : "Active"; }
    public String getAdvisor() { return advisor; }
    public String getCreatedAt() { return createdAt != null ? createdAt : "Sep 01, 2023"; }
    public String getLastLogin() { return lastLogin != null ? lastLogin : "May 29, 2024, 10:30 AM"; }
    public int getAttendanceRate() { return attendanceRate; }
    public int getAssignmentScore() { return assignmentScore; }
    public int getQuizScore() { return quizScore; }
    public int getAvatarRes() { return avatarRes; }
    public String getAvatarBgColor() { return avatarBgColor; }
    public String getAvatarTextColor() { return avatarTextColor; }
    public String getCourseCode() { return courseCode; }

    public void setId(String id) { this.id = id; this.studentCode = id; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; this.id = studentCode; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDepartment(String department) { this.department = department; }
    public void setProgram(String program) { this.program = program; }
    public void setYear(String year) { this.year = year; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setEnrolledCoursesCount(String enrolledCoursesCount) { this.enrolledCoursesCount = enrolledCoursesCount; }
    public void setStatus(String status) { this.status = status; }
    public void setAvatarRes(int avatarRes) { this.avatarRes = avatarRes; }
    public void setAttendanceRate(int attendanceRate) { this.attendanceRate = attendanceRate; }
    public void setAssignmentScore(int assignmentScore) { this.assignmentScore = assignmentScore; }
    public void setQuizScore(int quizScore) { this.quizScore = quizScore; }
    public void setAvatarBgColor(String avatarBgColor) { this.avatarBgColor = avatarBgColor; }
    public void setAvatarTextColor(String avatarTextColor) { this.avatarTextColor = avatarTextColor; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
}
