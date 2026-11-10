# CS213 Advanced JAVA Programming  -  Project Report Reference

**Project Title**: CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks  
**Department**: Department of Computer Science and Engineering, SRM University - AP, Amaravati  
**Course Code**: CS213 Advanced JAVA Programming  
**Academic Year**: 2025 - 2026 | Regulation: 2025  
**Team Designation**: Team 6  

---

### Team Member Roster & Module Ownership

| S.No | Student Name | Register Number | Assigned Module | Class Name | CRUD Method Pattern |
|:----:|:-------------|:----------------|:----------------|:-----------|:--------------------|
| 1 | **Yuvraj Singh** (Team Lead) | `AP25110090004` | Alert Management (CRUD on Alerts) | `Team6_AlertManagement` | `ap25110090004_AlertManagement_*` |
| 2 | **Likhith Sai Ande** | `AP25110090196` | Threat Graph & CTI Mapping (Assets, Techniques, Weaknesses) | `Team6_ThreatGraph` | `ap25110090196_ThreatGraph_*` |
| 3 | **Charan Krishna Y** | `AP25110090041` | Telemetry Ingest (CAN/V2X CSV Streaming, Background Threads) | `Team6_TelemetryIngest` | `ap25110090041_TelemetryIngest_*` |
| 4 | **Nandam Pardhu Kumar** | `AP25110090149` | Risk Scoring & Ranking (Streams, Weights, Sensitivity) | `Team6_RiskRanking` | `ap25110090149_RiskRanking_*` |
| 5 | **Kaushik Kaligotla** | `AP25110090168` | Users, Login & Report Export (Salted SHA-256 Auth, CSV Export) | `Team6_UserReports` | `ap25110090168_UserReports_*` |

---

## 1. Introduction and Project Identification

### 1.1 Project Title
**CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks**

### 1.2 Project Overview
Modern Intelligent Transportation Systems (ITS) and Connected and Autonomous Vehicles (CAVs) communicate through dual critical interfaces: in-vehicle broadcast buses (Controller Area Network - CAN Bus) and inter-vehicle wireless networks (Vehicle-to-Everything - V2X). However, standard automotive communication protocols inherently lack message encryption and source authentication, exposing connected vehicles to severe adversarial exploits including Denial of Service (DoS), payload spoofing, and false telemetry injection.

This project delivers an enterprise-grade desktop **Connected Vehicle Security Operations Console** developed in **Java 27 (Java SE)** and **Java AWT**, backed by an embedded **SQLite 3** relational database. The application ingests vehicle communication telemetry, correlates intrusions with Cyber Threat Intelligence (CTI) ontologies (MITRE ATT&CK for ICS/Enterprise and Common Weakness Enumeration CWE), computes heuristic asset impact risk scores, and provides interactive visualization of the 28-node, 46-edge Vehicle-Threat Relationship Graph.

### 1.3 Project Identification
| Item | Project Details |
|------|-----------------|
| **Project Title** | CTI-Driven Intelligent Threat Detection and Attack Attribution for Connected Vehicle Networks |
| **Application Domain** | Automotive Cybersecurity, Connected Vehicles, Cyber Threat Intelligence (CTI) |
| **Target Users** | Vehicle Security Operations Center (VSOC) Analysts, Fleet Safety Operators, Automotive Engineers |
| **Application Type** | Standalone Desktop Application (N-Tier Architecture) |
| **Programming Language** | Java JDK 27 (explicit path: `C:\Program Files\Java\jdk-27.0.1.0.3`) |
| **GUI Technology** | Java AWT (`javax.swing.*`, `java.awt.*`) |
| **Database** | SQLite 3 via `org.sqlite.JDBC` driver (`sqlite-jdbc-3.46.1.3.jar`) |
| **Development Tools** | JDK 27 `javac.exe`, `java.exe`, Windows Batch (`build.bat`, `run.bat`, `test.bat`), Google Gson |

