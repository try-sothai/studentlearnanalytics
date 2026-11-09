package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Teacher implements Serializable {
    private String id;
    private Long dbId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String department;
    private Integer departmentId;
    private String position;
    private String hireDate;
    private String workType;
    private String officeLocation;
    private String bio;
    private String status;
    private String createdAt;
    private String lastLogin;
    private String coursesSummary;
    private String classesSummary;

    public Teacher(String id, String name, String email, String department, String position) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.position = position != null ? position : "Lecturer";
        this.workType = "Full-time";
        this.officeLocation = "Faculty Office";
        this.bio = "Faculty member in " + (department != null ? department : "University") + ".";
        this.status = "Active";
    }

    public Teacher(String id, String name, String email, String department, String position, String status) {
        this(id, name, email, department, position);
        this.status = status != null ? status : "Active";
    }

    public String getId() { return id; }
    public Long getDbId() { return dbId; }
    public void setDbId(Long dbId) { this.dbId = dbId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name != null ? name : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getPhone() { return phone != null && !phone.isEmpty() ? phone : "N/A"; }
    public String getDob() { return dob != null && !dob.isEmpty() ? dob : "N/A"; }
    public String getGender() { return gender != null && !gender.isEmpty() ? gender : "N/A"; }
    public String getDepartment() { return department != null && !department.isEmpty() ? department : "Unassigned"; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getPosition() { return position != null && !position.isEmpty() ? position : "Lecturer"; }
    public String getTitle() { return getPosition(); }
    public String getHireDate() { return hireDate != null && !hireDate.isEmpty() ? hireDate : "N/A"; }
    public String getWorkType() { return workType != null && !workType.isEmpty() ? workType : "Full-time"; }
    public String getOfficeLocation() { return officeLocation != null && !officeLocation.isEmpty() ? officeLocation : "Faculty Office"; }
    public String getBio() { return bio != null && !bio.isEmpty() ? bio : "Faculty member."; }
    public String getStatus() { return status != null && !status.isEmpty() ? status : "Active"; }
    public String getCreatedAt() { return createdAt != null && !createdAt.isEmpty() ? createdAt : "N/A"; }
    public String getLastLogin() { return lastLogin != null && !lastLogin.isEmpty() ? lastLogin : "N/A"; }
    public String getCoursesSummary() { return coursesSummary != null ? coursesSummary : ""; }
    public void setCoursesSummary(String coursesSummary) { this.coursesSummary = coursesSummary; }
    public String getClassesSummary() { return classesSummary != null ? classesSummary : ""; }
    public void setClassesSummary(String classesSummary) { this.classesSummary = classesSummary; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }
    public void setWorkType(String workType) { this.workType = workType; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
    public void setBio(String bio) { this.bio = bio; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
