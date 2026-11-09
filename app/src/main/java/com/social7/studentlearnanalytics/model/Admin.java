package com.social7.studentlearnanalytics.model;

import java.io.Serializable;

public class Admin implements Serializable {
    private String id;
    private Long dbId;
    private Long userId;
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
        this.role = role != null ? role : "System Administrator";
        this.accessLevel = "Full Access";
        this.managedModules = "All Modules";
        this.description = "Has permission to manage the entire system, subject to assigned access policies.";
        this.status = status != null ? status : "Active";
    }

    public Admin(String id, String name, String email, String role, String accessLevel, String managedModules, String status) {
        this(id, name, email, role, status);
        if (accessLevel != null) this.accessLevel = accessLevel;
        if (managedModules != null) this.managedModules = managedModules;
    }

    public String getId() { return id; }
    public Long getDbId() { return dbId; }
    public void setDbId(Long dbId) { this.dbId = dbId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name != null ? name : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getPhone() { return phone != null && !phone.isEmpty() ? phone : "N/A"; }
    public String getGender() { return gender != null && !gender.isEmpty() ? gender : "N/A"; }
    public String getDob() { return dob != null && !dob.isEmpty() ? dob : "N/A"; }
    public String getRole() { return role != null && !role.isEmpty() ? role : "System Administrator"; }
    public String getAccessLevel() { return accessLevel != null && !accessLevel.isEmpty() ? accessLevel : "Full Access"; }
    public String getManagedModules() { return managedModules != null && !managedModules.isEmpty() ? managedModules : "All Modules"; }
    public String getDescription() { return description != null ? description : "Has permission to manage the system."; }
    public String getStatus() { return status != null && !status.isEmpty() ? status : "Active"; }
    public String getCreatedAt() { return createdAt != null && !createdAt.isEmpty() ? createdAt : "N/A"; }
    public String getLastLogin() { return lastLogin != null && !lastLogin.isEmpty() ? lastLogin : "N/A"; }

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
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
