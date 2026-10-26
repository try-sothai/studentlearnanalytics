package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Admin implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String dob;
    private String role;
    private String accessLevel;
    private String managedModules;
    private String description;
    private String status;
    private String createdAt;
    private String lastLogin;

    public Admin(String id, String name, String email, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = "+855 12 345 678";
        this.gender = "Male";
        this.dob = "Mar 10, 1988";
        this.role = role != null ? role : "System Administrator";
        this.accessLevel = "Full Access";
        this.managedModules = "All Modules";
        this.description = "Has permission to manage the entire system, subject to assigned access policies.";
        this.status = status != null ? status : "Active";
        this.createdAt = "Sep 01, 2020";
        this.lastLogin = "May 29, 2024, 09:45 AM";
    }

    public Admin(String id, String name, String email, String role, String accessLevel, String managedModules, String status) {
        this(id, name, email, role, status);
        if (accessLevel != null) this.accessLevel = accessLevel;
        if (managedModules != null) this.managedModules = managedModules;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone != null ? phone : "+855 12 345 678"; }
    public String getGender() { return gender != null ? gender : "Male"; }
    public String getDob() { return dob != null ? dob : "Mar 10, 1988"; }
    public String getRole() { return role != null ? role : "System Administrator"; }
    public String getAccessLevel() { return accessLevel != null ? accessLevel : "Full Access"; }
    public String getManagedModules() { return managedModules != null ? managedModules : "All Modules"; }
    public String getDescription() { return description != null ? description : "Has permission to manage the system."; }
    public String getStatus() { return status != null ? status : "Active"; }
    public String getCreatedAt() { return createdAt != null ? createdAt : "Sep 01, 2020"; }
    public String getLastLogin() { return lastLogin != null ? lastLogin : "May 29, 2024, 09:45 AM"; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDob(String dob) { this.dob = dob; }
    public void setRole(String role) { this.role = role; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
    public void setManagedModules(String managedModules) { this.managedModules = managedModules; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
}
