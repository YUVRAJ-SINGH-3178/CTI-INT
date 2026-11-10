package service;

import dao.TelemetryDao;
import model.TelemetryRecord;
import util.CsvHelper;
import util.DataAccessException;
import util.FullDatasetScanner;
import util.ValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * ==============================================================================
 * CS213 Advanced JAVA Programming - Module 3
 * Member: Charan Krishna Y | Registration No: AP25110090041
 * Module Name: Telemetry Ingest (CAN and V2X CSV / VeReMi .tgz Loading, Background Threads)
 * Class: Team6_TelemetryIngest
 * ==============================================================================
 */
public class Team6_TelemetryIngest {

    private final TelemetryDao telemetryDao;
    private final ExecutorService executorService;
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Team6_TelemetryIngest() {
        this.telemetryDao = new TelemetryDao();
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public Team6_TelemetryIngest(TelemetryDao telemetryDao, ExecutorService executorService) {
        this.telemetryDao = telemetryDao;
        this.executorService = executorService;
    }

    // --- Mandatory Course Rubric CRUD Operations ---

    public TelemetryRecord ap25110090041_TelemetryIngest_create(TelemetryRecord record) throws ValidationException, DataAccessException {
        validateRecord(record);
        return telemetryDao.save(record);
    }

    public TelemetryRecord AP25110090041_TelemetryIngest_create(TelemetryRecord record) throws ValidationException, DataAccessException {
        return ap25110090041_TelemetryIngest_create(record);
    }

    public boolean ap25110090041_TelemetryIngest_update(TelemetryRecord record) throws ValidationException, DataAccessException {
        validateRecord(record);
        return telemetryDao.update(record);
    }

    public boolean AP25110090041_TelemetryIngest_update(TelemetryRecord record) throws ValidationException, DataAccessException {
        return ap25110090041_TelemetryIngest_update(record);
    }

    public Optional<TelemetryRecord> ap25110090041_TelemetryIngest_retrive(int recordId) throws DataAccessException {
        return telemetryDao.findById(recordId);
    }

    public Optional<TelemetryRecord> AP25110090041_TelemetryIngest_retrive(int recordId) throws DataAccessException {
        return ap25110090041_TelemetryIngest_retrive(recordId);
    }

    public boolean ap25110090041_TelemetryIngest_delete(int recordId) throws DataAccessException {
        return telemetryDao.deleteById(recordId);
    }

    public boolean AP25110090041_TelemetryIngest_delete(int recordId) throws DataAccessException {
        return ap25110090041_TelemetryIngest_delete(recordId);
    }

    // --- Advanced Java Multithreading & Full-Dataset Ingestion ---

    public CompletableFuture<Integer> ingestCsvAsync(File csvFile, String domain, Consumer<Integer> progressConsumer) {
        return ingestCsvAsync(csvFile, domain, 0, progressConsumer);
    }

    /**
     * Ingests real CAN (CICIoV2024 12-col or sample) or V2X CSV streams asynchronously
     * using ExecutorService without blocking the AWT Event Dispatch Thread.
     *
     * @param maxRows maximum rows to insert into SQLite (0 = entire file)
     */
    public CompletableFuture<Integer> ingestCsvAsync(File csvFile, String domain, int maxRows,
                                                     Consumer<Integer> progressConsumer) {
        return CompletableFuture.supplyAsync(() -> {
            List<TelemetryRecord> batch = new ArrayList<>();
            int totalInserted = 0;
            String now = LocalDateTime.now().format(ISO_FMT);

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8), 65536)) {
                String headerLine = br.readLine();
                if (headerLine == null) return 0;

                String upperHeader = headerLine.toUpperCase();
                boolean isCiciov12Col = upperHeader.startsWith("ID,DATA_0");
                boolean isV2xBsm9Col = upperHeader.startsWith("TIMESTAMP,SENDER_ID,POS_X");

                String line;
                int rowNum = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    if (maxRows > 0 && rowNum >= maxRows) break;

                    List<String> tokens = CsvHelper.parseLine(line);
                    if (tokens.isEmpty()) continue;

                    try {
                        TelemetryRecord rec = parseTelemetryRow(tokens, domain, rowNum, isCiciov12Col, isV2xBsm9Col, now);
                        batch.add(rec);
                        rowNum++;

                        if (batch.size() >= 500) {
                            int saved = telemetryDao.saveBatch(batch);
                            totalInserted += saved;
                            batch.clear();
                            if (progressConsumer != null) {
                                progressConsumer.accept(totalInserted);
                            }
                        }
                    } catch (ValidationException ignored) {}
                }

                if (!batch.isEmpty()) {
                    int saved = telemetryDao.saveBatch(batch);
                    totalInserted += saved;
                    if (progressConsumer != null) {
                        progressConsumer.accept(totalInserted);
                    }
                }
            } catch (IOException | DataAccessException e) {
                throw new CompletionException("Asynchronous CSV ingest failed: " + e.getMessage(), e);
            }

