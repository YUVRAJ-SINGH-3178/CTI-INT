# Build and Execution Instructions for Windows 11

**Course**: CS213 Advanced JAVA Programming  
**Team**: Team 6  
**Project**: Connected Vehicle CTI Security Operations Console  

---

## 1. Prerequisites & Environment

- **Operating System**: Windows 11 (64-bit)
- **Java Development Kit**: Java JDK 27 installed at:  
  `C:\Program Files\Java\jdk-27.0.1.0.3`
  *(All batch scripts explicitly use this absolute path; `java` and `javac` do not need to be on system PATH)*.
- **Build Tools**: No Maven or Gradle required. Standard Windows batch scripts compile source files via `javac` directly against library JARs in `lib/`.
- **Database**: SQLite 3 (self-contained via `sqlite-jdbc-3.46.1.3.jar` in `lib/`). No separate database service installation required.

---

## 2. Step-by-Step Execution Guide

Open a Windows Command Prompt (`cmd.exe`) or PowerShell window and navigate to the `Implementation/` directory:

```cmd
cd /d "C:\Users\satis\OneDrive\Desktop\CTI Intelligence - Copy\VehicleCTI_AP25110090004\Implementation"
```

### Step 1: Compile the Java Application (`build.bat`)
Run the compilation batch script:
```cmd
build.bat
```
- **What it does**: Gathers all Java source files across `src/model`, `src/dao`, `src/service`, `src/ui`, `src/util` and compiles them with JDK 27 `javac.exe` into bytecode stored in `bin/`.
- **Expected Output**:
  ```
  [*] Compiling Java source files using JDK 27...
  [*] Classpath: "lib\*"
  [SUCCESS] Build completed successfully. Bytecode compiled to bin\
  ```

---

### Step 2: Execute Automated Test Runner (`test.bat`)
Run the plain-Java automated test harness:
```cmd
test.bat
```
- **What it does**: Runs 18 rigorous automated test scenarios covering alert CRUD, input validation, duplicate keys, password hashing, CSV resilience, stream ranking, and polymorphic risk calculation.
- **Expected Output**: Prints the formatted test-case matrix to the console and generates `Documentation/test_results.csv`. All 18 tests will report `PASS`.

---

### Step 3: Launch the GUI Security Console (`run.bat`)
Run the runtime execution script:
```cmd
run.bat
```
- **What it does**: Launches the Java AWT GUI operations console using JDK 27 `java.exe`.
- **Default Authentication Credentials**:
  - **Administrator**: Username: `admin` | Password: `admin123` (Full administrative privileges, database maintenance, user provisioning).
  - **Security Analyst**: Username: `analyst` | Password: `analyst123` (Alert management, filtering, graph exploration, telemetry streaming, CSV export).
  - **Fleet Operator**: Username: `operator` | Password: `operator123` (Telemetry monitoring).

---

## 3. How to Reset / Re-seed the Database

If the database needs to be reset back to its pristine state with original threat graph nodes and baseline alerts:

### Method A: From the Running GUI
1. Log in with the `admin` account (`admin` / `admin123`).
2. Click on the **"Administration & Audit Logs"** tab.
3. Navigate to the **"Database Maintenance"** sub-tab.
4. Click **"Reseed Database from Baseline JSON/CSV"** and confirm. The system will drop/recreate tables and reload `vehicle_threat_graph.json` and `alerts_sample.csv`.

### Method B: From the Command Line
Simply delete the SQLite database file:
```cmd
del database\vehicle_cti.db
```
When `run.bat` or `test.bat` is next executed, `DatabaseInitializer` will automatically detect the absence of the database, execute `database/schema.sql`, reseed the 28 threat nodes, 46 edges, 40 ranked alerts, default users, and asset risk configurations.

