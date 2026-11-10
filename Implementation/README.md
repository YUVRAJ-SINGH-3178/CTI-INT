# Connected Vehicle CTI Security Operations Console — Implementation

This directory contains the complete Java 27 Java AWT application and SQLite database layer for CS213 Advanced JAVA Programming (Team 6).

---

## 1. Directory Structure

```
Implementation/
├── build.bat                 # Explicit JDK 27 compilation script
├── run.bat                   # Explicit JDK 27 execution script (Java AWT GUI)
├── test.bat                  # Automated plain-Java test suite runner
├── README.md                 # Technical architecture & run guide
├── database/
│   ├── schema.sql            # SQLite DDL schema with PK/FK constraints & indices
│   ├── seed_notes.md         # Data seeding provenance & integrity documentation
│   └── vehicle_cti.db        # Pre-seeded SQLite database instance
├── lib/
│   ├── sqlite-jdbc-3.46.1.3.jar  # SQLite JDBC driver
│   └── gson-2.11.0.jar       # Google Gson for Vehicle-Threat Graph JSON parsing
└── src/
    ├── Main.java             # System entry point
    ├── TestRunner.java       # 18-case automated plain-Java test suite
    ├── model/                # Domain entities (Alert, CanAlert, V2xAlert, ThreatNode, etc.)
    ├── dao/                  # JDBC Data Access Objects & Repository<T, ID> interfaces
    ├── service/              # Module classes owned by each team member
    ├── ui/                   # Java AWT GUI (Login, Dashboard, Graph Canvas, Telemetry, Benchmarks)
    └── util/                 # Database connection, crypto, and validation utilities
```

---

## 2. Team Member Ownership & Assigned Modules

| Member | Reg. No. | Module Responsibility | Class Name | CRUD Method Prefix |
|--------|----------|-----------------------|------------|-------------------|
| **Yuvraj Singh** (Lead) | `AP25110090004` | Alert Management & CRUD | `Team6_AlertManagement` | `ap25110090004_AlertManagement_*` |
| **Likhith Sai Ande** | `AP25110090196` | Threat Graph & CTI Mapping | `Team6_ThreatGraph` | `ap25110090196_ThreatGraph_*` |
| **Charan Krishna Y** | `AP25110090041` | Telemetry Ingest & Threads | `Team6_TelemetryIngest` | `ap25110090041_TelemetryIngest_*` |
| **Nandam Pardhu Kumar** | `AP25110090149` | Risk Scoring & Ranking | `Team6_RiskRanking` | `ap25110090149_RiskRanking_*` |
| **Kaushik Kaligotla** | `AP25110090168` | Users, Login & Reports | `Team6_UserReports` | `ap25110090168_UserReports_*` |

---

## 3. How to Build, Test, and Run

All scripts use the explicit JDK 27 installation path (`C:\Program Files\Java\jdk-27.0.1.0.3`):

1. **Build the Project**:
   ```cmd
   build.bat
   ```
2. **Execute Automated Test Runner**:
   ```cmd
   test.bat
   ```
3. **Launch Java AWT GUI Operations Console**:
   ```cmd
   run.bat
   ```

*Default Login Credentials*:
- **Administrator**: `admin` / `admin123`
- **Analyst**: `analyst` / `analyst123`
- **Operator**: `operator` / `operator123`

