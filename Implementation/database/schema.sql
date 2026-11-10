-- ==============================================================================
-- CONNECTED VEHICLE CTI SECURITY OPERATIONS CONSOLE
-- Database: SQLite 3
-- Schema Definition: schema.sql
-- Description: Enforces relational integrity, primary/foreign keys, and constraints
-- ==============================================================================

PRAGMA foreign_keys = ON;

-- 1. Users Table (Module 5: Team6_UserReports)
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('ADMIN', 'ANALYST', 'OPERATOR')),
    created_at TEXT NOT NULL
);

-- 2. Audit Reports Table (Module 5: Team6_UserReports)
CREATE TABLE IF NOT EXISTS audit_reports (
    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_title TEXT NOT NULL,
    exported_by TEXT NOT NULL,
    file_path TEXT NOT NULL,
    record_count INTEGER NOT NULL CHECK (record_count >= 0),
    generated_at TEXT NOT NULL
);

-- 3. Threat Nodes Table (Module 2: Team6_ThreatGraph)
CREATE TABLE IF NOT EXISTS threat_nodes (
    node_id TEXT PRIMARY KEY,
    node_name TEXT NOT NULL,
    node_type TEXT NOT NULL CHECK (node_type IN ('Asset', 'Technique', 'Weakness', 'Event')),
    description TEXT,
    criticality REAL NOT NULL DEFAULT 1.0 CHECK (criticality >= 0.0)
);

-- 4. Threat Edges Table (Module 2: Team6_ThreatGraph)
CREATE TABLE IF NOT EXISTS threat_edges (
    edge_id INTEGER PRIMARY KEY AUTOINCREMENT,
    source_node TEXT NOT NULL REFERENCES threat_nodes(node_id) ON DELETE CASCADE,
    target_node TEXT NOT NULL REFERENCES threat_nodes(node_id) ON DELETE CASCADE,
    relation_type TEXT NOT NULL,
    weight REAL NOT NULL DEFAULT 1.0 CHECK (weight > 0.0)
);

-- 5. Telemetry Records Table (Module 3: Team6_TelemetryIngest)
CREATE TABLE IF NOT EXISTS telemetry_records (
    record_id INTEGER PRIMARY KEY AUTOINCREMENT,
    domain TEXT NOT NULL CHECK (domain IN ('CAN', 'V2X')),
    timestamp REAL NOT NULL,
    source_id TEXT NOT NULL,
    payload_data TEXT NOT NULL,
    status_flag TEXT NOT NULL DEFAULT 'INGESTED',
    ingested_at TEXT NOT NULL
);

-- 6. Risk Configurations Table (Module 4: Team6_RiskRanking)
CREATE TABLE IF NOT EXISTS risk_configurations (
    config_id INTEGER PRIMARY KEY AUTOINCREMENT,
    asset_name TEXT UNIQUE NOT NULL,
    criticality_factor REAL NOT NULL CHECK (criticality_factor >= 0.0),
    impact_weight REAL NOT NULL CHECK (impact_weight >= 0.0),
    baseline_threshold REAL NOT NULL CHECK (baseline_threshold >= 0.0),
    updated_at TEXT NOT NULL
);

-- 7. Alerts Table (Module 1: Team6_AlertManagement)
CREATE TABLE IF NOT EXISTS alerts (
    alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sample_id INTEGER NOT NULL,
    domain TEXT NOT NULL CHECK (domain IN ('CAN', 'V2X')),
    attack_type TEXT NOT NULL,
    confidence REAL NOT NULL CHECK (confidence >= 0.0 AND confidence <= 1.0),
    risk_score REAL NOT NULL CHECK (risk_score >= 0.0),
    mitre_technique TEXT NOT NULL,
    technique_name TEXT NOT NULL,
    severity TEXT NOT NULL CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')),
    target_assets TEXT NOT NULL,
    cwe_id TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED'))
);

-- Indices for rapid query and filtering performance
CREATE INDEX IF NOT EXISTS idx_alerts_risk ON alerts(risk_score DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_domain ON alerts(domain);
CREATE INDEX IF NOT EXISTS idx_alerts_technique ON alerts(mitre_technique);
CREATE INDEX IF NOT EXISTS idx_edges_source ON threat_edges(source_node);
CREATE INDEX IF NOT EXISTS idx_edges_target ON threat_edges(target_node);

