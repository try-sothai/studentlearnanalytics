package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserData user;

    @SerializedName("token")
    private String token;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("student_id")
    private String studentId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public String getToken() {
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return accessToken != null ? accessToken : "";
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStudentId() {
        if (studentId != null && !studentId.isEmpty()) {
            return studentId;
        }
        if (user != null && user.getStudentId() != null && !user.getStudentId().isEmpty()) {
            return user.getStudentId();
        }
        return "";
    }

    public static class UserData {
        @SerializedName("id")
        private Object id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("status")
        private String status;

        @SerializedName("login_id")
        private String loginId;

        @SerializedName("student_id")
        private String studentId;

        @SerializedName("department")
        private String department;

        @SerializedName("year")
        private String year;

        public String getId() {
            return id != null ? String.valueOf(id) : "";
        }

        public String getName() {
            return name != null ? name : "";
        }

        public String getEmail() {
            return email != null ? email : "";
        }

        public String getRole() {
            return role != null ? role : "";
        }

        public String getStatus() {
            return status != null ? status : "";
        }

        public String getStudentId() {
            return studentId != null ? studentId : (loginId != null ? loginId : "");
        }

        public String getLoginId() {
            return getStudentId();
        }

        public String getDepartment() {
            return department != null ? department : "";
        }

        public String getYear() {
            return year != null ? year : "";
        }
    }
}
