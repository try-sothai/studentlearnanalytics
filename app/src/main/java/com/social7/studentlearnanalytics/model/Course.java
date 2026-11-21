package com.social7.studentlearnanalytics.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Course implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName(value = "code", alternate = {"course_code"})
    private String code;

    @SerializedName(value = "name", alternate = {"course_name"})
    private String name;

    @SerializedName(value = "teacher_name", alternate = {"teacherName"})
    private String teacher;

    @SerializedName("performance")
    private int performance;

    @SerializedName(value = "credits", alternate = {"credit"})
    private double credits = 3.0;

    @SerializedName("type")
    private String type; // "Core" or "Elective"

    @SerializedName("attendance")
    private int attendance;

    @SerializedName("assignments")
    private int assignments;

    @SerializedName("quizzes")
    private int quizzes;

    @SerializedName("exam")
    private int exam;

    @SerializedName(value = "department_name", alternate = {"departmentName"})
    private String department;

    @SerializedName("status")
    private String status = "Active";

    @SerializedName("schedule")
    private String schedule;

    @SerializedName("room")
    private String room;

    @SerializedName(value = "studentCount", alternate = {"enrollments_count"})
    private int studentCount;

    @SerializedName("atRiskCount")
    private int atRiskCount;

    @SerializedName("pendingScoresCount")
    private int pendingScoresCount;

    @SerializedName("iconBgRes")
    private int iconBgRes;

    @SerializedName("iconRes")
    private int iconRes;

    @SerializedName("semester")
    private String semester;

    @SerializedName("description")
    private String description;

    @SerializedName("department_id")
    private int departmentId = -1;

    @SerializedName("department_code")
    private String departmentCode;

    @SerializedName("program_id")
    private int programId = -1;

    @SerializedName("program_name")
    private String programName;

    @SerializedName("program_code")
    private String programCode;

    @SerializedName("teacher_id")
    private int teacherId = -1;

    @SerializedName("academic_year_id")
    private int academicYearId = -1;

    @SerializedName("academic_year")
    private String academicYear;

    @SerializedName("semester_id")
    private int semesterId = -1;

    @SerializedName("classes_summary")
    private String classesSummary;

    @SerializedName("offerings_count")
    private int offeringsCount;

    @SerializedName(" assessments_count")
    private int assessmentsCount;

    private String rawJson;

    public Course() {
    }

    public Course(String id, String code, String name, String teacher, int performance,
                  int credits, String type, int attendance, int assignments,
                  int quizzes, int exam, String department) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.teacher = teacher;
        this.performance = performance;
        this.credits = credits;
        this.type = type;
        this.attendance = attendance;
        this.assignments = assignments;
        this.quizzes = quizzes;
        this.exam = exam;
        this.department = department;
        this.status = "Active";
        this.semester = "Semester 1";
    }

    public Course(String id, String code, String name, String department, String schedule, String room,
                  int studentCount, int atRiskCount, int pendingScoresCount, int iconBgRes, int iconRes, String semester) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.department = department;
        this.schedule = schedule;
        this.room = room;
        this.studentCount = studentCount;
        this.atRiskCount = atRiskCount;
        this.pendingScoresCount = pendingScoresCount;
        this.iconBgRes = iconBgRes;
        this.iconRes = iconRes;
        this.semester = semester != null ? semester : "Semester 1";
        this.teacher = "";
        this.status = "Active";
        this.type = "Core";
        this.credits = 3;
    }

    public Course(String code, String name, int credits, String status) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.status = status;
        this.type = "Core";
    }

    public Course(String id, String code, String name, String teacher, String semester, int credits, String room, String schedule, int studentCount, int performance, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.teacher = teacher;
        this.semester = semester;
        this.credits = credits;
        this.room = room;
        this.schedule = schedule;
        this.studentCount = studentCount;
        this.performance = performance;
        this.status = status;
        this.type = "Core";
    }

    public static Course fromJson(JsonObject obj) {
        Course c = new Course();
        if (obj == null) return c;
        c.rawJson = obj.toString();
        c.id = optString(obj, "id", "");
        c.code = optString(obj, "code", optString(obj, "course_code", ""));
        c.name = optString(obj, "name", optString(obj, "course_name", ""));
        c.description = optString(obj, "description", "");
        c.credits = optDouble(obj, "credit", optDouble(obj, "credits", 3.0));

        String rawStatus = optString(obj, "status", "active");
        c.status = "inactive".equalsIgnoreCase(rawStatus) ? "Inactive" : "Active";

        c.departmentId = optInt(obj, "department_id", -1);
        c.departmentCode = optString(obj, "department_code", "");
        c.department = optString(obj, "department_name", "");
        if (c.department.isEmpty() && obj.has("department") && !obj.get("department").isJsonNull()) {
            JsonElement dEl = obj.get("department");
            if (dEl.isJsonObject()) {
                JsonObject dObj = dEl.getAsJsonObject();
                c.department = optString(dObj, "name", "");
                if (c.departmentCode.isEmpty()) c.departmentCode = optString(dObj, "code", "");
                if (c.departmentId <= 0) c.departmentId = optInt(dObj, "id", -1);
            } else if (dEl.isJsonPrimitive()) {
                c.department = dEl.getAsString();
            }
        }

        c.programId = optInt(obj, "program_id", -1);
        c.programName = optString(obj, "program_name", "");
        c.programCode = optString(obj, "program_code", "");

        c.teacherId = optInt(obj, "teacher_id", -1);
        c.teacher = optString(obj, "teacher_name", "");
        if (c.teacher.isEmpty() && obj.has("teacher") && !obj.get("teacher").isJsonNull()) {
            JsonElement tEl = obj.get("teacher");
            if (tEl.isJsonObject()) {
                JsonObject tObj = tEl.getAsJsonObject();
                String tName = optString(tObj, "name", "");
                if (tName.isEmpty()) {
                    tName = (optString(tObj, "first_name", "") + " " + optString(tObj, "last_name", "")).trim();
                }
                c.teacher = tName;
                if (c.teacherId <= 0) c.teacherId = optInt(tObj, "id", -1);
            } else if (tEl.isJsonPrimitive()) {
                c.teacher = tEl.getAsString();
            }
        }

        c.academicYearId = optInt(obj, "academic_year_id", -1);
        c.academicYear = optString(obj, "academic_year", "");
        c.semesterId = optInt(obj, "semester_id", -1);
        c.semester = optString(obj, "semester", "");
        c.room = optString(obj, "room", "");
        c.classesSummary = optString(obj, "classes_summary", "");
        c.offeringsCount = optInt(obj, "offerings_count", 0);
        c.studentCount = optInt(obj, "enrollments_count", optInt(obj, "studentCount", 0));
        c.assessmentsCount = optInt(obj, "assessments_count", 0);

        String day = optString(obj, "day_of_week", "");
        String st = optString(obj, "start_time", "");
        String et = optString(obj, "end_time", "");
        if (!day.isEmpty() && !st.isEmpty()) {
            c.schedule = day + " " + st + (et.isEmpty() ? "" : "-" + et);
        } else {
            c.schedule = optString(obj, "schedule", "");
        }

        return c;
    }

    private static String optString(JsonObject o, String key, String fallback) {
        if (o == null || !o.has(key) || o.get(key) == null || o.get(key).isJsonNull()) return fallback;
        try {
            if (o.get(key).isJsonPrimitive()) return o.get(key).getAsString();
        } catch (Exception ignored) {}
        return fallback;
    }

    private static int optInt(JsonObject o, String key, int fallback) {
        if (o == null || !o.has(key) || o.get(key) == null || o.get(key).isJsonNull()) return fallback;
        try {
            if (o.get(key).isJsonPrimitive()) return o.get(key).getAsInt();
        } catch (Exception ignored) {}
        return fallback;
    }

    private static double optDouble(JsonObject o, String key, double fallback) {
        if (o == null || !o.has(key) || o.get(key) == null || o.get(key).isJsonNull()) return fallback;
        try {
            if (o.get(key).isJsonPrimitive()) return o.get(key).getAsDouble();
        } catch (Exception ignored) {}
        return fallback;
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code != null ? code : ""; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }
    public String getTeacher() { return teacher != null ? teacher : ""; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public int getPerformance() { return performance; }
    public int getCredits() { return (int) Math.round(credits); }
    public double getCreditHours() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }
    public String getType() { return type != null ? type : "Core"; }
    public int getAttendance() { return attendance; }
    public int getAssignments() { return assignments; }
    public int getQuizzes() { return quizzes; }
    public int getExam() { return exam; }
    public String getDepartment() { return department != null ? department : ""; }
    public void setDepartment(String department) { this.department = department; }
    public String getStatus() { return status != null ? status : "Active"; }
    public void setStatus(String status) { this.status = status; }

    public String getSchedule() { return schedule != null ? schedule : ""; }
    public String getRoom() { return room != null ? room : ""; }
    public int getStudentCount() { return studentCount; }
    public int getAtRiskCount() { return atRiskCount; }
    public int getPendingScoresCount() { return pendingScoresCount; }
    public int getIconBgRes() { return iconBgRes; }
    public int getIconRes() { return iconRes; }
    public String getSemester() { return semester != null && !semester.isEmpty() ? semester : "Semester 1"; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentCode() { return departmentCode != null ? departmentCode : ""; }
    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }
    public String getProgramName() { return programName != null ? programName : ""; }
    public void setProgramName(String programName) { this.programName = programName; }
    public String getProgramCode() { return programCode != null ? programCode : ""; }
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public String getAcademicYear() { return academicYear != null ? academicYear : ""; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public int getSemesterId() { return semesterId; }
    public void setSemesterId(int semesterId) { this.semesterId = semesterId; }
    public String getClassesSummary() { return classesSummary != null ? classesSummary : ""; }
    public int getOfferingsCount() { return offeringsCount; }
    public int getEnrollmentsCount() { return studentCount; }
    public int getAssessmentsCount() { return assessmentsCount; }
    public String getRawJson() { return rawJson != null ? rawJson : ""; }
}