# CS213 Advanced JAVA Programming  -  Presentation Slide Guide (18 Slides)

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Template Reference**: `Java_Project_Report_Template_SRMAP.pptx` (18 Slides)  

---

### Slide 1: Title Slide
- **Title**: Connected Vehicle CTI Security Operations Console
- **Subtitle**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks
- **Course**: CS213 Advanced JAVA Programming (Regulation: 2025 | Academic Year: 2025 - 2026)
- **Department**: Department of Computer Science and Engineering, SRM University - AP, Amaravati
- **Team Members**:
  1. **Yuvraj Singh** (Team Lead)  -  `AP25110090004` (Module 1: Alert Management)
  2. **Likhith Sai Ande**  -  `AP25110090196` (Module 2: Threat Graph & CTI Mapping)
  3. **Charan Krishna Y**  -  `AP25110090041` (Module 3: Telemetry Ingest & Multithreading)
  4. **Nandam Pardhu Kumar**  -  `AP25110090149` (Module 4: Risk Scoring & Ranking)
  5. **Kaushik Kaligotla**  -  `AP25110090168` (Module 5: Users, Login & Reports)

---

### Slide 2: Project Overview
- **Application Domain**: Automotive Cybersecurity & Cyber Threat Intelligence (CTI).
- **Core Challenge**: Modern vehicles deploy 70+ ECUs on unauthenticated broadcast buses (CAN) and transmit wireless Basic Safety Messages (V2X), exposing them to Denial of Service (DoS) and spoofing.
- **Solution Delivered**: An enterprise desktop Operations Console engineered in Java 27 and Java AWT backed by an embedded SQLite database.
- **Key Capabilities**: Ingests CAN/V2X telemetry, maps attacks to MITRE ATT&CK and CWE, renders a 28-node threat relationship graph, and computes heuristic risk scores:
  $$\text{Risk Score} = \text{Asset Criticality} \times \text{Impact Weight} \times \text{Attack Probability}$$

---

### Slide 3: 1. Identification of the Project
- **Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks
- **Target Users**: VSOC (Vehicle Security Operations Center) Tier-1/Tier-2 Analysts, Fleet Safety Managers, Automotive OEMs.
- **Target Platform**: Desktop (Windows 11, Java SE JDK 27).
- **GUI Framework**: Java AWT (Pure Java SE, zero external runtime dependencies).
- **Database Engine**: SQLite 3 via `org.sqlite.JDBC` driver (`sqlite-jdbc-3.46.1.3.jar`).
- **Development Tooling**: JDK 27 `javac`/`java`, Windows batch scripts (`build.bat`, `run.bat`, `test.bat`), Google Gson.

---

### Slide 4: 2. Project Background
- **Automotive Architecture**: Electronic Control Units (ECUs) govern braking, steering, engine timing, and gateway telemetry.
- **Protocol Vulnerabilities**:
  - *CAN Bus (In-Vehicle)*: Broadcast medium lacking encryption or source authenticity (RFC/SAE J1939).
  - *V2X Telemetry (Inter-Vehicle)*: Wireless 802.11p/C-V2X vulnerable to position and trajectory spoofing.
- **Need for CTI Framework**: Traditional IDS only outputs binary labels ("Attack"). Security analysts require explainable attribution linking attacks to MITRE ATT&CK techniques, affected physical assets, and CWE vulnerabilities.

---

### Slide 5: 3. Problem Statement
- **Existing Problem**: Connected vehicles face severe cyber-physical risks (remote engine shutoff, steering override, false collision alerts). Current security consoles lack unified attribution, cross-domain CAN/V2X coverage, and explainable risk metrics.
- **Root Causes**:
  - Missing hardware authentication on legacy in-vehicle buses.
  - Siloed detection tools separating in-vehicle bus logs from external V2X telemetry.
  - Lack of structured cyber threat intelligence ontologies in automotive operations.
- **Impact**: Operational blind spots for fleet operators and delayed incident triage during live vehicular attacks.

---

### Slide 6: 4. Objectives and Major Functionalities
- **Primary Objectives**:
  1. Build an N-Tier Java 27 desktop console with an interactive Java AWT GUI.
  2. Maintain relational integrity in SQLite across 7 tables with foreign keys and ACID transactions.
  3. Enforce core OOP paradigms (encapsulation, abstract `Alert` inheritance, polymorphic risk calculation).
  4. Implement advanced Java concepts (custom exceptions, multithreaded ingestion, Streams API, Generics).
- **Major Functionalities**:
  - *Create*: Add new intrusion alerts, register users, insert graph elements, ingest telemetry.
  - *Retrieve*: Dynamic multi-criteria filtering (search, domain, technique, severity) via Java Streams.
  - *Update*: Modify alert parameters, update asset criticality weights, edit user roles.
  - *Delete*: Safe deletion with mandatory confirmation dialogs.
  - *Export*: Generate timestamped CSV alert reports with persistent audit logging.

---

