# CS213 Advanced JAVA Programming - Project Report Content Reference

**Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks  
**Department**: Department of Computer Science and Engineering, SRM University - AP, Amaravati  
**Course Code**: CS213 Advanced JAVA Programming  
**Academic Year**: 2025 - 2026 | Regulation: 2025  
**Team Designation**: Team 6  

---

## Team Member Roster & Module Ownership

| S.No | Student Name | Register Number | Assigned Module | Class Name | CRUD Method Pattern |
|:----:|:-------------|:----------------|:----------------|:-----------|:--------------------|
| 1 | **Yuvraj Singh** (Team Lead) | `AP25110090004` | Alert Management (CRUD on Alerts) | `Team6_AlertManagement` | `ap25110090004_AlertManagement_*` |
| 2 | **Likhith Sai Ande** | `AP25110090196` | Threat Graph & CTI Mapping (Assets, Techniques, Weaknesses) | `Team6_ThreatGraph` | `ap25110090196_ThreatGraph_*` |
| 3 | **Charan Krishna Y** | `AP25110090041` | Telemetry Ingest (CAN/V2X CSV & VeReMi .tgz Background Threads) | `Team6_TelemetryIngest` | `ap25110090041_TelemetryIngest_*` |
| 4 | **Nandam Pardhu Kumar** | `AP25110090149` | Risk Scoring & Ranking (Streams, Weights, Sensitivity) | `Team6_RiskRanking` | `ap25110090149_RiskRanking_*` |
| 5 | **Kaushik Kaligotla** | `AP25110090168` | Users, Login & Report Export (Salted SHA-256 Auth, CSV Export) | `Team6_UserReports` | `ap25110090168_UserReports_*` |

---

## 1. Introduction and Project Identification

### 1.1 Project Title
**CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks**

### 1.2 Project Overview
Modern Intelligent Transportation Systems (ITS) and Connected Vehicles rely on two primary communication channels: in-vehicle serial buses (Controller Area Network - CAN Bus) and wireless inter-vehicle links (Vehicle-to-Everything - V2X). Because CAN frames lack built-in message authentication and V2X Basic Safety Messages (BSMs) can be forged over wireless channels, vehicular networks face Denial of Service (DoS), parameter spoofing, and false trajectory attacks.

This project delivers a standalone **Connected Vehicle CTI Security Operations Console** built with **Java 27 (Java SE)** and **Pure Java AWT (`java.awt.*`)**, backed by an embedded **SQLite 3** database. It seeds **7,683 alerts** (7,499 CAN alerts from CICIoV2024 and 184 V2X alerts from VeReMi), visualizes a **28-node, 46-edge** Vehicle Threat Graph, supports live full-dataset auditing across all 6 CICIoV2024 files (**1,408,219 CAN frames**), **225 VeReMi simulation runs** (`.tgz` archives), and **994 MITRE ATT&CK STIX 2.1 techniques**, and displays verified 20-seed and 5-seed experimental results directly from CSV and JSON outputs on disk.

### 1.3 Project Identification

| Item | Project Details |
|------|-----------------|
| **Project Title** | CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks |
| **Application Domain** | Automotive Cybersecurity, Connected Vehicles, Cyber Threat Intelligence (CTI) |
| **Target Users** | Vehicle Security Operations Center (VSOC) Analysts, Fleet Safety Operators, Automotive Security Engineers |
| **Application Type** | Standalone Desktop Application (N-Tier Architecture) |
| **Programming Language** | Java JDK 27 (`C:\Program Files\Java\jdk-27.0.1.0.3`) |
| **GUI Technology** | Pure Java AWT (`java.awt.*`, `java.awt.event.*`) |
| **Database** | SQLite 3 via `org.sqlite.JDBC` (`sqlite-jdbc-3.46.1.3.jar`) |
| **Libraries & Tooling** | Google Gson (`gson-2.11.0.jar`), JDK 27 `javac.exe` / `java.exe`, Windows Batch (`build.bat`, `run.bat`, `test.bat`) |

