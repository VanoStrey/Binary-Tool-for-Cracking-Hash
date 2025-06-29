package benchmark;

import coreChunk.ChunkBinaryFileAccessor;
import coreChunk.ChunkValueEncoding;
import coreChunk.HashBinarySearch;
import hashFunc.Hasher;
import hashFunc.SHA256Hash;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

public class ChunkPerformanceBenchmark {

    private static final String CHUNK_PATH = "chuncks_SHA256_allSimbols/chunk_0.bin";
    private static final String DICTIONARY_PATH = "chuncks_SHA256_allSimbols";
    private static final String RANGE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:',.<>?/";

    public static void main(String[] args) throws Exception {

        ChunkBinaryFileAccessor accessor = new ChunkBinaryFileAccessor(CHUNK_PATH);
        ChunkValueEncoding encoder = new ChunkValueEncoding(RANGE_CHARS);
        Hasher hasher = new SHA256Hash(); // Твоя реализация SHA256

        printSystemInfo(accessor);

        warmUp(accessor, encoder, hasher);

        benchmarkElementAccess(accessor);
        benchmarkEncoding(encoder, accessor);
        benchmarkHashing(encoder, accessor, hasher);
        benchmarkEncodingAndHashing(accessor, encoder, hasher);
        benchmarkBinarySearch(accessor, encoder, hasher,
                "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");

        accessor.close();
    }

    private static void printSystemInfo(ChunkBinaryFileAccessor accessor) throws Exception {
        long totalElements = accessor.getTotalElements();
        long totalSize = getFolderSize(DICTIONARY_PATH);

        System.out.println("📊 Техническая информация:");
        System.out.printf("- Всего комбинаций: %,d%n", totalElements);
        System.out.printf("- Размер словаря: %.2f ГБ%n", totalSize / 1024.0 / 1024 / 1024);
        System.out.printf("- Размер одной записи: %d байт%n", 3);
        System.out.printf("- Кол-во логических ядер: %d%n", Runtime.getRuntime().availableProcessors());
        System.out.println();
    }

    private static void warmUp(ChunkBinaryFileAccessor accessor,
                               ChunkValueEncoding encoder,
                               Hasher hasher) {
        System.out.println("🔥 Прогрев...");
        long total = accessor.getTotalElements();
        long[] offsets = {
                0,
                total / 4,
                total / 2,
                (3 * total) / 4,
                total - 1
        };

        for (long offset : offsets) {
            byte[] el = accessor.getElement(offset);
            if (el == null) continue;
            String encoded = encoder.convertToBaseString(el);
            byte[] hash = hasher.getBinHash(encoded);
            hash[0] ^= el[0]; // фиктивно
        }
        System.out.println("✅ Прогрев завершён\n");
    }

    public static void benchmarkElementAccess(ChunkBinaryFileAccessor accessor) {
        final int step = 10_000;
        final int iterations = 50;
        long total = 0;

        for (int j = 0; j < iterations; j++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < 1_000_000; i += step) accessor.getElement(i);
            long t1 = System.nanoTime();

            long tEmpty0 = System.nanoTime();
            for (int i = 0; i < 1_000_000; i += step) {}
            long tEmpty1 = System.nanoTime();

            total += (t1 - t0) - (tEmpty1 - tEmpty0);
        }

        double avgNs = total / (double) (iterations * (1_000_000 / step));
        System.out.printf("📎 Доступ к элементу: %.2f нс\n", avgNs);
    }

    public static void benchmarkEncoding(ChunkValueEncoding encoder, ChunkBinaryFileAccessor accessor) {
        long t0 = System.nanoTime();
        for (int i = 0; i < 100_000; i += 500) {
            byte[] element = accessor.getElement(i);
            String encoded = encoder.convertToBaseString(element);
        }
        long t1 = System.nanoTime();
        double avg = (t1 - t0) / (double) (100_000 / 500);
        System.out.printf("🔤 Энкодинг: %.2f нс/запись\n", avg);
    }

    public static void benchmarkHashing(ChunkValueEncoding encoder, ChunkBinaryFileAccessor accessor, Hasher hasher) {
        long t0 = System.nanoTime();
        for (int i = 0; i < 100_000; i += 500) {
            byte[] element = accessor.getElement(i);
            String encoded = encoder.convertToBaseString(element);
            byte[] hash = hasher.getBinHash(encoded);
        }
        long t1 = System.nanoTime();
        double avg = (t1 - t0) / (double) (100_000 / 500);
        System.out.printf("🔐 Хеширование: %.2f нс/запись\n", avg);
    }

    public static void benchmarkEncodingAndHashing(ChunkBinaryFileAccessor accessor,
                                                   ChunkValueEncoding encoder,
                                                   Hasher hasher) {
        long t0 = System.nanoTime();
        for (int i = 0; i < 100_000; i += 500) {
            byte[] element = accessor.getElement(i);
            String encoded = encoder.convertToBaseString(element);
            byte[] hash = hasher.getBinHash(encoded);
        }
        long t1 = System.nanoTime();
        double perOp = (t1 - t0) / (double) (100_000 / 500);
        System.out.printf("🧬 Хеш + энкодинг: %.2f нс/запись\n", perOp);
    }

    public static void benchmarkBinarySearch(ChunkBinaryFileAccessor accessor,
                                             ChunkValueEncoding encoder,
                                             Hasher hasher,
                                             String testHash) {
        HashBinarySearch search = new HashBinarySearch(accessor, encoder, hasher);
        long t0 = System.nanoTime();
        String result = search.search(testHash);
        long t1 = System.nanoTime();

        System.out.printf("🔎 Бинарный поиск: %.2f мс, результат: %s\n",
                (t1 - t0) / 1_000_000.0, result.isEmpty() ? "не найден" : result);
    }

    private static long getFolderSize(String directoryPath) throws Exception {
        return Files.walk(Path.of(directoryPath))
                .filter(Files::isRegularFile)
                .mapToLong(p -> {
                    try {
                        return Files.size(p);
                    } catch (Exception e) {
                        return 0L;
                    }
                }).sum();
    }
}