### 1.4 Project Scope
- **In Scope**:
  - Multithreaded telemetry stream ingestion for in-vehicle CAN (CICIoV2024) and inter-vehicle V2X (VeReMi).
  - Relational SQLite schema with primary/foreign keys, indices, and check constraints across 7 tables.
  - Complete CRUD operations across all five student-assigned modules.
  - Object-Oriented design implementing encapsulation, abstract inheritance (`CanAlert`, `V2xAlert`), polymorphism, and interfaces (`Repository<T, ID>`, `Ranker<T>`).
  - Advanced Java concepts: custom exception hierarchy, thread pools (`ExecutorService`, `Java AWTWorker`), Java Collections, Generics (`Result<T>`), and Java Streams API.
  - Interactive 2D Java AWT graph visualizer rendering 28 nodes and 46 relational edges.
  - Salted SHA-256 cryptographic user authentication.
  - Visible alert list CSV export with persistent audit logging.
- **Out of Scope / Assumptions**:
  - Physical hardware flashing of physical ECUs (simulated via verified real-vehicle dataset traces).
  - Direct learned model inference inside Java (model inference was executed in Python; verified 5-seed benchmark matrices and alerts are ingested directly from CSV/JSON).

---

## 2. Project Background and Motivation

### 2.1 Background
Contemporary automotive architectures deploy 70 - 100 Electronic Control Units (ECUs) interconnected through serial broadcast buses like the CAN Bus. The CAN standard uses non-destructive bitwise arbitration where messages have arbitration IDs but lack sender identifiers or message authentication codes. Similarly, inter-vehicle V2X communications transmit periodic Basic Safety Messages (BSMs) over wireless channels, which can be forged to broadcast false position coordinates or velocity vectors, leading to phantom braking or severe physical collisions.

### 2.2 Motivation
Existing automotive Intrusion Detection Systems (IDS) typically function as "black boxes": they flag anomalous frames as binary labels (Normal vs. Attack) without providing contextual attribution:
1. *What adversarial technique is being utilized?*
2. *Which internal vehicular assets (ECM, ABS, Steering ECU, Gateway) are targeted?*
3. *What physical vulnerability allows the exploit?*
4. *What is the operational risk score to passenger safety?*

By coupling intrusion alerts with structured Cyber Threat Intelligence (MITRE ATT&CK and CWE) and a formal Vehicle-Threat Graph, this console equips security analysts with immediate, explainable threat attribution and prioritization.

### 2.3 Stakeholders and Users
| Stakeholder / User | Operational Role | Expected Benefit |
|--------------------|------------------|------------------|
| **VSOC Tier-1 Analyst** | Real-time intrusion triage & monitoring | Rapidly filters alerts by domain and severity; inspects MITRE techniques. |
| **VSOC Tier-2 Incident Responder** | Root-cause analysis & ECU isolation | Uses Vehicle-Threat Graph to identify affected physical assets and CWE weaknesses. |
| **Fleet Safety Administrator** | Policy management & reporting | Provisions analyst roles, configures asset criticality weights, exports audit reports. |

### 2.4 Existing vs. Proposed System
| Feature / Limitation | Existing Automotive IDS Consoles | Proposed Connected Vehicle CTI Console |
|----------------------|-----------------------------------|----------------------------------------|
| **Attribution** | Raw packet logs; zero CTI mapping | Correlates alerts to MITRE ATT&CK techniques (T0814, T0806, etc.) |
| **Vulnerability Context** | Missing root cause context | Links attacks to specific CWE weaknesses (CWE-287, CWE-400, CWE-345) |
| **Risk Scoring** | Static or arbitrary severity | Dynamic heuristic: $\text{Criticality} \times \text{Impact Weight} \times \text{Attack Probability}$ |
| **Domain Coverage** | Siloed (either CAN or V2X only) | Unified console supporting both in-vehicle CAN and inter-vehicle V2X |
| **Graph Context** | Text-only tabular outputs | Interactive 2D graph visualizer with 28 nodes and 46 relational links |

---

## 3. Problem Statement and Objectives

### 3.1 Problem Statement
Vehicle communication networks are vulnerable to catastrophic cyber-physical attacks due to missing cryptographic authentication in CAN bus broadcasts and forged position data in V2X wireless telemetry. Cybersecurity operators lack unified, explainable operational tooling to detect, attribute, prioritize, and manage vehicular intrusions in real time.

