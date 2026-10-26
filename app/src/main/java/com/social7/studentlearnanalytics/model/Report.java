package com.social7.studentlearnanalytics.model;

public class Report {
    private String studentName;
    private String studentId;
    private String department;
    private String semester;
    private int attendanceRate;
    private int assignmentScore;
    private int quizScore;
    private double gpa;
    private double predictedExamScore;
    private String riskLevel;

    public Report(String studentName, String studentId, String department, String semester,
                  int attendanceRate, int assignmentScore, int quizScore,
                  double gpa, double predictedExamScore, String riskLevel) {
        this.studentName = studentName;
        this.studentId = studentId;
        this.department = department;
        this.semester = semester;
        this.attendanceRate = attendanceRate;
        this.assignmentScore = assignmentScore;
        this.quizScore = quizScore;
        this.gpa = gpa;
        this.predictedExamScore = predictedExamScore;
        this.riskLevel = riskLevel;
    }

    public String getStudentName() { return studentName; }
    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public String getSemester() { return semester; }
    public int getAttendanceRate() { return attendanceRate; }
    public int getAssignmentScore() { return assignmentScore; }
    public int getQuizScore() { return quizScore; }
    public double getGpa() { return gpa; }
    public double getPredictedExamScore() { return predictedExamScore; }
    public String getRiskLevel() { return riskLevel; }
}
