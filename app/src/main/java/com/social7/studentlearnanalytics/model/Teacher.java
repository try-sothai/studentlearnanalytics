package com.social7.studentlearnanalytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Teacher implements Serializable {
    private String id;
    private Long dbId;
    private Long userId;
    private String name;
    private String khmerName;
    private String englishName;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String department;
    private Integer departmentId;
    private String position;
    private String hireDate;
    private String workType;
    private Integer officeLocationId;
    private String officeLocation;
    private String specialization;
    private List<String> specializations = new ArrayList<>();
    private String bio;
    private String status;
    private String createdAt;
    private String lastLogin;
    private String coursesSummary;
    private String classesSummary;
    private String loginId;
    private String profileStatus;
    private String loginAccountStatus;
    private boolean accountCreated = true;
    private boolean accountLinked = true;
    private boolean canLogin = true;

    public Teacher(String id, String name, String email, String department, String position) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.position = position != null ? position : "Lecturer";
        this.workType = "Full-time";
        this.officeLocation = "";
        this.specialization = "";
        this.bio = "";
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
    public String getName() {
        if (name != null && !name.isEmpty()) return name;
        if (englishName != null && !englishName.isEmpty()) return englishName;
        if (khmerName != null && !khmerName.isEmpty()) return khmerName;
        return "";
    }
    public String getKhmerName() { return khmerName != null ? khmerName : ""; }
    public void setKhmerName(String khmerName) { this.khmerName = khmerName; }
    public String getEnglishName() { return englishName != null ? englishName : ""; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
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
    public Integer getOfficeLocationId() { return officeLocationId; }
    public void setOfficeLocationId(Integer officeLocationId) { this.officeLocationId = officeLocationId; }
    public String getRawOfficeLocation() { return officeLocation != null ? officeLocation.trim() : ""; }
    public String getOfficeLocation() { return officeLocation != null && !officeLocation.isEmpty() ? officeLocation : "Not set"; }
    public String getSpecialization() { return specialization != null ? specialization : ""; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public List<String> getSpecializations() {
        if (specializations != null && !specializations.isEmpty()) {
            return new ArrayList<>(specializations);
        }
        List<String> list = new ArrayList<>();
        if (specialization != null && !specialization.trim().isEmpty()) {
            for (String s : specialization.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) list.add(trimmed);
            }
        }
        return list;
    }
    public void setSpecializations(List<String> list) {
        this.specializations = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.specialization = String.join(", ", this.specializations);
    }
    public String getSpecializationDisplay() {
        List<String> items = getSpecializations();
        return items.isEmpty() ? "Not set" : String.join(", ", items);
    }
    public String getRawBio() { return bio != null ? bio : ""; }
    public String getBio() { return bio != null && !bio.isEmpty() ? bio : "Not set"; }
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

    public String getLoginId() { return loginId != null && !loginId.isEmpty() ? loginId : (id != null ? id : ""); }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getProfileStatus() { return profileStatus != null && !profileStatus.isEmpty() ? profileStatus : getStatus(); }
    public void setProfileStatus(String profileStatus) { this.profileStatus = profileStatus; }
    public String getLoginAccountStatus() { return loginAccountStatus != null && !loginAccountStatus.isEmpty() ? loginAccountStatus : getStatus(); }
    public void setLoginAccountStatus(String loginAccountStatus) { this.loginAccountStatus = loginAccountStatus; }
    public boolean isAccountCreated() { return accountCreated; }
    public void setAccountCreated(boolean accountCreated) { this.accountCreated = accountCreated; }
    public boolean isAccountLinked() { return accountLinked; }
    public void setAccountLinked(boolean accountLinked) { this.accountLinked = accountLinked; }
    public boolean isCanLogin() { return canLogin; }
    public void setCanLogin(boolean canLogin) { this.canLogin = canLogin; }
}
