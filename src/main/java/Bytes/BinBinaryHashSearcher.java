package Bytes;

import java.math.BigInteger;

public class BinBinaryHashSearcher {
    private final Hasher hasher;
    private final BinFileAccessor accessor;
    private final BinBaseConverter converter;

    public BinBinaryHashSearcher(BinFileAccessor accessor, BinBaseConverter converter, Hasher hasher) {
        this.hasher = hasher;
        this.converter = converter;
        this.accessor = accessor;
    }

    public String search(String targetHashHEX) {
        long totalElements = accessor.getTotalElements();
        long low = 0;
        long high = totalElements - 1;
        byte[] targetHash = hasher.hexToBytes(targetHashHEX);

        while (low <= high) {
            long mid = low + ((high - low) / 2);
            byte[] elementBytes = accessor.getElement(mid);
            String elementString = converter.convertToBaseString(elementBytes); // 🔥 Преобразование
            byte[] computedHash = hasher.getBinHash(elementString); // 🔥 Хеширование `String`

            int cmp = new BigInteger(1, computedHash).compareTo(new BigInteger(1, targetHash));
            if (cmp < 0) {
                low = mid + 1; // ✅ Исправлено
            } else if (cmp > 0) {
                high = mid - 1; // ✅ Исправлено
            } else {
                return elementString; // 🔥 Возвращаем найденный элемент
            }
        }
        return "";
    }
}
