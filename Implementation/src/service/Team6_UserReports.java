package service;

import dao.AuditReportDao;
import dao.UserDao;
import model.Alert;
import model.AuditReport;
import model.User;
import util.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * CS213 Advanced JAVA Programming - Module 5
 * Member: Kaushik Kaligotla | Registration No: AP25110090168
 * Module Name: Users, Login and Report Export
 * Class: Team6_UserReports
 * ==============================================================================
 */
public class Team6_UserReports {

    private final UserDao userDao;
    private final AuditReportDao auditReportDao;
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Team6_UserReports() {
        this.userDao = new UserDao();
        this.auditReportDao = new AuditReportDao();
    }

    public Team6_UserReports(UserDao userDao, AuditReportDao auditReportDao) {
        this.userDao = userDao;
        this.auditReportDao = auditReportDao;
    }

    // --- Mandatory Course Rubric CRUD Operations ---

    /**
     * Create Operation: Registers and persists a new system user with salted SHA-256 hashing.
     */
    public User ap25110090168_UserReports_create(User user) throws ValidationException, DataAccessException {
        validateUser(user);
        if (user.getSalt() == null || user.getSalt().isBlank()) {
            String salt = PasswordSecurity.generateSalt();
            user.setSalt(salt);
            user.setPasswordHash(PasswordSecurity.hashPassword(user.getPasswordHash(), salt));
        }
        user.setCreatedAt(LocalDateTime.now().format(ISO_FMT));
        return userDao.save(user);
    }

    public User AP25110090168_UserReports_create(User user) throws ValidationException, DataAccessException {
        return ap25110090168_UserReports_create(user);
    }

    /**
     * Update Operation: Updates existing user credentials or role.
     */
    public boolean ap25110090168_UserReports_update(User user) throws ValidationException, DataAccessException {
        validateUser(user);
        return userDao.update(user);
    }

    public boolean AP25110090168_UserReports_update(User user) throws ValidationException, DataAccessException {
        return ap25110090168_UserReports_update(user);
    }

    /**
     * Retrieve Operation: Retrieves a user account by primary key ID.
     */
    public Optional<User> ap25110090168_UserReports_retrive(int userId) throws DataAccessException {
        return userDao.findById(userId);
    }

    public Optional<User> AP25110090168_UserReports_retrive(int userId) throws DataAccessException {
        return ap25110090168_UserReports_retrive(userId);
    }

    /**
     * Delete Operation: Deletes a user account by ID.
     */
    public boolean ap25110090168_UserReports_delete(int userId) throws DataAccessException {
        return userDao.deleteById(userId);
    }

    public boolean AP25110090168_UserReports_delete(int userId) throws DataAccessException {
        return ap25110090168_UserReports_delete(userId);
    }

    // --- Authentication & Report Export Operations ---

    /**
     * Authenticates an operator using SHA-256 password hashing with salt verification.
     */
    public User authenticate(String username, String password) throws AuthenticationException, DataAccessException {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new AuthenticationException("Username and password cannot be empty");
        }

        Optional<User> userOpt = userDao.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username or credentials");
        }

        User user = userOpt.get();
        boolean valid = PasswordSecurity.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid password. Authentication failed.");
        }

        return user;
    }

    /**
     * Exports a list of visible alerts to a CSV report on disk and records an audit log entry.
     */
    public File exportAlertsToCsv(List<Alert> alerts, File targetFile, String exportedBy) throws IOException, ValidationException, DataAccessException {
        if (targetFile == null) {
            throw new ValidationException("targetFile", "Target export file cannot be null");
        }
        if (alerts == null) {
            throw new ValidationException("alerts", "Alerts list cannot be null");
        }

        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            // Write CSV Header
            bw.write("alert_id,sample_id,domain,attack_type,confidence,risk_score,mitre_technique,technique_name,severity,target_assets,cwe_id,timestamp,status");
            bw.newLine();

            for (Alert a : alerts) {
                bw.write(a.toCsvRow());
                bw.newLine();
            }
        }

        // Record audit trail
        String now = LocalDateTime.now().format(ISO_FMT);
        AuditReport audit = new AuditReport(0, "Alerts Export - " + targetFile.getName(),
                                           (exportedBy == null ? "System" : exportedBy),
                                           targetFile.getAbsolutePath(), alerts.size(), now);
        auditReportDao.save(audit);

        return targetFile;
    }

    public List<AuditReport> getAllAuditReports() throws DataAccessException {
        return auditReportDao.findAll();
    }

    public List<User> getAllUsers() throws DataAccessException {
        return userDao.findAll();
    }

    private void validateUser(User user) throws ValidationException {
        if (user == null) {
            throw new ValidationException("user", "User instance cannot be null");
        }
        if (user.getUsername() == null || user.getUsername().trim().length() < 3) {
            throw new ValidationException("username", "Username must be at least 3 characters");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("fullName", "Full name cannot be blank");
        }
    }
}

