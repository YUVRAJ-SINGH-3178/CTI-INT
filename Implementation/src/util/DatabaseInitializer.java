package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Initializes database tables, executes schema scripts, and seeds initial datasets.
 */
public class DatabaseInitializer {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void initializeDatabase(String dbPath, String schemaPath, String graphJsonPath, String alertsCsvPath) {
        DatabaseConnection.setDatabasePath(dbPath);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Run schema script
            runSchemaScript(conn, schemaPath);

            // 2. Seed default users if empty
            seedUsersIfEmpty(conn);

            // 3. Seed risk configs if empty
            seedRiskConfigsIfEmpty(conn);

            // 4. Seed threat graph from JSON if empty
            if (graphJsonPath != null && new File(graphJsonPath).exists()) {
                seedThreatGraphIfEmpty(conn, graphJsonPath);
            }

            // 5. Seed alerts from CSV if empty
            if (alertsCsvPath != null && new File(alertsCsvPath).exists()) {
                seedAlertsIfEmpty(conn, alertsCsvPath);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private static void runSchemaScript(Connection conn, String schemaPath) throws SQLException {
        String sql;
        File file = new File(schemaPath);
        if (file.exists()) {
            try {
                sql = Files.readString(file.toPath());
            } catch (IOException e) {
                sql = getEmbeddedSchemaSql();
            }
        } else {
            sql = getEmbeddedSchemaSql();
        }

        // Split by semicolon statements
        String[] statements = sql.split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String s : statements) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private static void seedUsersIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM users")) {
            if (rs.next() && rs.getInt("total") > 0) {
                return; // already seeded
            }
        }

        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        String now = LocalDateTime.now().format(ISO_FMT);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Admin
            String salt1 = PasswordSecurity.generateSalt();
            pstmt.setString(1, "admin");
            pstmt.setString(2, PasswordSecurity.hashPassword("admin123", salt1));
            pstmt.setString(3, salt1);
            pstmt.setString(4, "System Administrator (SOC Lead)");
            pstmt.setString(5, "ADMIN");
            pstmt.setString(6, now);
            pstmt.addBatch();

            // Analyst
            String salt2 = PasswordSecurity.generateSalt();
            pstmt.setString(1, "analyst");
            pstmt.setString(2, PasswordSecurity.hashPassword("analyst123", salt2));
            pstmt.setString(3, salt2);
            pstmt.setString(4, "Tier-2 Vehicle Security Analyst");
            pstmt.setString(5, "ANALYST");
            pstmt.setString(6, now);
            pstmt.addBatch();

            // Operator
            String salt3 = PasswordSecurity.generateSalt();
            pstmt.setString(1, "operator");
            pstmt.setString(2, PasswordSecurity.hashPassword("operator123", salt3));
            pstmt.setString(3, salt3);
            pstmt.setString(4, "Fleet Operations Specialist");
            pstmt.setString(5, "OPERATOR");
            pstmt.setString(6, now);
            pstmt.addBatch();

