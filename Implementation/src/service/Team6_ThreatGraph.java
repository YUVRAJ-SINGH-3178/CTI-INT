package service;

import dao.ThreatGraphDao;
import model.ThreatEdge;
import model.ThreatNode;
import util.DataAccessException;
import util.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * CS213 Advanced JAVA Programming - Module 2
 * Member: Likhith Sai Ande | Registration No: AP25110090196
 * Module Name: Threat Graph and CTI Mapping (Techniques, CWE, Assets)
 * Class: Team6_ThreatGraph
 * ==============================================================================
 */
public class Team6_ThreatGraph {

    private final ThreatGraphDao threatGraphDao;

    public Team6_ThreatGraph() {
        this.threatGraphDao = new ThreatGraphDao();
    }

    public Team6_ThreatGraph(ThreatGraphDao threatGraphDao) {
        this.threatGraphDao = threatGraphDao;
    }

    // --- Mandatory Course Rubric CRUD Operations ---

    /**
     * Create Operation: Inserts a new node into the threat graph.
     */
    public ThreatNode ap25110090196_ThreatGraph_create(ThreatNode node) throws ValidationException, DataAccessException {
        validateNode(node);
        return threatGraphDao.save(node);
    }

    public ThreatNode AP25110090196_ThreatGraph_create(ThreatNode node) throws ValidationException, DataAccessException {
        return ap25110090196_ThreatGraph_create(node);
    }

    /**
     * Update Operation: Updates an existing node in the threat graph.
     */
    public boolean ap25110090196_ThreatGraph_update(ThreatNode node) throws ValidationException, DataAccessException {
        validateNode(node);
        return threatGraphDao.update(node);
    }

    public boolean AP25110090196_ThreatGraph_update(ThreatNode node) throws ValidationException, DataAccessException {
        return ap25110090196_ThreatGraph_update(node);
    }

    /**
     * Retrieve Operation: Fetches a threat graph node by its node ID.
     */
    public Optional<ThreatNode> ap25110090196_ThreatGraph_retrive(String nodeId) throws DataAccessException {
        if (nodeId == null || nodeId.isBlank()) return Optional.empty();
        return threatGraphDao.findById(nodeId.trim());
    }

    public Optional<ThreatNode> AP25110090196_ThreatGraph_retrive(String nodeId) throws DataAccessException {
        return ap25110090196_ThreatGraph_retrive(nodeId);
    }

    /**
     * Delete Operation: Deletes a node and cascades its edges.
     */
    public boolean ap25110090196_ThreatGraph_delete(String nodeId) throws DataAccessException {
        if (nodeId == null || nodeId.isBlank()) return false;
        return threatGraphDao.deleteById(nodeId.trim());
    }

    public boolean AP25110090196_ThreatGraph_delete(String nodeId) throws DataAccessException {
        return ap25110090196_ThreatGraph_delete(nodeId);
    }

    // --- Extended Graph Operations & Collections ---

    public List<ThreatNode> getAllNodes() throws DataAccessException {
        return threatGraphDao.findAll();
    }

    public List<ThreatEdge> getAllEdges() throws DataAccessException {
        return threatGraphDao.findAllEdges();
    }

    public ThreatEdge addEdge(ThreatEdge edge) throws ValidationException, DataAccessException {
        return threatGraphDao.saveEdge(edge);
    }

    /**
     * Returns graph adjacency representation using Java Collections Map<String, List<ThreatEdge>>.
     */
    public Map<String, List<ThreatEdge>> getAdjacencyMap() throws DataAccessException {
        return threatGraphDao.getAdjacencyList();
    }

    /**
     * Filters nodes by category using Java Streams: Asset, Technique, Weakness, Event.
     */
    public List<ThreatNode> getNodesByType(String type) throws DataAccessException {
        return threatGraphDao.findAll().stream()
                .filter(n -> n.getNodeType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Attributions lookup: maps an attack technique to connected assets and CWE weaknesses.
     */
    public Map<String, Object> getAttributionDetails(String nodeId) throws DataAccessException {
        Map<String, Object> details = new HashMap<>();
        Optional<ThreatNode> nodeOpt = threatGraphDao.findById(nodeId);
        if (nodeOpt.isEmpty()) return details;

        ThreatNode node = nodeOpt.get();
        details.put("node", node);

        List<ThreatEdge> edges = threatGraphDao.findAllEdges();
        List<ThreatEdge> outgoing = edges.stream()
                .filter(e -> e.getSourceNode().equals(nodeId))
                .collect(Collectors.toList());
        List<ThreatEdge> incoming = edges.stream()
                .filter(e -> e.getTargetNode().equals(nodeId))
                .collect(Collectors.toList());

        details.put("outgoingEdges", outgoing);
        details.put("incomingEdges", incoming);
        return details;
    }

    private void validateNode(ThreatNode node) throws ValidationException {
        if (node == null) {
            throw new ValidationException("node", "Threat node cannot be null");
        }
        if (node.getNodeId() == null || node.getNodeId().isBlank()) {
            throw new ValidationException("nodeId", "Node ID cannot be empty");
        }
        if (node.getNodeName() == null || node.getNodeName().isBlank()) {
            throw new ValidationException("nodeName", "Node name cannot be empty");
        }
    }
}