### 3.2 Project Objectives
1. **Develop an N-Tier Desktop Application**: Engineer a responsive Java AWT GUI backed by an embedded SQLite database.
2. **Implement Relational Integrity**: Enforce primary and foreign key constraints across seven relational tables with parameterized queries and ACID transactions.
3. **Incorporate Core OOP Paradigms**: Realize encapsulation, abstract inheritance (`Alert`), polymorphic risk calculation, and interface abstraction (`Repository<T, ID>`).
4. **Demonstrate Advanced Java Concepts**: Implement custom checked/unchecked exceptions, multithreading via `ExecutorService`/`Java AWTWorker`, Java Collections, Generics, and Java Streams API.
5. **Provide Interactive Threat Intelligence**: Visualize a 28-node, 46-edge vehicle threat graph, stream raw telemetry, and export audited CSV threat reports.

### 3.3 Major Functionalities
- **Create**: Add new intrusion alerts, register users, add graph nodes/edges, and ingest telemetry.
- **Retrieve**: Filter alerts across keyword, domain, technique, and severity; search graph nodes; retrieve audit logs.
- **Update**: Edit existing alert parameters, update asset criticality weights, and modify user credentials.
- **Delete**: Remove alerts with mandatory confirmation dialogs, delete graph nodes with cascade edge removal.
- **Additional**: Salted SHA-256 authentication, asynchronous telemetry streaming, 5-seed benchmark inspection, and CSV export.

---

## 4. Project Modules and Functional Requirements

| Module No. | Module Name | Owner | Key Operations & Rubric Implementation |
|:----------:|:------------|:------|:---------------------------------------|
| **1** | **Alert Management** | Yuvraj Singh (`AP25110090004`) | Full CRUD on `alerts` table (`ap25110090004_AlertManagement_*`); dynamic stream filtering by domain, technique, severity; severity distribution aggregation. |
| **2** | **Threat Graph & CTI Mapping** | Likhith Sai Ande (`AP25110090196`) | Full CRUD on `threat_nodes` and `threat_edges` (`ap25110090196_ThreatGraph_*`); graph adjacency map generation; CTI technique and CWE attribution. |
| **3** | **Telemetry Ingest** | Charan Krishna Y (`AP25110090041`) | Full CRUD on `telemetry_records` (`ap25110090041_TelemetryIngest_*`); multithreaded background CSV ingestion via `ExecutorService` with progress callbacks. |
| **4** | **Risk Scoring & Ranking** | Nandam Pardhu Kumar (`AP25110090149`) | Full CRUD on `risk_configurations` (`ap25110090149_RiskRanking_*`); heuristic risk calculation; stream ranking via `Ranker<Alert>`; weight sensitivity sweeps. |
| **5** | **Users, Login & Report Export** | Kaushik Kaligotla (`AP25110090168`) | Full CRUD on `users` and `audit_reports` (`ap25110090168_UserReports_*`); salted SHA-256 password verification; visible alert list CSV export with audit trail. |

---

## 5. System Architecture and Design

### 5.1 Architecture Layers
1. **Presentation Layer (`src/ui/`)**: `LoginFrame`, `MainDashboardFrame`, `AlertFormDialog`, `GraphCanvasPanel`, `TelemetryPanel`, `BenchmarkPanel`, `AdminPanel`.
2. **Service Layer (`src/service/`)**: Contains the five student module classes enforcing business rules and coordinate transactions.
3. **Data Access Object Layer (`src/dao/`)**: Implements generic `Repository<T, ID>` using parameterized `PreparedStatement` and connection pooling.
4. **Database Layer (`database/`)**: Embedded SQLite 3 database (`vehicle_cti.db`).

---

## 6. UML Diagrams

All five diagrams were generated and rendered into high-resolution PNG files located in `Documentation/`:
- **Figure 6.1: UML Class Diagram** (`Documentation/uml_class_diagram.png`)
- **Figure 6.2: Use-Case Diagram** (`Documentation/uml_use_case_diagram.png`)
- **Figure 6.3: Sequence Diagram** (`Documentation/uml_sequence_diagram.png`)
- **Figure 6.4: Activity Diagram** (`Documentation/uml_activity_diagram.png`)
- **Figure 6.5: Entity-Relationship Diagram** (`Documentation/uml_er_diagram.png`)

---

## 7. Object-Oriented Programming (OOP) Concepts Evidence