### 1.4 Project Scope
- **In Scope**:
  - Normalized 7-table SQLite schema seeded with 7,683 ranked alerts, 28 threat nodes, 46 directed edges, 10 asset risk configs, and 3 salted SHA-256 user accounts.
  - Full CRUD across all 5 student-assigned service modules (`Team6_*`) and DAOs.
  - Pure Java AWT console (`AwtLoginFrame`, `AwtDashboardFrame`, `AwtGraphCanvas`, `AwtResultsPanel`, `AwtAlertFormDialog`).
  - Asynchronous dataset scanning across raw CICIoV2024 CSV files, VeReMi `.tgz` archives (via pure-Java `TarGzHelper`), and MITRE `attack-stix-data` JSON bundles (via streaming `StixAttackLoader`).
  - Direct disk loading of 20-seed, 5-seed, full CAN/V2X benchmarks, per-class metrics, LOAO zero-day results, leakage validation, and Monte Carlo risk weight sensitivity outputs.
- **Out of Scope / Assumptions**:
  - Live physical CAN hardware actuation (evaluated against real recorded CICIoV2024 and VeReMi benchmark captures).
  - Model training loops remain offline in Python; the Java console consumes exact CSV/JSON metrics, raw captures, and STIX bundles directly from disk without altering numbers.

---

## 2. Project Background, Stakeholders, and Comparison

### 2.1 Background & Motivation
A modern production vehicle connects dozens of Electronic Control Units (ECUs) over shared buses. Without message-level attribution, a binary intrusion flag fails to answer which ECU is impacted, which MITRE ATT&CK technique applies, which CWE software/protocol weakness enabled the attack, and what operational risk score should guide response triage. Linking intrusion telemetry directly to CTI ontologies (`T0814`, `T0855`, `T0856`, `T0831`, `T1498`, `T1565.002`) and CWE weaknesses (`CWE-306`, `CWE-345`, `CWE-399`, `CWE-400`) provides structured, explainable incident context.

### 2.2 Stakeholders and Users

| Stakeholder / User | Operational Role | Expected Benefit |
|--------------------|------------------|------------------|
| **VSOC Tier-1 Analyst** | Real-time triage and alert filtering | Filters 7,683 ranked alerts by keyword, CAN/V2X domain, MITRE ID, or severity; checks STIX 2.1 details. |
| **VSOC Tier-2 Responder** | Root-cause analysis & ECU tracing | Inspects the interactive 28-node, 46-edge canvas (`Asset -> Event -> Technique -> Weakness`). |
| **Fleet Safety Administrator** | Configuration, ingestion & auditing | Updates criticality weights, streams CICIoV2024/VeReMi files asynchronously, and exports audited CSVs. |

### 2.3 Existing vs. Proposed System

| Feature | Traditional Automotive IDS | Proposed Connected Vehicle CTI Console |
|---------|----------------------------|----------------------------------------|
| **Attribution** | Binary packet labels without context | Correlates alerts to MITRE ATT&CK ICS/Enterprise IDs loaded from STIX 2.1 bundles |
| **Weakness Mapping** | Missing software/protocol root cause | Connects events and techniques to CWE entries via the SQLite threat graph |
| **Risk Scoring** | Fixed severity rules | Computes `Asset Criticality x Impact Weight x Confidence` rounded to 2 decimals |
| **Coverage** | Separate CAN or V2X inspection tools | Unified console handling 7,499 CAN + 184 V2X alerts plus full disk datasets |
| **Verification** | Unverified summary screens | Built-in Verified Real Results tab displaying 20-seed, 5-seed, full CAN/V2X CSVs and plots |

---

## 3. Problem Statement, Objectives & Functionalities

### 3.1 Problem Statement
Connected vehicle buses and V2X links remain exposed to spoofing and flooding attacks while analysts lack unified desktop software that pairs raw CAN/V2X telemetry and MITRE STIX attribution with relational persistence, explainable risk ranking, and reproducible empirical metrics.

