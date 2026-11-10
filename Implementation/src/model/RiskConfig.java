package model;

import util.ValidationException;

/**
 * Domain entity representing heuristic risk scoring configuration and asset criticality weights.
 */
public class RiskConfig {

    private int configId;
    private String assetName;
    private double criticalityFactor; // [1.0, 5.0]
    private double impactWeight;      // [1.0, 5.0]
    private double baselineThreshold; // e.g. 10.0
    private String updatedAt;

    public RiskConfig() {
        this.criticalityFactor = 3.0;
        this.impactWeight = 3.0;
        this.baselineThreshold = 10.0;
    }

    public RiskConfig(int configId, String assetName, double criticalityFactor,
                      double impactWeight, double baselineThreshold, String updatedAt) throws ValidationException {
        this.configId = configId;
        setAssetName(assetName);
        setCriticalityFactor(criticalityFactor);
        setImpactWeight(impactWeight);
        setBaselineThreshold(baselineThreshold);
        this.updatedAt = (updatedAt == null || updatedAt.isBlank()) ? java.time.LocalDateTime.now().toString() : updatedAt;
    }

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) throws ValidationException {
        if (assetName == null || assetName.trim().isEmpty()) {
            throw new ValidationException("assetName", "Asset name cannot be empty");
        }
        this.assetName = assetName.trim();
    }

    public double getCriticalityFactor() {
        return criticalityFactor;
    }

    public void setCriticalityFactor(double criticalityFactor) throws ValidationException {
        if (criticalityFactor < 0.0) {
            throw new ValidationException("criticalityFactor", "Criticality factor cannot be negative");
        }
        this.criticalityFactor = criticalityFactor;
    }

    public double getImpactWeight() {
        return impactWeight;
    }

    public void setImpactWeight(double impactWeight) throws ValidationException {
        if (impactWeight < 0.0) {
            throw new ValidationException("impactWeight", "Impact weight cannot be negative");
        }
        this.impactWeight = impactWeight;
    }

    public double getBaselineThreshold() {
        return baselineThreshold;
    }

    public void setBaselineThreshold(double baselineThreshold) throws ValidationException {
        if (baselineThreshold < 0.0) {
            throw new ValidationException("baselineThreshold", "Baseline threshold cannot be negative");
        }
        this.baselineThreshold = baselineThreshold;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

