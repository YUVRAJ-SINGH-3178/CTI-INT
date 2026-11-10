package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.TelemetryRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Direct Java processor for full external research datasets:
 * 1) CICIoV2024: All 6 decimal_*.csv CAN bus captures (1,408,219 frames)
 * 2) VeReMi: VeReMi/VeReMi/index.md (225 simulation runs) and streaming .tgz BSM archives
 */
public class FullDatasetScanner {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class CanStreamSummary {
        public final String fileName;
        public final long frameCount;
        public final Set<Integer> uniqueCanIds;
        public final String attackClass;

        public CanStreamSummary(String fileName, long frameCount, Set<Integer> uniqueCanIds, String attackClass) {
            this.fileName = fileName;
            this.frameCount = frameCount;
            this.uniqueCanIds = uniqueCanIds;
            this.attackClass = attackClass;
        }
    }

    public static class VeremiRunEntry {
        public final int repetition;
        public final int density;
        public final int attackerType;
        public final double attackerDensity;
        public final String archiveName;
        public final String split;
        public final File archiveFile;

        public VeremiRunEntry(int repetition, int density, int attackerType, double attackerDensity,
                              String archiveName, String split, File archiveFile) {
            this.repetition = repetition;
            this.density = density;
            this.attackerType = attackerType;
            this.attackerDensity = attackerDensity;
            this.archiveName = archiveName;
            this.split = split;
            this.archiveFile = archiveFile;
        }
    }

    public static class VeremiArchiveStats {
        public final String archiveName;
        public final int groundTruthEntries;
        public final int bsmMessages;
        public final Map<Integer, Integer> attackerTypeCounts;
        public final List<TelemetryRecord> sampleRecords;

        public VeremiArchiveStats(String archiveName, int groundTruthEntries, int bsmMessages,
                                  Map<Integer, Integer> attackerTypeCounts, List<TelemetryRecord> sampleRecords) {
            this.archiveName = archiveName;
            this.groundTruthEntries = groundTruthEntries;
            this.bsmMessages = bsmMessages;
            this.attackerTypeCounts = attackerTypeCounts;
            this.sampleRecords = sampleRecords;
        }
    }

    public static File resolveDir(String folderName) {
        File f = new File("../" + folderName);
        if (f.exists() && f.isDirectory()) return f;
        f = new File(folderName);
        if (f.exists() && f.isDirectory()) return f;
        return null;
    }

