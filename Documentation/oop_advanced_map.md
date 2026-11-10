# Object-Oriented Programming (OOP) and Advanced Java Concepts Mapping

This document maps every required Object-Oriented Programming (OOP) paradigm and Advanced Java feature directly to the files and line ranges inside `Implementation/src/`.

---

## 1. Object-Oriented Programming (OOP) Concepts

| OOP Concept | Class / Interface | Source File | Line Range | Description & Role |
|-------------|-------------------|-------------|------------|--------------------|
| **Class & Object** | `ThreatNode`, `ThreatEdge`, `Alert` | `model/ThreatNode.java` | L9 - L27 | Instantiated domain objects modeling vehicle ECU assets, attack techniques, and threat graph edges. |
| **Encapsulation** | `Alert` | `model/Alert.java` | L11 - L23, L101 - L213 | Private fields protected by public getters and validated setters throwing `ValidationException`. |
| **Inheritance** | `CanAlert`, `V2xAlert` (extend `Alert`) | `model/CanAlert.java`<br>`model/V2xAlert.java` | `CanAlert`: L9 - L31<br>`V2xAlert`: L9 - L35 | Both subclasses inherit shared alert fields and validation from abstract `Alert` and add CAN/V2X fields. |
| **Polymorphism** | `Alert`, `CanAlert`, `V2xAlert` | `model/CanAlert.java`<br>`model/V2xAlert.java` | `CanAlert`: L42 - L66<br>`V2xAlert`: L46 - L75 | Both subclasses override `calculateHeuristicRisk()` (`Criticality x Impact Weight x Confidence`, rounded to 2 decimals) and provide domain-specific `getDomainSpecificDetails()`, `getDomainExplanation()`, and `describe()`. |
| **Abstraction** | `Alert` | `model/Alert.java` | L9, L52 - L68 | Abstract base class declaring abstract methods (`getDomain()`, `calculateHeuristicRisk()`, `getDomainSpecificDetails()`, `getDomainExplanation()`). |
| **Interface** | `Repository<T, ID>`, `Ranker<T>` | `dao/Repository.java`<br>`dao/Ranker.java` | `Repository`: L15 - L41<br>`Ranker`: L11 - L22 | Decouples persistence and ranking contracts from concrete JDBC DAO and service implementations. |

---

## 2. Advanced Java Concepts

| Advanced Concept | Class Name | Source File | Line Range | Where & How Used |
|------------------|------------|-------------|------------|------------------|
| **Exception Handling** | `ValidationException`<br>`DataAccessException`<br>`AuthenticationException` | `util/ValidationException.java`<br>`util/DataAccessException.java`<br>`util/AuthenticationException.java` | Full classes; `dao/*`, `ui/*` | Custom checked and unchecked exceptions. `try-with-resources` closes JDBC `Connection`, `PreparedStatement`, `ResultSet`, and file streams; AWT dialogs report errors cleanly. |
| **Multithreading** | `Team6_TelemetryIngest`<br>`AwtDashboardFrame` | `service/Team6_TelemetryIngest.java`<br>`ui/AwtDashboardFrame.java` | `TelemetryIngest`: L33, L96 - L170<br>`AwtDashboardFrame`: L370 - L545 | `ExecutorService` thread pools and `CompletableFuture` execute CICIoV2024 CSV ingestion, VeReMi `.tgz` extraction, and STIX 2.1 parsing in background threads, posting UI updates safely via `EventQueue.invokeLater()`. |
| **Collections Framework** | `ThreatGraphDao`<br>`Team6_RiskRanking` | `dao/ThreatGraphDao.java`<br>`service/Team6_RiskRanking.java` | `ThreatGraphDao`: L171 - L180<br>`RiskRanking`: L94 - L96 | `Map<String, List<ThreatEdge>>` builds the threat graph adjacency map; `List<Alert>` stores ordered alerts; `TreeSet<Integer>` collects unique CAN IDs across streams. |
| **Generics** | `Repository<T, ID>`<br>`Result<T>` | `dao/Repository.java`<br>`model/Result.java` | `Repository`: L15 - L41<br>`Result`: L9 - L55 | Generic DAO contract parameterized by entity type `T` and key `ID`; generic response wrapper `Result<T>`. |
| **Java Streams API** | `Team6_AlertManagement`<br>`Team6_RiskRanking` | `service/Team6_AlertManagement.java`<br>`service/Team6_RiskRanking.java` | `AlertManagement`: L99 - L136<br>`RiskRanking`: L97 - L168 | Stream pipelines execute multi-predicate filtering (`filter`), risk-score sorting (`sorted`), severity counts (`groupingBy`), and summary statistics (`DoubleSummaryStatistics`). |
