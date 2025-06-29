package coreChunk;

public class ChunkValueEncoding {
    private final String rangeChars;
    private final int base;

    public ChunkValueEncoding(String rangeChars) {
        this.rangeChars = rangeChars;
        this.base = rangeChars.length();
    }

    public String convertToBaseString(byte[] value) {
        if (value == null || value.length == 0)
            throw new IllegalArgumentException("Недопустимое значение");

        // Копируем значение, чтобы не портить оригинал
        byte[] number = value.clone();
        StringBuilder result = new StringBuilder();

        while (!isZero(number)) {
            int rem = divide(number, base);
            result.insert(0, rangeChars.charAt(rem));
        }

        return result.length() > 0 ? result.toString() : String.valueOf(rangeChars.charAt(0));
    }

    private boolean isZero(byte[] bytes) {
        for (byte b : bytes) {
            if (b != 0) return false;
        }
        return true;
    }

    private int divide(byte[] number, int base) {
        int remainder = 0;
        for (int i = 0; i < number.length; i++) {
            int current = (remainder << 8) | (number[i] & 0xFF);
            number[i] = (byte) (current / base);
            remainder = current % base;
        }
        return remainder;
    }
}
