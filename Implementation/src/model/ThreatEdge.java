package model;

import util.ValidationException;

/**
 * Domain entity representing a directed relationship in the Vehicle-Threat Relationship Graph.
 */
public class ThreatEdge {

    private int edgeId;
    private String sourceNode;
    private String targetNode;
    private String relationType; // 'TARGETS', 'EXPLOITS', 'ASSOCIATED_WITH'
    private double weight;

    public ThreatEdge() {
        this.weight = 1.0;
    }

    public ThreatEdge(int edgeId, String sourceNode, String targetNode, String relationType, double weight) throws ValidationException {
        this.edgeId = edgeId;
        setSourceNode(sourceNode);
        setTargetNode(targetNode);
        setRelationType(relationType);
        setWeight(weight);
    }

    public int getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(int edgeId) {
        this.edgeId = edgeId;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) throws ValidationException {
        if (sourceNode == null || sourceNode.trim().isEmpty()) {
            throw new ValidationException("sourceNode", "Source node ID cannot be empty");
        }
        this.sourceNode = sourceNode.trim();
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) throws ValidationException {
        if (targetNode == null || targetNode.trim().isEmpty()) {
            throw new ValidationException("targetNode", "Target node ID cannot be empty");
        }
        this.targetNode = targetNode.trim();
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) throws ValidationException {
        if (relationType == null || relationType.trim().isEmpty()) {
            throw new ValidationException("relationType", "Relation type cannot be empty");
        }
        this.relationType = relationType.trim().toUpperCase();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) throws ValidationException {
        if (weight <= 0.0) {
            throw new ValidationException("weight", "Edge weight must be positive. Given: " + weight);
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.format("%s --[%s (w=%.1f)]--> %s", sourceNode, relationType, weight, targetNode);
    }
}

