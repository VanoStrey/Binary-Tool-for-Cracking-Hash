import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    public static ArrayList<ArrayList<BinaryHashSearcher>> binarySearch = new ArrayList<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        SymbolFileAccessor accessor = new SymbolFileAccessor("UC2_2^30/chunk_15.txt", 2);
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();
        DictionarySplitter dictionarySplitter = new DictionarySplitter("UC2_2^30.txt", 2);
        DictionaryAddition dictionaryAddition = new DictionaryAddition();
        DictionarySorter dictionarySorter = new DictionarySorter();
        SHA256Hash sha256 = new SHA256Hash();
        SHA1Hash sha1 = new SHA1Hash();
        MD5Hash md5 = new MD5Hash();

        //dictionarySplitter.slitAll("UC2_2^30", 32);
        //dictionarySplitter.split("UC2_2^28_4.txt", accessor.getTotalElements()/8 + accessor.getTotalElements()/16, accessor.getTotalElements()/4);
        /*
        for (int i = 7; i < 32; i++) {
            dictionarySorter.testSort("UC2_2^30/chunk_" + i + ".txt", "sortUC2_2^30/MD5_chunk_" + i + ".txt", 2, unicodeConverter, md5);
            dictionarySorter.testSort("UC2_2^30/chunk_" + i + ".txt", "sortUC2_2^30/SHA1_chunk_" + i + ".txt", 2, unicodeConverter, sha1);
            dictionarySorter.testSort("UC2_2^30/chunk_" + i + ".txt", "sortUC2_2^30/SHA256_chunk_" + i + ".txt", 2, unicodeConverter, sha256);
        }
        System.out.println("Все отсортированно!");
         */


        ArrayList<BinaryHashSearcher> md5Dictionary = new ArrayList<>();
        binarySearch.add(md5Dictionary);
        for (int i = 0; i < 32; i++)
            binarySearch.get(0).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30/MD5_chunk_" + i + ".txt", 2), unicodeConverter, md5));
        ArrayList<BinaryHashSearcher> sha1Dictionary = new ArrayList<>();
        binarySearch.add(md5Dictionary);
        for (int i = 0; i < 32; i++) {
            binarySearch.get(1).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30/SHA1_chunk_" + i + ".txt", 2), unicodeConverter, sha1));
        }
        ArrayList<BinaryHashSearcher> sha256Dictionary = new ArrayList<>();
        binarySearch.add(md5Dictionary);
        for (int i = 0; i < 32; i++) {
            binarySearch.get(2).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30/SHA256_chunk_" + i + ".txt", 2), unicodeConverter, sha256));
        }

        //menuApp();
        startTelegramBot();
    }

    public static void startTelegramBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
            System.out.println("🚀 TelegramBot успешно запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static String universalCrackHash(String hash) {
        int index = switch (hash.length()) {
            case 32 -> 0;
            case 40 -> 1;
            case 64 -> 2;
            default -> -1;
        };

        if (index == -1) {
            return "unavailable hash type";
        }

        List<BinaryHashSearcher> searchModules = binarySearch.get(index);
        List<Future<String>> futures = new ArrayList<>();

        for (BinaryHashSearcher module : searchModules) {
            futures.add(executor.submit(() -> {
                String result = module.search(hash);
                return (result != null && !result.isEmpty()) ? result : null;
            }));
        }

        for (Future<String> future : futures) {
            try {
                String decrypted = future.get(); // 🚀 Ожидаем результат
                if (decrypted != null) {
                    return decrypted; // ✅ Возвращаем найденный хэш, но НЕ завершаем `executor`
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return "not found"; // 🔥 `executor` остаётся активным для новых запросов
    }

    private static void menuApp() {
        Scanner scanner = new Scanner(System.in);
        String hash, result;
        long startTime, endTime;
        while (true) {
            System.out.println("hash для расшифровки: ");
            hash = scanner.next();

            startTime = System.currentTimeMillis();
            result = universalCrackHash(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
        }
    }
}