### 3.2 Objectives & Major Functionalities
1. Build an N-Tier Pure Java AWT desktop application backed by SQLite 3 and JDBC.
2. Enforce relational integrity with foreign keys, cascade deletes, check constraints, prepared statements, and ACID transactions.
3. Implement core OOP concepts (`Alert` hierarchy, polymorphic `CanAlert`/`V2xAlert`, `Repository<T, ID>` and `Ranker<T>` interfaces) and Advanced Java features (custom exceptions, `ExecutorService` + `CompletableFuture`, Collections, Generics, Java Streams).
4. Execute CRUD cleanly across all five team modules:
   - **Create**: Insert validated alerts, threat nodes/edges, telemetry records, criticality configs, and users.
   - **Retrieve**: Stream-filter alerts by keyword/domain/technique/severity; load graph adjacency and audit trails.
   - **Update**: Edit alerts with asset-aware risk recalculation; adjust asset risk weights; update user/telemetry status.
   - **Delete**: Confirm deletion via modal AWT dialog; cascade delete edges when removing nodes.
   - **Additional**: Salted SHA-256 login, asynchronous CICIoV2024/VeReMi `.tgz` ingestion, live STIX 2.1 lookup, and audited CSV export.

---

## 4. System Architecture and Design

### 4.1 N-Tier Architecture Layers (`Implementation/src/`)
1. **Presentation Layer (`src/ui/`)**: `AwtLoginFrame`, `AwtDashboardFrame`, `AwtGraphCanvas`, `AwtResultsPanel`, `AwtAlertFormDialog`. Background results post back to the UI safely via `EventQueue.invokeLater(...)`.
2. **Service Layer (`src/service/`)**: `Team6_AlertManagement`, `Team6_ThreatGraph`, `Team6_TelemetryIngest`, `Team6_RiskRanking`, `Team6_UserReports`.
3. **DAO Layer (`src/dao/`)**: `Repository<T, ID>`, `Ranker<T>`, `AlertDao`, `ThreatGraphDao`, `TelemetryDao`, `RiskConfigDao`, `UserDao`, `AuditReportDao`.
4. **Database & Utility Layer (`src/util/`, `database/`)**: `DatabaseConnection`, `DatabaseInitializer`, `FullDatasetScanner`, `StixAttackLoader`, `TarGzHelper`, `ResearchResultsLoader`, `PasswordSecurity`, `CsvHelper`, `vehicle_cti.db`.

### 4.2 UML Diagrams (`Documentation/`)
- **Figure 6.1**: UML Class Diagram (`uml_class_diagram.png`)
- **Figure 6.2**: Use-Case Diagram (`uml_use_case_diagram.png`)
- **Figure 6.3**: Sequence Diagram (`uml_sequence_diagram.png`)
- **Figure 6.4**: Activity Diagram (`uml_activity_diagram.png`)
- **Figure 6.5**: Entity-Relationship Diagram (`uml_er_diagram.png`)

---

## 5. Object-Oriented Programming & Advanced Java Evidence

### 5.1 Core OOP Mapping
1. **Class & Object (`ThreatNode.java`, `Alert.java`)**: Encapsulated domain classes instantiated from SQLite rows and JSON/CSV streams.
2. **Encapsulation (`Alert.java`)**: Private attributes validated inside setters before persistence, throwing checked `ValidationException` when confidence leaves `[0.0, 1.0]`.
3. **Inheritance (`CanAlert.java`, `V2xAlert.java`)**: Concrete domain subclasses extending abstract `Alert.java`, adding CAN ID/DLC/payload or V2X sender/receiver/position/speed metadata.
4. **Polymorphism (`CanAlert.java` L43-65, `V2xAlert.java` L47-72)**: Both override `calculateHeuristicRisk(assetCriticality, impactWeight)` to compute `Math.round(assetCriticality * impactWeight * getConfidence() * 100.0) / 100.0`, while providing distinct `getDomainSpecificDetails()` and `getDomainExplanation()` outputs at runtime without artificial multipliers.
5. **Abstraction (`Alert.java` L52-68)**: Abstract methods `getDomain()`, `calculateHeuristicRisk()`, `getDomainSpecificDetails()`, and `getDomainExplanation()`.
6. **Interfaces (`dao/Repository.java`, `dao/Ranker.java`)**: Type-parameterized CRUD and stream ranking contracts decoupled from SQLite.

