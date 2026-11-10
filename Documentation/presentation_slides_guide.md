# CS213 Advanced JAVA Programming - Presentation Slide Guide (18 Slides)

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Template Reference**: `Java_Project_Report_Template_SRMAP.pptx` (18 Slides)  

---

### Slide 1: Title Slide
- **Title**: Connected Vehicle CTI Security Operations Console
- **Subtitle**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks
- **Course**: CS213 Advanced JAVA Programming (Regulation: 2025 | Academic Year: 2025 - 2026)
- **Department**: Computer Science and Engineering, SRM University - AP, Amaravati
- **Team Members**:
  1. **Yuvraj Singh** (Team Lead) - `AP25110090004` (Module 1: Alert Management)
  2. **Likhith Sai Ande** - `AP25110090196` (Module 2: Threat Graph & CTI Mapping)
  3. **Charan Krishna Y** - `AP25110090041` (Module 3: Telemetry Ingest & Multithreading)
  4. **Nandam Pardhu Kumar** - `AP25110090149` (Module 4: Risk Scoring & Ranking)
  5. **Kaushik Kaligotla** - `AP25110090168` (Module 5: Users, Login & CSV Reports)

---

### Slide 2: Project Overview
- **Domain**: Automotive Cybersecurity & Connected Vehicle Cyber Threat Intelligence (CTI).
- **Challenge**: Modern CAN buses lack message authentication and wireless V2X BSM broadcasts can be spoofed, leaving binary IDS alerts without ECU or technique context.
- **Solution**: Standalone desktop Console engineered in Java 27 and Pure Java AWT (`java.awt.*`) with an embedded SQLite 3 database (`vehicle_cti.db`).
- **Real Scope**: 7,683 seeded alerts (7,499 CAN + 184 V2X), 28-node/46-edge threat graph, 1.4M-frame CICIoV2024 scan, 225-run VeReMi `.tgz` parser, and 994 MITRE STIX 2.1 techniques.

---

### Slide 3: 1. Identification of the Project
- **Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks
- **Target Users**: VSOC Analysts, Incident Responders, Fleet Safety Engineers.
- **Platform**: Windows 11 Desktop running Java SE JDK 27 (`C:\Program Files\Java\jdk-27.0.1.0.3`).
- **GUI Technology**: Pure Java AWT (`Frame`, `Dialog`, `CardLayout`, `Canvas`, `Choice`, `List`, `FileDialog`).
- **Database Engine**: SQLite 3 via `org.sqlite.JDBC` (`sqlite-jdbc-3.46.1.3.jar`) and Gson 2.11.0.

---

### Slide 4: 2. Project Background
- **Vehicular Networks**: Connected vehicles combine internal CAN buses controlling braking/steering/powertrain with external 802.11p/C-V2X Basic Safety Messages.
- **Attribution Gap**: Traditional IDS models emit attack labels without mapping to MITRE ATT&CK techniques, affected ECUs, or underlying CWE weaknesses.
- **Verified Threat Datasets Used**: CICIoV2024 (6 raw decimal CSVs, 1,408,219 frames), VeReMi (225 `.tgz` simulation runs), and MITRE `attack-stix-data` (ICS + Enterprise STIX 2.1 bundles).

---

### Slide 5: 3. Problem Statement
- **Core Problem**: Security operators need unified tooling linking CAN and V2X intrusions with CTI ontologies, physical ECU risk weighting, and honest empirical metrics.
- **Root Causes**: Siloed bus/wireless monitoring, unauthenticated broadcast frames, and lack of explainable risk scoring formulas (`Criticality x Impact Weight x Confidence`).
- **Goal**: Deliver a self-contained N-tier Java AWT desktop system combining operational CRUD workflows with direct full-dataset inspection.

---

