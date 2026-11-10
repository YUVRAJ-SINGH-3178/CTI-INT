# Dataset Provenance & Experimental Files

This directory stores the verified evaluation outputs, CTI mappings, and research plots used by the Java AWT operations console, alongside the full raw source directories at project root (`CICIoV2024/`, `VeReMi/`, `attack-stix-data/`).

## 1. Alert & Knowledge Graph Files
- `full_alerts_dataset.csv`: 7,683 real intrusion alerts combined from `can_ciciov2024_risk_ranked_alerts.csv` (7,499 CAN alerts) and `v2x_veremi_risk_ranked_alerts.csv` (184 V2X alerts).
- `vehicle_threat_graph.json`: Heterogeneous connected vehicle threat graph (28 nodes, 46 edges).
- `cti_technique_mapping.json` / `cti_technique_mapping.csv`: Formal mappings from 10 CAN/V2X attack types to MITRE ATT&CK STIX IDs, tactics, CWEs, affected assets, and author-assigned heuristic weights.

## 2. Empirical Benchmark Results (Zero Synthetic Numbers)
- `multi_seed_summary_20seeds.csv` / `multi_seed_all_runs_20seeds.csv` / `paired_statistical_tests_20seeds.json`: 20-seed multi-run statistics and paired significance tests across KNN, Random Forest, SVM, XGBoost, LSTM, Proposed CTI-GNN, and Ablation.
- `multi_seed_summary_5seeds.csv` / `multi_seed_all_runs_5seeds.csv` / `paired_statistical_tests_5seeds.json`: 5-seed benchmark evaluation (seeds 42, 101, 202, 303, 404).
- `ciciov_full_metrics.csv`: Benchmark on 100,000 train / 30,000 test frames from all 1,408,219 `CICIoV2024` CAN frames.
- `veremi_full_metrics.csv`: Benchmark on 50,000 train / 20,000 test samples across all 225 `VeReMi` simulation archives.
- `full_model_comparison.csv` & `per_class_metrics_seed42.csv`: Single-seed per-model and per-class breakdown.
- `ciciov_loao_zero_day_results.csv`: Leave-One-Attack-Out CAN zero-day detection evaluation.
- `can_rigorous_leakage_and_order_checks.csv` & `ciciov_leakage_results.csv`: Overlap removal, order-independent ablation, and contiguous time-block evaluation.
- `weight_sensitivity_analysis.csv`: 1,000-trial Monte Carlo risk ranking stability under +/-5% to +/-30% weight jitter.
- `figures/*.png`: 19 empirical benchmark plots, confusion matrices, and graph visualizations.

