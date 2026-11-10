# System Architecture & Technical Design

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Project**: Connected Vehicle CTI Security Operations Console  
**Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks  

---

## 1. Architectural Overview

The application follows an enterprise **N-Tier Layered Architecture** establishing strict separation of concerns across presentation, business logic, persistence, and external data storage.

```
+-------------------------------------------------------------------------------+
|                             Presentation Layer (UI)                           |
|  LoginFrame | MainDashboardFrame | AlertFormDialog | GraphCanvas | Benchmark |
+---------------------------------------+---------------------------------------+
                                        | (Calls Service Layer APIs)
+---------------------------------------v---------------------------------------+
|                              Service Layer                                    |
|   Team6_AlertManagement  (CRUD on Alerts, Stream-based Filtering & Sorters)   |
|   Team6_ThreatGraph      (Topology, 28 Nodes, 46 Edges, CTI Graph Adjacency)  |
|   Team6_TelemetryIngest  (Background Ingest, Thread Pool, Worker Callbacks)  |
|   Team6_RiskRanking      (Heuristic Scoring, Weight Sensitivity Sweeps)       |
|   Team6_UserReports      (Salted SHA-256 Auth, CSV Export, Audit Trail)       |
+---------------------------------------+---------------------------------------+
                                        | (Repository<T, ID> Contract)
+---------------------------------------v---------------------------------------+
|                        Data Access Object (DAO) Layer                         |
|   AlertDao | ThreatGraphDao | TelemetryDao | RiskConfigDao | UserDao | Report |
+---------------------------------------+---------------------------------------+
                                        | (JDBC PreparedStatements & Transactions)
+---------------------------------------v---------------------------------------+
|                              Persistence Layer                                |
|                        SQLite 3 (vehicle_cti.db)                              |
|           Tables: alerts, threat_nodes, threat_edges, telemetry,              |
|                   risk_configurations, users, audit_reports                   |
+-------------------------------------------------------------------------------+
```

---

## 2. Layer Descriptions

