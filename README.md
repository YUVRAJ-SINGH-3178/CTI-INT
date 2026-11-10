# Connected Vehicle CTI Security Operations Console (Java AWT - Full Dataset)

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks  
**Department**: Computer Science and Engineering, SRM University - AP, Amaravati  

---

## 1. Project Overview

This workspace contains the Pure Java 27 AWT desktop application and embedded SQLite 3 persistence layer for the research project **"CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks"**.

The console pairs vehicular Cyber Threat Intelligence (CTI) with empirical multi-seed ML evaluation results across both **In-Vehicle Controller Area Network (CAN Bus)** and **Inter-Vehicle Vehicle-to-Everything (V2X)** communication domains.

The application loads the full seeded dataset of **7,683 alerts** (7,499 CAN alerts from CICIoV2024 and 184 V2X alerts from VeReMi), renders the **28-node, 46-edge** Vehicle Threat Graph on an AWT Canvas, parses native raw datasets on disk (**1,408,219 CAN frames** across 6 `CICIoV2024/decimal_*.csv` files, **225 simulation runs** in `VeReMi/VeReMi/index.md` + `.tgz` archives, and **994 MITRE ATT&CK STIX 2.1 techniques** in `attack-stix-data/`), and includes a **Verified Real Results** panel reading experimental CSV/JSON artifacts directly from disk with zero placeholder or mock numbers.

---

## 2. Team Members and Module Responsibilities

| S.No | Team Member | Registration No. | Module Assigned | Class Name | CRUD Method Pattern |
|:----:|:------------|:-----------------|:----------------|:-----------|:--------------------|
| 1 | **Yuvraj Singh** (Lead) | `AP25110090004` | Alert Management (CRUD on Alerts) | `Team6_AlertManagement` | `ap25110090004_AlertManagement_*` |
| 2 | **Likhith Sai Ande** | `AP25110090196` | Threat Graph and CTI Mapping (Assets, Techniques, Weaknesses) | `Team6_ThreatGraph` | `ap25110090196_ThreatGraph_*` |
| 3 | **Charan Krishna Y** | `AP25110090041` | Telemetry Ingest (CAN/V2X CSV & VeReMi .tgz Async Streams) | `Team6_TelemetryIngest` | `ap25110090041_TelemetryIngest_*` |
| 4 | **Nandam Pardhu Kumar** | `AP25110090149` | Risk Scoring and Ranking (Streams, Criticality Weights, Sensitivity) | `Team6_RiskRanking` | `ap25110090149_RiskRanking_*` |
| 5 | **Kaushik Kaligotla** | `AP25110090168` | Users, Login and Report Export (Salted SHA-256 Auth, CSV Export) | `Team6_UserReports` | `ap25110090168_UserReports_*` |

---

## 3. Directory Layout

```
VehicleCTI_AWT_AP25110090004/
├── .gitignore
├── README.md
├── Dataset/
│   ├── README.md
│   ├── full_alerts_dataset.csv
│   ├── vehicle_threat_graph.json
│   ├── cti_technique_mapping.csv / .json
│   ├── telemetry_sample_can.csv
│   ├── telemetry_sample_v2x.csv
│   ├── can_ciciov2024_risk_ranked_alerts.csv
│   ├── v2x_veremi_risk_ranked_alerts.csv
│   ├── multi_seed_summary_20seeds.csv
│   ├── multi_seed_all_runs_20seeds.csv
│   ├── paired_statistical_tests_20seeds.json
│   ├── multi_seed_summary_5seeds.csv
│   ├── multi_seed_all_runs_5seeds.csv
│   ├── paired_statistical_tests_5seeds.json
│   ├── ciciov_full_metrics.csv
│   ├── veremi_full_metrics.csv
│   ├── full_model_comparison.csv
│   ├── per_class_metrics_seed42.csv
│   ├── ciciov_loao_zero_day_results.csv
│   ├── can_rigorous_leakage_and_order_checks.csv
│   ├── ciciov_leakage_results.csv
│   ├── weight_sensitivity_analysis.csv
│   ├── baseline_metrics.csv
│   ├── cti_gnn_ablation_results.csv
│   └── figures/                      # 19 genuine evaluation plots and confusion matrices
├── Implementation/
│   ├── build.bat                     # JDK 27 compilation script
│   ├── run.bat                       # Runtime launch script (Pure Java AWT GUI)
│   ├── test.bat                      # Automated test runner (TC-01 to TC-23)
│   ├── README.md
│   ├── database/
│   │   ├── schema.sql
│   │   ├── seed_notes.md
│   │   └── vehicle_cti.db            # Pre-seeded SQLite database (7,683 alerts, 28 nodes, 46 edges)
│   ├── lib/
│   │   ├── sqlite-jdbc-3.46.1.3.jar
│   │   └── gson-2.11.0.jar
│   └── src/
│       ├── Main.java
│       ├── TestRunner.java
│       ├── model/                    # Alert, CanAlert, V2xAlert, ThreatNode, ThreatEdge, TelemetryRecord, RiskConfig, User, AuditReport, ModelMetric, Result<T>
│       ├── dao/                      # Repository<T, ID>, Ranker<T>, AlertDao, ThreatGraphDao, TelemetryDao, RiskConfigDao, UserDao, AuditReportDao
│       ├── service/                  # Team6_AlertManagement, Team6_ThreatGraph, Team6_TelemetryIngest, Team6_RiskRanking, Team6_UserReports
│       ├── ui/                       # AwtLoginFrame, AwtDashboardFrame, AwtGraphCanvas, AwtResultsPanel, AwtAlertFormDialog
│       └── util/                     # DatabaseConnection, DatabaseInitializer, FullDatasetScanner, StixAttackLoader, TarGzHelper, ResearchResultsLoader, PasswordSecurity, CsvHelper, Exceptions
├── Documentation/
│   ├── architecture.md
│   ├── oop_advanced_map.md
│   ├── jdbc_notes.md
│   ├── run_instructions.md
│   ├── test_results.csv              # Run 20 detail table (23 / 23 PASS)
│   ├── multi_run_verification_20runs.csv # 20 consecutive TestRunner executions (460 / 460 PASS)
│   ├── project_report_content.md
│   ├── presentation_slides_guide.md
│   ├── uml_*.puml / uml_*.png        # Class, Use-Case, Sequence, Activity, ER diagrams
│   └── screenshots/                  # 6 genuine Win32-captured AWT UI screenshots
├── Report/                           # CS213_Team6_VehicleCTI_Full_AWT_Report.docx
└── PPT/                              # CS213_Team6_VehicleCTI_Full_AWT_Presentation.pptx
```

