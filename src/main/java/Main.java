import hashFunc.*;
import coreChunk.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class Main {

    public static ArrayList<HashBinarySearch> hashBinarySearch = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        SHA256Hash sha256 = new SHA256Hash();
        String dictionarySymbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:',.<>?/";
        ChunkValueEncoding chunkValueEncoding = new ChunkValueEncoding("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:',.<>?/");
        String outputDir = "chuncks_SHA256_allSimbols";


        /*
        HashSortedChunkBuilder builder = new HashSortedChunkBuilder("master_chunk.bin", sha256, chunkValueEncoding);
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
        Scanner scanner = new Scanner(System.in);
        String baseDir = getParentDirectory(); // или "." если хочешь текущую
        List<String> availableFolders = listAllFolders(baseDir);

        if (availableFolders.isEmpty()) {
            System.out.println("❌ Нет доступных папок.");
            return;
        }

        String selectedFolder = inputFolder(availableFolders);
        outputDir = baseDir + File.separator + selectedFolder;

        startTelegramBot(new DictionarySearch(outputDir));
    }

    private static void startTelegramBot(DictionarySearch dictionarySearch) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot(dictionarySearch));
            System.out.println("🚀 TelegramBot успешно запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void menuApp(DictionarySearch dictionarySearch) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);
        String hash, result;
        long startTime, endTime;
        while (true) {
            System.out.println("hash для расшифровки: ");
            hash = scanner.next();

            startTime = System.currentTimeMillis();
            result =  dictionarySearch.search(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
        }
    }
    public static String getParentDirectory() {
        return new File(".").getAbsoluteFile().getParent();
    }

    public static String inputFolder(List<String> folders) {
        System.out.println("Доступные папки:");
        for (int i = 0; i < folders.size(); i++) {
            System.out.println((i + 1) + "... " + folders.get(i));
        }

        System.out.print("Выберите номер папки: ");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt() - 1;

        return (choice >= 0 && choice < folders.size()) ? folders.get(choice) : "";
    }

    public static List<String> listAllFolders(String directoryPath) {
        File dir = new File(directoryPath);
        return Arrays.stream(dir.listFiles(File::isDirectory))
                .map(File::getName)
                .collect(Collectors.toList());
    }

}