package com.social7.studentlearnanalytics.model;

import com.google.gson.annotations.SerializedName;

public class LearningAnalytic {
    @SerializedName("id")
    public Long id;

    @SerializedName("student_id")
    public Long studentId;

    @SerializedName("semester_id")
    public Long semesterId;

    @SerializedName("period_type")
    public String periodType;

    @SerializedName("period_start")
    public String periodStart;

    @SerializedName("period_end")
    public String periodEnd;

    @SerializedName("attendance_rate")
    public Float attendanceRate;

    @SerializedName("assignment_average")
    public Float assignmentAverage;

    @SerializedName("quiz_average")
    public Float quizAverage;

    @SerializedName("exam_average")
    public Float examAverage;

    @SerializedName("overall_performance")
    public Float overallPerformance;
}
