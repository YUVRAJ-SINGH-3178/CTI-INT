package model;

/**
 * Domain entity representing experimental performance benchmark metrics.
 */
public class ModelMetric {

    private String modelName;
    private String domain; // "CAN", "V2X", or "Overall"
    private double accuracy;
    private double precision;
    private double recall;
    private double f1Score;
    private double falsePositiveRate;
    private double latencyMs;

    public ModelMetric(String modelName, String domain, double accuracy, double precision,
                       double recall, double f1Score, double falsePositiveRate, double latencyMs) {
        this.modelName = modelName;
        this.domain = domain;
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.f1Score = f1Score;
        this.falsePositiveRate = falsePositiveRate;
        this.latencyMs = latencyMs;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDomain() {
        return domain;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1Score() {
        return f1Score;
    }

    public double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public double getLatencyMs() {
        return latencyMs;
    }
}

