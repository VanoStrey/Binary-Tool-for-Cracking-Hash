package coreChunk;

import hashFunc.Hasher;

import java.math.BigInteger;

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
        long low = 0;
        long high = total - 1;
        byte[] targetHash = hasher.hexToBytes(targetHashHEX);

        while (low <= high) {
            long mid = low + ((high - low) >>> 1);
            BigInteger midValue = accessor.getElement(mid);
            String midString = converter.convertToBaseString(midValue);
            byte[] hash = hasher.getBinHash(midString);

            int cmp = new BigInteger(1, hash).compareTo(new BigInteger(1, targetHash));

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
}
