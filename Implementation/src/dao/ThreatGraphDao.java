package dao;

import model.ThreatEdge;
import model.ThreatNode;
import util.DatabaseConnection;
import util.DataAccessException;
import util.ValidationException;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for Threat Graph nodes and relational edges.
 * Manages the Vehicle-Threat Relationship Graph topology and adjacency representations.
 */
public class ThreatGraphDao implements Repository<ThreatNode, String> {

    @Override
    public ThreatNode save(ThreatNode node) throws DataAccessException, ValidationException {
        if (node == null) {
            throw new ValidationException("node", "Threat node cannot be null");
        }

        String sql = "INSERT INTO threat_nodes (node_id, node_name, node_type, description, criticality) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, node.getNodeId());
            pstmt.setString(2, node.getNodeName());
            pstmt.setString(3, node.getNodeType());
            pstmt.setString(4, node.getDescription());
            pstmt.setDouble(5, node.getCriticality());

            pstmt.executeUpdate();
            return node;

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting threat node ID " + node.getNodeId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ThreatNode> findById(String nodeId) throws DataAccessException {
        String sql = "SELECT * FROM threat_nodes WHERE node_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nodeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToNode(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding threat node " + nodeId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<ThreatNode> findAll() throws DataAccessException {
        List<ThreatNode> list = new ArrayList<>();
        String sql = "SELECT * FROM threat_nodes ORDER BY node_type, node_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToNode(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all threat nodes: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(ThreatNode node) throws DataAccessException, ValidationException {
        if (node == null || node.getNodeId() == null) {
            throw new ValidationException("nodeId", "Valid node ID required for update");
        }

        String sql = "UPDATE threat_nodes SET node_name = ?, node_type = ?, description = ?, criticality = ? WHERE node_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, node.getNodeName());
            pstmt.setString(2, node.getNodeType());
            pstmt.setString(3, node.getDescription());
            pstmt.setDouble(4, node.getCriticality());
            pstmt.setString(5, node.getNodeId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating threat node " + node.getNodeId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String nodeId) throws DataAccessException {
        String sql = "DELETE FROM threat_nodes WHERE node_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nodeId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting threat node " + nodeId + ": " + e.getMessage(), e);
        }
    }

    public List<ThreatEdge> findAllEdges() throws DataAccessException {
        List<ThreatEdge> edges = new ArrayList<>();
        String sql = "SELECT * FROM threat_edges ORDER BY edge_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                edges.add(mapRowToEdge(rs));
            }
            return edges;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving threat edges: " + e.getMessage(), e);
        }
    }

    public ThreatEdge saveEdge(ThreatEdge edge) throws DataAccessException, ValidationException {
        if (edge == null) {
            throw new ValidationException("edge", "Threat edge cannot be null");
        }

        String sql = "INSERT INTO threat_edges (source_node, target_node, relation_type, weight) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, edge.getSourceNode());
            pstmt.setString(2, edge.getTargetNode());
            pstmt.setString(3, edge.getRelationType());
            pstmt.setDouble(4, edge.getWeight());

            pstmt.executeUpdate();
            try (ResultSet gk = pstmt.getGeneratedKeys()) {
                if (gk.next()) {
                    edge.setEdgeId(gk.getInt(1));
                }
            }
            return edge;

        } catch (SQLException e) {
            throw new DataAccessException("Error saving threat edge: " + e.getMessage(), e);
        }
    }

    /**
     * Builds and returns an adjacency list map of the threat graph using Java Collections (Map<String, List<ThreatEdge>>).
     */
    public Map<String, List<ThreatEdge>> getAdjacencyList() throws DataAccessException {
        Map<String, List<ThreatEdge>> adj = new HashMap<>();
        for (ThreatNode node : findAll()) {
            adj.put(node.getNodeId(), new ArrayList<>());
        }
        for (ThreatEdge edge : findAllEdges()) {
            adj.computeIfAbsent(edge.getSourceNode(), k -> new ArrayList<>()).add(edge);
        }
        return adj;
    }

    private ThreatNode mapRowToNode(ResultSet rs) throws SQLException {
        try {
            return new ThreatNode(
                rs.getString("node_id"),
                rs.getString("node_name"),
                rs.getString("node_type"),
                rs.getString("description"),
                rs.getDouble("criticality")
            );
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping threat node: " + e.getMessage(), e);
        }
    }

    private ThreatEdge mapRowToEdge(ResultSet rs) throws SQLException {
        try {
            return new ThreatEdge(
                rs.getInt("edge_id"),
                rs.getString("source_node"),
                rs.getString("target_node"),
                rs.getString("relation_type"),
                rs.getDouble("weight")
            );
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping threat edge: " + e.getMessage(), e);
        }
    }
}

