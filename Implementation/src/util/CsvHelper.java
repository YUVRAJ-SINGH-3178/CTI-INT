package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for parsing and escaping CSV lines according to RFC 4180 standards.
 */
public class CsvHelper {

    /**
     * Splits a CSV line into tokens, respecting double-quoted fields.
     */
    public static List<String> parseLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Escapes a string for safe CSV output.
     */
    public static String escapeField(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}

