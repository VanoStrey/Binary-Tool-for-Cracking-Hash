import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static ArrayList<BinaryHashSearcher> binarySearch = new ArrayList<>();


    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        SymbolFileAccessor accessor = new SymbolFileAccessor("UC2_2^25_test.txt", 2);
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();
        DictionarySplitter dictionarySplitter = new DictionarySplitter(accessor);
        DictionarySorter dictionarySorter = new DictionarySorter();
        SHA256Hash sha256 = new SHA256Hash();
        SHA1Hash sha1 = new SHA1Hash();
        MD5Hash md5 = new MD5Hash();

        /*
        binarySearch.add(new BinaryHashSearcher(new SymbolFileAccessor("sortMD5_UC2_2^25.txt", 2), unicodeConverter, md5));
        binarySearch.add(new BinaryHashSearcher(new SymbolFileAccessor("sortSHA1_UC2_2^25.txt", 2), unicodeConverter, sha1));
        binarySearch.add(new BinaryHashSearcher(new SymbolFileAccessor("sortSHA256_UC2_2^25.txt", 2), unicodeConverter, sha256));
        startTekegramBot();

        long range = accessor.getTotalElements()/256;
        dictionarySplitter.split("UC2_2^25_test2.txt", range, range * 2);
         */

        //dictionarySorter.sort("sortmd5_UC2_2^25.txt", accessor, unicodeConverter, md5);
        dictionarySorter.sort("sortSHA1_UC2_2^25.txt", accessor, unicodeConverter, sha1);
        dictionarySorter.sort("sortSHA256_UC2_2^25.txt", accessor, unicodeConverter, sha256);


    }

    public static String universalCrackHash(String hash) {
        return switch (hash.length()) {
            case 32 -> binarySearch.get(0).search(hash);
            case 40 -> binarySearch.get(1).search(hash);
            case 64 -> binarySearch.get(2).search(hash);
            default -> "unavailable hash type";
        };
    }

    private static void startTekegramBot(){
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
            System.out.println("🚀 Бот успешно запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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