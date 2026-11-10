package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Streaming STIX 2.1 MITRE ATT&CK Knowledge Base Loader.
 * Streams attack-stix-data/ics-attack/ics-attack.json and
 * attack-stix-data/enterprise-attack/enterprise-attack.json via Gson JsonReader.
 */
public class StixAttackLoader {

    public static class StixTechnique {
        public final String mitreId;
        public final String name;
        public final String domain;
        public final List<String> tactics;
        public final String description;
        public final String url;
        public final boolean revoked;

        public StixTechnique(String mitreId, String name, String domain, List<String> tactics,
                             String description, String url, boolean revoked) {
            this.mitreId = mitreId;
            this.name = name;
            this.domain = domain;
            this.tactics = tactics;
            this.description = description;
            this.url = url;
            this.revoked = revoked;
        }
    }

    public static class CtiAttackMapping {
        public String attackType;
        public String networkDomain;
        public String mitreTechniqueId;
        public String techniqueName;
        public String cweId;
        public String cweName;
        public String severity;
        public double impactScore;
        public String justification;
        public List<String> affectedAssets;
    }

    private static final Map<String, StixTechnique> cachedCatalog = new LinkedHashMap<>();
    private static int icsCount = 0;
    private static int enterpriseCount = 0;
    private static boolean loaded = false;

    public static File resolveStixDir() {
        File dir = new File("../attack-stix-data");
        if (dir.exists() && dir.isDirectory()) return dir;
        dir = new File("attack-stix-data");
        if (dir.exists() && dir.isDirectory()) return dir;
        return null;
    }

    /**
     * Streams STIX 2.1 bundles once and caches MITRE ATT&CK attack-patterns.
     */
    public static synchronized Map<String, StixTechnique> getCatalog() throws IOException {
        if (loaded) {
            return Collections.unmodifiableMap(cachedCatalog);
        }

        File stixDir = resolveStixDir();
        if (stixDir == null) {
            loaded = true;
            return Collections.emptyMap();
        }

        File icsFile = new File(stixDir, "ics-attack/ics-attack.json");
        File entFile = new File(stixDir, "enterprise-attack/enterprise-attack.json");

        if (icsFile.exists()) {
            icsCount = parseStixBundle(icsFile, "ICS", cachedCatalog);
        }
        if (entFile.exists()) {
            enterpriseCount = parseStixBundle(entFile, "Enterprise", cachedCatalog);
        }
        loaded = true;
        return Collections.unmodifiableMap(cachedCatalog);
    }

    public static int getIcsTechniqueCount() {
        return icsCount;
    }

    public static int getEnterpriseTechniqueCount() {
        return enterpriseCount;
    }

