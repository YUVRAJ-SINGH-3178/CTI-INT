package model;

import util.ValidationException;

/**
 * Concrete Alert subclass representing in-vehicle Controller Area Network (CAN) intrusions.
 * Demonstrates Inheritance and Polymorphic risk calculation.
 */
public class CanAlert extends Alert {

    private int canId;
    private String payloadHex;
    private int dlc; // Data Length Code (typically 8)

    public CanAlert() {
        super();
        try {
            setDomain("CAN");
        } catch (ValidationException ignored) {}
    }

    public CanAlert(int alertId, int sampleId, String attackType, double confidence,
                    double riskScore, String mitreTechnique, String techniqueName, String severity,
                    String targetAssets, String cweId, String timestamp, String status,
                    int canId, String payloadHex, int dlc) throws ValidationException {
        super(alertId, sampleId, "CAN", attackType, confidence, riskScore, mitreTechnique,
              techniqueName, severity, targetAssets, cweId, timestamp, status);
        this.canId = canId;
        this.payloadHex = (payloadHex == null) ? "00 00 00 00 00 00 00 00" : payloadHex;
        this.dlc = (dlc <= 0) ? 8 : dlc;
    }

    @Override
    public String getDomain() {
        return "CAN";
    }

    /**
     * Heuristic risk computation for the CAN in-vehicle domain.
     * Evaluates: Criticality x Impact Weight x Predicted Probability (confidence), rounded to 2 decimals.
     */
    @Override
    public double calculateHeuristicRisk(double assetCriticality, double impactWeight) {
        double rawRisk = assetCriticality * impactWeight * getConfidence();
        double rounded = Math.round(rawRisk * 100.0) / 100.0;
        try {
            setRiskScore(rounded);
        } catch (ValidationException ignored) {}
        return rounded;
    }

    @Override
    public String getDomainSpecificDetails() {
        if (payloadHex == null || payloadHex.isBlank()) {
            return String.format("CAN Arbitration ID: 0x%03X (%d) | DLC: %d | Payload: Not persisted in summary alert row (inspect CICIoV2024 CSV stream)",
                    canId, canId, dlc);
        }
        return String.format("CAN Arbitration ID: 0x%03X (%d) | DLC: %d | Payload: %s",
                canId, canId, dlc, payloadHex);
    }

    @Override
    public String getDomainExplanation() {
        return String.format("CAN in-vehicle bus transmission: Frame 0x%03X broadcast on powertrain/chassis bus with DLC %d.", canId, dlc);
    }

    @Override
    public String describe() {
        return super.describe() + String.format(" [CAN ID: 0x%03X]", canId);
    }

    public int getCanId() {
        return canId;
    }

    public void setCanId(int canId) {
        this.canId = canId;
    }

    public String getPayloadHex() {
        return payloadHex;
    }

    public void setPayloadHex(String payloadHex) {
        this.payloadHex = payloadHex;
    }

    public int getDlc() {
        return dlc;
    }

    public void setDlc(int dlc) {
        this.dlc = dlc;
    }
}

