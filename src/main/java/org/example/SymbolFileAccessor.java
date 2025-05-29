package org.example;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class SymbolFileAccessor {
    private String filePath;
    private int symbolsPerElement;

    /**
     * Конструктор инициализирует путь к файлу и количество символов в элементе.
     *
     * @param filePath путь к файлу
     * @param symbolsPerElement количество символов, которые составляют один элемент
     */
    public SymbolFileAccessor(String filePath, int symbolsPerElement) {
        this.filePath = filePath;
        this.symbolsPerElement = symbolsPerElement;
    }

    /**
     * Метод возвращает элемент с заданным индексом.
     * Элемент – это подстрока из файла, начиная с позиции index * symbolsPerElement.
     *
     * @param index индекс элемента (начиная с 0)
     * @return строка, содержащая symbolsPerElement символов, либо null, если индекс вне диапазона файла
     */
    public String getElement(long index) {
        int bytesPerChar = 2; // UTF-16BE → каждый символ занимает ровно 2 байта
        long byteOffset = (long) index * symbolsPerElement * bytesPerChar;
        int bytesToRead = symbolsPerElement * bytesPerChar;

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileLength = raf.length();
            if (byteOffset >= fileLength) {
                return null; // Индекс выходит за пределы файла
            }
            // Если до конца файла осталось меньше символов, читаем только доступные
            if (byteOffset + bytesToRead > fileLength) {
                bytesToRead = (int) (fileLength - byteOffset);
            }

            byte[] buffer = new byte[bytesToRead];
            raf.seek(byteOffset);
            raf.readFully(buffer);

            return new String(buffer, StandardCharsets.UTF_16BE); // Преобразование в строку
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getTotalElements() {
        int bytesPerChar = 2; // UTF-16BE → каждый символ занимает ровно 2 байта
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileLength = raf.length();
            return fileLength / (symbolsPerElement * bytesPerChar);
        } catch (IOException e) {
            e.printStackTrace();
            return -1; // Ошибка при чтении файла
        }
    }
}
