package com.social7.studentlearnanalytics.model;

public class Prediction {
    private double predictedScore;
    private int confidence;
    private String riskLevel;
    private String riskDescription;

    public Prediction(double predictedScore, int confidence, String riskLevel, String riskDescription) {
        this.predictedScore = predictedScore;
        this.confidence = confidence;
        this.riskLevel = riskLevel;
        this.riskDescription = riskDescription;
    }

    public double getPredictedScore() { return predictedScore; }
    public int getConfidence() { return confidence; }
    public String getRiskLevel() { return riskLevel; }
    public String getRiskDescription() { return riskDescription; }
}