### 5.2 Advanced Java Concepts
- **Custom Exceptions**: Checked `ValidationException` and `AuthenticationException`, unchecked `DataAccessException`, and try-with-resources resource management (`AlertDao.java` L31-61).
- **Multithreading**: `Team6_TelemetryIngest` runs a 3-thread `ExecutorService` returning `CompletableFuture<Integer>` and `CompletableFuture<VeremiArchiveStats>` with progress consumers marshaled cleanly onto the AWT EDT using `EventQueue.invokeLater()`.
- **Collections Framework**: `Map<String, List<ThreatEdge>>` graph adjacency list in `ThreatGraphDao`, `List<Alert>` and `Set<String>` for alert filtering and unique CAN arbitration ID tracking in `FullDatasetScanner`.
- **Generics**: `Repository<T, ID>`, `Ranker<T>`, and `Result<T>`.
- **Java Streams API**: Multi-predicate `.stream().filter(...)` pipeline in `Team6_AlertManagement.filterAlerts()` (L99-125) and severity `groupingBy` aggregation.

---

## 6. Database Design & JDBC Implementation

- **Engine & Driver**: SQLite 3 via `org.sqlite.JDBC` (`sqlite-jdbc-3.46.1.3.jar`).
- **Connection URL**: `jdbc:sqlite:database/vehicle_cti.db` with `PRAGMA foreign_keys = ON;`.
- **Transactions**: Batch seeding (`DatabaseInitializer.seedAlertsIfEmpty`) and bulk telemetry ingestion wrap `PreparedStatement` batches inside `conn.setAutoCommit(false)` / `conn.commit()` / `conn.rollback()` in 1,000-row batches.
- **Security**: Salted SHA-256 digests using 16-byte `SecureRandom` salts (`PasswordSecurity.java`), zero embedded passwords in JDBC URLs, and 100% parameterized queries.

---

## 7. Real Experimental Benchmarks & Verification Results

### 7.1 Genuine Multi-Seed (20 Seeds) & Full-Dataset Benchmarks
Loaded directly in Java AWT Tab 3 (`AwtResultsPanel`) from `Dataset/` CSV/JSON files generated by the upstream ML pipeline:
- **20-Seed Summary (`multi_seed_summary_20seeds.csv`, 14 model-dataset pairs / 280 runs)**:
  - **CAN (CICIoV2024)**: Proposed CTI-GNN achieved Macro F1 `1.0000 ± 0.0000` (Accuracy `1.0000 ± 0.0000`), matching Random Forest and KNN.
  - **V2X (VeReMi)**: Random Forest Macro F1 `0.4595 ± 0.0127` (Accuracy `0.7927`), XGBoost `0.4560 ± 0.0163` (Accuracy `0.7823`), KNN `0.3819 ± 0.0082`, Proposed CTI-GNN `0.3056 ± 0.0486` (Accuracy `0.7794`, 95% CI `[0.2829, 0.3283]`), Ablation (No CTI/Graph) `0.2968 ± 0.0221`, LSTM `0.2397 ± 0.0548`, SVM `0.2238 ± 0.0045`.
  - **Paired Statistical Tests (`paired_statistical_tests_20seeds.json`)**: On V2X comparing Proposed CTI-GNN against Ablation over 20 seeds, paired t-test yielded `t = 0.9144, p = 0.371979` and Wilcoxon `stat = 81.0, p = 0.388376` (`significant_p05 = false`), reported candidly in Tab 3.
