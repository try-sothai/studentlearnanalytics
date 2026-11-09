package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Student implements Serializable {
    private String id;
    private String studentCode;
    private Long dbId;
    private Long userId;
    private String name;
    private String khmerName;
    private String englishName;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String placeOfBirth;
    private String address;
    private String department;
    private String departmentCode;
    private Integer departmentId;
    private String program;
    private String programCode;
    private Integer programId;
    private String year;
    private Integer yearLevel;
    private String academicYear;
    private Integer academicYearId;
    private String semester;
    private Integer semesterId;
    private String className;
    private Integer classId;
    private String enrolledCoursesCount;
    private String enrolledCoursesSummary;
    private double gpa;
    private String riskLevel;
    private double predictedScore;
    private String status;
    private String advisor;
    private String createdAt;
    private String lastLogin;
    private int attendanceRate;
    private int totalClasses;
    private int presentClasses;
    private int absentClasses;
    private int lateClasses;
    private int excusedClasses;
    private int assignmentScore;
    private int quizScore;
    private int avatarRes;
    private String avatarBgColor;
    private String avatarTextColor;
    private String courseCode;

    // Family & Emergency Contact Information
    private String fatherKhmerName;
    private String fatherEnglishName;
    private String fatherPhone;
    private String motherKhmerName;
    private String motherEnglishName;
    private String motherPhone;
    private String emergencyName;
    private String emergencyRelationship;
    private String emergencyPhone;
    private String emergencyAddress;

    public Student() {
        this.status = "Active";
        this.avatarBgColor = "#EAF0FF";
        this.avatarTextColor = "#2563FF";
    }

    public Student(String studentCode, String name, String email, String department, String program, String year, double gpa, String riskLevel, double predictedScore, String avatarBgColor, String avatarTextColor, String courseCode) {
        this.id = studentCode != null ? studentCode : "STD001";
        this.studentCode = this.id;
        this.name = name;
        this.englishName = name;
        this.email = email;
        this.department = department;
        this.program = program;
        this.year = year;
        this.gpa = gpa;
        this.riskLevel = riskLevel;
        this.predictedScore = predictedScore;
        this.avatarBgColor = avatarBgColor != null ? avatarBgColor : "#EAF0FF";
        this.avatarTextColor = avatarTextColor != null ? avatarTextColor : "#2563FF";
        this.courseCode = courseCode != null ? courseCode : "CS101";
        this.status = "Active";
        this.advisor = "Academic Advisor";
    }

    public Student(String studentCode, String name, String email, String department, String year, double gpa, String riskLevel, double predictedScore) {
        this(studentCode, name, email, department, department, year, gpa, riskLevel, predictedScore, "#EAF0FF", "#2563FF", "CS101");
    }

    public Student(String studentCode, String name, String email, String status) {
        this(studentCode, name, email, "Computer Science", "Bachelor of Computer Science", "Year 1", 0.0, "Low", 0.0, "#EAF0FF", "#2563FF", "CS101");
        this.status = status != null ? status : "Active";
    }

    public Student(String studentCode, String name, double gpa, String riskLevel) {
        this(studentCode, name, "", "Computer Science", "Year 1", gpa, riskLevel, 75.0);
    }

    public String getId() { return studentCode != null && !studentCode.isEmpty() ? studentCode : (id != null && !id.isEmpty() ? id : "STD001"); }
    public String getStudentCode() { return getId(); }
    public Long getDbId() { return dbId; }
    public void setDbId(Long dbId) { this.dbId = dbId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() {
        if (name != null && !name.isEmpty()) return name;
        if (englishName != null && !englishName.isEmpty()) return englishName;
        if (khmerName != null && !khmerName.isEmpty()) return khmerName;
        return "Student";
    }
    public String getKhmerName() { return khmerName != null ? khmerName : ""; }
    public String getEnglishName() { return englishName != null && !englishName.isEmpty() ? englishName : (name != null ? name : ""); }
    public String getEmail() { return email != null ? email : ""; }
    public String getPhone() { return phone != null && !phone.isEmpty() ? phone : "N/A"; }
    public String getDob() { return dob != null && !dob.isEmpty() ? dob : "N/A"; }
    public String getGender() { return gender != null && !gender.isEmpty() ? gender : "N/A"; }
    public String getPlaceOfBirth() { return placeOfBirth != null ? placeOfBirth : ""; }
    public String getAddress() { return address != null ? address : ""; }
    public String getDepartment() { return department != null && !department.isEmpty() ? department : "N/A"; }
    public String getDepartmentCode() { return departmentCode != null ? departmentCode : ""; }
    public Integer getDepartmentId() { return departmentId; }
    public String getProgram() { return program != null && !program.isEmpty() ? program : (department != null && !department.isEmpty() ? department : "N/A"); }
    public String getProgramCode() { return programCode != null ? programCode : ""; }
    public Integer getProgramId() { return programId; }
    public String getYear() {
        if (year != null && !year.isEmpty()) return year;
        if (yearLevel != null && yearLevel > 0) return "Year " + yearLevel;
        return "Year 1";
    }
    public Integer getYearLevel() { return yearLevel; }
    public String getAcademicYear() { return academicYear != null && !academicYear.isEmpty() ? academicYear : "2025-2026"; }
    public Integer getAcademicYearId() { return academicYearId; }
    public String getSemester() { return semester != null && !semester.isEmpty() ? semester : ""; }
    public Integer getSemesterId() { return semesterId; }
    public String getClassName() { return className != null ? className : ""; }
    public Integer getClassId() { return classId; }
    public String getEnrolledCoursesCount() { return enrolledCoursesCount != null && !enrolledCoursesCount.isEmpty() ? enrolledCoursesCount : "0 Courses"; }
    public String getEnrolledCoursesSummary() { return enrolledCoursesSummary != null ? enrolledCoursesSummary : ""; }
    public double getGpa() { return gpa; }
    public String getRiskLevel() { return riskLevel != null && !riskLevel.isEmpty() ? riskLevel : "Low"; }
    public double getPredictedScore() { return predictedScore; }
    public String getStatus() { return status != null && !status.isEmpty() ? status : "Active"; }
    public String getAdvisor() { return advisor != null && !advisor.isEmpty() ? advisor : "Academic Advisor"; }
    public String getCreatedAt() { return createdAt != null && !createdAt.isEmpty() ? createdAt : "N/A"; }
    public String getLastLogin() { return lastLogin != null && !lastLogin.isEmpty() ? lastLogin : "N/A"; }
    public int getAttendanceRate() { return attendanceRate; }
    public int getTotalClasses() { return totalClasses; }
    public int getPresentClasses() { return presentClasses; }
    public int getAbsentClasses() { return absentClasses; }
    public int getLateClasses() { return lateClasses; }
    public int getExcusedClasses() { return excusedClasses; }
    public int getAssignmentScore() { return assignmentScore; }
    public int getQuizScore() { return quizScore; }
    public int getAvatarRes() { return avatarRes; }
    public String getAvatarBgColor() { return avatarBgColor != null ? avatarBgColor : "#EAF0FF"; }
    public String getAvatarTextColor() { return avatarTextColor != null ? avatarTextColor : "#2563FF"; }
    public String getCourseCode() { return courseCode != null ? courseCode : ""; }

    public String getFatherKhmerName() { return fatherKhmerName != null ? fatherKhmerName : ""; }
    public String getFatherEnglishName() { return fatherEnglishName != null ? fatherEnglishName : ""; }
    public String getFatherPhone() { return fatherPhone != null ? fatherPhone : ""; }
    public String getMotherKhmerName() { return motherKhmerName != null ? motherKhmerName : ""; }
    public String getMotherEnglishName() { return motherEnglishName != null ? motherEnglishName : ""; }
    public String getMotherPhone() { return motherPhone != null ? motherPhone : ""; }
    public String getEmergencyName() { return emergencyName != null ? emergencyName : ""; }
    public String getEmergencyRelationship() { return emergencyRelationship != null ? emergencyRelationship : ""; }
    public String getEmergencyPhone() { return emergencyPhone != null ? emergencyPhone : ""; }
    public String getEmergencyAddress() { return emergencyAddress != null ? emergencyAddress : ""; }

    public void setId(String id) { this.id = id; this.studentCode = id; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; this.id = studentCode; }
    public void setName(String name) { this.name = name; }
    public void setKhmerName(String khmerName) { this.khmerName = khmerName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }
    public void setAddress(String address) { this.address = address; }
    public void setDepartment(String department) { this.department = department; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public void setProgram(String program) { this.program = program; }
    public void setProgramCode(String programCode) { this.programCode = programCode; }
    public void setProgramId(Integer programId) { this.programId = programId; }
    public void setYear(String year) { this.year = year; }
    public void setYearLevel(Integer yearLevel) { this.yearLevel = yearLevel; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setAcademicYearId(Integer academicYearId) { this.academicYearId = academicYearId; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setSemesterId(Integer semesterId) { this.semesterId = semesterId; }
    public void setClassName(String className) { this.className = className; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public void setEnrolledCoursesCount(String enrolledCoursesCount) { this.enrolledCoursesCount = enrolledCoursesCount; }
    public void setEnrolledCoursesSummary(String enrolledCoursesSummary) { this.enrolledCoursesSummary = enrolledCoursesSummary; }
    public void setGpa(double gpa) { this.gpa = gpa; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setPredictedScore(double predictedScore) { this.predictedScore = predictedScore; }
    public void setStatus(String status) { this.status = status; }
    public void setAdvisor(String advisor) { this.advisor = advisor; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public void setAvatarRes(int avatarRes) { this.avatarRes = avatarRes; }
    public void setAttendanceRate(int attendanceRate) { this.attendanceRate = attendanceRate; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    public void setPresentClasses(int presentClasses) { this.presentClasses = presentClasses; }
    public void setAbsentClasses(int absentClasses) { this.absentClasses = absentClasses; }
    public void setLateClasses(int lateClasses) { this.lateClasses = lateClasses; }
    public void setExcusedClasses(int excusedClasses) { this.excusedClasses = excusedClasses; }
    public void setAssignmentScore(int assignmentScore) { this.assignmentScore = assignmentScore; }
    public void setQuizScore(int quizScore) { this.quizScore = quizScore; }
    public void setAvatarBgColor(String avatarBgColor) { this.avatarBgColor = avatarBgColor; }
    public void setAvatarTextColor(String avatarTextColor) { this.avatarTextColor = avatarTextColor; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public void setFatherKhmerName(String fatherKhmerName) { this.fatherKhmerName = fatherKhmerName; }
    public void setFatherEnglishName(String fatherEnglishName) { this.fatherEnglishName = fatherEnglishName; }
    public void setFatherPhone(String fatherPhone) { this.fatherPhone = fatherPhone; }
    public void setMotherKhmerName(String motherKhmerName) { this.motherKhmerName = motherKhmerName; }
    public void setMotherEnglishName(String motherEnglishName) { this.motherEnglishName = motherEnglishName; }
    public void setMotherPhone(String motherPhone) { this.motherPhone = motherPhone; }
    public void setEmergencyName(String emergencyName) { this.emergencyName = emergencyName; }
    public void setEmergencyRelationship(String emergencyRelationship) { this.emergencyRelationship = emergencyRelationship; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    public void setEmergencyAddress(String emergencyAddress) { this.emergencyAddress = emergencyAddress; }
}