### Slide 7: 5. System Design  -  Overall Architecture
- **N-Tier Architecture**:
  - **Presentation Layer**: Java AWT GUI (`LoginFrame`, `MainDashboardFrame`, `AlertFormDialog`, `GraphCanvasPanel`, `TelemetryPanel`, `BenchmarkPanel`, `AdminPanel`).
  - **Service Layer**: 5 Module classes owned by team members (`Team6_AlertManagement`, `Team6_ThreatGraph`, `Team6_TelemetryIngest`, `Team6_RiskRanking`, `Team6_UserReports`).
  - **DAO Layer**: Generic `Repository<T, ID>` implementation using parameterized `PreparedStatement` and connection management.
  - **Database Layer**: Embedded SQLite 3 database (`vehicle_cti.db`) with `PRAGMA foreign_keys = ON;`.
- **Data Flow**: Operator Action $\rightarrow$ Validation $\rightarrow$ Service Coordination $\rightarrow$ DAO Parameterized Query $\rightarrow$ SQLite Storage $\rightarrow$ Real-Time UI Update.

---

### Slide 8: 5. System Design  -  UML Class Diagram
- *(Insert `Documentation/uml_class_diagram.png`)*
- **Key Model Hierarchy**:
  - Abstract base class `Alert` with encapsulated fields and abstract methods.
  - Concrete subclasses `CanAlert` (adds `canId`, `payloadHex`, `dlc`) and `V2xAlert` (adds `senderId`, `posX`, `posY`, `speed`).
  - Generic `Repository<T, ID>` interface implemented by `AlertDao`, `ThreatGraphDao`, `TelemetryDao`, `RiskConfigDao`, `UserDao`.
  - Service classes delegating to DAOs and exposing rubric CRUD methods (`ap25110090004_*`, etc.).

---

### Slide 9: 5. System Design  -  Supporting Diagrams
- **Use-Case Diagram** *(Insert `Documentation/uml_use_case_diagram.png`)*:
  - Actors: Security Analyst, SOC Administrator, Connected Vehicle Telemetry.
  - Use cases: Salted SHA-256 Authentication, View Ranked Alerts, Dynamic Filtering, Graph Exploration, Multithreaded Ingest, CSV Export with Audit Log.
- **Activity Diagram** *(Insert `Documentation/uml_activity_diagram.png`)*:
  - Workflow: Login $\rightarrow$ Telemetry Stream Ingest $\rightarrow$ Feature Extraction $\rightarrow$ Heuristic Risk Scoring $\rightarrow$ Java Streams Ranking $\rightarrow$ Detail Breakdown.

---

### Slide 10: 6. Object-Oriented Programming Concepts
| OOP Concept | Project Class | Implementation & Code Evidence |
|-------------|---------------|--------------------------------|
| **Class & Object** | `ThreatNode`, `Alert` | Domain models representing vehicle assets, attack techniques, and alerts. |
| **Encapsulation** | `Alert.java` | Private fields; mutators enforce bounds (e.g. `confidence` $\in [0.0, 1.0]$) throwing `ValidationException`. |
| **Inheritance** | `CanAlert`, `V2xAlert` | Subclasses extend `Alert`, inheriting core properties and adding domain attributes (`canId`, `speed`). |
| **Polymorphism** | `CanAlert`, `V2xAlert` | Dynamic method dispatch: `CanAlert` applies bus priority factor (1.15x); `V2xAlert` applies velocity factor (1.20x). |
| **Abstraction** | `Alert.java` | Abstract class defining abstract contracts (`getDomain()`, `calculateHeuristicRisk()`). |
| **Interface** | `Repository<T, ID>` | Establishes uniform CRUD contracts decoupled from underlying SQLite storage. |

---

### Slide 11: 7. Advanced Java Concepts
| Concept | Where Used | Justification & Architectural Role |
|---------|------------|-----------------------------------|
| **Exception Handling** | `util/*`, `dao/*` | Custom checked `ValidationException`, `AuthenticationException`, and unchecked `DataAccessException`. Try-with-resources resource management. |
| **Multithreading** | `Team6_TelemetryIngest` | Background frame ingestion via `ExecutorService` and `Java AWTWorker` with `JProgressBar` (no UI freezing). |
| **Collections Framework** | `ThreatGraphDao`, `service/*` | `Map<String, List<ThreatEdge>>` for graph adjacency; `List<Alert>` for tables; `Map` for $O(1)$ asset weights. |
| **Generics** | `Repository<T, ID>`, `Result<T>` | Type-safe generic persistence interface and generic result wrapper `Result<T>`. |
| **Java Streams API** | `Team6_AlertManagement` | Declarative filtering (`filter`), stream sorting (`sorted`), severity counting (`groupingBy`), and summary statistics. |

---