- **5-Seed Summary (`multi_seed_summary_5seeds.csv`, Seeds 42, 101, 202, 303, 404 / 70 runs)**: V2X Random Forest `0.4638 ± 0.0077`, XGBoost `0.4541 ± 0.0077`, Proposed CTI-GNN `0.3371 ± 0.0416`, Ablation `0.3045 ± 0.0205`.
- **Full CICIoV2024 Benchmark (`ciciov_full_metrics.csv`, 100k train / 30k contiguous test from 1,408,219 frames)**: XGBoost Macro F1 `0.8026` (Accuracy `0.9822`), Linear SVM `0.6050` (Accuracy `0.9427`), KNN `0.6033`, Random Forest `0.5135`.
- **Full VeReMi Benchmark (`veremi_full_metrics.csv`, 50k train / 20k test from 225 simulation runs)**: XGBoost Macro F1 `0.4754` (Accuracy `0.8349`), Random Forest `0.4729` (Accuracy `0.8368`), KNN `0.3797`, Linear SVM `0.2427`.
- **Zero-Day Generalization (`ciciov_loao_zero_day_results.csv`)**: Leave-One-Attack-Out anomaly detection achieved `1.0` F1 across all 5 CAN attack classes (`DoS`, `GAS`, `RPM`, `SPEED`, `STEERING_WHEEL`).

### 7.2 Automated Test Suite (23 Test Cases x 20 Consecutive Runs)
Running `TestRunner.java` across 20 consecutive cycles (`Documentation/multi_run_verification_20runs.csv`) achieved **20 / 20 successful runs** (`460 / 460` cumulative tests passed, mean duration `2.934s`, std `0.433s`, min `1.910s`, max `3.396s`), covering:
- **TC-01 to TC-08**: CRUD creation (`CanAlert`), validation (`ValidationException` on blank attack or confidence `1.85`), retrieval, status update (`RESOLVED`), deletion, `'DoS'` search (`1501` matched), duplicate node key `DataAccessException`.
- **TC-09 to TC-13**: Valid/invalid SHA-256 authentication, malformed CSV recovery, descending Stream risk ranking (`19.0`), threat node lifecycle.
- **TC-14 to TC-18**: Seeded threat graph (`28` nodes, `46` edges, `28` adjacency buckets), telemetry record persistence, 7-tier risk sensitivity sweep (`-30%` to `+30%`), audited visible CSV export (`7,685` lines: header + `7,684` rows), polymorphic `CanAlert` & `V2xAlert` (`risk = 18.0`).
- **TC-19 to TC-23**: Full DB seeding (`7,683` alerts = `7,499` CAN + `184` V2X; Row 1 `RPM @ 6.18`), STIX 2.1 bundle extraction (`994` techniques: `127` ICS + `867` Enterprise, all 6 project IDs verified), full 6-file CICIoV2024 scan (`1,408,219` frames) + `200`-row DoS ingest, 225-run VeReMi index + `.tgz` archive stream (`GT=959`, `BSM=4004`), and 20-seed / full CSV verification (`S20=14`, `R20=280`, `FullCAN=4`, `FullV2X=4`).

---

## 8. Conclusion & Future Enhancements

The Connected Vehicle CTI Security Operations Console fulfills every CS213 course requirement using Pure Java AWT (`java.awt.*`), SQLite JDBC, structured OOP/Advanced Java design, live disk dataset parsing across CICIoV2024, VeReMi, and MITRE `attack-stix-data`, and verified 20-run automated test stability (`460 / 460` PASS).

**Future Enhancements**:
1. Connect a physical SocketCAN or USB-CAN transceiver adapter for live vehicle bus captures inside the console.
2. Add a STIX/TAXII 2.1 client for automated periodic threat catalog updates.
3. Expand in-app analytics with lightweight time-window packet rate plotting directly over ingested `telemetry_records`.
