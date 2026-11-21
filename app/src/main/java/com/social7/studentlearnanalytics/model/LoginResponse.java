package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    @SerializedName("login_id")
    private String loginId;

    @SerializedName("role")
    private String role;

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
        if (loginId != null && !loginId.isEmpty()) {
            return loginId;
        }
        if (studentId != null && !studentId.isEmpty()) {
            return studentId;
        }
        if (user != null && user.getLoginId() != null && !user.getLoginId().isEmpty()) {
            return user.getLoginId();
        }
        return "";
    }

    public String getRole() {
        if (role != null && !role.isEmpty()) {
            return role;
        }
        return user != null ? user.getRole() : "";
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

        @SerializedName("teacher_id")
        private String teacherId;

        @SerializedName("admin_id")
        private String adminId;

        @SerializedName("department")
        private String department;

        @SerializedName("year")
        private String year;

        @SerializedName("student")
        private JsonElement student;

        @SerializedName("teacher")
        private JsonElement teacher;

        @SerializedName("admin")
        private JsonElement admin;

        public String getId() {
            if (id instanceof Number) {
                return String.valueOf(((Number) id).longValue());
            }
            return id != null ? String.valueOf(id) : "";
        }

        public String getStudentDbId() {
            if (student != null && student.isJsonObject()) {
                JsonObject studentObj = student.getAsJsonObject();
                if (studentObj.has("id") && !studentObj.get("id").isJsonNull()) {
                    return String.valueOf(studentObj.get("id").getAsLong());
                }
            }
            return "";
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
            if (studentId != null && !studentId.isEmpty()) {
                return studentId;
            }
            if (teacherId != null && !teacherId.isEmpty()) {
                return teacherId;
            }
            if (adminId != null && !adminId.isEmpty()) {
                return adminId;
            }
            if (teacher != null && teacher.isJsonObject()) {
                JsonObject tObj = teacher.getAsJsonObject();
                if (tObj.has("teacher_code") && !tObj.get("teacher_code").isJsonNull()) {
                    return tObj.get("teacher_code").getAsString();
                }
            }
            if (student != null && student.isJsonObject()) {
                JsonObject studentObj = student.getAsJsonObject();
                if (studentObj.has("student_code") && !studentObj.get("student_code").isJsonNull()) {
                    return studentObj.get("student_code").getAsString();
                }
            }
            if (admin != null && admin.isJsonObject()) {
                JsonObject aObj = admin.getAsJsonObject();
                if (aObj.has("admin_code") && !aObj.get("admin_code").isJsonNull()) {
                    return aObj.get("admin_code").getAsString();
                }
            }
            return loginId != null ? loginId : "";
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
