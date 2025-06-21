import hashFunc.*;
import threeByteChunkOffset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class Main {

    public static ArrayList<ThreeByteChunkSearcher> threeByteChunkSearcher = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        SHA256Hash sha256 = new SHA256Hash();
        SHA1Hash sha1 = new SHA1Hash();
        MD5Hash md5 = new MD5Hash();


        ThreeByteBaseConverter threeByteBaseConverter = new ThreeByteBaseConverter("0123456789");
        initThreeByteChunckSearch(threeByteBaseConverter, sha256);
        menuApp();

        /*
        ThreeByteChunkDictionarySorter threeByteChunkDictionarySorter = new ThreeByteChunkDictionarySorter("master_chunk.bin", sha256, threeByteBaseConverter );
        String pathDictionary = "chuncks_SHA256_0123456789";
        threeByteChunkDictionarySorter.sortChunkToFile(pathDictionary, 8);

         */

    }

    public static String ThreeByteCrackSHA256(String hash) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<String>> futures = new ArrayList<>();

        for (ThreeByteChunkSearcher searcher : threeByteChunkSearcher) {
            futures.add(executor.submit(() -> searcher.search(hash))); // 🔥 Параллельный поиск
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        StringBuilder rezult = new StringBuilder();
        for (Future<String> future : futures) {
            try {
                String found = future.get();
                if (found != null) {
                    rezult.append(found); // 🔥 Объединяем результаты
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return rezult.length() > 0 ? rezult.toString() : "hash not found";
    }

    private static void initThreeByteChunckSearch(ThreeByteBaseConverter converter, Hasher sha256) {
        for (int i = 0; i < 8; i++) {
            threeByteChunkSearcher.add(new ThreeByteChunkSearcher(new ThreeByteFileAccessor("chuncks_SHA256_0123456789/chunk_" + i + ".bin"), converter, sha256));
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
            result = ThreeByteCrackSHA256(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
        }
    }
}