package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Teacher implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String department;
    private String position;
    private String hireDate;
    private String workType;
    private String officeLocation;
    private String bio;
    private String status;
    private String createdAt;
    private String lastLogin;

    public Teacher(String id, String name, String email, String department, String position) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = "+855 12 345 678";
        this.dob = "Mar 10, 1990";
        this.gender = "Male";
        this.department = department;
        this.position = position != null ? position : "Lecturer";
        this.hireDate = "Sep 01, 2020";
        this.workType = "Full-time";
        this.officeLocation = "Building A, Room 203";
        this.bio = "Experienced lecturer in " + department + ".";
        this.status = "Active";
        this.createdAt = "Sep 01, 2020";
        this.lastLogin = "May 29, 2024, 09:45 AM";
    }

    public Teacher(String id, String name, String email, String department, String position, String status) {
        this(id, name, email, department, position);
        this.status = status != null ? status : "Active";
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone != null ? phone : "+855 12 345 678"; }
    public String getDob() { return dob != null ? dob : "Mar 10, 1990"; }
    public String getGender() { return gender != null ? gender : "Male"; }
    public String getDepartment() { return department; }
    public String getPosition() { return position != null ? position : "Lecturer"; }
    public String getTitle() { return getPosition(); }
    public String getHireDate() { return hireDate != null ? hireDate : "Sep 01, 2020"; }
    public String getWorkType() { return workType != null ? workType : "Full-time"; }
    public String getOfficeLocation() { return officeLocation != null ? officeLocation : "Building A, Room 203"; }
    public String getBio() { return bio != null ? bio : "Experienced lecturer."; }
    public String getStatus() { return status != null ? status : "Active"; }
    public String getCreatedAt() { return createdAt != null ? createdAt : "Sep 01, 2020"; }
    public String getLastLogin() { return lastLogin != null ? lastLogin : "May 29, 2024, 09:45 AM"; }

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
}
