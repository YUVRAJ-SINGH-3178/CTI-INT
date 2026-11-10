package dao;

import model.RiskConfig;
import util.DatabaseConnection;
import util.DataAccessException;
import util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for asset risk scoring configurations.
 */
public class RiskConfigDao implements Repository<RiskConfig, Integer> {

    @Override
    public RiskConfig save(RiskConfig config) throws DataAccessException, ValidationException {
        if (config == null) {
            throw new ValidationException("config", "Risk configuration cannot be null");
        }

        String sql = "INSERT INTO risk_configurations (asset_name, criticality_factor, impact_weight, baseline_threshold, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, config.getAssetName());
            pstmt.setDouble(2, config.getCriticalityFactor());
            pstmt.setDouble(3, config.getImpactWeight());
            pstmt.setDouble(4, config.getBaselineThreshold());
            pstmt.setString(5, config.getUpdatedAt());

            pstmt.executeUpdate();
            try (ResultSet gk = pstmt.getGeneratedKeys()) {
                if (gk.next()) {
                    config.setConfigId(gk.getInt(1));
                }
            }
            return config;

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting risk configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<RiskConfig> findById(Integer configId) throws DataAccessException {
        String sql = "SELECT * FROM risk_configurations WHERE config_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, configId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToConfig(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding risk config ID " + configId + ": " + e.getMessage(), e);
        }
    }

    public Optional<RiskConfig> findByAssetName(String assetName) throws DataAccessException {
        String sql = "SELECT * FROM risk_configurations WHERE asset_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, assetName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToConfig(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding risk config for asset " + assetName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<RiskConfig> findAll() throws DataAccessException {
        List<RiskConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM risk_configurations ORDER BY criticality_factor DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToConfig(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all risk configurations: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(RiskConfig config) throws DataAccessException, ValidationException {
        if (config == null || config.getConfigId() <= 0) {
            throw new ValidationException("configId", "Valid config ID required for update");
        }

        String sql = "UPDATE risk_configurations SET asset_name = ?, criticality_factor = ?, " +
                     "impact_weight = ?, baseline_threshold = ?, updated_at = ? WHERE config_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, config.getAssetName());
            pstmt.setDouble(2, config.getCriticalityFactor());
            pstmt.setDouble(3, config.getImpactWeight());
            pstmt.setDouble(4, config.getBaselineThreshold());
            pstmt.setString(5, config.getUpdatedAt());
            pstmt.setInt(6, config.getConfigId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating risk config " + config.getConfigId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer configId) throws DataAccessException {
        String sql = "DELETE FROM risk_configurations WHERE config_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, configId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting risk config " + configId + ": " + e.getMessage(), e);
        }
    }

    private RiskConfig mapRowToConfig(ResultSet rs) throws SQLException {
        try {
            return new RiskConfig(
                rs.getInt("config_id"),
                rs.getString("asset_name"),
                rs.getDouble("criticality_factor"),
                rs.getDouble("impact_weight"),
                rs.getDouble("baseline_threshold"),
                rs.getString("updated_at")
            );
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping risk config: " + e.getMessage(), e);
        }
    }
}

