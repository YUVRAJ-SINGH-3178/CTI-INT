package dao;

import model.TelemetryRecord;
import util.DatabaseConnection;
import util.DataAccessException;
import util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for in-vehicle CAN and V2X telemetry streams.
 * Handles high-throughput batch insertions and analytical queries.
 */
public class TelemetryDao implements Repository<TelemetryRecord, Integer> {

    @Override
    public TelemetryRecord save(TelemetryRecord record) throws DataAccessException, ValidationException {
        if (record == null) {
            throw new ValidationException("record", "Telemetry record cannot be null");
        }

        String sql = "INSERT INTO telemetry_records (domain, timestamp, source_id, payload_data, status_flag, ingested_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, record.getDomain());
            pstmt.setDouble(2, record.getTimestamp());
            pstmt.setString(3, record.getSourceId());
            pstmt.setString(4, record.getPayloadData());
            pstmt.setString(5, record.getStatusFlag());
            pstmt.setString(6, record.getIngestedAt());

            pstmt.executeUpdate();
            try (ResultSet gk = pstmt.getGeneratedKeys()) {
                if (gk.next()) {
                    record.setRecordId(gk.getInt(1));
                }
            }
            return record;

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting telemetry record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TelemetryRecord> findById(Integer recordId) throws DataAccessException {
        String sql = "SELECT * FROM telemetry_records WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRecord(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding telemetry record " + recordId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<TelemetryRecord> findAll() throws DataAccessException {
        return findRecent(100);
    }

    public List<TelemetryRecord> findRecent(int limit) throws DataAccessException {
        List<TelemetryRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM telemetry_records ORDER BY record_id DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving recent telemetry records: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(TelemetryRecord record) throws DataAccessException, ValidationException {
        if (record == null || record.getRecordId() <= 0) {
            throw new ValidationException("recordId", "Valid record ID required for update");
        }

        String sql = "UPDATE telemetry_records SET domain = ?, timestamp = ?, source_id = ?, " +
                     "payload_data = ?, status_flag = ?, ingested_at = ? WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getDomain());
            pstmt.setDouble(2, record.getTimestamp());
            pstmt.setString(3, record.getSourceId());
            pstmt.setString(4, record.getPayloadData());
            pstmt.setString(5, record.getStatusFlag());
            pstmt.setString(6, record.getIngestedAt());
            pstmt.setInt(7, record.getRecordId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating telemetry record " + record.getRecordId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer recordId) throws DataAccessException {
        String sql = "DELETE FROM telemetry_records WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting telemetry record " + recordId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Bulk inserts a list of telemetry frames in a single database transaction.
     */
    public int saveBatch(List<TelemetryRecord> records) throws DataAccessException {
        if (records == null || records.isEmpty()) return 0;

        String sql = "INSERT INTO telemetry_records (domain, timestamp, source_id, payload_data, status_flag, ingested_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (TelemetryRecord r : records) {
                    pstmt.setString(1, r.getDomain());
                    pstmt.setDouble(2, r.getTimestamp());
                    pstmt.setString(3, r.getSourceId());
                    pstmt.setString(4, r.getPayloadData());
                    pstmt.setString(5, r.getStatusFlag());
                    pstmt.setString(6, r.getIngestedAt());
                    pstmt.addBatch();
                }
                int[] count = pstmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
                return count.length;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                throw new DataAccessException("Failed batch telemetry transaction: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error: " + e.getMessage(), e);
        }
    }

    private TelemetryRecord mapRowToRecord(ResultSet rs) throws SQLException {
        try {
            return new TelemetryRecord(
                rs.getInt("record_id"),
                rs.getString("domain"),
                rs.getDouble("timestamp"),
                rs.getString("source_id"),
                rs.getString("payload_data"),
                rs.getString("status_flag"),
                rs.getString("ingested_at")
            );
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping telemetry record: " + e.getMessage(), e);
        }
    }
}

