# Database Seeding & Provenance Notes

**Database File**: `Implementation/database/vehicle_cti.db`  
**Schema DDL**: `Implementation/database/schema.sql`  
**Initializer Class**: `Implementation/src/util/DatabaseInitializer.java`

---

## 1. Seeding Pipeline

When `DatabaseInitializer.initializeDatabase(...)` runs on first startup (or if `vehicle_cti.db` does not exist):
1. Executes `database/schema.sql` with `PRAGMA foreign_keys = ON;`.
2. Seeds default operator accounts in `users` (`admin`, `analyst`, `operator`) using 16-byte random salts (`SecureRandom`) and SHA-256 hashes.
3. Seeds 6 default ECU risk profiles in `risk_configurations`.
4. Parses `Dataset/vehicle_threat_graph.json` using Google Gson to populate `threat_nodes` (28 nodes) and `threat_edges` (46 edges) in a single atomic transaction.
5. Parses `Dataset/full_alerts_dataset.csv` using header-mapped CSV tokenizing inside SQLite batch transactions (1,000 rows per batch) to seed all 7,683 genuine alerts (7,499 CAN + 184 V2X) with exact evaluated `confidence`, `risk_score`, `mitre_technique`, `cwe_id`, and `target_assets`.

---

## 2. Live Dataset Ingestion

Raw telemetry rows in `telemetry_records` can be streamed asynchronously on background threads (`Team6_TelemetryIngest`) directly from:
- `CICIoV2024/decimal_*.csv` (all 6 CAN bus CSV streams totaling 1,408,219 frames)
- `VeReMi/VeReMi/securecomm2018/*.tgz` (225 simulation runs parsed via `TarGzHelper` + `FullDatasetScanner`)
- `Dataset/telemetry_sample_can.csv` and `Dataset/telemetry_sample_v2x.csv`

