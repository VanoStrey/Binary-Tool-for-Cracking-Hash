package coreChunk;

import java.math.BigInteger;

public class ChunkValueEncoding {
    private final String rangeChars;
    private final int base;

    public ChunkValueEncoding(String rangeChars) {
        this.rangeChars = rangeChars;
        this.base = rangeChars.length();
    }

    public String convertToBaseString(BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0)
            throw new IllegalArgumentException("Число должно быть положительным!");

        StringBuilder result = new StringBuilder();
        BigInteger current = value;
        BigInteger baseBI = BigInteger.valueOf(base);

        while (current.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divMod = current.divideAndRemainder(baseBI);
            int index = divMod[1].intValue(); // остаток
            result.insert(0, rangeChars.charAt(index));
            current = divMod[0]; // частное
        }

        return result.length() > 0 ? result.toString() : String.valueOf(rangeChars.charAt(0));
    }
}