    public static Optional<StixTechnique> lookupTechnique(String mitreId) {
        if (mitreId == null || mitreId.isBlank()) return Optional.empty();
        try {
            Map<String, StixTechnique> map = getCatalog();
            return Optional.ofNullable(map.get(mitreId.trim().toUpperCase()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static int parseStixBundle(File stixFile, String domainName, Map<String, StixTechnique> targetMap) throws IOException {
        int added = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stixFile), StandardCharsets.UTF_8), 131072);
             JsonReader reader = new JsonReader(br)) {

            if (reader.peek() != JsonToken.BEGIN_OBJECT) return 0;
            reader.beginObject();
            while (reader.hasNext()) {
                String topName = reader.nextName();
                if ("objects".equals(topName) && reader.peek() == JsonToken.BEGIN_ARRAY) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        StixTechnique st = readSingleObject(reader, domainName);
                        if (st != null) {
                            // Keep active entry prioritized over deprecated if duplicate ID occurs
                            String key = st.mitreId.toUpperCase();
                            StixTechnique existing = targetMap.get(key);
                            if (existing == null || (existing.revoked && !st.revoked)) {
                                targetMap.put(key, st);
                            }
                            added++;
                        }
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        return added;
    }

    /**
     * Memory-lean field-selective streaming parser for a STIX bundle object.
     * Skips non-attack-pattern metadata fields without allocating DOM objects.
     */
    private static StixTechnique readSingleObject(JsonReader reader, String domainName) throws IOException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }

        String type = "";
        String name = "";
        String desc = "";
        boolean revoked = false;
        boolean deprecated = false;
        String mitreId = null;
        String url = null;
        List<String> tactics = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if ("type".equals(key)) {
                type = nextStringSafe(reader);
                // Fast check if type appears early and is not attack-pattern? Keep reading remaining keys quickly via skipValue.
            } else if ("name".equals(key)) {
                name = nextStringSafe(reader);
            } else if ("description".equals(key)) {
                desc = nextStringSafe(reader);
            } else if ("revoked".equals(key)) {
                revoked = nextBoolSafe(reader);
            } else if ("x_mitre_deprecated".equals(key)) {
                deprecated = nextBoolSafe(reader);
            } else if ("external_references".equals(key) && reader.peek() == JsonToken.BEGIN_ARRAY) {
                reader.beginArray();
                while (reader.hasNext()) {
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        String refSrc = "";
                        String extId = null;
                        String extUrl = null;
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String rk = reader.nextName();
                            if ("source_name".equals(rk)) refSrc = nextStringSafe(reader);
                            else if ("external_id".equals(rk)) extId = nextStringSafe(reader);
                            else if ("url".equals(rk)) extUrl = nextStringSafe(reader);
                            else reader.skipValue();
                        }
                        reader.endObject();
                        if (("mitre-attack".equals(refSrc) || "mitre-ics-attack".equals(refSrc)) && extId != null && !extId.isEmpty()) {
                            mitreId = extId;
                            url = extUrl;
                        }
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endArray();
            } else if ("kill_chain_phases".equals(key) && reader.peek() == JsonToken.BEGIN_ARRAY) {
                reader.beginArray();
                while (reader.hasNext()) {
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String kk = reader.nextName();
                            if ("phase_name".equals(kk)) {
                                String ph = nextStringSafe(reader);
                                if (!ph.isEmpty()) tactics.add(ph);
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        if (!"attack-pattern".equals(type) || mitreId == null || mitreId.isEmpty()) {
            return null;
        }
        String cleanDesc = desc.replaceAll("\\s+", " ").trim();
        if (url == null || url.isEmpty()) {
            url = "https://attack.mitre.org/techniques/" + mitreId.replace(".", "/");
        }
        return new StixTechnique(mitreId, name, domainName, tactics, cleanDesc, url, revoked || deprecated);
    }

    private static String nextStringSafe(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.STRING) return reader.nextString();
        reader.skipValue();
        return "";
    }

    private static boolean nextBoolSafe(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.BOOLEAN) return reader.nextBoolean();
        reader.skipValue();
        return false;
    }

    /**
     * Loads structured CTI mappings from Dataset/cti_technique_mapping.json.
     */
    public static List<CtiAttackMapping> loadCtiMappings() {
        File f = new File(ResearchResultsLoader.resolveDatasetDir(), "cti_technique_mapping.json");
        if (!f.exists()) return Collections.emptyList();

        List<CtiAttackMapping> list = new ArrayList<>();
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            JsonArray arr = new Gson().fromJson(r, JsonArray.class);
            if (arr != null) {
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    CtiAttackMapping m = new CtiAttackMapping();
                    m.attackType = getString(o, "attack_type");
                    m.networkDomain = getString(o, "network_domain");
                    m.mitreTechniqueId = getString(o, "mitre_technique_id");
                    m.techniqueName = getString(o, "technique_name");
                    m.cweId = getString(o, "cwe_id");
                    m.cweName = getString(o, "cwe_name");
                    m.severity = getString(o, "severity");
                    m.impactScore = o.has("impact_score") ? o.get("impact_score").getAsDouble() : 0.0;
                    m.justification = getString(o, "justification");
                    m.affectedAssets = new ArrayList<>();
                    if (o.has("affected_assets") && o.get("affected_assets").isJsonArray()) {
                        for (JsonElement a : o.getAsJsonArray("affected_assets")) {
                            m.affectedAssets.add(a.getAsString());
                        }
                    }
                    list.add(m);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    private static String getString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }
}
