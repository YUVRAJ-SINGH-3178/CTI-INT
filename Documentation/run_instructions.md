# Build and Execution Instructions for Windows 11

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Project**: Connected Vehicle CTI Security Operations Console (Java AWT)

---

## 1. Prerequisites & Environment

- **Operating System**: Windows 11 (64-bit)
- **Java Development Kit**: Java JDK 27 at `C:\Program Files\Java\jdk-27.0.1.0.3`
  *(All `.bat` scripts invoke this explicit JDK path; `java` does not need to be on system `PATH`.)*
- **Libraries**: `Implementation/lib/sqlite-jdbc-3.46.1.3.jar` and `Implementation/lib/gson-2.11.0.jar` (no Maven or Gradle needed).
- **Datasets**:
  - `Dataset/`: Pre-computed CSV/JSON research outputs and figures (zero fake data).
  - `CICIoV2024/`: Full 6 CAN bus CSV streams (1,408,219 frames).
  - `VeReMi/`: Full 225 simulation `.tgz` archives and `VeReMi/VeReMi/index.md`.
  - `attack-stix-data/`: Full MITRE ATT&CK STIX 2.1 JSON bundles (`ics-attack.json`, `enterprise-attack.json`).

---

## 2. Step-by-Step Execution Guide

Open Command Prompt or PowerShell inside the `Implementation` directory:

### Step 1: Compile the Java Application
```bat
build.bat
```
Compiles all Java sources in `src/` with JDK 27 `javac.exe` into `bin/`.

### Step 2: Execute the Automated Test Runner
```bat
test.bat
```
Runs the full plain-Java test suite (`TestRunner`), writes `Documentation/test_results.csv`, and prints pass/fail counts to the console.

### Step 3: Launch the Pure Java AWT Console
```bat
run.bat
```
Starts `AwtLoginFrame` -> authenticates user via salted SHA-256 -> opens `AwtDashboardFrame`.

*Default Login Credentials*:
- **Administrator**: `admin` / `admin123`
- **Security Analyst**: `analyst` / `analyst123`
- **Fleet Operator**: `operator` / `operator123`

---

## 3. Resetting / Re-seeding the SQLite Database

Delete `Implementation/database/vehicle_cti.db`:
```bat
del database\vehicle_cti.db
```
Next time `run.bat` or `test.bat` executes, `DatabaseInitializer` recreates the schema from `database/schema.sql`, seeds default users/asset risk configs, loads all 28 threat nodes and 46 edges from `Dataset/vehicle_threat_graph.json`, and loads all 7,683 genuine alerts from `Dataset/full_alerts_dataset.csv`.
