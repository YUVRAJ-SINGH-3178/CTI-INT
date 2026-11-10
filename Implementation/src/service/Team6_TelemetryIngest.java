package service;

import dao.TelemetryDao;
import model.TelemetryRecord;
import util.CsvHelper;
import util.DataAccessException;
import util.ValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
 * Module Name: Telemetry Ingest (CAN and V2X CSV Loading, Background Threads)
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

    /**
     * Create Operation: Inserts a single telemetry record.
     */
    public TelemetryRecord ap25110090041_TelemetryIngest_create(TelemetryRecord record) throws ValidationException, DataAccessException {
        validateRecord(record);
        return telemetryDao.save(record);
    }

    public TelemetryRecord AP25110090041_TelemetryIngest_create(TelemetryRecord record) throws ValidationException, DataAccessException {
        return ap25110090041_TelemetryIngest_create(record);
    }

    /**
     * Update Operation: Updates an existing telemetry record.
     */
    public boolean ap25110090041_TelemetryIngest_update(TelemetryRecord record) throws ValidationException, DataAccessException {
        validateRecord(record);
        return telemetryDao.update(record);
    }

    public boolean AP25110090041_TelemetryIngest_update(TelemetryRecord record) throws ValidationException, DataAccessException {
        return ap25110090041_TelemetryIngest_update(record);
    }

    /**
     * Retrieve Operation: Fetches a telemetry record by its ID.
     */
    public Optional<TelemetryRecord> ap25110090041_TelemetryIngest_retrive(int recordId) throws DataAccessException {
        return telemetryDao.findById(recordId);
    }

    public Optional<TelemetryRecord> AP25110090041_TelemetryIngest_retrive(int recordId) throws DataAccessException {
        return ap25110090041_TelemetryIngest_retrive(recordId);
    }

    /**
     * Delete Operation: Deletes a telemetry record by ID.
     */
    public boolean ap25110090041_TelemetryIngest_delete(int recordId) throws DataAccessException {
        return telemetryDao.deleteById(recordId);
    }

    public boolean AP25110090041_TelemetryIngest_delete(int recordId) throws DataAccessException {
        return ap25110090041_TelemetryIngest_delete(recordId);
    }

    // --- Advanced Java Multithreading & Asynchronous Ingestion ---

    public interface IngestProgressListener {
        void onProgress(int processed, int total, String statusMessage);
        void onComplete(int totalIngested);
        void onError(Exception ex);
    }

    public interface IngestErrorListener {
        void onError(Exception ex);
    }

    /**
     * Ingests a CAN or V2X telemetry CSV file asynchronously in a background thread
     * using ExecutorService, notifying caller via progress callbacks without freezing GUI.
     */
    public CompletableFuture<Integer> ingestCsvAsync(File csvFile, String domain,
                                                    Consumer<Integer> progressConsumer) {
        return CompletableFuture.supplyAsync(() -> {
            List<TelemetryRecord> batch = new ArrayList<>();
            int totalInserted = 0;
            String now = LocalDateTime.now().format(ISO_FMT);

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line = br.readLine(); // skip header
                int lineCount = 0;

                while ((line = br.readLine()) != null) {
                    List<String> tokens = CsvHelper.parseLine(line);
                    if (tokens.isEmpty()) continue;

                    try {
                        TelemetryRecord rec = parseTelemetryRow(tokens, domain, now);
                        batch.add(rec);
                        lineCount++;

                        if (batch.size() >= 50) {
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
                throw new CompletionException("Asynchronous ingest failed: " + e.getMessage(), e);
            }

            return totalInserted;
        }, executorService);
    }

    private TelemetryRecord parseTelemetryRow(List<String> tokens, String domain, String now) throws ValidationException {
        if ("CAN".equalsIgnoreCase(domain)) {
            // CAN columns: timestamp, id, dlc, payload...
            double ts = tokens.size() > 0 ? parseDoubleSafe(tokens.get(0)) : System.currentTimeMillis() / 1000.0;
            String canId = tokens.size() > 1 ? tokens.get(1) : "0x000";
            String payload = tokens.size() > 3 ? tokens.get(3) : "00 00";
            return new TelemetryRecord(0, "CAN", ts, canId, payload, "INGESTED", now);
        } else {
            // V2X columns: timestamp, sender_id, pos_x, pos_y...
            double ts = tokens.size() > 0 ? parseDoubleSafe(tokens.get(0)) : System.currentTimeMillis() / 1000.0;
            String sender = tokens.size() > 1 ? tokens.get(1) : "NODE_1";
            String payload = "Pos:(" + (tokens.size() > 2 ? tokens.get(2) : "0") + "," +
                             (tokens.size() > 3 ? tokens.get(3) : "0") + ")";
            return new TelemetryRecord(0, "V2X", ts, sender, payload, "INGESTED", now);
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