### Slide 6: 4. Objectives and Functionalities
- **Objectives**: N-tier Java AWT architecture, 7-table SQLite relational integrity, full OOP hierarchy, Advanced Java multithreading/Streams, and real disk-backed evaluation.
- **Major Functionalities**:
  - *Create*: Validated CAN/V2X alert creation modal, graph element creation, CSV/VeReMi async ingestion.
  - *Retrieve*: Live stream filtering (keyword, domain, technique, severity) and async STIX 2.1 lookup.
  - *Update*: Edit alert details with automatic `Team6_RiskRanking` score recalculation; adjust asset weights.
  - *Delete*: Mandatory modal confirmation dialog before SQLite deletion; cascading graph node deletion.
  - *Export & Verify*: Native `FileDialog` CSV export with audit logging plus 14 real benchmark views.

---

### Slide 7: 5. System Design - Overall Architecture
- **Presentation (`src/ui/`)**: `AwtLoginFrame`, `AwtDashboardFrame`, `AwtGraphCanvas`, `AwtResultsPanel`, `AwtAlertFormDialog`.
- **Service (`src/service/`)**: `Team6_AlertManagement`, `Team6_ThreatGraph`, `Team6_TelemetryIngest`, `Team6_RiskRanking`, `Team6_UserReports`.
- **DAO (`src/dao/`)**: Generic `Repository<T, ID>`, `Ranker<T>`, `AlertDao`, `ThreatGraphDao`, `TelemetryDao`, `RiskConfigDao`, `UserDao`, `AuditReportDao`.
- **Database (`database/vehicle_cti.db`)**: SQLite 3 with `PRAGMA foreign_keys = ON;`.

---

### Slide 8: 5. System Design - UML Class Diagram
- *(Insert `Documentation/uml_class_diagram.png`)*
- Abstract `Alert` base class extended by `CanAlert` and `V2xAlert`, generic `Repository<T, ID>` interface, all 5 `Team6_*` services, and Pure AWT UI layer.

---

### Slide 9: 5. System Design - Supporting Diagrams
- *(Insert `Documentation/uml_use_case_diagram.png` and `Documentation/uml_activity_diagram.png`)*
- Use-Case actors (Security Analyst, SOC Administrator) across CRUD, graph exploration, full-dataset auditing, and CSV export.
- Activity flow from Salted SHA-256 Login through `ExecutorService` ingestion and `EventQueue.invokeLater()` UI updates.

---

### Slide 10: 6. Object-Oriented Programming Concepts
- **Class & Object**: `ThreatNode`, `ThreatEdge`, `CanAlert`, `V2xAlert`, `RiskConfig`, `User`.
- **Encapsulation**: Private attributes in `Alert` with validated mutators throwing `ValidationException`.
- **Inheritance**: `CanAlert` and `V2xAlert` extend abstract `Alert`.
- **Polymorphism**: Both compute `Criticality x Impact Weight x Confidence` (rounded to 2 decimals) while overriding `getDomainSpecificDetails()` and `getDomainExplanation()`.
- **Abstraction & Interface**: Abstract `Alert` plus generic `Repository<T, ID>` and `Ranker<T>`.

---

### Slide 11: 7. Advanced Java Concepts
- **Exception Handling**: Checked `ValidationException` / `AuthenticationException`, unchecked `DataAccessException`, try-with-resources everywhere.
- **Multithreading**: `ExecutorService` (`Executors.newFixedThreadPool(3)`) + `CompletableFuture` posting results safely to the AWT EDT via `EventQueue.invokeLater()`.
- **Collections**: `Map<String, List<ThreatEdge>>` graph adjacency, `List<Alert>`, `Set<String>` unique CAN ID tracking.
- **Generics & Streams**: `Result<T>`, `Repository<T, ID>`, Java Streams `filter`/`sorted`/`groupingBy`/`collect`.

---

