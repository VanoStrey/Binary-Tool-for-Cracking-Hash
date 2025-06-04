package Bytes;

public class BinBaseConverter {
    private final String rangeChars;
    private final int base;

    public BinBaseConverter(String rangeChars) {
        this.rangeChars = rangeChars;
        this.base = rangeChars.length();
    }


    public String convertToBaseString(byte[] bytes){

        long decimalValue = 0;
        for (int i = 0; i < 5; i++) {
            decimalValue = (decimalValue << 8) | (bytes[i] & 0xFF); // Добавляем байт в число
        }

        if (decimalValue < 0) throw new IllegalArgumentException("Число должно быть положительным!");

        StringBuilder result = new StringBuilder();
        while (decimalValue > 0) {
            int index = (int) (decimalValue % base); // Получаем индекс символа
            result.insert(0, rangeChars.charAt(index)); // Добавляем символ
            decimalValue /= base; // Делим на основание
        }

        return result.length() > 0 ? result.toString() : String.valueOf(rangeChars.charAt(0)); // Возвращаем "0", если число было 0
    }

    // 🔹 Преобразует `long` в строку с основанием `base`
    public String convertToBaseString(long decimalValue) {
        if (decimalValue < 0) throw new IllegalArgumentException("Число должно быть положительным!");

        StringBuilder result = new StringBuilder();
        while (decimalValue > 0) {
            int index = (int) (decimalValue % base); // Получаем индекс символа
            result.insert(0, rangeChars.charAt(index)); // Добавляем символ
            decimalValue /= base; // Делим на основание
        }

        return result.length() > 0 ? result.toString() : String.valueOf(rangeChars.charAt(0)); // Возвращаем "0", если число было 0
    }
}