            return totalInserted;
        }, executorService);
    }

    /**
     * Streams a real VeReMi .tgz simulation archive asynchronously on ExecutorService,
     * joining GroundTruthJSONlog.json with received BSM type-3 messages and persisting into SQLite.
     */
    public CompletableFuture<FullDatasetScanner.VeremiArchiveStats> ingestVeremiArchiveAsync(
            File tgzFile, int maxRecordsToPersist, Consumer<Integer> progressConsumer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FullDatasetScanner.VeremiArchiveStats stats = FullDatasetScanner.inspectVeremiArchive(tgzFile, maxRecordsToPersist);
                if (!stats.sampleRecords.isEmpty()) {
                    int saved = telemetryDao.saveBatch(stats.sampleRecords);
                    if (progressConsumer != null) {
                        progressConsumer.accept(saved);
                    }
                }
                return stats;
            } catch (IOException | DataAccessException e) {
                throw new CompletionException("VeReMi archive ingest failed: " + e.getMessage(), e);
            }
        }, executorService);
    }

    private TelemetryRecord parseTelemetryRow(List<String> tokens, String domain, int rowNum,
                                            boolean isCiciov12Col, boolean isV2xBsm9Col, String now) throws ValidationException {
        if ("CAN".equalsIgnoreCase(domain)) {
            if (isCiciov12Col && tokens.size() >= 10) {
                // Real CICIoV2024 format: ID, DATA_0..DATA_7, label, category, specific_class
                int decId = (int) parseDoubleSafe(tokens.get(0));
                String canHexId = String.format("0x%03X", decId);
                StringBuilder hexBytes = new StringBuilder();
                for (int i = 1; i <= 8 && i < tokens.size(); i++) {
                    int byteVal = (int) parseDoubleSafe(tokens.get(i));
                    if (i > 1) hexBytes.append(" ");
                    hexBytes.append(String.format("%02X", byteVal & 0xFF));
                }
                String label = tokens.size() > 9 ? tokens.get(9).trim().toUpperCase() : "BENIGN";
                String cls = tokens.size() > 11 ? tokens.get(11).trim().toUpperCase() : label;
                String statusFlag = "BENIGN".equals(label) ? "BENIGN" : ("ANOMALOUS_" + cls);
                return new TelemetryRecord(0, "CAN", (double) rowNum, canHexId, hexBytes.toString(), statusFlag, now);
            } else {
                double ts = tokens.size() > 0 ? parseDoubleSafe(tokens.get(0)) : rowNum;
                String canId = tokens.size() > 1 ? tokens.get(1) : "0x000";
                String payload = tokens.size() > 3 ? tokens.get(3) : "";
                return new TelemetryRecord(0, "CAN", ts, canId, payload, "INGESTED", now);
            }
        } else {
            if (isV2xBsm9Col && tokens.size() >= 9) {
                // VeReMi sample format: timestamp, sender_id, pos_x, pos_y, pos_z, speed_x, speed_y, heading, attack_type
                double ts = parseDoubleSafe(tokens.get(0));
                String sender = "VEH_" + tokens.get(1);
                String payload = String.format("Pos:(%s,%s) Spd:(%s,%s)", tokens.get(2), tokens.get(3), tokens.get(5), tokens.get(6));
                String atk = tokens.get(8).trim().toUpperCase();
                String statusFlag = "BENIGN".equals(atk) ? "BENIGN" : ("ANOMALOUS_" + atk);
                return new TelemetryRecord(0, "V2X", ts, sender, payload, statusFlag, now);
            } else {
                double ts = tokens.size() > 0 ? parseDoubleSafe(tokens.get(0)) : rowNum;
                String sender = tokens.size() > 1 ? tokens.get(1) : "VEH_1";
                String payload = "Pos:(" + (tokens.size() > 2 ? tokens.get(2) : "0") + "," +
                                 (tokens.size() > 3 ? tokens.get(3) : "0") + ")";
                return new TelemetryRecord(0, "V2X", ts, sender, payload, "INGESTED", now);
            }
        }
    }

    private double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<TelemetryRecord> getRecentTelemetry(int count) throws DataAccessException {
        return telemetryDao.findRecent(count);
    }

    private void validateRecord(TelemetryRecord record) throws ValidationException {
        if (record == null) {
            throw new ValidationException("record", "Telemetry record cannot be null");
        }
        if (record.getSourceId() == null || record.getSourceId().isBlank()) {
            throw new ValidationException("sourceId", "Source identifier is required");
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
