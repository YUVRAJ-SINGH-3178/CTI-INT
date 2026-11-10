package dao;

import model.AuditReport;
import util.DatabaseConnection;
import util.DataAccessException;
import util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for audit logging of exported threat intelligence reports.
 */
public class AuditReportDao implements Repository<AuditReport, Integer> {

    @Override
    public AuditReport save(AuditReport report) throws DataAccessException, ValidationException {
        if (report == null) {
            throw new ValidationException("report", "Audit report cannot be null");
        }

        String sql = "INSERT INTO audit_reports (report_title, exported_by, file_path, record_count, generated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, report.getReportTitle());
            pstmt.setString(2, report.getExportedBy());
            pstmt.setString(3, report.getFilePath());
            pstmt.setInt(4, report.getRecordCount());
            pstmt.setString(5, report.getGeneratedAt());

            pstmt.executeUpdate();
            try (ResultSet gk = pstmt.getGeneratedKeys()) {
                if (gk.next()) {
                    report.setReportId(gk.getInt(1));
                }
            }
            return report;

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting audit report: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AuditReport> findById(Integer reportId) throws DataAccessException {
        String sql = "SELECT * FROM audit_reports WHERE report_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToReport(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding audit report ID " + reportId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<AuditReport> findAll() throws DataAccessException {
        List<AuditReport> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_reports ORDER BY report_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToReport(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all audit reports: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(AuditReport report) throws DataAccessException, ValidationException {
        if (report == null || report.getReportId() <= 0) {
            throw new ValidationException("reportId", "Valid report ID required for update");
        }

        String sql = "UPDATE audit_reports SET report_title = ?, exported_by = ?, file_path = ?, " +
                     "record_count = ?, generated_at = ? WHERE report_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, report.getReportTitle());
            pstmt.setString(2, report.getExportedBy());
            pstmt.setString(3, report.getFilePath());
            pstmt.setInt(4, report.getRecordCount());
            pstmt.setString(5, report.getGeneratedAt());
            pstmt.setInt(6, report.getReportId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating audit report " + report.getReportId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer reportId) throws DataAccessException {
        String sql = "DELETE FROM audit_reports WHERE report_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting audit report " + reportId + ": " + e.getMessage(), e);
        }
    }

    private AuditReport mapRowToReport(ResultSet rs) throws SQLException {
        try {
            return new AuditReport(
                rs.getInt("report_id"),
                rs.getString("report_title"),
                rs.getString("exported_by"),
                rs.getString("file_path"),
                rs.getInt("record_count"),
                rs.getString("generated_at")
            );
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping audit report: " + e.getMessage(), e);
        }
    }
}

