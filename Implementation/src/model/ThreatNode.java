package model;

import util.ValidationException;

/**
 * Domain entity representing a node in the Vehicle-Threat Relationship Graph.
 * Node types: 'Asset', 'Technique', 'Weakness', 'Event'.
 */
public class ThreatNode {

    private String nodeId;
    private String nodeName;
    private String nodeType; // 'Asset', 'Technique', 'Weakness', 'Event'
    private String description;
    private double criticality; // Heuristic weighting factor >= 0.0

    public ThreatNode() {
        this.criticality = 1.0;
    }

    public ThreatNode(String nodeId, String nodeName, String nodeType, String description, double criticality) throws ValidationException {
        setNodeId(nodeId);
        setNodeName(nodeName);
        setNodeType(nodeType);
        this.description = (description == null) ? "" : description.trim();
        setCriticality(criticality);
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) throws ValidationException {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new ValidationException("nodeId", "Node ID cannot be empty");
        }
        this.nodeId = nodeId.trim();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) throws ValidationException {
        if (nodeName == null || nodeName.trim().isEmpty()) {
            throw new ValidationException("nodeName", "Node name cannot be empty");
        }
        this.nodeName = nodeName.trim();
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) throws ValidationException {
        if (nodeType == null) {
            throw new ValidationException("nodeType", "Node type cannot be null");
        }
        String t = nodeType.trim();
        if (!t.equalsIgnoreCase("Asset") && !t.equalsIgnoreCase("Technique") &&
            !t.equalsIgnoreCase("Weakness") && !t.equalsIgnoreCase("Event")) {
            throw new ValidationException("nodeType", "Node type must be Asset, Technique, Weakness, or Event. Given: " + nodeType);
        }
        this.nodeType = Character.toUpperCase(t.charAt(0)) + t.substring(1).toLowerCase();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCriticality() {
        return criticality;
    }

    public void setCriticality(double criticality) throws ValidationException {
        if (criticality < 0.0) {
            throw new ValidationException("criticality", "Criticality cannot be negative. Given: " + criticality);
        }
        this.criticality = criticality;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, Crit: %.1f)", nodeName, nodeType, criticality);
    }
}

