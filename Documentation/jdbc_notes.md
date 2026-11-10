# Database Connectivity using JDBC (SQLite 3) Notes

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Application**: Connected Vehicle CTI Security Operations Console  

---

## 1. Database & Driver Configuration

| Parameter | Project Specification | Details / Rationale |
|-----------|------------------------|---------------------|
| **Database Engine** | SQLite 3 (Embedded Serverless Relational DB) | Zero-configuration file-based storage. Guarantees portability across testing environments without requiring external SQL service daemons. |
| **JDBC Driver** | `org.sqlite.JDBC` (Xerial SQLite-JDBC) | Driver archive: `lib/sqlite-jdbc-3.46.1.3.jar`. Pure Java + bundled native SQLite binaries for Windows x64. |
| **Connection URL** | `jdbc:sqlite:database/vehicle_cti.db` | Absolute or relative file path. Strictly **no embedded passwords or cleartext credentials** in the connection URL. |
| **Security & Constraints** | `PRAGMA foreign_keys = ON;` | Enforced immediately upon opening every connection via `DatabaseConnection.getConnection()`. |
| **Password Storage** | Salted SHA-256 Digest | Unique 16-byte cryptographically secure salt (`SecureRandom`) per user account stored alongside SHA-256 hash in `users` table. |

---

## 2. Relational Schema & Table Definitions

The SQLite database enforces primary and foreign key constraints across seven core tables:

### 1. `users` Table (Owned by `Team6_UserReports`)
- `user_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `username` TEXT UNIQUE NOT NULL
- `password_hash` TEXT NOT NULL
- `salt` TEXT NOT NULL
- `full_name` TEXT NOT NULL
- `role` TEXT NOT NULL CHECK (role IN ('ADMIN', 'ANALYST', 'OPERATOR'))
- `created_at` TEXT NOT NULL

### 2. `audit_reports` Table (Owned by `Team6_UserReports`)
- `report_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `report_title` TEXT NOT NULL
- `exported_by` TEXT NOT NULL
- `file_path` TEXT NOT NULL
- `record_count` INTEGER NOT NULL CHECK (record_count >= 0)
- `generated_at` TEXT NOT NULL

### 3. `threat_nodes` Table (Owned by `Team6_ThreatGraph`)
- `node_id` TEXT PRIMARY KEY
- `node_name` TEXT NOT NULL
- `node_type` TEXT NOT NULL CHECK (node_type IN ('Asset', 'Technique', 'Weakness', 'Event'))
- `description` TEXT
- `criticality` REAL NOT NULL DEFAULT 1.0 CHECK (criticality >= 0.0)

### 4. `threat_edges` Table (Owned by `Team6_ThreatGraph`)
- `edge_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `source_node` TEXT NOT NULL REFERENCES threat_nodes(node_id) ON DELETE CASCADE
- `target_node` TEXT NOT NULL REFERENCES threat_nodes(node_id) ON DELETE CASCADE
- `relation_type` TEXT NOT NULL
- `weight` REAL NOT NULL DEFAULT 1.0 CHECK (weight > 0.0)

### 5. `telemetry_records` Table (Owned by `Team6_TelemetryIngest`)
- `record_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `domain` TEXT NOT NULL CHECK (domain IN ('CAN', 'V2X'))
- `timestamp` REAL NOT NULL
- `source_id` TEXT NOT NULL
- `payload_data` TEXT NOT NULL
- `status_flag` TEXT NOT NULL DEFAULT 'INGESTED'
- `ingested_at` TEXT NOT NULL

### 6. `risk_configurations` Table (Owned by `Team6_RiskRanking`)
- `config_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `asset_name` TEXT UNIQUE NOT NULL
- `criticality_factor` REAL NOT NULL CHECK (criticality_factor >= 0.0)
- `impact_weight` REAL NOT NULL CHECK (impact_weight >= 0.0)
- `baseline_threshold` REAL NOT NULL CHECK (baseline_threshold >= 0.0)
- `updated_at` TEXT NOT NULL

### 7. `alerts` Table (Owned by `Team6_AlertManagement`)
- `alert_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `sample_id` INTEGER NOT NULL
- `domain` TEXT NOT NULL CHECK (domain IN ('CAN', 'V2X'))
- `attack_type` TEXT NOT NULL
- `confidence` REAL NOT NULL CHECK (confidence >= 0.0 AND confidence <= 1.0)
- `risk_score` REAL NOT NULL CHECK (risk_score >= 0.0)
- `mitre_technique` TEXT NOT NULL
- `technique_name` TEXT NOT NULL
- `severity` TEXT NOT NULL CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW'))
- `target_assets` TEXT NOT NULL
- `cwe_id` TEXT NOT NULL
- `timestamp` TEXT NOT NULL
- `status` TEXT NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED'))

---

## 3. CRUD SQL Statements & PreparedStatement Implementations

Every SQL query in the system executes via parameterized `PreparedStatement` to ensure SQL injection immunity:

### A. CREATE / INSERT Operation
```sql
INSERT INTO alerts (
    sample_id, domain, attack_type, confidence, risk_score,
    mitre_technique, technique_name, severity, target_assets,
    cwe_id, timestamp, status
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
```
**Java Implementation (`AlertDao.java`)**:
```java
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
    pstmt.executeUpdate();
    try (ResultSet gk = pstmt.getGeneratedKeys()) {
        if (gk.next()) alert.setAlertId(gk.getInt(1));
    }
}
```

### B. RETRIEVE / SELECT Operation
```sql
SELECT * FROM alerts WHERE alert_id = ?;
```
**Java Implementation (`AlertDao.java`)**:
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, alertId);
    try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) return Optional.of(mapRowToAlert(rs));
    }
    return Optional.empty();
}
```

### C. UPDATE Operation
```sql
UPDATE alerts SET 
    sample_id = ?, domain = ?, attack_type = ?, confidence = ?,
    risk_score = ?, mitre_technique = ?, technique_name = ?, severity = ?,
    target_assets = ?, cwe_id = ?, timestamp = ?, status = ? 
WHERE alert_id = ?;
```
**Java Implementation (`AlertDao.java`)**:
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, alert.getSampleId());
    pstmt.setString(2, alert.getDomain());
    // ... parameters 3 to 12 ...
    pstmt.setInt(13, alert.getAlertId());
    return pstmt.executeUpdate() > 0;
}
```

### D. DELETE Operation
```sql
DELETE FROM alerts WHERE alert_id = ?;
```
**Java Implementation (`AlertDao.java`)**:
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, alertId);
    return pstmt.executeUpdate() > 0;
}
```

---

## 4. Transaction Handling & Bulk Ingestion

To maintain ACID properties during high-throughput alert imports and telemetry ingestion, auto-commit is disabled and explicit batch transactions are committed:

```java
try (Connection conn = DatabaseConnection.getConnection()) {
    conn.setAutoCommit(false); // Begin ACID Transaction
    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
        for (Alert a : alerts) {
            pstmt.setInt(1, a.getSampleId());
            pstmt.setString(2, a.getDomain());
            // ... set batch parameters ...
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        conn.commit();         // Atomic Commit
        conn.setAutoCommit(true);
    } catch (SQLException e) {
        conn.rollback();       // Rollback on any failure
        conn.setAutoCommit(true);
        throw new DataAccessException("Transaction aborted and rolled back", e);
    }
}
```

