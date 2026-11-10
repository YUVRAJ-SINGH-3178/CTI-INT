package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Lightweight pure-Java streaming reader for .tar.gz (.tgz) archives (VeReMi simulation logs).
 * Uses built-in GZIPInputStream with zero third-party native dependencies.
 */
public class TarGzHelper {

    public interface TarEntryHandler {
        /**
         * Processes a regular file entry from the tar stream.
         * Return false to stop scanning remaining entries early.
         */
        boolean onEntry(String entryName, long size, InputStream entryStream) throws IOException;
    }

    /**
     * Streams regular file entries inside a .tgz archive to the provided handler.
     */
    public static void readTarGz(File tgzFile, TarEntryHandler handler) throws IOException {
        try (FileInputStream fis = new FileInputStream(tgzFile);
             BufferedInputStream bis = new BufferedInputStream(fis, 65536);
             GZIPInputStream gzis = new GZIPInputStream(bis, 65536)) {

            byte[] header = new byte[512];
            String pendingLongName = null;

            while (readFully(gzis, header) == 512) {
                if (isAllZero(header)) {
                    break;
                }

                String name = extractString(header, 0, 100);
                String prefix = extractString(header, 345, 155);
                if (!prefix.isEmpty()) {
                    name = prefix + "/" + name;
                }
                if (pendingLongName != null) {
                    name = pendingLongName;
                    pendingLongName = null;
                }

                long size = parseOctal(header, 124, 12);
                byte typeFlag = header[156];

                if (typeFlag == 'L') {
                    // GNU tar long filename metadata block
                    byte[] nameBytes = readExactBytes(gzis, (int) size);
                    pendingLongName = new String(nameBytes, StandardCharsets.UTF_8).replace("\0", "").trim();
                    skipPadding(gzis, size);
                    continue;
                }

                if (typeFlag == 'x' || typeFlag == 'g') {
                    // POSIX pax header block: skip payload + padding
                    skipFully(gzis, size);
                    skipPadding(gzis, size);
                    continue;
                }

                boolean isRegularFile = (typeFlag == '0' || typeFlag == 0);
                if (isRegularFile && size >= 0) {
                    BoundedInputStream entryStream = new BoundedInputStream(gzis, size);
                    boolean keepGoing = handler.onEntry(name, size, entryStream);
                    // Drain any unread bytes of this entry before advancing to next header
                    long remaining = entryStream.getRemaining();
                    if (remaining > 0) {
                        skipFully(gzis, remaining);
                    }
                    skipPadding(gzis, size);
                    if (!keepGoing) {
                        break;
                    }
                } else {
                    if (size > 0) {
                        skipFully(gzis, size);
                        skipPadding(gzis, size);
                    }
                }
            }
        }
    }

    private static void skipPadding(InputStream is, long size) throws IOException {
        long mod = size % 512;
        if (mod > 0) {
            skipFully(is, 512 - mod);
        }
    }

    private static int readFully(InputStream is, byte[] buf) throws IOException {
        int off = 0;
        while (off < buf.length) {
            int r = is.read(buf, off, buf.length - off);
            if (r < 0) break;
            off += r;
        }
        return off;
    }

    private static byte[] readExactBytes(InputStream is, int len) throws IOException {
        byte[] buf = new byte[Math.max(0, len)];
        int off = 0;
        while (off < len) {
            int r = is.read(buf, off, len - off);
            if (r < 0) break;
            off += r;
        }
        return buf;
    }

    private static void skipFully(InputStream is, long bytes) throws IOException {
        long remaining = bytes;
        byte[] discard = new byte[8192];
        while (remaining > 0) {
            int toRead = (int) Math.min(discard.length, remaining);
            int r = is.read(discard, 0, toRead);
            if (r < 0) break;
            remaining -= r;
        }
    }

    private static boolean isAllZero(byte[] buf) {
        for (byte b : buf) {
            if (b != 0) return false;
        }
        return true;
    }

    private static String extractString(byte[] buf, int offset, int length) {
        int end = offset;
        int limit = Math.min(buf.length, offset + length);
        while (end < limit && buf[end] != 0) {
            end++;
        }
        return new String(buf, offset, end - offset, StandardCharsets.UTF_8).trim();
    }

    private static long parseOctal(byte[] buf, int offset, int length) {
        String s = extractString(buf, offset, length);
        if (s.isEmpty()) return 0;
        try {
            return Long.parseLong(s, 8);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class BoundedInputStream extends InputStream {
        private final InputStream in;
        private long remaining;

        BoundedInputStream(InputStream in, long limit) {
            this.in = in;
            this.remaining = limit;
        }

        long getRemaining() {
            return remaining;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) return -1;
            int b = in.read();
            if (b >= 0) remaining--;
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) return -1;
            int toRead = (int) Math.min(len, remaining);
            int r = in.read(b, off, toRead);
            if (r > 0) remaining -= r;
            return r;
        }
    }
}