    /**
     * Streams all CICIoV2024/decimal_*.csv files completely to compute exact frame counts and CAN IDs.
     */
    public static List<CanStreamSummary> scanAllCiciov2024Files() throws IOException {
        File canDir = resolveDir("CICIoV2024");
        if (canDir == null) return Collections.emptyList();

        File[] csvs = canDir.listFiles((dir, name) -> name.startsWith("decimal_") && name.endsWith(".csv"));
        if (csvs == null) return Collections.emptyList();
        Arrays.sort(csvs, Comparator.comparing(File::getName));

        List<CanStreamSummary> summaries = new ArrayList<>();
        for (File csv : csvs) {
            long count = 0;
            Set<Integer> ids = new TreeSet<>();
            String cls = "UNKNOWN";

            try (BufferedReader br = new BufferedReader(new FileReader(csv, StandardCharsets.UTF_8), 65536)) {
                String line = br.readLine(); // skip header
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    int c1 = line.indexOf(',');
                    if (c1 > 0) {
                        try {
                            int id = Integer.parseInt(line.substring(0, c1).trim());
                            ids.add(id);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (count == 0) {
                        int lastComma = line.lastIndexOf(',');
                        if (lastComma > 0 && lastComma + 1 < line.length()) {
                            cls = line.substring(lastComma + 1).trim();
                        }
                    }
                    count++;
                }
            }
            summaries.add(new CanStreamSummary(csv.getName(), count, ids, cls));
        }
        return summaries;
    }

    /**
     * Parses VeReMi/VeReMi/index.md listing all 225 simulation runs and resolves local .tgz files.
     */
    public static List<VeremiRunEntry> parseVeremiIndex() throws IOException {
        File veremiRoot = resolveDir("VeReMi");
        if (veremiRoot == null) return Collections.emptyList();

        File indexFile = new File(veremiRoot, "VeReMi/index.md");
        if (!indexFile.exists()) indexFile = new File(veremiRoot, "index.md");
        if (!indexFile.exists()) return Collections.emptyList();

        File archiveDir = new File(indexFile.getParentFile(), "securecomm2018");
        Pattern p = Pattern.compile("\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*([\\d.]+)\\s*\\|\\s*\\[(.*?)\\]");

        List<VeremiRunEntry> entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(indexFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    int rep = Integer.parseInt(m.group(1));
                    int dens = Integer.parseInt(m.group(2));
                    int atk = Integer.parseInt(m.group(3));
                    double atkDens = Double.parseDouble(m.group(4));
                    String archiveName = m.group(5).trim();
                    String split = (rep <= 2) ? "TRAIN" : (rep == 3 ? "VAL" : "TEST");
                    File archiveFile = new File(archiveDir, archiveName);
                    entries.add(new VeremiRunEntry(rep, dens, atk, atkDens, archiveName, split, archiveFile));
                }
            }
        }
        return entries;
    }

    /**
     * Streams a single VeReMi .tgz simulation archive in memory without extracting to disk,
     * reading GroundTruthJSONlog.json + received BSM type-3 packets.
     */
    public static VeremiArchiveStats inspectVeremiArchive(File tgzFile, int maxSampleRecords) throws IOException {
        if (tgzFile == null || !tgzFile.exists()) {
            throw new FileNotFoundException("VeReMi archive not found: " + tgzFile);
        }

        Map<String, Integer> groundTruth = new HashMap<>();
        AtomicInteger gtCount = new AtomicInteger(0);
        AtomicInteger bsmCount = new AtomicInteger(0);
        Map<Integer, Integer> atkCounts = new TreeMap<>();
        List<TelemetryRecord> samples = new ArrayList<>();
        String now = LocalDateTime.now().format(ISO_FMT);

        // Pass 1: Read GroundTruthJSONlog.json first so received BSM packets join accurately
        TarGzHelper.readTarGz(tgzFile, (entryName, size, entryStream) -> {
            if (entryName.contains("GroundTruthJSONlog.json")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(entryStream, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                        int sender = obj.get("sender").getAsInt();
                        int msgId = obj.get("messageID").getAsInt();
                        int atkType = obj.has("attackerType") ? obj.get("attackerType").getAsInt() : 0;
                        groundTruth.put(sender + ":" + msgId, atkType);
                        gtCount.incrementAndGet();
                    } catch (Exception ignored) {}
                }
                return false; // found ground truth
            }
            return true;
        });

        // Pass 2: Stream received JSONlog-*.json files and count BSM type=3 records
        TarGzHelper.readTarGz(tgzFile, (entryName, size, entryStream) -> {
            if (entryName.contains("JSONlog-") && size > 2000 && entryName.endsWith(".json")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(entryStream, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                        if (obj.has("type") && obj.get("type").getAsInt() == 3) {
                            int sender = obj.get("sender").getAsInt();
                            int msgId = obj.get("messageID").getAsInt();
                            int actualAtk = groundTruth.getOrDefault(sender + ":" + msgId, 0);
                            atkCounts.merge(actualAtk, 1, Integer::sum);
                            bsmCount.incrementAndGet();

                            if (samples.size() < maxSampleRecords) {
                                double rcvTime = obj.get("rcvTime").getAsDouble();
                                JsonArray pos = obj.getAsJsonArray("pos");
                                JsonArray spd = obj.getAsJsonArray("spd");
                                String payload = String.format(Locale.US,
                                        "Pos:(%.1f,%.1f) Spd:(%.2f,%.2f) RSSI:%.2e",
                                        pos.get(0).getAsDouble(), pos.get(1).getAsDouble(),
                                        spd.get(0).getAsDouble(), spd.get(1).getAsDouble(),
                                        obj.has("RSSI") ? obj.get("RSSI").getAsDouble() : 0.0);
                                String statusFlag = (actualAtk == 0) ? "BENIGN" : ("ANOMALOUS_T" + actualAtk);
                                samples.add(new TelemetryRecord(0, "V2X", rcvTime, "VEH_" + sender, payload, statusFlag, now));
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            return true;
        });

        return new VeremiArchiveStats(tgzFile.getName(), gtCount.get(), bsmCount.get(), atkCounts, samples);
    }

    public static String veremiAttackName(int attackerType) {
        switch (attackerType) {
            case 0: return "BENIGN";
            case 1: return "V2X_CONST_POS";
            case 2: return "V2X_CONST_OFFSET";
            case 4: return "V2X_RANDOM_POS";
            case 8: return "V2X_RANDOM_OFFSET";
            case 16: return "V2X_EVENTUAL_STOP";
            default: return "UNKNOWN_T" + attackerType;
        }
    }
}

