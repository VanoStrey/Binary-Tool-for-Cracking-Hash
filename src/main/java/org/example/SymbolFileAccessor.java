package org.example;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class SymbolFileAccessor {
    private String filePath;
    private int symbolsPerElement;

    public SymbolFileAccessor(String filePath, int symbolsPerElement) {
        this.filePath = filePath;
        this.symbolsPerElement = symbolsPerElement;
    }

    public String getElement(long index) {
        int bytesPerChar = 2; // UTF-16BE → каждый символ занимает ровно 2 байта
        long byteOffset = (long) index * symbolsPerElement * bytesPerChar;
        int bytesToRead = symbolsPerElement * bytesPerChar;

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileLength = raf.length();
            if (byteOffset >= fileLength) {
                return null;
            }
            if (byteOffset + bytesToRead > fileLength) {
                bytesToRead = (int) (fileLength - byteOffset);
            }

            byte[] buffer = new byte[bytesToRead];
            raf.seek(byteOffset);
            raf.readFully(buffer);

            return new String(buffer, StandardCharsets.UTF_16BE);
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
            return -1;
        }
    }
}
