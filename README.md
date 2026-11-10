# Connected Vehicle CTI Security Operations Console (Java AWT - Full Dataset)

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks  
**Department**: Computer Science and Engineering, SRM University - AP  

---

## 1. Project Overview

This repository houses the complete Java 27 desktop application and SQLite database layer for the research project **"CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks"**.

The application bridges empirical machine learning intrusion detection with vehicular Cyber Threat Intelligence (CTI). It provides connected vehicle security analysts and Security Operations Center (SOC) personnel with real-time intrusion monitoring across both **In-Vehicle Controller Area Network (CAN Bus)** and **Inter-Vehicle Vehicle-to-Everything (V2X)** communication domains.

The system loads the complete evaluation dataset of **7,683 real intrusion alerts** (7,499 CAN from CICIoV2024 and 184 V2X from VeReMi) and includes a dedicated Real Research Results Verification Panel with zero mock data.

---

## 2. Team Members and Module Responsibilities

| S.No | Team Member | Registration No. | Module Assigned | Class Name | CRUD Method Pattern |
|:----:|:------------|:-----------------|:----------------|:-----------|:--------------------|
| 1 | **Yuvraj Singh** (Lead) | `AP25110090004` | Alert Management (CRUD on Alerts) | `Team6_AlertManagement` | `ap25110090004_AlertManagement_*` |
| 2 | **Likhith Sai Ande** | `AP25110090196` | Threat Graph and CTI Mapping (Assets, Techniques, Weaknesses) | `Team6_ThreatGraph` | `ap25110090196_ThreatGraph_*` |
| 3 | **Charan Krishna Y** | `AP25110090041` | Telemetry Ingest (CAN/V2X CSV Streaming, Background Threads) | `Team6_TelemetryIngest` | `ap25110090041_TelemetryIngest_*` |
| 4 | **Nandam Pardhu Kumar** | `AP25110090149` | Risk Scoring and Ranking (Streams, Criticality Weights, Sensitivity) | `Team6_RiskRanking` | `ap25110090149_RiskRanking_*` |
| 5 | **Kaushik Kaligotla** | `AP25110090168` | Users, Login and Report Export (Salted SHA-256 Auth, CSV Export) | `Team6_UserReports` | `ap25110090168_UserReports_*` |

---

## 3. Directory Layout

The folder strictly conforms to the course submission specification:

```
VehicleCTI_AWT_AP25110090004/
├── .gitignore                # Git ignore rules
├── README.md                 # Top-level project guide
├── Dataset/                  # Complete real evaluation dataset & research figures
│   ├── full_alerts_dataset.csv # 7,683 real ranked alerts (7,499 CAN + 184 V2X)
│   ├── vehicle_threat_graph.json # Connected vehicle threat graph (28 nodes, 46 edges)
│   ├── cti_technique_mapping.csv # MITRE ATT&CK technique & CWE catalog mappings
│   ├── telemetry_sample_can.csv  # CICIoV2024 CAN bus telemetry stream
│   ├── telemetry_sample_v2x.csv  # VeReMi V2X BSM telemetry stream
│   ├── multi_seed_summary_5seeds.csv # 5-seed benchmark summary
│   ├── multi_seed_all_runs_5seeds.csv # All 30 individual seed runs
│   ├── full_model_comparison.csv # Cross-model comparison matrix
│   ├── per_class_metrics_seed42.csv # Per-class attack metrics
│   ├── ciciov_loao_zero_day_results.csv # Zero-day LOAO evaluation
│   ├── can_rigorous_leakage_and_order_checks.csv # Rigorous leakage validation
│   ├── weight_sensitivity_analysis.csv # Risk weight sensitivity evaluation
│   └── figures/              # Real research plots and confusion matrices
├── Implementation/           # Java application implementation
│   ├── build.bat             # JDK 27 compilation script
│   ├── run.bat               # Runtime launch script (Java AWT GUI)
│   ├── test.bat              # Automated test runner (18 test cases)
│   ├── README.md             # Implementation details
│   ├── database/             # SQLite database schema, notes, and db instance
│   │   ├── schema.sql        # Table definitions and constraints
│   │   ├── seed_notes.md     # Seeding methodology and provenance
│   │   └── vehicle_cti.db    # Pre-seeded SQLite database instance (7,683 alerts)
│   ├── lib/                  # Required third-party libraries
│   │   ├── sqlite-jdbc-3.46.1.3.jar # SQLite JDBC Driver
│   │   └── gson-2.11.0.jar   # Google Gson parser
│   └── src/                  # Java 27 source code
│       ├── Main.java         # Application bootstrap entry point
│       ├── TestRunner.java   # 18-case automated test suite
│       ├── model/            # Alert, CanAlert, V2xAlert, ThreatNode, User, Result<T>
│       ├── dao/              # JDBC DAOs implementing generic Repository<T, ID>
│       ├── service/          # 5 Member Module Service Classes
│       ├── ui/               # Pure Java AWT GUI (Login, Dashboard, Canvas, Results)
│       └── util/             # Database connection, crypto, validation exceptions
├── Documentation/            # Technical & Architectural Documentation
│   ├── architecture.md       # N-tier architecture & data flow
│   ├── oop_advanced_map.md   # Concept-to-code mapping tables (file, line, snippets)
│   ├── jdbc_notes.md         # Database, schema, and prepared statement notes
│   ├── run_instructions.md   # Windows 11 build and run guide
│   ├── test_results.csv      # Observed test results table (18/18 PASS)
│   ├── uml_class_diagram.png       # UML Class Diagram
│   ├── uml_use_case_diagram.png    # Use-Case Diagram
│   ├── uml_sequence_diagram.png    # Sequence Diagram
│   ├── uml_activity_diagram.png    # Activity Diagram
│   ├── uml_er_diagram.png          # Entity-Relationship Diagram
│   └── screenshots/          # AWT GUI screenshots (login, dashboard, graph, results, form)
├── Report/                   # CS213_Team6_VehicleCTI_Full_AWT_Report.docx
└── PPT/                      # CS213_Team6_VehicleCTI_Full_AWT_Presentation.pptx
```

