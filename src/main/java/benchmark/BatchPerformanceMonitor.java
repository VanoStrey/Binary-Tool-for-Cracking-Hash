package benchmark;

import coreChunk.*;
import hashFunc.*;

import java.io.IOException;
import java.lang.management.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BatchPerformanceMonitor {

    private static final List<HashBinarySearch> hashSearches = new ArrayList<>();
    private static final DecimalFormat fmt = new DecimalFormat("0.00");
    private static final int TEST_HASH_COUNT = 1000;

    public static void main(String[] args) throws Exception {
        Hasher sha256 = new SHA256Hash();
        ChunkValueEncoding encoder = new ChunkValueEncoding(
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:',.<>?/"
        );
        String outputDir = "chuncks_SHA256_allSimbols";

        initChunkSearch(outputDir, encoder, sha256);
        List<String> testHashes = generateTestHashes(sha256, encoder, TEST_HASH_COUNT);

        System.gc(); Thread.sleep(100);
        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean tm = ManagementFactory.getThreadMXBean();
        if (tm.isThreadCpuTimeSupported() && !tm.isThreadCpuTimeEnabled()) {
            tm.setThreadCpuTimeEnabled(true);
        }

        Map<Long, Long> cpuBefore = snapshotCpuTime(tm);
        long memBeforeAll = runtime.totalMemory() - runtime.freeMemory();
        long wallStartAll = System.nanoTime();

        double totalWall = 0;
        double totalRam = 0;

        for (String hash : testHashes) {
            System.gc(); Thread.sleep(1); // минимальная стабилизация
            long memBefore = runtime.totalMemory() - runtime.freeMemory();
            long start = System.nanoTime();

            String result = crackSHA256(hash);

            long end = System.nanoTime();
            long memAfter = runtime.totalMemory() - runtime.freeMemory();

            double wallMs = (end - start) / 1_000_000.0;
            double ramMb = (memAfter - memBefore) / 1024.0 / 1024;

            totalWall += wallMs;
            totalRam += ramMb;
        }

        long wallEndAll = System.nanoTime();
        long memAfterAll = runtime.totalMemory() - runtime.freeMemory();
        Map<Long, Long> cpuAfter = snapshotCpuTime(tm);

        double avgWall = totalWall / TEST_HASH_COUNT;
        double avgRam = totalRam / TEST_HASH_COUNT;

        System.out.println("\n📊 Средняя нагрузка при расшифровке " + TEST_HASH_COUNT + " хэшей:");
        System.out.println("⏱ Среднее время подбора: " + fmt.format(avgWall) + " мс");
        System.out.println("📉 Среднее RAM на хеш: " + fmt.format(avgRam) + " МБ");
        System.out.println("🧠 Общее потребление RAM: " + fmt.format((memAfterAll - memBeforeAll) / 1024.0 / 1024) + " МБ");

        System.out.println("\n🔧 CPU-время по потокам:");
        for (long id : cpuAfter.keySet()) {
            long delta = cpuAfter.get(id) - cpuBefore.getOrDefault(id, 0L);
            if (delta > 1_000_000) {
                ThreadInfo info = tm.getThreadInfo(id);
                if (info != null) {
                    System.out.printf("- [%d] %s: %s мс%n", id, info.getThreadName(), fmt.format(delta / 1_000_000.0));
                }
            }
        }

        long totalSize = Files.walk(Path.of(outputDir))
                .filter(Files::isRegularFile)
                .mapToLong(p -> {
                    try { return Files.size(p); } catch (IOException e) { return 0L; }
                }).sum();

        System.out.printf("\n📂 Размер словаря: %.2f ГБ%n", totalSize / 1024.0 / 1024 / 1024);
    }

    private static List<String> generateTestHashes(Hasher hasher, ChunkValueEncoding encoder, int count) {
        List<String> hashes = new ArrayList<>();
        Random rand = new Random(42);
        String alphabet = encoder.getRangeChars();
        for (int i = 0; i < count; i++) {
            int len = 1 + rand.nextInt(5); // от 1 до 5 символов
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
            }
            hashes.add(hasher.getHash(sb.toString()));
        }
        return hashes;
    }

    private static void initChunkSearch(String dictionaryDir, ChunkValueEncoding converter, Hasher sha256) throws IOException {
        System.out.println("🧠 Инициализация и прогрев чанков...");
        for (int i = 0; i < 356; i++) {
            String path = dictionaryDir + "/chunk_" + i + ".bin";
            if (!Files.exists(Path.of(path))) continue;

            ChunkBinaryFileAccessor accessor = new ChunkBinaryFileAccessor(path);
            HashBinarySearch search = new HashBinarySearch(accessor, converter, sha256);
            hashSearches.add(search);

            long total = accessor.getTotalElements();
            long[] offsets = { 0, total / 3, total * 2 / 3, total - 1 };
            for (long offset : offsets) {
                byte[] el = accessor.getElement(offset);
                if (el == null) continue;
                String enc = converter.convertToBaseString(el);
                byte[] hash = sha256.getBinHash(enc);
                hash[0] ^= el[0];
            }
        }
        System.gc();
        System.out.println("✅ Система готова к работе\n");
    }

    private static Map<Long, Long> snapshotCpuTime(ThreadMXBean tm) {
        Map<Long, Long> cpu = new HashMap<>();
        for (long id : tm.getAllThreadIds()) {
            long time = tm.getThreadCpuTime(id);
            if (time > 0) cpu.put(id, time);
        }
        return cpu;
    }

    private static String crackSHA256(String hash) throws InterruptedException {
        AtomicReference<String> foundResult = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();
        for (HashBinarySearch searcher : hashSearches) {
            futures.add(executor.submit(() -> {
                if (foundResult.get() != null) return;
                String result = searcher.search(hash);
                if (!result.isEmpty()) foundResult.compareAndSet(null, result);
            }));
        }
        for (Future<?> f : futures) {
            try { f.get(); } catch (ExecutionException e) { e.printStackTrace(); }
        }
        executor.shutdown();
        return foundResult.get() != null ? foundResult.get() : "not found";
    }
}
