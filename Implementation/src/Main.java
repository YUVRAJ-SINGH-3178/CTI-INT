import service.*;
import ui.AwtLoginFrame;
import util.DatabaseInitializer;

import java.io.File;

/**
 * Connected Vehicle CTI Security Operations Console (Pure Java AWT Application).
 * CS213 Advanced JAVA Programming - Team 6
 * Full Dataset Implementation (7,683 Real Alerts, 28 Nodes, 46 Edges)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  Connected Vehicle CTI Security Operations Console (Java AWT - Full Dataset)");
        System.out.println("  CS213 Advanced JAVA Programming - Team 6");
        System.out.println("================================================================================");

        try {
            // Locate database and initialize schema/seed if needed
            String dbPath = "database/vehicle_cti.db";
            String schemaPath = "database/schema.sql";
            String graphPath = "../Dataset/vehicle_threat_graph.json";
            String alertsPath = "../Dataset/full_alerts_dataset.csv";

            // Fallback for paths when run from root
            if (!new File("database").exists() && new File("Implementation/database").exists()) {
                dbPath = "Implementation/" + dbPath;
                schemaPath = "Implementation/" + schemaPath;
                graphPath = "Dataset/vehicle_threat_graph.json";
                alertsPath = "Dataset/full_alerts_dataset.csv";
            }

            System.out.println("[*] Checking database integrity at: " + dbPath);
            DatabaseInitializer.initializeDatabase(dbPath, schemaPath, graphPath, alertsPath);

            // Instantiate member services
            Team6_AlertManagement alertService = new Team6_AlertManagement();
            Team6_ThreatGraph threatGraphService = new Team6_ThreatGraph();
            Team6_TelemetryIngest telemetryService = new Team6_TelemetryIngest();
            Team6_RiskRanking riskRankingService = new Team6_RiskRanking();
            Team6_UserReports userReportsService = new Team6_UserReports();

            System.out.println("[+] Core services initialized. Launching Java AWT Login Frame...");

            AwtLoginFrame loginFrame = new AwtLoginFrame(userReportsService, alertService,
                    threatGraphService, telemetryService, riskRankingService);
            loginFrame.setVisible(true);

        } catch (Exception e) {
            System.err.println("[-] Fatal error during console startup: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

