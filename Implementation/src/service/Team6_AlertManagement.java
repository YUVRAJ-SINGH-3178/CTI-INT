package service;

import dao.AlertDao;
import model.Alert;
import model.Result;
import util.DataAccessException;
import util.ValidationException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * CS213 Advanced JAVA Programming - Module 1
 * Member: Yuvraj Singh | Registration No: AP25110090004
 * Module Name: Alert Management (CRUD on Alerts)
 * Class: Team6_AlertManagement
 * ==============================================================================
 */
public class Team6_AlertManagement {

    private final AlertDao alertDao;

    public Team6_AlertManagement() {
        this.alertDao = new AlertDao();
    }

    public Team6_AlertManagement(AlertDao alertDao) {
        this.alertDao = alertDao;
    }

    // --- Mandatory Course Rubric CRUD Operations ---

    /**
     * Create Operation: Persists a new intrusion alert into the alerts table.
     */
    public Alert ap25110090004_AlertManagement_create(Alert alert) throws ValidationException, DataAccessException {
        validateAlert(alert);
        return alertDao.save(alert);
    }

    public Alert AP25110090004_AlertManagement_create(Alert alert) throws ValidationException, DataAccessException {
        return ap25110090004_AlertManagement_create(alert);
    }

    /**
     * Update Operation: Updates an existing intrusion alert.
     */
    public boolean ap25110090004_AlertManagement_update(Alert alert) throws ValidationException, DataAccessException {
        validateAlert(alert);
        if (alert.getAlertId() <= 0) {
            throw new ValidationException("alertId", "Valid alert ID required for update operation");
        }
        return alertDao.update(alert);
    }

    public boolean AP25110090004_AlertManagement_update(Alert alert) throws ValidationException, DataAccessException {
        return ap25110090004_AlertManagement_update(alert);
    }

    /**
     * Retrieve Operation: Fetches an alert by its primary key identifier.
     */
    public Optional<Alert> ap25110090004_AlertManagement_retrive(int alertId) throws DataAccessException {
        return alertDao.findById(alertId);
    }

    public Optional<Alert> AP25110090004_AlertManagement_retrive(int alertId) throws DataAccessException {
        return ap25110090004_AlertManagement_retrive(alertId);
    }

    /**
     * Delete Operation: Deletes an alert by its primary key identifier.
     */
    public boolean ap25110090004_AlertManagement_delete(int alertId) throws DataAccessException {
        return alertDao.deleteById(alertId);
    }

    public boolean AP25110090004_AlertManagement_delete(int alertId) throws DataAccessException {
        return ap25110090004_AlertManagement_delete(alertId);
    }

    // --- Extended Business Logic & Java Streams ---

    /**
     * Retrieves all alerts ordered by descending risk score.
     */
    public List<Alert> getAllAlerts() throws DataAccessException {
        return alertDao.findAll();
    }

    /**
     * Filters alerts dynamically using Java Streams:
     * filters by keyword search, domain selection, technique selection, and severity.
     */
    public List<Alert> filterAlerts(String query, String domainFilter, String techniqueFilter, String severityFilter) throws DataAccessException {
        List<Alert> all = alertDao.findAll();

        return all.stream()
                .filter(a -> {
                    if (query == null || query.isBlank()) return true;
                    String q = query.trim().toLowerCase();
                    return a.getAttackType().toLowerCase().contains(q) ||
                           a.getMitreTechnique().toLowerCase().contains(q) ||
                           a.getTechniqueName().toLowerCase().contains(q) ||
                           a.getTargetAssets().toLowerCase().contains(q) ||
                           a.getCweId().toLowerCase().contains(q) ||
                           String.valueOf(a.getSampleId()).contains(q);
                })
                .filter(a -> {
                    if (domainFilter == null || domainFilter.equalsIgnoreCase("ALL")) return true;
                    return a.getDomain().equalsIgnoreCase(domainFilter.trim());
                })
                .filter(a -> {
                    if (techniqueFilter == null || techniqueFilter.equalsIgnoreCase("ALL")) return true;
                    return a.getMitreTechnique().equalsIgnoreCase(techniqueFilter.trim()) ||
                           a.getTechniqueName().equalsIgnoreCase(techniqueFilter.trim());
                })
                .filter(a -> {
                    if (severityFilter == null || severityFilter.equalsIgnoreCase("ALL")) return true;
                    return a.getSeverity().equalsIgnoreCase(severityFilter.trim());
                })
                .sorted(Comparator.comparingDouble(Alert::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Groups alerts by severity count using Java Streams Collectors.groupingBy.
     */
    public Map<String, Long> getSeverityDistribution() throws DataAccessException {
        return alertDao.findAll().stream()
                .collect(Collectors.groupingBy(Alert::getSeverity, Collectors.counting()));
    }

    /**
     * Validates domain constraints on alert before persistence.
     */
    private void validateAlert(Alert alert) throws ValidationException {
        if (alert == null) {
            throw new ValidationException("alert", "Alert instance cannot be null");
        }
        if (alert.getAttackType() == null || alert.getAttackType().isBlank()) {
            throw new ValidationException("attackType", "Attack type must be specified");
        }
        if (alert.getConfidence() < 0.0 || alert.getConfidence() > 1.0) {
            throw new ValidationException("confidence", "Confidence probability must be between 0.0 and 1.0");
        }
        if (alert.getRiskScore() < 0.0) {
            throw new ValidationException("riskScore", "Risk score cannot be negative");
        }
        if (alert.getMitreTechnique() == null || alert.getMitreTechnique().isBlank()) {
            throw new ValidationException("mitreTechnique", "MITRE technique must be provided");
        }
    }
}

