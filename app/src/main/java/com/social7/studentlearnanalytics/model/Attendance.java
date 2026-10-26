package com.social7.studentlearnanalytics.model;

public class Attendance {
    private int totalClasses;
    private int presentClasses;
    private int absentClasses;
    private int attendanceRate;

    public Attendance(int totalClasses, int presentClasses, int absentClasses) {
        this.totalClasses = totalClasses;
        this.presentClasses = presentClasses;
        this.absentClasses = absentClasses;
        this.attendanceRate = totalClasses > 0 ? (presentClasses * 100 / totalClasses) : 0;
    }

    public int getTotalClasses() { return totalClasses; }
    public int getPresentClasses() { return presentClasses; }
    public int getAbsentClasses() { return absentClasses; }
    public int getAttendanceRate() { return attendanceRate; }
}