### 1. Presentation Layer (`src/ui/`)
- Built exclusively with **Java AWT** (`javax.swing.*`, `java.awt.*`) requiring zero external native UI runtimes.
- Components:
  - `LoginFrame`: Authenticates operators using masked password inputs and cryptographic digest verification.
  - `MainDashboardFrame`: Central operations command center featuring:
    - Real-time search box with dynamic keyword filtering.
    - Multi-criteria dropdown filters: Domain (`CAN`, `V2X`), MITRE ATT&CK Technique, and Severity tier (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
    - Sortable table headers using `TableRowSorter`.
    - Custom cell renderer with colored status badges.
    - Alert detail panel displaying the heuristic calculation breakdown:
      $$\text{Heuristic Risk Score} = \text{Asset Criticality} \times \text{Impact Weight} \times \text{Predicted Attack Probability}$$
  - `AlertFormDialog`: Validated modal dialog enforcing input constraints before creating or updating alerts.
  - `GraphCanvasPanel`: Interactive custom 2D Java AWT canvas (`JPanel` with `paintComponent`) rendering 28 nodes and 46 edges with node selection and neighbor inspection.
  - `TelemetryPanel`: Real-time streaming monitor with asynchronous loading on background worker threads and a progress bar (`JProgressBar`).
  - `BenchmarkPanel`: Reads and renders verified 5-seed benchmark summaries and leakage check metrics from CSV files.
  - `AdminPanel`: Role-based access control, user account provisioning, export audit logs, and database maintenance.

### 2. Service Layer (`src/service/`)
- Contains the five individual module classes owned by team members under CS213 rubric specifications:
  - `Team6_AlertManagement` (Yuvraj Singh, `AP25110090004`): Owns CRUD on the `alerts` table (`ap25110090004_AlertManagement_*`), multi-predicate filtering via Java Streams, and severity distribution aggregation.
  - `Team6_ThreatGraph` (Likhith Sai Ande, `AP25110090196`): Owns CRUD on `threat_nodes` and `threat_edges` (`ap25110090196_ThreatGraph_*`), building in-memory adjacency maps (`Map<String, List<ThreatEdge>>`), and CTI attribution lookups.
  - `Team6_TelemetryIngest` (Charan Krishna Y, `AP25110090041`): Owns CRUD on `telemetry_records` (`ap25110090041_TelemetryIngest_*`) and asynchronous background frame ingestion via `ExecutorService`.
  - `Team6_RiskRanking` (Nandam Pardhu Kumar, `AP25110090149`): Owns CRUD on `risk_configurations` (`ap25110090149_RiskRanking_*`), heuristic risk scoring, Stream sorting via `Ranker<Alert>`, and weight sensitivity evaluation sweeps.
  - `Team6_UserReports` (Kaushik Kaligotla, `AP25110090168`): Owns CRUD on `users` and `audit_reports` (`ap25110090168_UserReports_*`), salted SHA-256 authentication, and CSV report export with audit logging.

### 3. Data Access Object (DAO) Layer (`src/dao/`)
- Implements the generic `Repository<T, ID>` interface:
  - `save(T entity)`
  - `findById(ID id)`
  - `findAll()`
  - `update(T entity)`
  - `deleteById(ID id)`
- Enforces strict parameterized SQL execution via `java.sql.PreparedStatement` to prevent SQL injection vulnerabilities.
- Handles atomic multi-row insertions inside explicit ACID transactions (`conn.setAutoCommit(false)`, `pstmt.executeBatch()`, `conn.commit()`).
- Encapsulates low-level `SQLException` into custom unchecked `DataAccessException`.

### 4. Database Layer (`database/`)
- Embedded SQLite 3 database engine accessed via `sqlite-jdbc-3.46.1.3.jar`.
- Enforces foreign key integrity via `PRAGMA foreign_keys = ON;`.
- Contains 7 primary tables (`users`, `audit_reports`, `threat_nodes`, `threat_edges`, `telemetry_records`, `risk_configurations`, `alerts`).

---

## 3. End-to-End Data Flow

1. **Authentication Flow**:
   - Operator enters username and password in `LoginFrame`.
   - `Team6_UserReports.authenticate()` queries `UserDao.findByUsername()`.
   - The stored Base64 salt is retrieved, and the candidate password is hashed with SHA-256 via `PasswordSecurity.verifyPassword()`.
   - On match, operator session context is passed to `MainDashboardFrame`.

2. **Alert Monitoring & Ranking Flow**:
   - `MainDashboardFrame` requests all alerts from `Team6_AlertManagement`.
   - `AlertDao.findAll()` executes `SELECT * FROM alerts ORDER BY risk_score DESC`.
   - Rows are polymorphically mapped to `CanAlert` or `V2xAlert`.
   - Java Streams filter visible records against search tokens and dropdown selectors without querying the database again.

3. **Telemetry Streaming Flow**:
   - Operator selects a raw CAN or V2X CSV file in `TelemetryPanel`.
   - Ingestion task is dispatched to an `ExecutorService` thread pool.
   - Batch records are parsed via `CsvHelper` and inserted in batches of 50 inside SQLite transactions.
   - `Java AWTWorker` publishes intermediate progress counts to `JProgressBar` without stalling the Java AWT Event Dispatch Thread (EDT).

4. **Threat Attribution & Heuristic Scoring Flow**:
   - When an intrusion is selected, the system correlates the MITRE ATT&CK technique (e.g. `T0814`) with mapped CWE weaknesses (e.g. `CWE-400`) and affected ECU assets.
   - Asset criticality weights from `risk_configurations` are fetched.
   - Polymorphic dispatch calculates heuristic risk incorporating domain factors (CAN arbitration ID priority or V2X velocity dynamics).

