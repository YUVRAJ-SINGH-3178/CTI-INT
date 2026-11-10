package model;

import util.ValidationException;

/**
 * Concrete Alert subclass representing inter-vehicle V2X (Vehicle-to-Everything) intrusions.
 * Demonstrates Inheritance and Polymorphic risk calculation.
 */
public class V2xAlert extends Alert {

    private int senderId;
    private int receiverId;
    private double posX;
    private double posY;
    private double speed;

    public V2xAlert() {
        super();
        try {
            setDomain("V2X");
        } catch (ValidationException ignored) {}
    }

    public V2xAlert(int alertId, int sampleId, String attackType, double confidence,
                    double riskScore, String mitreTechnique, String techniqueName, String severity,
                    String targetAssets, String cweId, String timestamp, String status,
                    int senderId, int receiverId, double posX, double posY, double speed) throws ValidationException {
        super(alertId, sampleId, "V2X", attackType, confidence, riskScore, mitreTechnique,
              techniqueName, severity, targetAssets, cweId, timestamp, status);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.posX = posX;
        this.posY = posY;
        this.speed = speed;
    }

    @Override
    public String getDomain() {
        return "V2X";
    }

    /**
     * Heuristic risk computation for the V2X wireless domain.
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
        if (receiverId == 0 && posX == 0.0 && posY == 0.0 && speed == 0.0) {
            return String.format("V2X Evaluation Sample ID: %d | Spatial Trajectory: Not persisted in summary alert row (inspect VeReMi .tgz stream)",
                    getSampleId());
        }
        return String.format("V2X Transmitter ID: %d | Receiver ID: %d | Pos: (%.1f, %.1f) | Velocity: %.1f m/s",
                senderId, receiverId, posX, posY, speed);
    }

    @Override
    public String getDomainExplanation() {
        if (receiverId == 0 && posX == 0.0 && posY == 0.0 && speed == 0.0) {
            return String.format("V2X DSRC/C-V2X alert (Sample #%d) classified from VeReMi simulation kinematics.", getSampleId());
        }
        return String.format("V2X DSRC/C-V2X broadcast: Wireless beacon from vehicle %d to station %d at velocity %.1f m/s.", senderId, receiverId, speed);
    }

    @Override
    public String describe() {
        return super.describe() + String.format(" [Sender: %d, Spd: %.1f m/s]", senderId, speed);
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}

