package Bytes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;


public class BinDictionarySorter {

    private static class DictionaryEntry {
        byte[] bytes;
        byte[] hash;

        DictionaryEntry(byte[] bytes, byte[] hash) {
            this.bytes = bytes;
            this.hash = hash;
        }
    }

    public void generateAndSortInMemory(String outputFilePath, int elementSize, BinBaseConverter converter, Hasher hasher, long count, BinCombinationGenerate generator) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<DictionaryEntry>> futures = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            futures.add(executor.submit(() -> {
                byte[] bytes = generator.nextCombination();
                byte[] hash = hasher.getBinHash(converter.convertToBaseString(bytes));
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
        entries.sort(Comparator.comparing(entry -> new BigInteger(1, entry.hash)));

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
