package coreChunk;

import hashFunc.Hasher;

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

    public String search(String targetHashHEX) {
        long total = accessor.getTotalElements();
        if (total == 0) return "";

        byte[] targetHash = hasher.hexToBytes(targetHashHEX);

        // Нижняя граница
        if (isTargetBeforeFirst(targetHash)) return "";

        // Верхняя граница
        if (isTargetAfterLast(targetHash, total)) return "";

        long low = 0;
        long high = total - 1;

        while (low <= high) {
            long mid = low + ((high - low) >>> 1);
            String midString = converter.convertToBaseString(accessor.getElement(mid));
            byte[] hash = hasher.getBinHash(midString);

            int cmp = compareHashes(hash, targetHash);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return midString;
            }
        }
        return "";
    }

    private boolean isTargetBeforeFirst(byte[] targetHash) {
        String first = converter.convertToBaseString(accessor.getElement(0));
        byte[] firstHash = hasher.getBinHash(first);
        return compareHashes(targetHash, firstHash) < 0;
    }

    private boolean isTargetAfterLast(byte[] targetHash, long total) {
        String last = converter.convertToBaseString(accessor.getElement(total - 1));
        byte[] lastHash = hasher.getBinHash(last);
        return compareHashes(targetHash, lastHash) > 0;
    }

    private int compareHashes(byte[] a, byte[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int ai = a[i] & 0xFF;
            int bi = b[i] & 0xFF;
            if (ai != bi) return Integer.compare(ai, bi);
        }
        return Integer.compare(a.length, b.length);
    }
}
