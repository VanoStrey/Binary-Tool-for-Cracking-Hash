package Bytes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class BinDictionarySorter {

    private static class DictionaryEntry {
        byte[] bytes;
        String hash;

        DictionaryEntry(byte[] bytes, String hash) {
            this.bytes = bytes;
            this.hash = hash;
        }
    }

    public void sort(String inputFilePath, String outputFilePath, int elementSize, BinBaseConverter converter, Hasher hasher) throws IOException {
        byte[] fileData = Files.readAllBytes(Path.of(inputFilePath));
        List<DictionaryEntry> entries = new ArrayList<>();

        for (int i = 0; i < fileData.length; i += elementSize) {
            byte[] buffer = Arrays.copyOfRange(fileData, i, i + elementSize);
            String combination = converter.convertToBaseString(buffer);
            String hash = hasher.getHash(combination);
            entries.add(new DictionaryEntry(buffer, hash));
        }

        entries.sort(Comparator.comparing(entry -> entry.hash));

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            for (DictionaryEntry entry : entries) {
                fos.write(entry.bytes);
            }
        }

        System.out.println("✅ Отсортированный `.bin` записан: " + outputFilePath);
    }

    public void generateAndSortInMemory(String outputFilePath, int elementSize, BinBaseConverter converter, Hasher hasher, long count, BinCombinationGenerate generator) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<DictionaryEntry>> futures = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            futures.add(executor.submit(() -> {
                byte[] bytes = generator.nextCombination();
                String combination = converter.convertToBaseString(bytes);
                String hash = hasher.getHash(combination);
                return new DictionaryEntry(bytes, hash);
            }));
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        List<DictionaryEntry> entries = new ArrayList<>();
        for (Future<DictionaryEntry> future : futures) {
            try {
                entries.add(future.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("🚀 Начинаем сортировку...");
        entries.sort(Comparator.comparing(entry -> entry.hash));

        byte[] fileData = new byte[elementSize * entries.size()];
        int index = 0;
        for (DictionaryEntry entry : entries) {
            System.arraycopy(entry.bytes, 0, fileData, index, elementSize);
            index += elementSize;
        }

        Files.write(Path.of(outputFilePath), fileData);

        System.out.println("✅ Генерация и сортировка завершена: " + entries.size() + " элементов");
    }
}
