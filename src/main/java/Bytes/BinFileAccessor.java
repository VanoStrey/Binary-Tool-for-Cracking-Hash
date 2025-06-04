package Bytes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class BinFileAccessor {
    private final String filePath;
    private static final int ELEMENT_SIZE = 5;

    public BinFileAccessor(String filePath) {
        this.filePath = filePath;
    }

    // 🔹 Получает число по индексу
    public byte[] getElement(long index) {
        long byteOffset = index * ELEMENT_SIZE; // Смещение по файлу
        byte[] buffer = new byte[ELEMENT_SIZE];

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            if (byteOffset >= raf.length()) {
                return null;
            }

            raf.seek(byteOffset);
            raf.readFully(buffer);

            return buffer; // 🔹 Интерпретируем 5 байт как число
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 🔹 Возвращает количество записанных элементов
    public long getTotalElements() {

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileLength = raf.length();

            return raf.length() / ELEMENT_SIZE; // 🔥 Количество 5-байтовых чисел
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
