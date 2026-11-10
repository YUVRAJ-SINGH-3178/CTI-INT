package ui;

import model.ThreatEdge;
import model.ThreatNode;
import service.Team6_ThreatGraph;
import util.DataAccessException;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Custom Java AWT Canvas rendering the 28-node, 46-edge Vehicle Threat Relationship Graph.
 * Pure Java AWT Graphics2D implementation with interactive click inspection.
 */
public class AwtGraphCanvas extends Canvas {

    private final Team6_ThreatGraph threatGraphService;
    private List<ThreatNode> nodes = new ArrayList<>();
    private List<ThreatEdge> edges = new ArrayList<>();
    private final Map<String, Point> nodePositions = new HashMap<>();
    private ThreatNode selectedNode = null;
    private String selectedDetails = "Click any node in the graph to inspect vehicular threat intelligence.";
    private int lastWidth = -1;
    private int lastHeight = -1;

    public AwtGraphCanvas(Team6_ThreatGraph threatGraphService) {
        this.threatGraphService = threatGraphService;
        setBackground(new Color(248, 250, 252));

        loadGraphData();
        calculateLayout();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleNodeClick(e.getPoint());
            }
        });
    }

    public void loadGraphData() {
        try {
            nodes = threatGraphService.getAllNodes();
            edges = threatGraphService.getAllEdges();
            calculateLayout();
            repaint();
        } catch (DataAccessException e) {
            selectedDetails = "Database error loading graph: " + e.getMessage();
        }
    }

    private void calculateLayout() {
        nodePositions.clear();
        int width = Math.max(getWidth(), 950);
        int height = Math.max(getHeight(), 600);

        Map<String, List<ThreatNode>> groups = new LinkedHashMap<>();
        groups.put("Asset", new ArrayList<>());
        groups.put("Event", new ArrayList<>());
        groups.put("Technique", new ArrayList<>());
        groups.put("Weakness", new ArrayList<>());

        for (ThreatNode n : nodes) {
            String type = n.getNodeType();
            groups.computeIfAbsent(type, k -> new ArrayList<>()).add(n);
        }

        // 4 Columns for Layered Automotive CTI Topology (Asset -> Event -> Technique -> Weakness)
        int c0 = 60;
        int c1 = (int) (width * 0.29);
        int c2 = (int) (width * 0.56);
        int c3 = Math.max((int) (width * 0.80), width - 250);
        int colX[] = { c0, c1, c2, c3 };
        int colIndex = 0;

        for (Map.Entry<String, List<ThreatNode>> entry : groups.entrySet()) {
            List<ThreatNode> groupNodes = entry.getValue();
            int x = colX[Math.min(colIndex, colX.length - 1)];
            int total = groupNodes.size();
            for (int i = 0; i < total; i++) {
                int y = 70 + (int) ((i + 0.5) * ((height - 140.0) / Math.max(total, 1)));
                nodePositions.put(groupNodes.get(i).getNodeId(), new Point(x, y));
            }
            colIndex++;
        }
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }

    @Override
    public void update(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (w != lastWidth || h != lastHeight) {
            lastWidth = w;
            lastHeight = h;
            calculateLayout();
        }

        Image offscreen = createImage(w, h);
        if (offscreen == null) return;
        Graphics2D g2 = (Graphics2D) offscreen.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setColor(new Color(248, 250, 252));
        g2.fillRect(0, 0, w, h);

        // Column Titles
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(100, 116, 139));
        g2.drawString("VEHICLE ASSETS (ECUs)", 30, 30);
        g2.drawString("COMMUNICATION EVENTS", (int) (w * 0.29) - 45, 30);
        g2.drawString("ATT&CK TECHNIQUES", (int) (w * 0.56) - 40, 30);
        g2.drawString("CWE WEAKNESSES", Math.max((int) (w * 0.80), w - 250) - 30, 30);

        // Draw Edges
        g2.setStroke(new BasicStroke(1.2f));
        for (ThreatEdge edge : edges) {
            Point p1 = nodePositions.get(edge.getSourceNode());
            Point p2 = nodePositions.get(edge.getTargetNode());
            if (p1 != null && p2 != null) {
                boolean highlighted = selectedNode != null &&
                        (edge.getSourceNode().equals(selectedNode.getNodeId()) || edge.getTargetNode().equals(selectedNode.getNodeId()));

                if (highlighted) {
                    g2.setColor(new Color(225, 29, 72));
                    g2.setStroke(new BasicStroke(2.2f));
                } else {
                    g2.setColor(new Color(203, 213, 225));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Draw Nodes
        int r = 18;
        for (ThreatNode node : nodes) {
            Point p = nodePositions.get(node.getNodeId());
            if (p == null) continue;

            Color nodeCol = getNodeColor(node.getNodeType());
            boolean isSelected = selectedNode != null && selectedNode.getNodeId().equals(node.getNodeId());

            if (isSelected) {
                g2.setColor(new Color(251, 191, 36));
                g2.fillOval(p.x - r - 4, p.y - r - 4, (r + 4) * 2, (r + 4) * 2);
            }

            g2.setColor(nodeCol);
            g2.fillOval(p.x - r, p.y - r, r * 2, r * 2);
            g2.setColor(new Color(30, 41, 59));
            g2.drawOval(p.x - r, p.y - r, r * 2, r * 2);

            // Node Name
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(15, 23, 42));
            String label = node.getNodeName();
            if (label.length() > 22) label = label.substring(0, 20) + "..";
            g2.drawString(label, p.x + r + 5, p.y + 4);
        }

        // Bottom Details Card
        g2.setColor(new Color(241, 245, 249));
        g2.fillRect(10, h - 55, w - 20, 45);
        g2.setColor(new Color(203, 213, 225));
        g2.drawRect(10, h - 55, w - 20, 45);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(30, 41, 59));
        g2.drawString("NODE ATTRIBUTION INSPECTOR:", 20, h - 35);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(51, 65, 85));
        g2.drawString(selectedDetails, 220, h - 35);

        g.drawImage(offscreen, 0, 0, this);
        g2.dispose();
    }

    private Color getNodeColor(String type) {
        if ("Asset".equalsIgnoreCase(type)) return new Color(59, 130, 246);      // Blue
        if ("Technique".equalsIgnoreCase(type)) return new Color(249, 115, 22); // Orange
        if ("Weakness".equalsIgnoreCase(type)) return new Color(239, 68, 68);   // Red
        return new Color(16, 185, 129);                                         // Green (Event)
    }

    private void handleNodeClick(Point pt) {
        int r = 20;
        for (ThreatNode node : nodes) {
            Point p = nodePositions.get(node.getNodeId());
            if (p != null && p.distance(pt) <= r) {
                selectedNode = node;
                selectedDetails = String.format("[%s] ID: %s | Name: %s | Criticality: %.1f | Desc: %s",
                        node.getNodeType().toUpperCase(), node.getNodeId(), node.getNodeName(),
                        node.getCriticality(), node.getDescription());
                repaint();
                return;
            }
        }
    }
}

