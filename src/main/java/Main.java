import Bytes.Hasher;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Bytes.*;

public class Main {

    public static ArrayList<ArrayList<BinaryHashSearcher>> binarySearch = new ArrayList<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
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

        /*
        for (int i = 0; i < 16; i++) {
            dictionarySorter.Sort("UC2_2^30_16chunks/chunk_" + i + ".txt", "sortUC2_2^30_16chunks/MD5_chunk_" + i + ".txt", 2, unicodeConverter, md5);
            dictionarySorter.Sort("UC2_2^30_16chunks/chunk_" + i + ".txt", "sortUC2_2^30_16chunks/SHA1_chunk_" + i + ".txt", 2, unicodeConverter, sha1);
            dictionarySorter.Sort("UC2_2^30_16chunks/chunk_" + i + ".txt", "sortUC2_2^30_16chunks/SHA256_chunk_" + i + ".txt", 2, unicodeConverter, sha256);
        }
        System.out.println("Все отсортированно!");



        ArrayList<BinaryHashSearcher> DictionaryChunks = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            binarySearch.add(DictionaryChunks);
        }
        for (int i = 0; i < 16; i++) {
            binarySearch.get(0).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30_16chunks/MD5_chunk_" + i + ".txt", 2), unicodeConverter, md5));
            binarySearch.get(1).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30_16chunks/SHA1_chunk_" + i + ".txt", 2), unicodeConverter, sha1));
            binarySearch.get(2).add(new BinaryHashSearcher(new SymbolFileAccessor("sortUC2_2^30_16chunks/SHA256_chunk_" + i + ".txt", 2), unicodeConverter, sha256));
        }

        //menuApp();
        startTelegramBot();

         */
        BinBaseConverter binBaseConverter = new BinBaseConverter("0123456789");

        BinFileAccessor binAccessor = new BinFileAccessor("outputSort.bin");

        BinDictionarySorter binDictionarySorter = new BinDictionarySorter();

        //binDictionarySorter.sort("output.bin", "outputSort.bin", 5, binBaseConverter, sha256);

        BinBinaryHashSearcher binBinaryHashSearcher = new BinBinaryHashSearcher(binAccessor, binBaseConverter, sha256);
        long startTime, endTime;
        String result = "";
        startTime = System.currentTimeMillis();
        result = binBinaryHashSearcher.search(sha256.getHash("123456"));
        endTime = System.currentTimeMillis();


        System.out.println("Результат: " + result);
        System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
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