### 1. Class & Object
- **Class**: `ThreatNode.java`, `ThreatEdge.java`, `Alert.java`.
- **Snippet**:
  ```java
  ThreatNode node = new ThreatNode("Asset:ECM", "Engine Control Module", "Asset", "Powertrain Controller", 4.5);
  ```

### 2. Encapsulation
- **Class**: `Alert.java` (Lines 11 - 23, 105 - 175).
- **Evidence**: All fields are `private`. Mutators enforce domain boundaries and throw checked `ValidationException`:
  ```java
  public void setConfidence(double confidence) throws ValidationException {
      if (confidence < 0.0 || confidence > 1.0) {
          throw new ValidationException("confidence", "Confidence probability must be between 0.0 and 1.0");
      }
      this.confidence = confidence;
  }
  ```

### 3. Inheritance
- **Classes**: `CanAlert.java` (Lines 10 - 31) and `V2xAlert.java` (Lines 10 - 35) extend abstract `Alert.java`.
- **Evidence**: Subclasses inherit common intrusion attributes and introduce domain-specific properties:
  ```java
  public class CanAlert extends Alert {
      private int canId;
      private String payloadHex;
      private int dlc;
      public CanAlert(...) throws ValidationException {
          super(alertId, sampleId, "CAN", attackType, confidence, riskScore, ...);
      }
  }
  ```

### 4. Polymorphism
- **Classes**: `CanAlert.java` (Lines 42 - 61) vs. `V2xAlert.java` (Lines 46 - 65).
- **Evidence**: Dynamic method dispatch overrides `calculateHeuristicRisk`, `getDomainSpecificDetails`, and `getDomainExplanation`. Both subclasses compute heuristic risk as $\text{Criticality} \times \text{Impact Weight} \times \text{Confidence}$ (rounded to 2 decimal places), while polymorphic runtime behavior is demonstrated through domain-specific physical telemetry parsing and context explanation strings:
  ```java
  // In CanAlert.java
  @Override
  public double calculateHeuristicRisk(double assetCriticality, double impactWeight) {
      double rawRisk = assetCriticality * impactWeight * getConfidence();
      double rounded = Math.round(rawRisk * 100.0) / 100.0;
      try { setRiskScore(rounded); } catch (ValidationException ignored) {}
      return rounded;
  }
  @Override
  public String getDomainSpecificDetails() {
      return String.format("CAN Arbitration ID: 0x%03X (%d) | DLC: %d | Payload: %s", canId, canId, dlc, payloadHex);
  }
  @Override
  public String getDomainExplanation() {
      return String.format("CAN in-vehicle bus transmission: Frame 0x%03X broadcast on powertrain/chassis bus with DLC %d.", canId, dlc);
  }

  // In V2xAlert.java
  @Override
  public double calculateHeuristicRisk(double assetCriticality, double impactWeight) {
      double rawRisk = assetCriticality * impactWeight * getConfidence();
      double rounded = Math.round(rawRisk * 100.0) / 100.0;
      try { setRiskScore(rounded); } catch (ValidationException ignored) {}
      return rounded;
  }
  @Override
  public String getDomainSpecificDetails() {
      return String.format("V2X Transmitter ID: %d | Receiver ID: %d | Pos: (%.1f, %.1f) | Velocity: %.1f m/s", senderId, receiverId, posX, posY, speed);
  }
  @Override
  public String getDomainExplanation() {
      return String.format("V2X DSRC/C-V2X broadcast: Wireless beacon from vehicle %d to station %d at velocity %.1f m/s.", senderId, receiverId, speed);
  }
  ```

### 5. Abstraction
- **Class**: `Alert.java` (Lines 9 - 10, 51 - 68).
- **Evidence**: Abstract methods declare essential operations that subclasses must implement:
  ```java
  public abstract class Alert {
      public abstract String getDomain();
      public abstract double calculateHeuristicRisk(double assetCriticality, double impactWeight);
      public abstract String getDomainSpecificDetails();
      public abstract String getDomainExplanation();
  }
  ```

