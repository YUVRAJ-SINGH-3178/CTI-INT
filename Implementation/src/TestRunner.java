import dao.*;
import model.*;
import service.*;
import util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * CS213 Advanced JAVA Programming - Automated Test Runner
 * Team 6: Full Real Dataset & Java AWT Console
 */
public class TestRunner {

    private static class TestCaseResult {
        String testId;
        String scenario;
        String expected;
        String actual;
        String status;

        TestCaseResult(String testId, String scenario, String expected, String actual, boolean pass) {
            this.testId = testId;
            this.scenario = scenario;
            this.expected = expected;
            this.actual = actual;
            this.status = pass ? "PASS" : "FAIL";
        }
    }

    private static final List<TestCaseResult> results = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  CS213 ADVANCED JAVA PROGRAMMING - AUTOMATED TEST SUITE (TEAM 6)");
        System.out.println("  Testing: OOP Concepts, Advanced Java, JDBC SQLite CRUD, Full Dataset & AWT");
        System.out.println("================================================================================");

        String testDb = "database/test_runner.db";
        File f = new File(testDb);
        if (f.exists()) f.delete();

        try {
            DatabaseInitializer.initializeDatabase(testDb, "database/schema.sql",
                    "../Dataset/vehicle_threat_graph.json",
                    "../Dataset/full_alerts_dataset.csv");
        } catch (Exception e) {
            System.err.println("[-] Failed initializing test database: " + e.getMessage());
        }

        DatabaseConnection.setDatabasePath(testDb);

        Team6_AlertManagement alertService = new Team6_AlertManagement();
        Team6_ThreatGraph threatGraphService = new Team6_ThreatGraph();
        Team6_TelemetryIngest telemetryService = new Team6_TelemetryIngest();
        Team6_RiskRanking riskRankingService = new Team6_RiskRanking();
        Team6_UserReports userReportsService = new Team6_UserReports();

        // Pre-mutation check for TC-19: Full CSV Database Seeding (7,683 rows)
        TestCaseResult tc19Result;
        try {
            List<Alert> seededAlerts = alertService.getAllAlerts();
            long canCount = seededAlerts.stream().filter(a -> "CAN".equals(a.getDomain())).count();
            long v2xCount = seededAlerts.stream().filter(a -> "V2X".equals(a.getDomain())).count();
            Optional<Alert> firstOpt = alertService.ap25110090004_AlertManagement_retrive(1);
            boolean firstOk = firstOpt.isPresent() && "RPM".equals(firstOpt.get().getAttackType())
                    && Math.abs(firstOpt.get().getRiskScore() - 6.18) < 0.01;
            boolean pass = (seededAlerts.size() == 7683) && (canCount == 7499) && (v2xCount == 184) && firstOk;
            tc19Result = new TestCaseResult("TC-19", "Full dataset database seeding verification (7,683 alerts)",
                    "Seeds 7,683 alerts (7,499 CAN + 184 V2X); Row 1=RPM (risk=6.18)",
                    "Total=" + seededAlerts.size() + " (CAN=" + canCount + ", V2X=" + v2xCount + "), Row1=" +
                    (firstOpt.isPresent() ? firstOpt.get().getAttackType() + "@" + firstOpt.get().getRiskScore() : "None"), pass);
        } catch (Exception e) {
            tc19Result = new TestCaseResult("TC-19", "Full dataset seeding verification", "7,683 alerts", "Exception: " + e.getMessage(), false);
        }

