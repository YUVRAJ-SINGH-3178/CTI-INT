package dao;

import model.Alert;
import model.CanAlert;
import model.V2xAlert;
import util.DatabaseConnection;
import util.DataAccessException;
import util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Alert entities, implementing Repository<Alert, Integer>.
 * Executes all database operations using PreparedStatements and try-with-resources.
 */
public class AlertDao implements Repository<Alert, Integer> {

    @Override
    public Alert save(Alert alert) throws DataAccessException, ValidationException {
        if (alert == null) {
            throw new ValidationException("alert", "Alert cannot be null");
        }

        String sql = "INSERT INTO alerts (sample_id, domain, attack_type, confidence, risk_score, " +
                     "mitre_technique, technique_name, severity, target_assets, cwe_id, timestamp, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, alert.getSampleId());
            pstmt.setString(2, alert.getDomain());
            pstmt.setString(3, alert.getAttackType());
            pstmt.setDouble(4, alert.getConfidence());
            pstmt.setDouble(5, alert.getRiskScore());
            pstmt.setString(6, alert.getMitreTechnique());
            pstmt.setString(7, alert.getTechniqueName());
            pstmt.setString(8, alert.getSeverity());
            pstmt.setString(9, alert.getTargetAssets());
            pstmt.setString(10, alert.getCweId());
            pstmt.setString(11, alert.getTimestamp());
            pstmt.setString(12, alert.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Creating alert failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    alert.setAlertId(generatedKeys.getInt(1));
                }
            }
            return alert;

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting alert into database: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Alert> findById(Integer alertId) throws DataAccessException {
        String sql = "SELECT * FROM alerts WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, alertId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAlert(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding alert by ID " + alertId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Alert> findAll() throws DataAccessException {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT * FROM alerts ORDER BY risk_score DESC, alert_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToAlert(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all alerts: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Alert alert) throws DataAccessException, ValidationException {
        if (alert == null || alert.getAlertId() <= 0) {
            throw new ValidationException("alertId", "Valid alert ID required for update");
        }

        String sql = "UPDATE alerts SET sample_id = ?, domain = ?, attack_type = ?, confidence = ?, " +
                     "risk_score = ?, mitre_technique = ?, technique_name = ?, severity = ?, " +
                     "target_assets = ?, cwe_id = ?, timestamp = ?, status = ? WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, alert.getSampleId());
            pstmt.setString(2, alert.getDomain());
            pstmt.setString(3, alert.getAttackType());
            pstmt.setDouble(4, alert.getConfidence());
            pstmt.setDouble(5, alert.getRiskScore());
            pstmt.setString(6, alert.getMitreTechnique());
            pstmt.setString(7, alert.getTechniqueName());
            pstmt.setString(8, alert.getSeverity());
            pstmt.setString(9, alert.getTargetAssets());
            pstmt.setString(10, alert.getCweId());
            pstmt.setString(11, alert.getTimestamp());
            pstmt.setString(12, alert.getStatus());
            pstmt.setInt(13, alert.getAlertId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating alert ID " + alert.getAlertId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer alertId) throws DataAccessException {
        String sql = "DELETE FROM alerts WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, alertId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting alert ID " + alertId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Searches alerts matching a keyword in attack type, technique, assets, or severity.
     */
    public List<Alert> searchAlerts(String keyword) throws DataAccessException {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE attack_type LIKE ? OR mitre_technique LIKE ? " +
                     "OR technique_name LIKE ? OR target_assets LIKE ? OR severity LIKE ? " +
                     "ORDER BY risk_score DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, term);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAlert(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error searching alerts with keyword '" + keyword + "': " + e.getMessage(), e);
        }
    }

    /**
     * Executes bulk insertion of alerts using a JDBC batch transaction.
     */
    public int saveAllBatch(List<Alert> alerts) throws DataAccessException {
        if (alerts == null || alerts.isEmpty()) return 0;

        String sql = "INSERT INTO alerts (sample_id, domain, attack_type, confidence, risk_score, " +
                     "mitre_technique, technique_name, severity, target_assets, cwe_id, timestamp, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Alert a : alerts) {
                    pstmt.setInt(1, a.getSampleId());
                    pstmt.setString(2, a.getDomain());
                    pstmt.setString(3, a.getAttackType());
                    pstmt.setDouble(4, a.getConfidence());
                    pstmt.setDouble(5, a.getRiskScore());
                    pstmt.setString(6, a.getMitreTechnique());
                    pstmt.setString(7, a.getTechniqueName());
                    pstmt.setString(8, a.getSeverity());
                    pstmt.setString(9, a.getTargetAssets());
                    pstmt.setString(10, a.getCweId());
                    pstmt.setString(11, a.getTimestamp());
                    pstmt.setString(12, a.getStatus());
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
                return results.length;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                throw new DataAccessException("Batch alert insertion transaction rolled back: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database transaction error: " + e.getMessage(), e);
        }
    }

    private Alert mapRowToAlert(ResultSet rs) throws SQLException {
        try {
            int alertId = rs.getInt("alert_id");
            int sampleId = rs.getInt("sample_id");
            String domain = rs.getString("domain");
            String attack = rs.getString("attack_type");
            double conf = rs.getDouble("confidence");
            double risk = rs.getDouble("risk_score");
            String tech = rs.getString("mitre_technique");
            String techName = rs.getString("technique_name");
            String sev = rs.getString("severity");
            String assets = rs.getString("target_assets");
            String cwe = rs.getString("cwe_id");
            String time = rs.getString("timestamp");
            String status = rs.getString("status");

            if ("CAN".equalsIgnoreCase(domain)) {
                // Empirical CICIoV2024 CAN arbitration IDs (decimal 291=0x123, 513=0x201, 476=0x1DC, 344=0x158, 128=0x080)
                int canId = 0;
                String upper = attack.toUpperCase();
                if (upper.contains("DOS")) canId = 291;
                else if (upper.contains("GAS")) canId = 513;
                else if (upper.contains("RPM")) canId = 476;
                else if (upper.contains("SPEED")) canId = 344;
                else if (upper.contains("STEERING")) canId = 128;
                return new CanAlert(alertId, sampleId, attack, conf, risk, tech, techName,
                                    sev, assets, cwe, time, status, canId, "", 8);
            } else {
                return new V2xAlert(alertId, sampleId, attack, conf, risk, tech, techName,
                                    sev, assets, cwe, time, status, sampleId, 0, 0.0, 0.0, 0.0);
            }
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping database row to Alert entity: " + e.getMessage(), e);
        }
    }
}