### 6. Interface
- **Interfaces**: `Repository<T, ID>` (`dao/Repository.java`) and `Ranker<T>` (`dao/Ranker.java`).
- **Evidence**: Establishes strict contracts for data persistence and stream ranking:
  ```java
  public interface Repository<T, ID> {
      T save(T entity) throws DataAccessException, ValidationException;
      Optional<T> findById(ID id) throws DataAccessException;
      List<T> findAll() throws DataAccessException;
      boolean update(T entity) throws DataAccessException, ValidationException;
      boolean deleteById(ID id) throws DataAccessException;
  }
  ```

---

## 8. Advanced Java Concepts Evidence

### 1. Exception Handling
- **Classes**: `ValidationException` (checked), `DataAccessException` (unchecked), `AuthenticationException` (checked).
- **Evidence**: Checked exceptions enforce input verification at compile-time; try-with-resources guarantees database resource closure:
  ```java
  try (Connection conn = DatabaseConnection.getConnection();
       PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, alertId);
      return pstmt.executeUpdate() > 0;
  } catch (SQLException e) {
      throw new DataAccessException("Error executing delete: " + e.getMessage(), e);
  }
  ```

### 2. Multithreading
- **Classes**: `Team6_TelemetryIngest.java` (Lines 32 - 36, 113 - 153), `ui/TelemetryPanel.java`.
- **Evidence**: Uses `ExecutorService` and `Java AWTWorker` for background frame streaming without freezing the Java AWT Event Dispatch Thread (EDT):
  ```java
  public CompletableFuture<Integer> ingestCsvAsync(File csvFile, String domain, Consumer<Integer> progressConsumer) {
      return CompletableFuture.supplyAsync(() -> {
          // Parse CSV, execute batch insert in SQLite, publish progress
          return totalInserted;
      }, executorService);
  }
  ```

### 3. Collections Framework
- **Classes**: `ThreatGraphDao.java` (Lines 160 - 171), `Team6_RiskRanking.java`.
- **Evidence**: `Map<String, List<ThreatEdge>>` maintains graph adjacency; `List<Alert>` maintains table rows; `Map<String, RiskConfig>` stores asset weights for $O(1)$ lookup:
  ```java
  public Map<String, List<ThreatEdge>> getAdjacencyList() {
      Map<String, List<ThreatEdge>> adj = new HashMap<>();
      for (ThreatNode node : findAll()) adj.put(node.getNodeId(), new ArrayList<>());
      for (ThreatEdge edge : findAllEdges()) adj.get(edge.getSourceNode()).add(edge);
      return adj;
  }
  ```

### 4. Generics
- **Classes**: `dao/Repository.java`, `model/Result.java`.
- **Evidence**: Parameterized generic repository interface `Repository<T, ID>` and response wrapper `Result<T>`:
  ```java
  public class Result<T> {
      private final boolean success;
      private final T data;
      private final String message;
      public static <T> Result<T> success(T data) { return new Result<>(true, data, "OK"); }
  }
  ```

### 5. Java Streams API
- **Classes**: `Team6_AlertManagement.java` (Lines 78 - 109), `Team6_RiskRanking.java` (Lines 97 - 124).
- **Evidence**: Multi-criteria pipeline combining `filter`, `sorted`, `groupingBy`, and `collect`:
  ```java
  return all.stream()
          .filter(a -> a.getDomain().equalsIgnoreCase(domainFilter))
          .filter(a -> a.getMitreTechnique().equalsIgnoreCase(techniqueFilter))
          .sorted(Comparator.comparingDouble(Alert::getRiskScore).reversed())
          .collect(Collectors.toList());
  ```

---

## 9. Database Design & JDBC Implementation

- **Database**: SQLite 3 (`database/vehicle_cti.db`).
- **JDBC Driver**: `org.sqlite.JDBC` via `sqlite-jdbc-3.46.1.3.jar`.
- **Connection URL**: `jdbc:sqlite:database/vehicle_cti.db` (no embedded credentials).
- **Foreign Key Enforcement**: `PRAGMA foreign_keys = ON;` executed on every connection.
- **Transactions**: Explicit ACID transaction handling (`conn.setAutoCommit(false)`, `pstmt.executeBatch()`, `conn.commit()`, `conn.rollback()`).
- **Security**: Salted SHA-256 password hashing via `SecureRandom` 16-byte salt generation.

---

## 10. Automated Testing & Verification Table

