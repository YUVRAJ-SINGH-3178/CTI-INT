# Connected Vehicle CTI Security Operations Console - Implementation

This directory contains the Java 27 Pure AWT application and SQLite 3 database layer for CS213 Advanced JAVA Programming (Team 6).

---

## 1. Directory Structure

```
Implementation/
├── build.bat                 # Explicit JDK 27 compilation script
├── run.bat                   # Explicit JDK 27 execution script (Java AWT GUI)
├── test.bat                  # Automated plain-Java test suite runner
├── README.md                 # Implementation details
├── database/
│   ├── schema.sql            # SQLite DDL schema with PK/FK constraints & indices
│   ├── seed_notes.md         # Seeding methodology & dataset provenance
│   └── vehicle_cti.db        # SQLite database (7,683 real alerts, 28 nodes, 46 edges)
├── lib/
│   ├── sqlite-jdbc-3.46.1.3.jar
│   └── gson-2.11.0.jar
└── src/
    ├── Main.java             # System entry point
    ├── TestRunner.java       # Automated plain-Java test suite
    ├── model/                # Alert, CanAlert, V2xAlert, ThreatNode, ThreatEdge, TelemetryRecord, RiskConfig, User, AuditReport, Result<T>, ModelMetric
    ├── dao/                  # Repository<T, ID>, Ranker<T>, AlertDao, ThreatGraphDao, TelemetryDao, RiskConfigDao, UserDao, AuditReportDao
    ├── service/              # 5 Member Module Service Classes
    ├── ui/                   # Pure Java AWT GUI (AwtLoginFrame, AwtDashboardFrame, AwtGraphCanvas, AwtResultsPanel, AwtAlertFormDialog)
    └── util/                 # DatabaseConnection, DatabaseInitializer, PasswordSecurity, CsvHelper, TarGzHelper, StixAttackLoader, FullDatasetScanner, ResearchResultsLoader
```

---

## 2. Team Member Ownership & Assigned Modules

| Member | Reg. No. | Module Responsibility | Class Name | CRUD Method Prefix |
|--------|----------|-----------------------|------------|--------------------|
| **Yuvraj Singh** (Lead) | `AP25110090004` | Alert Management & CRUD | `Team6_AlertManagement` | `ap25110090004_AlertManagement_*` |
| **Likhith Sai Ande** | `AP25110090196` | Threat Graph & CTI Mapping | `Team6_ThreatGraph` | `ap25110090196_ThreatGraph_*` |
| **Charan Krishna Y** | `AP25110090041` | Telemetry Ingest & Threads | `Team6_TelemetryIngest` | `ap25110090041_TelemetryIngest_*` |
| **Nandam Pardhu Kumar** | `AP25110090149` | Risk Scoring & Ranking | `Team6_RiskRanking` | `ap25110090149_RiskRanking_*` |
| **Kaushik Kaligotla** | `AP25110090168` | Users, Login & Reports | `Team6_UserReports` | `ap25110090168_UserReports_*` |

---

## 3. How to Build, Test, and Run

All scripts invoke `C:\Program Files\Java\jdk-27.0.1.0.3`:

```bat
build.bat
test.bat
run.bat
```

*Default Login Credentials*: `admin` / `admin123`, `analyst` / `analyst123`, `operator` / `operator123`.