### Slide 12: 8. Graphical User Interface (GUI)
- **GUI Technology**: Java AWT (`javax.swing.*`, `java.awt.*`) with System Look & Feel.
- **Dashboard Modules**:
  1. *Risk-Ranked Alerts Tab*: Real-time search box, domain filter (`CAN` vs `V2X`), MITRE technique selector, severity dropdown, sortable `JTable`.
  2. *Alert Detail Breakdown Panel*: Shows predicted attack, probability, ATT&CK ID, CWE weakness, affected assets, and heuristic calculation.
  3. *Vehicle-Threat Graph Canvas*: Interactive 2D Java AWT canvas rendering 28 nodes and 46 edges with click-to-inspect neighbor relationships.
  4. *Telemetry Ingestion Monitor*: Background streaming with live progress bar and frame counters.
  5. *Research Benchmarks Tab*: 5-seed model comparison matrix and CAN leakage check summaries.
  6. *Administration Tab*: User management, audit trail inspection, and database re-seeding.

---

### Slide 13: 8. GUI  -  Application Screenshots
- *(Reserve space to insert 2 - 3 screenshots captured from the running application)*:
  - **Screenshot 1**: Login Screen with credentials verification.
  - **Screenshot 2**: Main Dashboard showing filtered alerts and the heuristic risk breakdown panel.
  - **Screenshot 3**: Vehicle-Threat Relationship Graph (28 nodes, 46 edges rendered on the 2D canvas).

---

### Slide 14: 9. Database Connectivity  -  JDBC
- **Database Engine**: Embedded SQLite 3 (`database/vehicle_cti.db`).
- **JDBC Driver**: `org.sqlite.JDBC` via `sqlite-jdbc-3.46.1.3.jar`.
- **Connection URL**: `jdbc:sqlite:database/vehicle_cti.db` (strictly no cleartext passwords in URL).
- **Data Integrity**: `PRAGMA foreign_keys = ON;` enforced on every connection.
- **Prepared Statements**: All INSERT, SELECT, UPDATE, DELETE operations executed via parameterized SQL to eliminate SQL injection.
- **ACID Transactions**: Batch alert import and telemetry streams use `conn.setAutoCommit(false)` and `conn.commit()` with rollback on failure.
- **Security**: User passwords hashed with unique 16-byte random salts (`SecureRandom`) and SHA-256 digests.

---

### Slide 15: 10. Source Code and Key Routines
- **Team-Specific CRUD Methods**:
  - `ap25110090004_AlertManagement_create/update/retrive/delete` (Alerts Table)
  - `ap25110090196_ThreatGraph_create/update/retrive/delete` (Threat Nodes & Edges)
  - `ap25110090041_TelemetryIngest_create/update/retrive/delete` (Telemetry Records)
  - `ap25110090149_RiskRanking_create/update/retrive/delete` (Risk Configurations)
  - `ap25110090168_UserReports_create/update/retrive/delete` (Users & Audit Reports)
- **Heuristic Risk Formula**:
  $$\text{Risk Score} = \text{Criticality} \times \text{Impact Weight} \times \text{Attack Probability} \times \text{Domain Multiplier}$$

---

### Slide 16: 11. Testing and Results
- **Testing Approach**: Automated plain-Java test harness (`TestRunner.java`) executed with JDK 27 (no external JUnit binaries required).
- **Test Matrix (18 Tests, 100% Pass Rate)**:
  - *CRUD Tests*: Valid create (TC-01), Retrieve by ID (TC-04), Update (TC-05), Delete (TC-06) $\rightarrow$ **PASS**.
  - *Validation Tests*: Missing attack type (TC-02), Out-of-bounds confidence (TC-03), Duplicate primary key (TC-08) $\rightarrow$ **PASS**.
  - *Security & Resilience*: Salted SHA-256 auth success (TC-09), Auth failure (TC-10), Malformed CSV row (TC-11) $\rightarrow$ **PASS**.
  - *Streams & Polymorphism*: Descending stream ranking (TC-12), CAN vs V2X polymorphic risk (TC-18) $\rightarrow$ **PASS**.
- **Results Output**: Saved in `Documentation/test_results.csv` and logged to console.

---

### Slide 17: 12. Conclusion and Future Enhancements
- **Summary**: Delivered a robust, graded-compliant Connected Vehicle CTI Security Operations Console fulfilling every CS213 rubric requirement.
- **Objectives Achieved**: Full individual member CRUD ownership, strict course naming conventions, Java AWT GUI with interactive graph canvas, multithreaded ingestion, and 100% automated test coverage.
- **Future Enhancements**:
  1. *Hardware-in-the-Loop (HIL)*: Connect directly to physical vehicle CAN transceivers (SocketCAN / USB-CAN).
  2. *Live CTI Feeds*: Implement TAXII/STIX 2.1 protocol clients for real-time Auto-ISAC threat intelligence ingestion.
  3. *Automated Mitigation*: Actuate gateway isolation commands to quarantine compromised vehicular ECUs.

---

### Slide 18: THANK YOU
- **CS213 Advanced JAVA Programming  -  Team 6**
- *Yuvraj Singh (`AP25110090004`), Likhith Sai Ande (`AP25110090196`), Charan Krishna Y (`AP25110090041`), Nandam Pardhu Kumar (`AP25110090149`), Kaushik Kaligotla (`AP25110090168`)*
- Questions & Discussion.