Execution of `test.bat` (running `TestRunner.java`) confirms 18 / 18 tests passed (100% success rate):

```
----------------------------------------------------------------------------------------------------------------------------------
TEST ID | SCENARIO / INPUT                              | EXPECTED RESULT                          | ACTUAL RESULT             | STATUS
----------------------------------------------------------------------------------------------------------------------------------
TC-01   | Create valid CanAlert with physical CAN ID    | Alert persisted with positive generat... | Persisted alert_id=41     | PASS
TC-02   | Create alert with empty attack type           | Throw ValidationException for missing... | Caught: Validation err... | PASS
TC-03   | Create alert with invalid confidence 1.85 ... | Throw ValidationException for confide... | Caught: Validation err... | PASS
TC-04   | Retrieve existing alert by primary key ID=1   | Returns Optional containing Alert ent... | Found alert: RPM (T0856)  | PASS
TC-05   | Update alert status to 'RESOLVED' and seve... | Update returns true; retrieved entity... | Updated=true, Status=R... | PASS
TC-06   | Delete alert by primary key ID=42             | Delete returns true; subsequent findB... | Deleted=true, Present=... | PASS
TC-07   | Search alerts with keyword 'DoS'              | Returns non-empty list filtered stric... | Matched 1 alerts          | PASS
TC-08   | Insert duplicate node with existing primar... | Throw DataAccessException / SQLite UN... | Caught: Error insertin... | PASS
TC-09   | Authenticate valid user 'admin' with corre... | Returns User object with role 'ADMIN'    | Authenticated: admin (... | PASS
TC-10   | Authenticate 'admin' with incorrect password  | Throw AuthenticationException            | Caught: Invalid passwo... | PASS
TC-11   | Parse malformed CSV row with unclosed quote   | Gracefully recovers and outputs token... | Parsed 3 tokens safely    | PASS
TC-12   | Rank alerts list by heuristic risk score v... | List sorted strictly in descending ri... | Strictly descending: t... | PASS
TC-13   | Threat graph node create, retrieve and delete | Node persisted, retrieved with critic... | Retrieved name=Telemat... | PASS
TC-14   | Threat graph relational verification (28 nodes, 46 edges) | Seeded database contains exactly 28 nodes and 46 edges with adjacency map | Nodes=28, Edges=46, Adjacency buckets=28 | PASS
TC-15   | Telemetry record create & retrieve by primary key | Record persisted with generated ID and source_id=0x128 | Persisted record_id=1     | PASS
TC-16   | Heuristic weight sensitivity perturbation sweeps (-30% to +30%) | Computes 7 sensitivity perturbation evaluation tiers | Computed 7 sensitivity tiers | PASS
TC-17   | Export visible alerts to CSV and record audit trail | CSV file created with header and at least one data row, plus audit log entry | CSV rows=42 (Header + 41 data rows), Audit records=1 | PASS
TC-18   | Heuristic risk score calculation (18.00) and polymorphic domain details | Both yield risk=18.00; distinct domain details and explanation strings | CAN Risk=18.0, V2X Risk=18.0, CAN Details=[...], V2X Details=[...] | PASS
----------------------------------------------------------------------------------------------------------------------------------
```

---

## 11. Conclusion & Future Enhancements

### 11.1 Conclusion
The Connected Vehicle CTI Security Operations Console successfully bridges empirical machine learning intrusion detection with vehicular Cyber Threat Intelligence. The project meets all CS213 rubric requirements: complete multi-member module ownership, strict naming conventions, full JDBC CRUD operations, robust OOP paradigms, advanced Java concepts, automated testing, and comprehensive architectural documentation.

### 11.2 Future Enhancements
1. **Hardware-in-the-Loop (HIL) Integration**: Connect the console to physical CAN transceivers (e.g. SocketCAN on Linux or USB-CAN adapters) for live ECU hardware monitoring.
2. **Real-Time STIX/TAXII Ingestion**: Integrate live TAXII threat feeds from automotive ISAC (Auto-ISAC) to dynamically update the Vehicle-Threat Relationship Graph.
3. **Automated ECU Actuation & Isolation**: Implement active response capabilities enabling the console to broadcast CAN gateway isolation frames to quarantine compromised nodes automatically.

