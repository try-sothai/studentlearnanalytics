package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Course implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("teacher")
    private String teacher;

    @SerializedName("performance")
    private int performance;

    @SerializedName("credits")
    private int credits;

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

    @SerializedName("department")
    private String department;

    @SerializedName("status")
    private String status = "Active";

    @SerializedName("schedule")
    private String schedule;

    @SerializedName("room")
    private String room;

    @SerializedName("studentCount")
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
        this.semester = "Semester 1, 2024";
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
        this.semester = semester != null ? semester : "Semester 1, 2024";
        this.teacher = "Mr. Chan";
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

    public String getId() { return id != null ? id : ""; }
    public String getCode() { return code != null ? code : ""; }
    public String getName() { return name != null ? name : ""; }
    public String getTeacher() { return teacher != null ? teacher : ""; }
    public int getPerformance() { return performance; }
    public int getCredits() { return credits; }
    public String getType() { return type != null ? type : "Core"; }
    public int getAttendance() { return attendance; }
    public int getAssignments() { return assignments; }
    public int getQuizzes() { return quizzes; }
    public int getExam() { return exam; }
    public String getDepartment() { return department != null ? department : ""; }
    public String getStatus() { return status != null ? status : "Active"; }
    public void setStatus(String status) { this.status = status; }

    public String getSchedule() { return schedule != null ? schedule : ""; }
    public String getRoom() { return room != null ? room : ""; }
    public int getStudentCount() { return studentCount; }
    public int getAtRiskCount() { return atRiskCount; }
    public int getPendingScoresCount() { return pendingScoresCount; }
    public int getIconBgRes() { return iconBgRes; }
    public int getIconRes() { return iconRes; }
    public String getSemester() { return semester != null ? semester : "Semester 1, 2024"; }
}