            pstmt.executeBatch();
        }
    }

    private static void seedRiskConfigsIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM risk_configurations")) {
            if (rs.next() && rs.getInt("total") > 0) {
                return;
            }
        }

        String sql = "INSERT INTO risk_configurations (asset_name, criticality_factor, impact_weight, baseline_threshold, updated_at) VALUES (?, ?, ?, ?, ?)";
        String now = LocalDateTime.now().format(ISO_FMT);

        Object[][] defaultConfigs = {
            {"Engine_Control_Module", 4.5, 4.0, 10.0},
            {"Braking_ECU", 5.0, 5.0, 12.0},
            {"Steering_ECU", 5.0, 4.8, 12.0},
            {"Gateway_ECU", 4.0, 3.8, 8.0},
            {"Telematics_OBU", 3.5, 3.5, 6.0},
            {"Infotainment_Head_Unit", 2.0, 2.5, 4.0}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] row : defaultConfigs) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setDouble(2, (Double) row[1]);
                pstmt.setDouble(3, (Double) row[2]);
                pstmt.setDouble(4, (Double) row[3]);
                pstmt.setString(5, now);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public static void seedThreatGraphIfEmpty(Connection conn, String graphJsonPath) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM threat_nodes")) {
            if (rs.next() && rs.getInt("total") > 0) {
                return;
            }
        }

        File file = new File(graphJsonPath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(br, JsonObject.class);

            conn.setAutoCommit(false);

            // Insert nodes
            if (root.has("nodes")) {
                JsonArray nodes = root.getAsJsonArray("nodes");
                String nodeSql = "INSERT OR IGNORE INTO threat_nodes (node_id, node_name, node_type, description, criticality) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(nodeSql)) {
                    for (JsonElement el : nodes) {
                        JsonObject n = el.getAsJsonObject();
                        String id = n.get("id").getAsString();
                        String rawType = n.has("type") ? n.get("type").getAsString() : "Asset";
                        String type = "Asset";
                        if (rawType.equalsIgnoreCase("vehicle_asset") || rawType.equalsIgnoreCase("Asset")) {
                            type = "Asset";
                        } else if (rawType.equalsIgnoreCase("attack_technique") || rawType.equalsIgnoreCase("Technique")) {
                            type = "Technique";
                        } else if (rawType.equalsIgnoreCase("vulnerability") || rawType.equalsIgnoreCase("Weakness")) {
                            type = "Weakness";
                        } else if (rawType.equalsIgnoreCase("comm_event") || rawType.equalsIgnoreCase("Event")) {
                            type = "Event";
                        }

                        String name = n.has("name") ? n.get("name").getAsString() :
                                     (n.has("label") ? n.get("label").getAsString().replace("\n", " ") : id);
                        String desc = n.has("description") ? n.get("description").getAsString() :
                                     (n.has("category") ? "Category: " + n.get("category").getAsString() :
                                     (n.has("cwe") ? "Vulnerability: " + n.get("cwe").getAsString() :
                                     (n.has("mitre_id") ? "MITRE: " + n.get("mitre_id").getAsString() : "Threat Node")));

                        double crit = 1.0;
                        if (n.has("criticality")) crit = n.get("criticality").getAsDouble();
                        else if (n.has("severity")) crit = n.get("severity").getAsDouble();

                        pstmt.setString(1, id);
                        pstmt.setString(2, name);
                        pstmt.setString(3, type);
                        pstmt.setString(4, desc);
                        pstmt.setDouble(5, crit);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            // Insert edges
            if (root.has("edges")) {
                JsonArray edges = root.getAsJsonArray("edges");
                String edgeSql = "INSERT INTO threat_edges (source_node, target_node, relation_type, weight) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(edgeSql)) {
                    for (JsonElement el : edges) {
                        JsonObject e = el.getAsJsonObject();
                        String src = e.get("source").getAsString();
                        String tgt = e.get("target").getAsString();
                        String rel = e.has("relation") ? e.get("relation").getAsString() : "ASSOCIATED_WITH";
                        double weight = e.has("weight") ? e.get("weight").getAsDouble() : 1.0;

                        pstmt.setString(1, src);
                        pstmt.setString(2, tgt);
                        pstmt.setString(3, rel);
                        pstmt.setDouble(4, weight);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (Exception e) {
            conn.rollback();
            conn.setAutoCommit(true);
            System.err.println("Error seeding threat graph JSON: " + e.getMessage());
        }
    }

    public static void seedAlertsIfEmpty(Connection conn, String alertsCsvPath) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM alerts")) {
            if (rs.next() && rs.getInt("total") > 0) {
                return;
            }
        }

        File file = new File(alertsCsvPath);
        if (!file.exists()) return;

        String insertSql = "INSERT INTO alerts (sample_id, domain, attack_type, confidence, risk_score, mitre_technique, technique_name, severity, target_assets, cwe_id, timestamp, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String now = LocalDateTime.now().format(ISO_FMT);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine(); // skip header
            String line;

            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                int count = 0;
                while ((line = br.readLine()) != null) {
                    List<String> tokens = CsvHelper.parseLine(line);
                    if (tokens.size() >= 8) {
                        try {
                            int offset = (tokens.size() >= 9) ? 1 : 0;
                            String dataset = (offset == 1) ? tokens.get(0).toLowerCase() : "";
                            int sampleId = Integer.parseInt(tokens.get(offset));
                            String attackType = tokens.get(offset + 1);
                            double confidence = Double.parseDouble(tokens.get(offset + 2));
                            double riskScore = Double.parseDouble(tokens.get(offset + 3));
                            String technique = tokens.get(offset + 4);
                            String techniqueName = tokens.get(offset + 5);
                            String severity = tokens.get(offset + 6);
                            String assets = (tokens.size() > offset + 7) ? tokens.get(offset + 7) : "[]";

                            // Determine domain and CWE
                            String domain = dataset.contains("v2x") || attackType.toUpperCase().startsWith("V2X") ? "V2X" : "CAN";
                            String cwe = domain.equals("CAN") ?
                                    (attackType.contains("DoS") ? "CWE-400" : "CWE-287") :
                                    "CWE-345";

                            pstmt.setInt(1, sampleId);
                            pstmt.setString(2, domain);
                            pstmt.setString(3, attackType);
                            pstmt.setDouble(4, confidence);
                            pstmt.setDouble(5, riskScore);
                            pstmt.setString(6, technique);
                            pstmt.setString(7, techniqueName);
                            pstmt.setString(8, severity);
                            pstmt.setString(9, assets);
                            pstmt.setString(10, cwe);
                            pstmt.setString(11, now);
                            pstmt.setString(12, "NEW");
                            pstmt.addBatch();

                            count++;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                pstmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                throw e;
            }
        } catch (IOException e) {
            System.err.println("Error reading alerts CSV: " + e.getMessage());
        }
    }

    private static String getEmbeddedSchemaSql() {
        return "PRAGMA foreign_keys = ON;\n" +
               "CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password_hash TEXT NOT NULL, salt TEXT NOT NULL, full_name TEXT NOT NULL, role TEXT NOT NULL CHECK (role IN ('ADMIN', 'ANALYST', 'OPERATOR')), created_at TEXT NOT NULL);\n" +
               "CREATE TABLE IF NOT EXISTS audit_reports (report_id INTEGER PRIMARY KEY AUTOINCREMENT, report_title TEXT NOT NULL, exported_by TEXT NOT NULL, file_path TEXT NOT NULL, record_count INTEGER NOT NULL CHECK (record_count >= 0), generated_at TEXT NOT NULL);\n" +
               "CREATE TABLE IF NOT EXISTS threat_nodes (node_id TEXT PRIMARY KEY, node_name TEXT NOT NULL, node_type TEXT NOT NULL CHECK (node_type IN ('Asset', 'Technique', 'Weakness', 'Event')), description TEXT, criticality REAL NOT NULL DEFAULT 1.0 CHECK (criticality >= 0.0));\n" +
               "CREATE TABLE IF NOT EXISTS threat_edges (edge_id INTEGER PRIMARY KEY AUTOINCREMENT, source_node TEXT NOT NULL REFERENCES threat_nodes(node_id) ON DELETE CASCADE, target_node TEXT NOT NULL REFERENCES threat_nodes(node_id) ON DELETE CASCADE, relation_type TEXT NOT NULL, weight REAL NOT NULL DEFAULT 1.0 CHECK (weight > 0.0));\n" +
               "CREATE TABLE IF NOT EXISTS telemetry_records (record_id INTEGER PRIMARY KEY AUTOINCREMENT, domain TEXT NOT NULL CHECK (domain IN ('CAN', 'V2X')), timestamp REAL NOT NULL, source_id TEXT NOT NULL, payload_data TEXT NOT NULL, status_flag TEXT NOT NULL DEFAULT 'INGESTED', ingested_at TEXT NOT NULL);\n" +
               "CREATE TABLE IF NOT EXISTS risk_configurations (config_id INTEGER PRIMARY KEY AUTOINCREMENT, asset_name TEXT UNIQUE NOT NULL, criticality_factor REAL NOT NULL CHECK (criticality_factor >= 0.0), impact_weight REAL NOT NULL CHECK (impact_weight >= 0.0), baseline_threshold REAL NOT NULL CHECK (baseline_threshold >= 0.0), updated_at TEXT NOT NULL);\n" +
               "CREATE TABLE IF NOT EXISTS alerts (alert_id INTEGER PRIMARY KEY AUTOINCREMENT, sample_id INTEGER NOT NULL, domain TEXT NOT NULL CHECK (domain IN ('CAN', 'V2X')), attack_type TEXT NOT NULL, confidence REAL NOT NULL CHECK (confidence >= 0.0 AND confidence <= 1.0), risk_score REAL NOT NULL CHECK (risk_score >= 0.0), mitre_technique TEXT NOT NULL, technique_name TEXT NOT NULL, severity TEXT NOT NULL CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')), target_assets TEXT NOT NULL, cwe_id TEXT NOT NULL, timestamp TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED')));\n";
    }
}
