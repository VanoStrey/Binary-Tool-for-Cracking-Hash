import hashFunc.*;
import coreChunk.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


public class Main {

    public static ArrayList<HashBinarySearch> hashBinarySearch = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        SHA256Hash sha256 = new SHA256Hash();
        ChunkValueEncoding chunkValueEncoding = new ChunkValueEncoding("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:',.<>?/");
        HashSortedChunkBuilder builder = new HashSortedChunkBuilder("master_chunk.bin", sha256, chunkValueEncoding);
        String outputDir = "chuncks_SHA256_allSimbols";


        /*
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int chunkIndex = 0; chunkIndex < 356; chunkIndex++) {
            final int index = chunkIndex;
            executor.submit(() -> {
                try {
                    builder.sortChunkToFile(outputDir, index);
                } catch (IOException e) {
                    System.err.println("Ошибка при обработке чанка " + index + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        System.out.println("✅ Все чанки завершены");

         */


        initChunkSearch(outputDir, chunkValueEncoding, sha256);

        startTelegramBot();
    }


    public static String CrackSHA256(String hash) throws InterruptedException {
        AtomicReference<String> foundResult = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        for (HashBinarySearch searcher : hashBinarySearch) {
            futures.add(executor.submit(() -> {
                if (foundResult.get() != null) return;
                String result = searcher.search(hash);
                if (!result.isEmpty()) foundResult.compareAndSet(null, result);
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get(); // 🔥 Именно тут может быть ExecutionException
            } catch (ExecutionException e) {
                e.printStackTrace(); // или залогируй по-своему
            }
        }

        executor.shutdown();

        return foundResult.get() != null ? foundResult.get() : "hash not found";
    }


    private static void initChunkSearch(String dictionaryDir, ChunkValueEncoding converter, Hasher sha256) throws IOException {
        System.out.println("🧠 Инициализация и прогрев чанков...");

        for (int i = 0; i < 356; i++) {
            String path = dictionaryDir + "/chunk_" + i + ".bin";
            Path chunkPath = Paths.get(path);

            if (!Files.exists(chunkPath)) {
                System.err.printf("⛔ Пропущен: %s%n", chunkPath.getFileName());
                continue;
            }

            ChunkBinaryFileAccessor accessor = new ChunkBinaryFileAccessor(path);
            HashBinarySearch search = new HashBinarySearch(accessor, converter, sha256);
            hashBinarySearch.add(search);

            // 🔥 Прогрев диапазона значений по смещениям (начало, середина, конец)
            long total = accessor.getTotalElements();
            long[] offsets = {
                    0,
                    total / 4,
                    total / 2,
                    (3 * total) / 4,
                    Math.max(0, total - 1)
            };

            for (long offset : offsets) {
                byte[] el = accessor.getElement(offset);
                if (el == null) continue;
                String encoded = converter.convertToBaseString(el);
                byte[] hash = sha256.getBinHash(encoded);
                hash[0] ^= el[0]; // не даём оптимизатору игнорировать
            }
        }

        System.gc(); // на всякий случай — подчистить лишнее
        System.out.println("✅ Система прогрета и готова к работе\n");
    }




    private static void startTelegramBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
            System.out.println("🚀 TelegramBot успешно запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void menuApp() throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);
        String hash, result;
        long startTime, endTime;
        while (true) {
            System.out.println("hash для расшифровки: ");
            hash = scanner.next();

            startTime = System.currentTimeMillis();
            result = CrackSHA256(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
        }
    }
}