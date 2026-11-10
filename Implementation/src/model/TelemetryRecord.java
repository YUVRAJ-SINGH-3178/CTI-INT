package model;

import util.ValidationException;

/**
 * Domain entity representing raw or processed vehicle communication telemetry.
 */
public class TelemetryRecord {

    private int recordId;
    private String domain; // "CAN" or "V2X"
    private double timestamp;
    private String sourceId;
    private String payloadData;
    private String statusFlag; // "INGESTED", "PROCESSED", "ANOMALOUS", "BENIGN"
    private String ingestedAt;

    public TelemetryRecord() {
        this.statusFlag = "INGESTED";
    }

    public TelemetryRecord(int recordId, String domain, double timestamp, String sourceId,
                           String payloadData, String statusFlag, String ingestedAt) throws ValidationException {
        this.recordId = recordId;
        setDomain(domain);
        setTimestamp(timestamp);
        setSourceId(sourceId);
        this.payloadData = (payloadData == null) ? "" : payloadData.trim();
        this.statusFlag = (statusFlag == null) ? "INGESTED" : statusFlag.trim().toUpperCase();
        this.ingestedAt = (ingestedAt == null || ingestedAt.isBlank()) ? java.time.LocalDateTime.now().toString() : ingestedAt;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) throws ValidationException {
        if (domain == null || (!domain.equalsIgnoreCase("CAN") && !domain.equalsIgnoreCase("V2X"))) {
            throw new ValidationException("domain", "Domain must be strictly CAN or V2X");
        }
        this.domain = domain.toUpperCase();
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) throws ValidationException {
        if (timestamp < 0.0) {
            throw new ValidationException("timestamp", "Timestamp cannot be negative");
        }
        this.timestamp = timestamp;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) throws ValidationException {
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new ValidationException("sourceId", "Source identifier cannot be empty");
        }
        this.sourceId = sourceId.trim();
    }

    public String getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(String payloadData) {
        this.payloadData = payloadData;
    }

    public String getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(String statusFlag) {
        this.statusFlag = statusFlag;
    }

    public String getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(String ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}

