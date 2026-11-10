package model;

import util.ValidationException;

/**
 * Domain entity representing an audit log entry for exported CSV threat reports.
 */
public class AuditReport {

    private int reportId;
    private String reportTitle;
    private String exportedBy;
    private String filePath;
    private int recordCount;
    private String generatedAt;

    public AuditReport() {}

    public AuditReport(int reportId, String reportTitle, String exportedBy, String filePath,
                       int recordCount, String generatedAt) throws ValidationException {
        this.reportId = reportId;
        setReportTitle(reportTitle);
        setExportedBy(exportedBy);
        setFilePath(filePath);
        setRecordCount(recordCount);
        this.generatedAt = (generatedAt == null || generatedAt.isBlank()) ? java.time.LocalDateTime.now().toString() : generatedAt;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) throws ValidationException {
        if (reportTitle == null || reportTitle.trim().isEmpty()) {
            throw new ValidationException("reportTitle", "Report title cannot be empty");
        }
        this.reportTitle = reportTitle.trim();
    }

    public String getExportedBy() {
        return exportedBy;
    }

    public void setExportedBy(String exportedBy) throws ValidationException {
        if (exportedBy == null || exportedBy.trim().isEmpty()) {
            throw new ValidationException("exportedBy", "Exported by user cannot be empty");
        }
        this.exportedBy = exportedBy.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) throws ValidationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("filePath", "File path cannot be empty");
        }
        this.filePath = filePath.trim();
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) throws ValidationException {
        if (recordCount < 0) {
            throw new ValidationException("recordCount", "Record count cannot be negative");
        }
        this.recordCount = recordCount;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}

