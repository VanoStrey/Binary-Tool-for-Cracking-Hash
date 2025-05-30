package org.example;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ExternalDictionarySorter {
    private static final int RECORD_SIZE = 4; // Размер комбинации (2 символа * 2 байта)
    private static final long CHUNK_SIZE = 100L * 1024 * 1024; // Размер чанка (100 МБ)

    private static class DictionaryEntry {
        String originalCombination;
        String md5Hash;

        DictionaryEntry(String originalCombination, String md5Hash) {
            this.originalCombination = originalCombination;
            this.md5Hash = md5Hash;
        }
    }

    public void sort(String inputFilePath, String outputFilePath, UnicodeConverter converter, Hasher hasher) throws IOException {
        List<Path> tempChunkFiles = new ArrayList<>();
        Path inputPath = Paths.get(inputFilePath);
        long fileSize = Files.size(inputPath);
        long processedSize = 0; // Отслеживание объёма обработанных данных

        try (FileChannel inputChannel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            long position = 0;
            int chunkIndex = 0;

            while (position < fileSize) {
                long size = Math.min(CHUNK_SIZE, fileSize - position);
                ByteBuffer buffer = ByteBuffer.allocate((int) size);
                inputChannel.read(buffer, position);
                buffer.flip();

                // Читаем блоки по 4 байта (каждая запись)
                int numRecords = (int) (size / RECORD_SIZE);
                List<DictionaryEntry> entries = new ArrayList<>();

                for (int i = 0; i < numRecords; i++) {
                    byte[] recordBytes = new byte[RECORD_SIZE];
                    buffer.get(recordBytes);
                    String element = new String(recordBytes, StandardCharsets.UTF_16BE);
                    String converted = converter.unicodeToRangeString(element);
                    String hash = hasher.getHash(converted);
                    entries.add(new DictionaryEntry(element, hash));
                }

                // Сортируем записи по MD5-хэшу
                entries.sort(Comparator.comparing(entry -> entry.md5Hash));

                // Записываем отсортированный чанк во временный файл
                Path tempFile = Paths.get("chunk_" + chunkIndex++ + ".tmp");
                try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_16BE)) {
                    for (DictionaryEntry entry : entries) {
                        writer.write(entry.originalCombination);
                    }
                }
                tempChunkFiles.add(tempFile);

                processedSize += size;
                System.out.printf("Обработано: %.2f%%\n", (processedSize / (double) fileSize) * 100); // Вывод процента выполнения

                position += size;
            }
        }

        System.out.println("Все чанки отсортированы! Запускаем финальное слияние...");
        mergeSortedChunks(tempChunkFiles, outputFilePath, fileSize);

        for (Path tempFile : tempChunkFiles) {
            Files.deleteIfExists(tempFile);
        }
    }

    private void mergeSortedChunks(List<Path> chunkFiles, String outputFilePath, long totalSize) throws IOException {
        PriorityQueue<ChunkEntry> queue = new PriorityQueue<>(Comparator.comparing(e -> e.md5Hash));
        List<BufferedReader> readers = new ArrayList<>();
        long processedSize = 0;

        for (Path chunkFile : chunkFiles) {
            BufferedReader reader = Files.newBufferedReader(chunkFile, StandardCharsets.UTF_16BE);
            readers.add(reader);
            String element = reader.readLine();
            if (element != null) {
                queue.add(new ChunkEntry(element, element, reader)); // Здесь элемент передаётся дважды, но при необходимости можно сохранять хэш
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath), StandardCharsets.UTF_16BE)) {
            int totalChunks = chunkFiles.size();
            int processedChunks = 0;

            while (!queue.isEmpty()) {
                ChunkEntry entry = queue.poll();
                writer.write(entry.originalCombination); // Запись подряд, БЕЗ переносов строк!
                processedChunks++;

                // Рассчитываем процент выполнения
                processedSize += RECORD_SIZE;
                if (processedChunks % (totalChunks / 10) == 0 || processedChunks == totalChunks) { // Вывод каждые 10% прогресса
                    System.out.printf("Слияние: %.2f%%\n", (processedSize / (double) totalSize) * 100);
                }

                String nextElement = entry.reader.readLine();
                if (nextElement != null) {
                    queue.add(new ChunkEntry(nextElement, nextElement, entry.reader));
                }
            }
        }

        for (BufferedReader reader : readers) {
            reader.close();
        }

        System.out.println("Финальная сортировка завершена! Итоговый файл записан: " + outputFilePath);
    }

    private static class ChunkEntry {
        String originalCombination;
        String md5Hash;
        BufferedReader reader;

        public ChunkEntry(String originalCombination, String md5Hash, BufferedReader reader) {
            this.originalCombination = originalCombination;
            this.md5Hash = md5Hash;
            this.reader = reader;
        }
    }

    public static void main(String[] args) {
        try {
            UnicodeConverter converter = new UnicodeConverter("0123456789");
            Hasher md5Hasher = new MD5Hasher();
            ExternalDictionarySorter sorter = new ExternalDictionarySorter();
            sorter.sort("unicode_combinations2.txt", "sorted_unicode_combinations2.txt", converter, md5Hasher);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
