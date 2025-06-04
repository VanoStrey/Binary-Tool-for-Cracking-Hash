package Bytes;

import java.math.BigInteger;
import java.util.Arrays;

public class BinCombinationGenerate {
    private static final int BLOCK_SIZE = 5; // 5 байт на число
    private long currentValue;

    public BinCombinationGenerate(Long startValue) {
        this.currentValue = startValue;
    }

    public byte[] nextCombination(){
        currentValue++;
        return encode(currentValue);
    }

    public static byte[] encode(long number) {
        byte[] result = new byte[5];
        for (int i = 4; i >= 0; i--) {
            result[i] = (byte) (number & 0xFF); // Берём младший байт
            number >>= 8; // Смещаем число вправо на 8 бит
        }
        return result;
    }
    public static long decode(byte[] bytes) {
        long number = 0;
        for (int i = 0; i < 5; i++) {
            number = (number << 8) | (bytes[i] & 0xFF); // Добавляем байт в число
        }
        return number;
    }




}