---

## 4. Key Features and Rubric Alignment

1. **Graphical User Interface (Pure Java AWT)**:
   - Modern Java AWT desktop GUI with operator login authentication and CardLayout navigation.
   - Real-time search box, domain filtering (`CAN` vs `V2X`), MITRE ATT&CK technique filter, and severity choice.
   - Interactive 2D Vehicle Threat Relationship Graph on an AWT Canvas drawing 28 nodes and 46 relational links with click inspection.
   - Alert Detail panel displaying the heuristic risk formula:
     $$\text{Heuristic Risk Score} = \text{Asset Criticality} \times \text{Impact Weight} \times \text{Attack Probability}$$
   - Add/Edit/Delete modal dialogs with input validation and confirmation before delete.
   - Dedicated Verified Real Results tab displaying 5-seed metrics, 30 individual seed runs, zero-day LOAO, and research plots with zero mock data.

2. **Database Connectivity (JDBC + SQLite)**:
   - Relational schema with primary and foreign keys, cascade deletes, and check constraints across 7 tables.
   - All queries executed through `java.sql.PreparedStatement` using try-with-resources.
   - Explicit ACID transactions (`conn.setAutoCommit(false)`, `executeBatch()`, `conn.commit()`) for bulk ingestion.
   - Passwords secured with per-user unique 16-byte random salts and SHA-256 cryptographic digests.

3. **Object-Oriented Programming (OOP)**:
   - Class/Object, Encapsulation with validated setters throwing `ValidationException`.
   - Inheritance (`CanAlert` and `V2xAlert` extending abstract `Alert`).
   - Polymorphism (`calculateHeuristicRisk()` computing risk = Asset Criticality x Impact Weight x Confidence rounded to 2 decimals, with polymorphic `getDomainSpecificDetails()` and `getDomainExplanation()`).
   - Abstraction (abstract `Alert` class), and Interface contracts (`Repository<T, ID>`, `Ranker<T>`).

4. **Advanced Java Concepts**:
   - Custom checked/unchecked exceptions (`ValidationException`, `DataAccessException`, `AuthenticationException`).
   - Multithreading via `ExecutorService` for background telemetry streaming and risk recalculation without freezing the AWT window.
   - Java Collections Framework (`Map` for graph adjacency, `List` and `Set` for alert filtering and deduplication).
   - Java Generics (`Repository<T, ID>`, `Result<T>`).
   - Java Streams API (`filter`, `sorted`, `map`, `groupingBy`, `collect`).

5. **Automated Testing Suite**:
   - Built-in plain-Java test harness (`TestRunner.java`) executing 18 comprehensive tests.
   - 100% pass rate (18 / 18 PASS) saved in `Documentation/test_results.csv`.

---

## 5. Quick Start (Windows 11)

All scripts utilize the explicit JDK 27 installation path (`C:\Program Files\Java\jdk-27.0.1.0.3`):

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
