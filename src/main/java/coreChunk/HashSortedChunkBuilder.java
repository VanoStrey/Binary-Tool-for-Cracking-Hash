package coreChunk;

import hashFunc.Hasher;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

public class HashSortedChunkBuilder {
    private final Entry[] masterEntries;
    private final int elementSize = 3;
    private final int totalElements;
    private final Hasher hasher;
    private final ChunkValueEncoding converter;

    public HashSortedChunkBuilder(
            String relativeMasterChunkPath,
            Hasher hasher,
            ChunkValueEncoding converter
    ) throws IOException {
        Path absolutePath = Paths.get("").toAbsolutePath()
                .resolve(relativeMasterChunkPath);
        byte[] rawChunk = Files.readAllBytes(absolutePath);

        if (rawChunk.length != (1 << 24) * elementSize)
            throw new IllegalArgumentException(
                    "Размер masterChunk некорректен: " + rawChunk.length
            );

        this.totalElements = rawChunk.length / elementSize;
        this.masterEntries = new Entry[totalElements];
        for (int i = 0; i < totalElements; i++) {
            int off = i * elementSize;
            byte[] raw = Arrays.copyOfRange(rawChunk, off, off + elementSize);
            masterEntries[i] = new Entry(raw);
        }

        this.hasher = hasher;
        this.converter = converter;
    }

    public void sortChunkToFile(String outputDirectoryPath, int chunkIndex) throws IOException {
        long t0 = System.currentTimeMillis();
        System.out.println("📦 Обработка чанка #" + chunkIndex);

        // 1) Клонируем массив ссылок (raw остаётся тем же)
        Entry[] chunkEntries = Arrays.copyOf(masterEntries, totalElements);

        // 2) Генерим хэши для каждого элемента
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < totalElements; i++) {
            if (i % 2_000_000 == 0) {
                System.out.println("  ⏳ Генерация [" + i + "/" + totalElements + "]");
            }
            byte[] raw = chunkEntries[i].raw;
            BigInteger val = new BigInteger(1, raw)
                    .add(BigInteger.valueOf(chunkIndex).shiftLeft(24));
            String s = converter.convertToBaseString(val);
            chunkEntries[i].hash = hasher.getBinHash(s);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("✅ Хэши сгенерированы за " + (t2 - t1)/1000.0 + " сек");

        // 3) Сортировка по хэшу
        System.out.println("🔃 Сортировка...");
        long t3 = System.currentTimeMillis();
        Arrays.sort(chunkEntries, Comparator.comparing(e -> new BigInteger(1, e.hash)));
        long t4 = System.currentTimeMillis();
        System.out.println("✅ Отсортировано за " + (t4 - t3)/1000.0 + " сек");

        // 4) Подготовка директории
        Path outPath = Paths.get(outputDirectoryPath, "chunk_" + chunkIndex + ".bin");
        Path folder = outPath.getParent();
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
            System.out.println("📁 Папка создана: " + folder.toAbsolutePath());
        }

        // 5) Запись raw-байтов в файл
        System.out.println("💾 Сохранение в: " + outPath.toAbsolutePath());
        long t5 = System.currentTimeMillis();
        try (OutputStream os = Files.newOutputStream(outPath)) {
            for (Entry e : chunkEntries) {
                os.write(e.raw);
            }
        }
        long t6 = System.currentTimeMillis();
        System.out.println("✅ Записано за " + (t6 - t5)/1000.0 + " сек");

        long t7 = System.currentTimeMillis();
        System.out.println("🎉 Чанк #" + chunkIndex + " готов за " + (t7 - t0)/1000.0 + " сек\n");
    }

    private static class Entry {
        final byte[] raw;
        byte[] hash;
        Entry(byte[] raw) { this.raw = raw; }
    }
}
