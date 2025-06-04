package Bytes;

public class BinBinaryHashSearcher {
    private final Hasher hasher;
    private final BinFileAccessor accessor;
    private final BinBaseConverter converter;

    public BinBinaryHashSearcher(BinFileAccessor accessor, BinBaseConverter converter, Hasher hasher) {
        this.hasher = hasher;
        this.converter = converter;
        this.accessor = accessor;
    }

    public String search(String targetHash) {
        long totalElements = accessor.getTotalElements();
        long low = 0;
        long high = totalElements - 1;

        while (low <= high) {
            long mid = low + ((high - low) / 2);
            String element = converter.convertToBaseString(accessor.getElement(mid));
            String computedHash = hasher.getHash(element);

            int cmp = computedHash.compareTo(targetHash);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return element;
            }
        }
        return "";
    }
}