        // TC-01: Valid CanAlert Create
        try {
            CanAlert canAlert = new CanAlert(0, 501, "DoS_Injection", 0.95, 0.0,
                    "T0814", "Denial of Service", "CRITICAL", "['Braking_ECU']", "CWE-400",
                    "2026-11-09 10:00:00", "NEW", 0x128, "00 00 00 00", 8);
            canAlert.calculateHeuristicRisk(5.0, 4.0);
            Alert created = alertService.ap25110090004_AlertManagement_create(canAlert);
            boolean pass = created != null && created.getAlertId() > 0;
            results.add(new TestCaseResult("TC-01", "Create valid CanAlert with physical CAN ID",
                    "Alert persisted with positive generated alert_id", "Persisted alert_id=" + created.getAlertId(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-01", "Create valid CanAlert", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-02: Missing Attack Type
        try {
            Alert bad = new CanAlert(0, 502, "", 0.85, 10.0, "T0814", "DoS", "HIGH", "[]", "CWE-400", "2026-11-09", "NEW", 0x128, "", 8);
            alertService.ap25110090004_AlertManagement_create(bad);
            results.add(new TestCaseResult("TC-02", "Create alert with empty attack type", "Throw ValidationException", "Created unexpectedly", false));
        } catch (ValidationException e) {
            results.add(new TestCaseResult("TC-02", "Create alert with empty attack type", "Throw ValidationException for missing attack type", "Caught: " + e.getMessage(), true));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-02", "Create alert with empty attack type", "ValidationException", "Unexpected: " + e.getMessage(), false));
        }

        // TC-03: Invalid Confidence
        try {
            new CanAlert(0, 503, "RPM", 1.85, 10.0, "T0856", "Spoof", "HIGH", "[]", "CWE-345", "2026-11-09", "NEW", 0x291, "", 8);
            results.add(new TestCaseResult("TC-03", "Create alert with invalid confidence 1.85 (>1.0)", "Throw ValidationException", "Created unexpectedly", false));
        } catch (ValidationException e) {
            results.add(new TestCaseResult("TC-03", "Create alert with invalid confidence 1.85 (>1.0)", "Throw ValidationException for confidence out of range", "Caught: " + e.getMessage(), true));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-03", "Create alert with invalid confidence", "ValidationException", "Unexpected: " + e.getMessage(), false));
        }

        // TC-04: Retrieve existing alert
        try {
            Optional<Alert> a = alertService.ap25110090004_AlertManagement_retrive(1);
            boolean pass = a.isPresent() && a.get().getAlertId() == 1;
            results.add(new TestCaseResult("TC-04", "Retrieve existing alert by primary key ID=1",
                    "Returns Optional containing Alert entity with ID 1", "Found alert: " + (a.isPresent() ? a.get().getAttackType() : "None"), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-04", "Retrieve existing alert", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-05: Update alert
        try {
            Optional<Alert> opt = alertService.ap25110090004_AlertManagement_retrive(1);
            if (opt.isPresent()) {
                Alert a = opt.get();
                a.setStatus("RESOLVED");
                boolean ok = alertService.ap25110090004_AlertManagement_update(a);
                Optional<Alert> updated = alertService.ap25110090004_AlertManagement_retrive(1);
                boolean pass = ok && updated.isPresent() && updated.get().getStatus().equals("RESOLVED");
                results.add(new TestCaseResult("TC-05", "Update alert status to 'RESOLVED'",
                        "Update returns true; retrieved entity reflects modified status", "Updated=" + ok + ", Status=" + updated.get().getStatus(), pass));
            } else {
                results.add(new TestCaseResult("TC-05", "Update alert", "Updated", "Alert ID 1 not found", false));
            }
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-05", "Update alert", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-06: Delete alert
        try {
            CanAlert temp = new CanAlert(0, 9999, "Temp_Attack", 0.5, 5.0, "T0814", "DoS", "LOW", "[]", "CWE-400", "2026-11-09", "NEW", 0x100, "", 8);
            Alert saved = alertService.ap25110090004_AlertManagement_create(temp);
            boolean deleted = alertService.ap25110090004_AlertManagement_delete(saved.getAlertId());
            Optional<Alert> after = alertService.ap25110090004_AlertManagement_retrive(saved.getAlertId());
            boolean pass = deleted && after.isEmpty();
            results.add(new TestCaseResult("TC-06", "Delete alert by primary key ID",
                    "Delete returns true; subsequent findById returns Optional.empty()", "Deleted=" + deleted + ", Present=" + after.isPresent(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-06", "Delete alert", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-07: Search alerts
        try {
            List<Alert> matched = alertService.filterAlerts("DoS", "ALL", "ALL", "ALL");
            boolean pass = !matched.isEmpty() && matched.stream().allMatch(a -> a.getAttackType().toLowerCase().contains("dos"));
            results.add(new TestCaseResult("TC-07", "Search alerts with keyword 'DoS'",
                    "Returns non-empty list filtered strictly to DoS attacks", "Matched " + matched.size() + " alerts", pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-07", "Search alerts", "Non-empty list", "Exception: " + e.getMessage(), false));
        }

        // TC-08: Duplicate PK
        try {
            ThreatNode n1 = new ThreatNode("node_test_dup", "Duplicate Asset", "Asset", "Test", 1.0);
            threatGraphService.ap25110090196_ThreatGraph_create(n1);
            ThreatNode n2 = new ThreatNode("node_test_dup", "Duplicate Asset 2", "Asset", "Test", 1.0);
            threatGraphService.ap25110090196_ThreatGraph_create(n2);
            results.add(new TestCaseResult("TC-08", "Insert duplicate node with existing primary key", "Throw DataAccessException", "Inserted duplicate", false));
        } catch (DataAccessException e) {
            boolean pass = e.getMessage().toLowerCase().contains("unique") || e.getMessage().toLowerCase().contains("error");
            results.add(new TestCaseResult("TC-08", "Insert duplicate node with existing primary key",
                    "Throw DataAccessException / SQLite UNIQUE constraint violation", "Caught: " + e.getMessage(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-08", "Insert duplicate node", "DataAccessException", "Exception: " + e.getMessage(), false));
        } finally {
            try { threatGraphService.ap25110090196_ThreatGraph_delete("node_test_dup"); } catch (Exception ignored) {}
        }

        // TC-09: User Auth Success
        try {
            User admin = userReportsService.authenticate("admin", "admin123");
            boolean pass = admin != null && admin.getRole().equals("ADMIN");
            results.add(new TestCaseResult("TC-09", "Authenticate valid user 'admin' with correct password",
                    "Returns User object with role 'ADMIN'", "Authenticated: " + admin.getUsername() + " (" + admin.getRole() + ")", pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-09", "Authenticate user", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-10: User Auth Failure
        try {
            userReportsService.authenticate("admin", "wrongPassword");
            results.add(new TestCaseResult("TC-10", "Authenticate 'admin' with incorrect password", "Throw AuthenticationException", "Passed unexpectedly", false));
        } catch (AuthenticationException e) {
            results.add(new TestCaseResult("TC-10", "Authenticate 'admin' with incorrect password", "Throw AuthenticationException", "Caught: " + e.getMessage(), true));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-10", "Authenticate invalid credentials", "AuthenticationException", "Exception: " + e.getMessage(), false));
        }

        // TC-11: Malformed CSV Row
        try {
            List<String> tokens = CsvHelper.parseLine("corrupted,123,\"unclosed");
            boolean pass = tokens != null && !tokens.isEmpty();
            results.add(new TestCaseResult("TC-11", "Parse malformed CSV row with unclosed quote",
                    "Gracefully recovers and outputs tokenized strings without crash", "Parsed " + tokens.size() + " tokens safely", pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-11", "Parse malformed CSV", "Safe recovery", "Exception: " + e.getMessage(), false));
        }

        // TC-12: Streams Risk Sorting
        try {
            List<Alert> all = alertService.getAllAlerts();
            List<Alert> ranked = riskRankingService.rank(all);
            boolean sorted = true;
            for (int i = 0; i < ranked.size() - 1; i++) {
                if (ranked.get(i).getRiskScore() < ranked.get(i + 1).getRiskScore()) {
                    sorted = false; break;
                }
            }
            results.add(new TestCaseResult("TC-12", "Rank alerts list by heuristic risk score via Java Streams",
                    "List sorted strictly in descending risk score order", "Strictly descending: " + sorted + " (Top: " + ranked.get(0).getRiskScore() + ")", sorted));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-12", "Rank alerts via Streams", "Descending order", "Exception: " + e.getMessage(), false));
        }

        // TC-13: Threat Node CRUD
        try {
            ThreatNode tn = new ThreatNode("test_gw", "Test Gateway", "Asset", "ECU Gateway", 4.0);
            threatGraphService.ap25110090196_ThreatGraph_create(tn);
            Optional<ThreatNode> ret = threatGraphService.ap25110090196_ThreatGraph_retrive("test_gw");
            boolean pass = ret.isPresent() && ret.get().getCriticality() == 4.0;
            threatGraphService.ap25110090196_ThreatGraph_delete("test_gw");
            results.add(new TestCaseResult("TC-13", "Threat graph node create, retrieve and delete",
                    "Node persisted, retrieved with criticality=4.0, and deleted", "Retrieved name=" + ret.get().getNodeName(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-13", "Threat node CRUD", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-14: Relational Graph (28 Nodes, 46 Edges)
        try {
            List<ThreatNode> nodes = threatGraphService.getAllNodes();
            List<ThreatEdge> edges = threatGraphService.getAllEdges();
            Map<String, List<ThreatEdge>> adj = threatGraphService.getAdjacencyMap();
            boolean pass = (nodes.size() == 28) && (edges.size() == 46) && (adj != null && !adj.isEmpty());
            results.add(new TestCaseResult("TC-14", "Threat graph relational verification (28 nodes, 46 edges)",
                    "Seeded database contains exactly 28 nodes and 46 edges with adjacency map",
                    "Nodes=" + nodes.size() + ", Edges=" + edges.size() + ", Adjacency buckets=" + (adj != null ? adj.size() : 0), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-14", "Threat graph relational verification", "28 nodes, 46 edges", "Exception: " + e.getMessage(), false));
        }

        // TC-15: Telemetry Record CRUD
        try {
            TelemetryRecord rec = new TelemetryRecord(0, "CAN", 100.5, "0x128", "AA BB CC DD", "INGESTED", "2026-11-09");
            TelemetryRecord saved = telemetryService.ap25110090041_TelemetryIngest_create(rec);
            Optional<TelemetryRecord> ret = telemetryService.ap25110090041_TelemetryIngest_retrive(saved.getRecordId());
            boolean pass = ret.isPresent() && ret.get().getSourceId().equals("0x128");
            results.add(new TestCaseResult("TC-15", "Telemetry record create & retrieve by primary key",
                    "Record persisted with generated ID and source_id=0x128", "Persisted record_id=" + saved.getRecordId(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-15", "Telemetry record CRUD", "Success", "Exception: " + e.getMessage(), false));
        }

        // TC-16: Weight Sensitivity Sweeps
        try {
            List<Alert> all = alertService.getAllAlerts();
            List<Map<String, Object>> sens = riskRankingService.computeWeightSensitivity(all);
            boolean pass = sens != null && sens.size() == 7;
            results.add(new TestCaseResult("TC-16", "Heuristic weight sensitivity perturbation sweeps (-30% to +30%)",
                    "Computes 7 sensitivity perturbation evaluation tiers", "Computed " + (sens != null ? sens.size() : 0) + " sensitivity tiers", pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-16", "Weight sensitivity sweeps", "7 tiers", "Exception: " + e.getMessage(), false));
        }

        // TC-17: CSV Export Assertion (Header + At Least 1 Data Row)
        try {
            File exportFile = new File("database/test_export.csv");
            List<Alert> all = alertService.getAllAlerts();
            File exported = userReportsService.exportAlertsToCsv(all, exportFile, "test_admin");
            List<String> lines = java.nio.file.Files.readAllLines(exportFile.toPath());
            boolean hasHeader = !lines.isEmpty() && lines.get(0).toLowerCase().contains("alert_id");
            boolean hasDataRows = lines.size() >= 2;
            List<AuditReport> audits = userReportsService.getAllAuditReports();
            boolean pass = exported.exists() && hasHeader && hasDataRows && !audits.isEmpty();
            exportFile.delete();
            results.add(new TestCaseResult("TC-17", "Export visible alerts to CSV and record audit trail",
                    "CSV file created with header and at least one data row, plus audit log entry",
                    "CSV rows=" + lines.size() + " (Header + " + (lines.size() - 1) + " data rows), Audit records=" + audits.size(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-17", "Export CSV and audit trail", "Header + data row", "Exception: " + e.getMessage(), false));
        }

        // TC-18: Polymorphic Risk Score & Domain Details
        try {
            CanAlert canAlert = new CanAlert(0, 101, "DoS_Attack", 0.90, 0.0, "T0814", "DoS", "CRITICAL", "['Braking_ECU']", "CWE-400", "2026-11-09", "NEW", 0x128, "00", 8);
            V2xAlert v2xAlert = new V2xAlert(0, 102, "V2X_Position_Spoof", 0.90, 0.0, "T0806", "Spoofing", "CRITICAL", "['Telematics_OBU']", "CWE-345", "2026-11-09", "NEW", 14, 99, 100.0, 200.0, 35.0);

            double canRisk = canAlert.calculateHeuristicRisk(5.0, 4.0); // 5 x 4 x 0.9 = 18.00
            double v2xRisk = v2xAlert.calculateHeuristicRisk(5.0, 4.0); // 5 x 4 x 0.9 = 18.00

            String canDetails = canAlert.getDomainSpecificDetails();
            String v2xDetails = v2xAlert.getDomainSpecificDetails();
            String canExp = canAlert.getDomainExplanation();
            String v2xExp = v2xAlert.getDomainExplanation();

            boolean riskMatch = Math.abs(canRisk - 18.00) < 0.001 && Math.abs(v2xRisk - 18.00) < 0.001;
            boolean detailsPolymorphic = canDetails.contains("CAN Arbitration ID") && v2xDetails.contains("Transmitter ID");
            boolean expPolymorphic = canExp != null && v2xExp != null && !canExp.equals(v2xExp);
            boolean pass = riskMatch && detailsPolymorphic && expPolymorphic;

            results.add(new TestCaseResult("TC-18", "Heuristic risk score calculation (18.00) and polymorphic domain details",
                    "Both yield risk=18.00; distinct domain details and explanation strings",
                    "CAN Risk=" + canRisk + ", V2X Risk=" + v2xRisk + ", CAN Details=[" + canDetails + "], V2X Details=[" + v2xDetails + "]", pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-18", "Heuristic risk & polymorphic details", "Risk 18.00 & distinct details", "Exception: " + e.getMessage(), false));
        }

        // TC-19: Recorded Full Seeding Check
        results.add(tc19Result);

        // TC-20: Real MITRE ATT&CK STIX 2.1 Bundle Parsing
        try {
            Map<String, StixAttackLoader.StixTechnique> stix = StixAttackLoader.getCatalog();
            String[] reqIds = {"T0814", "T0855", "T0856", "T0831", "T1498", "T1565.002"};
            boolean allPresent = true;
            for (String id : reqIds) {
                if (!stix.containsKey(id)) {
                    allPresent = false;
                    break;
                }
            }
            boolean pass = stix.size() > 500 && allPresent;
            results.add(new TestCaseResult("TC-20", "MITRE attack-stix-data STIX 2.1 JSON extraction",
                    "Extracts ICS + Enterprise ATT&CK techniques including all 6 mapped IDs",
                    "Loaded " + stix.size() + " unique STIX techniques (ICS=" + StixAttackLoader.getIcsTechniqueCount() +
                    ", Enterprise=" + StixAttackLoader.getEnterpriseTechniqueCount() + ", all 6 verified)", pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-20", "STIX 2.1 extraction", "All 6 IDs", "Exception: " + e.getMessage(), false));
        }

        // TC-21: Full CICIoV2024 Raw Dataset Scan (All 6 Files / 1,408,219 Frames) + Batch Ingest
        try {
            List<FullDatasetScanner.CanStreamSummary> canStats = FullDatasetScanner.scanAllCiciov2024Files();
            long totalCanFrames = canStats.stream().mapToLong(s -> s.frameCount).sum();
            File dosFile = new File(FullDatasetScanner.resolveDir("CICIoV2024"), "decimal_DoS.csv");
            int ingestedDos = telemetryService.ingestCsvAsync(dosFile, "CAN", 200, null).get();
            boolean pass = (canStats.size() == 6) && (totalCanFrames == 1408219L) && (ingestedDos == 200);
            results.add(new TestCaseResult("TC-21", "Full CICIoV2024 6-CSV scan (1,408,219 frames) & async ingest",
                    "Scans 6 files totaling 1,408,219 frames and ingests 200 DoS CAN frames",
                    "Files=" + canStats.size() + ", TotalFrames=" + totalCanFrames + ", IngestedDoS=" + ingestedDos, pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-21", "Full CICIoV2024 scan & ingest", "1,408,219 frames", "Exception: " + e.getMessage(), false));
        }

        // TC-22: Full VeReMi Index (225 runs) + Live .tgz BSM Extraction
        try {
            List<FullDatasetScanner.VeremiRunEntry> index = FullDatasetScanner.parseVeremiIndex();
            Optional<FullDatasetScanner.VeremiRunEntry> sampleRun = index.stream().filter(r -> r.archiveFile.exists()).findFirst();
            boolean okArchive = false;
            String actualStr = "Runs=" + index.size();
            if (sampleRun.isPresent()) {
                FullDatasetScanner.VeremiArchiveStats vStats = telemetryService.ingestVeremiArchiveAsync(sampleRun.get().archiveFile, 100, null).get();
                okArchive = vStats.groundTruthEntries > 0 && vStats.bsmMessages > 0 && vStats.sampleRecords.size() == 100;
                actualStr += ", Archive=" + vStats.archiveName + " (GT=" + vStats.groundTruthEntries + ", BSM=" + vStats.bsmMessages + ")";
            }
            boolean pass = (index.size() == 225) && okArchive;
            results.add(new TestCaseResult("TC-22", "VeReMi index (225 runs) & .tgz GroundTruth+BSM stream",
                    "Parses 225 runs and streams GroundTruth + Type-3 BSMs from .tgz",
                    actualStr, pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-22", "VeReMi index & .tgz extraction", "225 runs", "Exception: " + e.getMessage(), false));
        }

        // TC-23: Genuine Multi-Seed (20-seed) & Full Dataset Result Loader Verification
        try {
            ResearchResultsLoader.CsvTable s20 = ResearchResultsLoader.loadCsv("multi_seed_summary_20seeds.csv");
            ResearchResultsLoader.CsvTable r20 = ResearchResultsLoader.loadCsv("multi_seed_all_runs_20seeds.csv");
            ResearchResultsLoader.CsvTable fCan = ResearchResultsLoader.loadCsv("ciciov_full_metrics.csv");
            ResearchResultsLoader.CsvTable fV2x = ResearchResultsLoader.loadCsv("veremi_full_metrics.csv");
            boolean pass = (s20.rows.size() == 14) && (r20.rows.size() == 280) && (fCan.rows.size() == 4) && (fV2x.rows.size() == 4);
            results.add(new TestCaseResult("TC-23", "Verified research results loader (20-seed & full dataset benchmarks)",
                    "Loads 14 summary rows, 280 run rows, 4 full-CAN rows, 4 full-V2X rows",
                    "S20=" + s20.rows.size() + ", R20=" + r20.rows.size() + ", FullCAN=" + fCan.rows.size() + ", FullV2X=" + fV2x.rows.size(), pass));
        } catch (Exception e) {
            results.add(new TestCaseResult("TC-23", "Verified research results loader", "All genuine CSVs loaded", "Exception: " + e.getMessage(), false));
        }

        // Shutdown thread pool and cleanup test database
        telemetryService.shutdown();
        new File(testDb).delete();

        // Print & Save Results
        printResultsTable();
        saveResultsCsv();

        long passedCount = results.stream().filter(r -> r.status.equals("PASS")).count();
        System.out.printf("%n[+] Test Summary: %d / %d Tests Passed (%.1f%% Success Rate)%n",
                passedCount, results.size(), (passedCount * 100.0 / results.size()));

        if (passedCount == results.size()) {
            System.out.println("[+] ALL TESTS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("[-] SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static void printResultsTable() {
        System.out.println("\n----------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-7s | %-42s | %-40s | %-25s | %-6s%n", "TEST ID", "SCENARIO / INPUT", "EXPECTED RESULT", "ACTUAL RESULT", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
        for (TestCaseResult r : results) {
            String sc = r.scenario.length() > 42 ? r.scenario.substring(0, 39) + "..." : r.scenario;
            String ex = r.expected.length() > 40 ? r.expected.substring(0, 37) + "..." : r.expected;
            String ac = r.actual.length() > 25 ? r.actual.substring(0, 22) + "..." : r.actual;
            System.out.printf("%-7s | %-42s | %-40s | %-25s | %-6s%n", r.testId, sc, ex, ac, r.status);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
    }

    private static void saveResultsCsv() {
        File outCsv = new File("../Documentation/test_results.csv");
        if (!outCsv.getParentFile().exists()) outCsv.getParentFile().mkdirs();

        try (PrintWriter pw = new PrintWriter(new FileWriter(outCsv))) {
            pw.println("Test ID,Scenario / Input,Expected Result,Actual Result,Status");
            for (TestCaseResult r : results) {
                pw.printf("%s,\"%s\",\"%s\",\"%s\",%s%n",
                        r.testId, r.scenario.replace("\"", "\"\""),
                        r.expected.replace("\"", "\"\""),
                        r.actual.replace("\"", "\"\""),
                        r.status);
            }
            System.out.println("[+] Test results saved to: " + outCsv.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[-] Error saving test results CSV: " + e.getMessage());
        }
    }
}
