package coreChunk;

import hashFunc.Hasher;

import java.io.IOException;

public class HashBinarySearch {
    private final ChunkBinaryFileAccessor accessor;
    private final ChunkValueEncoding converter;
    private final Hasher hasher;

    public HashBinarySearch(ChunkBinaryFileAccessor accessor,
                            ChunkValueEncoding converter,
                            Hasher hasher) {
        this.accessor = accessor;
        this.converter = converter;
        this.hasher = hasher;
    }

    public String search(String targetHashHEX) throws IOException {
        long total = accessor.getTotalElements();
        if (total == 0) return "";

        byte[] target = hasher.hexToBytes(targetHashHEX);
        // один буфер на весь метод
        char[] buf = new char[32];

        // границы
        if (compareBoundary(0, buf, target) > 0)      return "";
        if (compareBoundary(total-1, buf, target) < 0) return "";

        long low = 0, high = total - 1;
        while (low <= high) {
            long mid = (low + high) >>> 1;
            byte[] elem = accessor.getElement(mid);

            int len = converter.encodeToChars(elem, buf);
            byte[] hash = hasher.getBinHash(buf, buf.length - len, len);

            int cmp = compareHashes(hash, target);
            if (cmp < 0)      low  = mid + 1;
            else if (cmp > 0) high = mid - 1;
            else return new String(buf, buf.length - len, len);
        }
        return "";
    }

    private int compareBoundary(long idx, char[] buf, byte[] target) throws IOException {
        byte[] elem = accessor.getElement(idx);
        int len = converter.encodeToChars(elem, buf);
        byte[] hash = hasher.getBinHash(buf, buf.length - len, len);
        return compareHashes(hash, target);
    }

    private int compareHashes(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int ai = a[i] & 0xFF, bi = b[i] & 0xFF;
            if (ai != bi) return ai - bi;
        }
        return a.length - b.length;
    }
}
