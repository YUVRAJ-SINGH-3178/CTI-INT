package util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC connections to the SQLite embedded database.
 * Enforces PRAGMA foreign_keys = ON on all connections.
 */
public class DatabaseConnection {

    private static String dbPath = "database/vehicle_cti.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: SQLite JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    public static synchronized void setDatabasePath(String path) {
        dbPath = path;
    }

    public static synchronized String getDatabasePath() {
        return dbPath;
    }

    /**
     * Obtains a new JDBC Connection to SQLite without embedded credentials.
     */
    public static Connection getConnection() throws SQLException {
        // Ensure parent directory exists
        File file = new File(dbPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        String url = "jdbc:sqlite:" + dbPath.replace('\\', '/');
        Connection conn = DriverManager.getConnection(url);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}

