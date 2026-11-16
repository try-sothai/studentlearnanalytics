package com.social7.studentlearnanalytics.network;

import com.google.gson.annotations.SerializedName;
import com.social7.studentlearnanalytics.model.LearningAnalytic;
import java.util.List;

public class LearningAnalyticsResponse {
    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<LearningAnalytic> data;
}