# System Architecture & Technical Design

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Project**: Connected Vehicle CTI Security Operations Console (Java AWT)  
**Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks  

---

## 1. Architectural Overview

The application uses an **N-Tier Layered Architecture** establishing clear separation of concerns across presentation, business logic, data access, persistence, and raw external datasets (`CICIoV2024`, `VeReMi`, `attack-stix-data`).

```
+-------------------------------------------------------------------------------+
|                      Presentation Layer (Pure Java AWT UI)                    |
|  AwtLoginFrame | AwtDashboardFrame | AwtAlertFormDialog | AwtGraphCanvas      |
|  AwtResultsPanel | EventQueue.invokeLater (EDT-safe UI updates)               |
+---------------------------------------+---------------------------------------+
                                        | (Calls Service Layer APIs)
+---------------------------------------v---------------------------------------+
|                              Service Layer                                    |
|   Team6_AlertManagement  (CRUD on 7,683 Alerts, Stream Filtering & Sorters)   |
|   Team6_ThreatGraph      (Topology, 28 Nodes, 46 Edges, CTI Graph Adjacency)  |
|   Team6_TelemetryIngest  (Async CICIoV2024 CSV & VeReMi .tgz Stream Ingest)   |
|   Team6_RiskRanking      (Heuristic Scoring, Weight Sensitivity Sweeps)       |
|   Team6_UserReports      (Salted SHA-256 Auth, CSV Export, Audit Trail)       |
+---------------------------------------+---------------------------------------+
                                        | (Repository<T, ID> Contract)
+---------------------------------------v---------------------------------------+
|                     Data Access Object (DAO) & Dataset Utils                  |
|   AlertDao | ThreatGraphDao | TelemetryDao | RiskConfigDao | UserDao | Audit  |
|   TarGzHelper | StixAttackLoader | FullDatasetScanner | ResearchResultsLoader |
+---------------------------------------+---------------------------------------+
                                        | (JDBC PreparedStatements & Transactions)
+---------------------------------------v---------------------------------------+
|                              Persistence Layer                                |
|                        SQLite 3 (vehicle_cti.db)                              |
|           Tables: alerts, threat_nodes, threat_edges, telemetry_records,      |
|                   risk_configurations, users, audit_reports                   |
+-------------------------------------------------------------------------------+
```

---

## 2. Layer Descriptions

### 1. Presentation Layer (`src/ui/`)
- Built using **Pure Java AWT** (`java.awt.*`, zero Swing controls).
- Components:
  - `AwtLoginFrame`: Authenticates operators with masked password input (`setEchoChar('*')`) and salted SHA-256 verification.
  - `AwtDashboardFrame`: Central `CardLayout` operations console featuring:
    - Real-time keyword search and dropdown filters for Domain (`CAN`, `V2X`), MITRE ATT&CK Technique (`T0814`, `T0855`, `T0856`, `T0831`, `T1498`, `T1565.002`), and Severity (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
    - Modal AWT dialogs for Add/Edit (`AwtAlertFormDialog`), Delete confirmation, error messages, and native `FileDialog` CSV export.
    - Alert detail inspector enriched asynchronously with official MITRE ATT&CK STIX 2.1 descriptions and tactics from `attack-stix-data/`.
  - `AwtGraphCanvas`: Custom `java.awt.Canvas` drawing 28 threat graph nodes and 46 edges with interactive click inspection.
  - `AwtResultsPanel`: Displays 14 genuine research result views (20-seed summary, 280 runs, 20-seed paired tests, 5-seed summary, 70 runs, full 1.4M CAN metrics, full 225-archive VeReMi metrics, zero-day LOAO, leakage checks, weight sensitivity, all 19 genuine PNG plots, and live disk provenance verification).
  - Full-dataset async telemetry controls for scanning/streaming `CICIoV2024/`, `VeReMi/` `.tgz` archives, and `attack-stix-data/`.

### 2. Service Layer (`src/service/`)
- Five module classes owned individually by Team 6 members:
  - `Team6_AlertManagement` (Yuvraj Singh, `AP25110090004`): CRUD on `alerts` (`ap25110090004_AlertManagement_*`), Java Stream filtering, and severity grouping.
  - `Team6_ThreatGraph` (Likhith Sai Ande, `AP25110090196`): CRUD on `threat_nodes` and `threat_edges` (`ap25110090196_ThreatGraph_*`), adjacency list `Map<String, List<ThreatEdge>>`, and attribution queries.
  - `Team6_TelemetryIngest` (Charan Krishna Y, `AP25110090041`): CRUD on `telemetry_records` (`ap25110090041_TelemetryIngest_*`) and background `ExecutorService` / `CompletableFuture` ingestion for CICIoV2024 CSVs and VeReMi `.tgz` archives.
  - `Team6_RiskRanking` (Nandam Pardhu Kumar, `AP25110090149`): CRUD on `risk_configurations` (`ap25110090149_RiskRanking_*`), heuristic risk calculation (`Criticality x Impact Weight x Confidence`), Stream sorting via `Ranker<Alert>`, and weight sensitivity sweeps.
  - `Team6_UserReports` (Kaushik Kaligotla, `AP25110090168`): CRUD on `users` and `audit_reports` (`ap25110090168_UserReports_*`), salted SHA-256 authentication, and CSV report export with audit logging.

### 3. Data Access Layer (`src/dao/`) & Utilities (`src/util/`)
- Generic `Repository<T, ID>` implemented across all 6 DAO classes with 100% parameterized `PreparedStatement` queries inside `try-with-resources` blocks.
- Atomic SQLite batch transactions (`conn.setAutoCommit(false)`, `executeBatch()`, `conn.commit()`) for seeding and bulk telemetry ingestion.
- External full-dataset utilities: `TarGzHelper` (pure Java `GZIPInputStream` tar streaming), `StixAttackLoader` (Gson `JsonReader` streaming STIX parser), `FullDatasetScanner` (CICIoV2024 1.4M frames + VeReMi 225-run parser), `ResearchResultsLoader`.
