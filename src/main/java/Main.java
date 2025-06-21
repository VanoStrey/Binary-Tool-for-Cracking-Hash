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


public class Main {

    public static ArrayList<HashBinarySearch> hashBinarySearch = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        SHA256Hash sha256 = new SHA256Hash();
        ChunkValueEncoding chunkValueEncoding = new ChunkValueEncoding("0123456789");
        HashSortedChunkBuilder builder = new HashSortedChunkBuilder("master_chunk.bin", sha256, chunkValueEncoding);
        String outputDir = "chuncks_SHA256_0123456789";


        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int chunkIndex = 12; chunkIndex < 256; chunkIndex++) {
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





        initChunkSearch(outputDir, chunkValueEncoding, sha256);
        startTelegramBot();
    }

    public static String CrackSHA256(String hash) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<String>> futures = new ArrayList<>();

        for (HashBinarySearch searcher : hashBinarySearch) {
            futures.add(executor.submit(() -> searcher.search(hash))); // 🔥 Параллельный поиск
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        StringBuilder result = new StringBuilder();
        for (Future<String> future : futures) {
            try {
                String found = future.get();
                if (found != null) {
                    result.append(found); // 🔥 Объединяем результаты
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return result.length() > 0 ? result.toString() : "hash not found";
    }

    private static void initChunkSearch(String dictionaryDir, ChunkValueEncoding converter, Hasher sha256) {
        for (int i = 0; i < 256; i++) {
            String path = dictionaryDir + "/chunk_" + i + ".bin";
            Path chunkPath = Paths.get(path);

            if (!Files.exists(chunkPath)) {
                System.err.println("⛔ Пропущен: " + chunkPath.getFileName());
                continue;
            }

            var accessor = new ChunkBinaryFileAccessor(path);
            hashBinarySearch.add(new HashBinarySearch(accessor, converter, sha256));
        }
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

    private static void menuApp() throws InterruptedException {
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