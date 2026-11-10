package service;

import dao.Ranker;
import dao.RiskConfigDao;
import model.Alert;
import model.RiskConfig;
import util.DataAccessException;
import util.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * CS213 Advanced JAVA Programming - Module 4
 * Member: Nandam Pardhu Kumar | Registration No: AP25110090149
 * Module Name: Risk Scoring and Ranking (Streams, Weights, Sensitivity View)
 * Class: Team6_RiskRanking
 * ==============================================================================
 */
public class Team6_RiskRanking implements Ranker<Alert> {

    private final RiskConfigDao riskConfigDao;
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Team6_RiskRanking() {
        this.riskConfigDao = new RiskConfigDao();
    }

    public Team6_RiskRanking(RiskConfigDao riskConfigDao) {
        this.riskConfigDao = riskConfigDao;
    }

    // --- Mandatory Course Rubric CRUD Operations ---

    /**
     * Create Operation: Persists a new risk configuration for an asset.
     */
    public RiskConfig ap25110090149_RiskRanking_create(RiskConfig config) throws ValidationException, DataAccessException {
        validateConfig(config);
        config.setUpdatedAt(LocalDateTime.now().format(ISO_FMT));
        return riskConfigDao.save(config);
    }

    public RiskConfig AP25110090149_RiskRanking_create(RiskConfig config) throws ValidationException, DataAccessException {
        return ap25110090149_RiskRanking_create(config);
    }

    /**
     * Update Operation: Updates existing asset criticality weights.
     */
    public boolean ap25110090149_RiskRanking_update(RiskConfig config) throws ValidationException, DataAccessException {
        validateConfig(config);
        config.setUpdatedAt(LocalDateTime.now().format(ISO_FMT));
        return riskConfigDao.update(config);
    }

    public boolean AP25110090149_RiskRanking_update(RiskConfig config) throws ValidationException, DataAccessException {
        return ap25110090149_RiskRanking_update(config);
    }

    /**
     * Retrieve Operation: Retrieves a risk configuration by its ID.
     */
    public Optional<RiskConfig> ap25110090149_RiskRanking_retrive(int configId) throws DataAccessException {
        return riskConfigDao.findById(configId);
    }

    public Optional<RiskConfig> AP25110090149_RiskRanking_retrive(int configId) throws DataAccessException {
        return ap25110090149_RiskRanking_retrive(configId);
    }

    /**
     * Delete Operation: Deletes a risk configuration by ID.
     */
    public boolean ap25110090149_RiskRanking_delete(int configId) throws DataAccessException {
        return riskConfigDao.deleteById(configId);
    }

    public boolean AP25110090149_RiskRanking_delete(int configId) throws DataAccessException {
        return ap25110090149_RiskRanking_delete(configId);
    }

    // --- Heuristic Risk Computation & Java Streams Ranking ---

    /**
     * Recomputes heuristic risk scores for a list of alerts using the formula:
     * Heuristic Risk Score = Asset Criticality x Impact Weight x Predicted Attack Probability
     * Note: This is an explicit rule-based heuristic scoring formulation, NOT a learned model.
     */
    public List<Alert> recalculateHeuristicRiskScores(List<Alert> alerts) throws DataAccessException {
        Map<String, RiskConfig> configMap = riskConfigDao.findAll().stream()
                .collect(Collectors.toMap(RiskConfig::getAssetName, c -> c, (c1, c2) -> c1));

        return alerts.stream()
                .peek(alert -> {
                    // Match asset in config or use defaults
                    double criticality = 3.5;
                    double impact = 3.5;
                    for (Map.Entry<String, RiskConfig> entry : configMap.entrySet()) {
                        if (alert.getTargetAssets().toLowerCase().contains(entry.getKey().toLowerCase())) {
                            criticality = entry.getValue().getCriticalityFactor();
                            impact = entry.getValue().getImpactWeight();
                            break;
                        }
                    }

                    // Polymorphic dispatch to CanAlert or V2xAlert
                    alert.calculateHeuristicRisk(criticality, impact);

                    // Re-evaluate severity tier based on risk threshold
                    double r = alert.getRiskScore();
                    try {
                        if (r >= 20.0) alert.setSeverity("CRITICAL");
                        else if (r >= 12.0) alert.setSeverity("HIGH");
                        else if (r >= 6.0) alert.setSeverity("MEDIUM");
                        else alert.setSeverity("LOW");
                    } catch (ValidationException ignored) {}
                })
                .sorted(Comparator.comparingDouble(Alert::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Implementation of Ranker<Alert> interface.
     * Orders alerts in descending order of calculated heuristic risk score using Streams.
     */
    @Override
    public List<Alert> rank(List<Alert> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .sorted(Comparator.comparingDouble(Alert::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> rankWithComparator(List<Alert> items, Comparator<Alert> comparator) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Generates Weight Sensitivity Analysis evaluating risk score distribution stability
     * under +/- 10%, 20%, and 30% weight perturbation sweeps.
     */
    public List<Map<String, Object>> computeWeightSensitivity(List<Alert> alerts) {
        List<Map<String, Object>> results = new ArrayList<>();
        double[] deltas = {-0.30, -0.20, -0.10, 0.0, 0.10, 0.20, 0.30};

        for (double delta : deltas) {
            double multiplier = 1.0 + delta;
            DoubleSummaryStatistics stats = alerts.stream()
                    .mapToDouble(a -> a.getRiskScore() * multiplier)
                    .summaryStatistics();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Perturbation", String.format("%+d%%", (int)(delta * 100)));
            row.put("MeanRisk", Math.round(stats.getAverage() * 100.0) / 100.0);
            row.put("MaxRisk", Math.round(stats.getMax() * 100.0) / 100.0);
            row.put("MinRisk", Math.round(stats.getMin() * 100.0) / 100.0);
            results.add(row);
        }
        return results;
    }

    public List<RiskConfig> getAllConfigs() throws DataAccessException {
        return riskConfigDao.findAll();
    }

    private void validateConfig(RiskConfig config) throws ValidationException {
        if (config == null) {
            throw new ValidationException("config", "Risk configuration cannot be null");
        }
        if (config.getAssetName() == null || config.getAssetName().isBlank()) {
            throw new ValidationException("assetName", "Asset name is required");
        }
        if (config.getCriticalityFactor() < 0.0 || config.getImpactWeight() < 0.0) {
            throw new ValidationException("weights", "Criticality and impact weights must be non-negative");
        }
    }
}