### Slide 12: 8. Graphical User Interface (GUI)
- **Framework**: Pure Java AWT (`java.awt.*`) with native Windows controls and `CardLayout`.
- **Four Primary Views**:
  1. **Ranked Alerts**: Search/domain/technique/severity filters, 7,683 ranked alerts, async MITRE STIX 2.1 lookup.
  2. **Vehicle Threat Graph**: Custom `AwtGraphCanvas` plotting 28 nodes and 46 edges (`Asset -> Event -> Technique -> Weakness`).
  3. **Verified Real Results**: 14 disk-backed CSV/JSON/PNG views including 20-seed, 5-seed, full CICIoV2024 & VeReMi benchmarks.
  4. **Full Datasets & Async Stream**: Live scan controls for 6 CICIoV2024 CSVs, 225 VeReMi `.tgz` archives, and 994 STIX techniques.

---

### Slide 13: 8. GUI - Application Screenshots
- *(Insert genuine Win32-captured screenshots from `Documentation/screenshots/`: `awt_dashboard.png`, `awt_graph.png`, `awt_results.png`, `awt_telemetry.png`)*
- Verified captures showing Row 1 (`RPM`, Risk `6.18`, `T0856` STIX details), 28-node graph with `ABS_ESC` highlighted, 20-seed results table, and live 1.4M frame / 225 VeReMi / 994 STIX scan output.

---

### Slide 14: 9. Database Connectivity - JDBC
- **Engine**: Embedded SQLite 3 (`jdbc:sqlite:database/vehicle_cti.db`) using `sqlite-jdbc-3.46.1.3.jar`.
- **Schema & Integrity**: 7 tables with primary/foreign keys, `ON DELETE CASCADE`, `CHECK` constraints, and `PRAGMA foreign_keys = ON;`.
- **Transactions & Security**: Parameterized `PreparedStatement`, ACID `conn.setAutoCommit(false)` batch commits, and 16-byte random salted SHA-256 user credentials.

---

### Slide 15: 10. Source Code and Key Routines
- **Required Course CRUD Naming**:
  - `ap25110090004_AlertManagement_create/update/retrive/delete`
  - `ap25110090196_ThreatGraph_create/update/retrive/delete`
  - `ap25110090041_TelemetryIngest_create/update/retrive/delete`
  - `ap25110090149_RiskRanking_create/update/retrive/delete`
  - `ap25110090168_UserReports_create/update/retrive/delete`
- **Risk Formula**: `riskScore = Math.round(assetCriticality * impactWeight * confidence * 100.0) / 100.0`.

---

### Slide 16: 11. Testing and Results
- **20-Run Repeatability Verification**: Executed all **23 test cases (`TC-01..TC-23`)** 20 consecutive times (`Documentation/multi_run_verification_20runs.csv`): **20 / 20 runs passed (460 / 460 cumulative checks, mean runtime 2.934s)**.
- **Real Benchmark Figures (`Dataset/` CSVs)**:
  - 20-Seed CAN CTI-GNN Macro F1 `1.0000 ± 0.0000`; V2X RF `0.4595 ± 0.0127`, XGB `0.4560 ± 0.0163`, CTI-GNN `0.3056 ± 0.0486` (`p = 0.3720` vs Ablation).
  - Full Datasets: CICIoV2024 XGBoost F1 `0.8026` (Acc `0.9822`); VeReMi XGBoost F1 `0.4754` (Acc `0.8349`); Zero-Day LOAO F1 `1.0`.

---

### Slide 17: 12. Conclusion and Future Enhancements
- **Conclusion**: Delivered a pure Java AWT N-tier Connected Vehicle CTI Console tested across all 7,683 seeded alerts, 28 threat nodes, 1.4M CAN frames, 225 VeReMi archives, and 994 STIX 2.1 techniques with 100% pass repeatability across 20 runs.
- **Future Enhancements**: Hardware-in-the-loop SocketCAN transceiver streaming, automated STIX/TAXII polling, and in-console time-series telemetry rate charts.

---

### Slide 18: THANK YOU
- **CS213 Advanced JAVA Programming - Team 6**
- Yuvraj Singh (`AP25110090004`), Likhith Sai Ande (`AP25110090196`), Charan Krishna Y (`AP25110090041`), Nandam Pardhu Kumar (`AP25110090149`), Kaushik Kaligotla (`AP25110090168`)
- Questions & Discussion.