*(Note: When running locally with the full external raw archives next to `Dataset/` and `Implementation/`, placing `CICIoV2024/`, `VeReMi/`, and `attack-stix-data/` in the root directory enables full 1.4M CAN scan, all 225 `.tgz` extraction, and STIX 2.1 bundle lookup.)*

---

## 4. Key Features and Rubric Alignment

1. **Graphical User Interface (Pure Java AWT)**:
   - Built strictly with `java.awt.*` (`Frame`, `Dialog`, `Panel`, `CardLayout`, `Canvas`, `Choice`, `List`, `TextArea`, `FileDialog`) and no Swing UI dependencies.
   - Real-time keyword search, domain choice (`ALL`, `CAN`, `V2X`), MITRE ATT&CK technique selector (`T0814`, `T0855`, `T0856`, `T0831`, `T1498`, `T1565.002`), and severity selector.
   - Interactive 4-column AWT `Canvas` (`AwtGraphCanvas`) drawing 28 nodes and 46 directed edges (`Asset -> Event -> Technique -> Weakness`) with node click attribution details.
   - Dedicated Tab 3 (`AwtResultsPanel`) offering 14 genuine views: 20-seed summary, 280 seed runs, paired t-test/Wilcoxon JSON, 5-seed summary, 70 seed runs, full CICIoV2024/VeReMi benchmarks, per-class metrics, LOAO zero-day checks, leakage checks, weight sensitivity sweeps, PNG figure viewer, and live disk provenance verifier.
   - Dedicated Tab 4 scanning all 6 CICIoV2024 CSV files, parsing `VeReMi/VeReMi/index.md` + streaming `.tgz` BSM records via `TarGzHelper`, and loading `attack-stix-data` via `StixAttackLoader`.

2. **Database Connectivity (JDBC + SQLite)**:
   - Seven normalized tables with `PRAGMA foreign_keys = ON;`, foreign key cascade constraints, check constraints, and indexes.
   - Parameterized `PreparedStatement` queries with try-with-resources across all DAOs.
   - Explicit ACID transactions (`conn.setAutoCommit(false)`, `executeBatch()`, `conn.commit()`, `conn.rollback()`) when seeding 7,683 alerts and batching telemetry.
   - Salted SHA-256 password hashing with per-user 16-byte `SecureRandom` salts.

3. **Object-Oriented Programming (OOP) & Advanced Java**:
   - Encapsulated domain models validating ranges and throwing checked `ValidationException`.
   - Abstract `Alert` superclass extended by `CanAlert` and `V2xAlert`, overriding `calculateHeuristicRisk()` (`assetCriticality * impactWeight * confidence` rounded to 2 decimal places) along with polymorphic `getDomainSpecificDetails()` and `getDomainExplanation()`.
   - Custom checked/unchecked exception hierarchy (`ValidationException`, `AuthenticationException`, `DataAccessException`), `ExecutorService` + `CompletableFuture` background workers marshaling UI updates onto the AWT EDT via `EventQueue.invokeLater()`, Collections (`Map<String, List<ThreatEdge>>`, `List`, `Set`), Generics (`Repository<T, ID>`, `Ranker<T>`, `Result<T>`), and Java Streams (`filter`, `sorted`, `groupingBy`, `collect`).

4. **Automated Verification (20 Consecutive Runs)**:
   - Built-in JDK 27 harness (`TestRunner.java`) executing 23 test cases (`TC-01` through `TC-23`).
   - Verified across 20 consecutive execution cycles in `Documentation/multi_run_verification_20runs.csv`: 20 / 20 runs passed (`460 / 460` cumulative assertions, mean runtime `2.934s`, min `1.910s`, max `3.396s`).

---

## 5. Quick Start (Windows 11)

All scripts target JDK 27 at `C:\Program Files\Java\jdk-27.0.1.0.3`:

```cmd
cd Implementation
build.bat
test.bat
run.bat
```

*Default Login Credentials*:
- **Administrator**: `admin` / `admin123`
- **Analyst**: `analyst` / `analyst123`
- **Operator**: `operator` / `operator123`
