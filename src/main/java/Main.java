import Bytes.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class Main {

    public static ArrayList<ArrayList<BinaryHashSearcher>> binarySearch = new ArrayList<>();
    public static ArrayList<BinBinaryHashSearcher> binBinarySearch = new ArrayList<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public static void main(String[] args) throws IOException, InterruptedException {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        SymbolFileAccessor accessor = new SymbolFileAccessor("UC2_2^30/chunk_15.txt", 2);
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();
        DictionarySplitter dictionarySplitter = new DictionarySplitter("UC2_2^30.txt", 2);
        DictionaryAddition dictionaryAddition = new DictionaryAddition();
        DictionarySorter dictionarySorter = new DictionarySorter();
        SHA256Hash sha256 = new SHA256Hash();
        SHA1Hash sha1 = new SHA1Hash();
        MD5Hash md5 = new MD5Hash();

        //dictionarySplitter.slitAll("UC2_2^30_16chunks", 16);
        //initBinarySearchHash(unicodeConverter, md5, sha1, sha256);
        //menuApp();
        //startTelegramBot();

        long rangeCombinations = (long) Math.pow(2, 26);

        BinCombinationGenerate binCombinationGenerate = new BinCombinationGenerate((5 * rangeCombinations) - 1L);
        BinBaseConverter binBaseConverter = new BinBaseConverter("0123456789");
        BinDictionarySorter binDictionarySorter = new BinDictionarySorter();
        /*
        for (int i = 5; i < 16; i++) {
            binDictionarySorter.generateAndSortInMemory("SHA256_2^26/chunk_" + i + ".bin", 5, binBaseConverter, sha256, rangeCombinations, binCombinationGenerate);
        }*/ctionari

        initBinBinarySearchHash(binBaseConverter, sha256);
        startTelegramBot();
    }

    public static String BinCrackSHA256(String hash) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<String>> futures = new ArrayList<>();

        for (BinBinaryHashSearcher searcher : binBinarySearch) {
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

    private static void initBinBinarySearchHash(BinBaseConverter converter, Hasher sha256) {
        for (int i = 0; i < 16; i++) {
            binBinarySearch.add(new BinBinaryHashSearcher(new BinFileAccessor("SHA256_2^26/chunk_" + i + ".bin"), converter, sha256));
        }
    }

    private static void initBinarySearchHash(UnicodeConverter unicodeConverter, Hasher md5, Hasher sha1, Hasher sha256) {
        ArrayList<BinaryHashSearcher> DictionaryChunks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            binarySearch.add(DictionaryChunks);
        }
        for (int i = 0; i < 16; i++) {
            binarySearch.get(0).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30_16chunks/MD5_chunk_" + i + ".txt", 2), unicodeConverter, md5));
            binarySearch.get(1).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30_16chunks/SHA1_chunk_" + i + ".txt", 2), unicodeConverter, sha1));
            binarySearch.get(2).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30_16chunks/SHA256_chunk_" + i + ".txt", 2), unicodeConverter, sha256));
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

    private static void menuApp() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String hash, result;
        long startTime, endTime;
        while (true) {
            System.out.println("hash для расшифровки: ");
            hash = scanner.next();

            startTime = System.currentTimeMillis();
            result = BinCrackSHA256(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
        }
    }
}