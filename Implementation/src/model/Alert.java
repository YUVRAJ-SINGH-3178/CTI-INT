package model;

import util.ValidationException;

/**
 * Abstract Base Class representing an intrusion detection alert in the connected vehicle network.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Alert {

    private int alertId;
    private int sampleId;
    private String domain; // "CAN" or "V2X"
    private String attackType;
    private double confidence; // [0.0, 1.0]
    private double riskScore;  // Heuristic: Criticality x Impact x Probability
    private String mitreTechnique;
    private String techniqueName;
    private String severity; // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    private String targetAssets;
    private String cweId;
    private String timestamp;
    private String status; // "NEW", "ACKNOWLEDGED", "RESOLVED", "DISMISSED"

    public Alert() {
        this.status = "NEW";
    }

    public Alert(int alertId, int sampleId, String domain, String attackType, double confidence,
                 double riskScore, String mitreTechnique, String techniqueName, String severity,
                 String targetAssets, String cweId, String timestamp, String status) throws ValidationException {
        this.alertId = alertId;
        this.sampleId = sampleId;
        setDomain(domain);
        setAttackType(attackType);
        setConfidence(confidence);
        setRiskScore(riskScore);
        setMitreTechnique(mitreTechnique);
        this.techniqueName = (techniqueName == null || techniqueName.isBlank()) ? "Unknown Technique" : techniqueName.trim();
        setSeverity(severity);
        this.targetAssets = (targetAssets == null) ? "[]" : targetAssets.trim();
        this.cweId = (cweId == null || cweId.isBlank()) ? "CWE-UNKNOWN" : cweId.trim();
        this.timestamp = (timestamp == null || timestamp.isBlank()) ? java.time.LocalDateTime.now().toString() : timestamp;
        setStatus(status);
    }

    // --- Abstract Methods (Abstraction & Polymorphism) ---

    /**
     * Returns the vehicular communication domain.
     */
    public abstract String getDomain();

    /**
     * Calculates the heuristic risk score based on asset criticality and attack impact.
     * Heuristic formula: Criticality x Impact Weight x Predicted Probability (confidence), rounded to 2 decimals.
     */
    public abstract double calculateHeuristicRisk(double assetCriticality, double impactWeight);

    /**
     * Returns domain-specific physical details (e.g. CAN ID or V2X coordinates).
     */
    public abstract String getDomainSpecificDetails();

    /**
     * Returns a domain-specific explanation string describing physical vehicular network context.
     */
    public abstract String getDomainExplanation();

    // --- Concrete Methods ---

    public String describe() {
        return String.format("[%s Alert] ID:%d | %s | Conf:%.2f | Risk:%.2f | %s (%s)",
                getDomain(), alertId, attackType, confidence, riskScore, mitreTechnique, severity);
    }

    public String toCsvRow() {
        return String.format("%d,%d,%s,\"%s\",%.4f,%.2f,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                alertId, sampleId, getDomain(), attackType, confidence, riskScore,
                mitreTechnique, techniqueName, severity, targetAssets, cweId, timestamp, status);
    }

    // --- Encapsulation: Validated Getters and Setters ---

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public void setDomain(String domain) throws ValidationException {
        if (domain == null || (!domain.equalsIgnoreCase("CAN") && !domain.equalsIgnoreCase("V2X"))) {
            throw new ValidationException("domain", "Domain must be strictly either 'CAN' or 'V2X'");
        }
        this.domain = domain.toUpperCase();
    }

    public String getAttackType() {
        return attackType;
    }

    public void setAttackType(String attackType) throws ValidationException {
        if (attackType == null || attackType.trim().isEmpty()) {
            throw new ValidationException("attackType", "Attack type cannot be null or empty");
        }
        this.attackType = attackType.trim();
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) throws ValidationException {
        if (confidence < 0.0 || confidence > 1.0) {
            throw new ValidationException("confidence", "Confidence probability must be between 0.0 and 1.0. Given: " + confidence);
        }
        this.confidence = confidence;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) throws ValidationException {
        if (riskScore < 0.0) {
            throw new ValidationException("riskScore", "Risk score cannot be negative. Given: " + riskScore);
        }
        this.riskScore = riskScore;
    }

    public String getMitreTechnique() {
        return mitreTechnique;
    }

    public void setMitreTechnique(String mitreTechnique) throws ValidationException {
        if (mitreTechnique == null || mitreTechnique.trim().isEmpty()) {
            throw new ValidationException("mitreTechnique", "MITRE ATT&CK technique cannot be empty");
        }
        this.mitreTechnique = mitreTechnique.trim();
    }

    public String getTechniqueName() {
        return techniqueName;
    }

    public void setTechniqueName(String techniqueName) {
        this.techniqueName = techniqueName;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) throws ValidationException {
        if (severity == null) {
            throw new ValidationException("severity", "Severity cannot be null");
        }
        String upper = severity.trim().toUpperCase();
        if (!upper.equals("CRITICAL") && !upper.equals("HIGH") && !upper.equals("MEDIUM") && !upper.equals("LOW")) {
            throw new ValidationException("severity", "Severity must be CRITICAL, HIGH, MEDIUM, or LOW. Given: " + severity);
        }
        this.severity = upper;
    }

    public String getTargetAssets() {
        return targetAssets;
    }

    public void setTargetAssets(String targetAssets) {
        this.targetAssets = targetAssets;
    }

    public String getCweId() {
        return cweId;
    }

    public void setCweId(String cweId) {
        this.cweId = cweId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) throws ValidationException {
        if (status == null) {
            this.status = "NEW";
            return;
        }
        String upper = status.trim().toUpperCase();
        if (!upper.equals("NEW") && !upper.equals("ACKNOWLEDGED") && !upper.equals("RESOLVED") && !upper.equals("DISMISSED")) {
            throw new ValidationException("status", "Invalid status: " + status);
        }
        this.status = upper;
    }
}

