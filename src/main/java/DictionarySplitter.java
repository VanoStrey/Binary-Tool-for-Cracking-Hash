import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DictionarySplitter {
    private final String filePath;
    private final int symbolsPerElement;

    public DictionarySplitter(String filePath, int symbolsPerElement) {
        this.filePath = filePath;
        this.symbolsPerElement = symbolsPerElement;
    }
    public void slitAll(String outPathDirectory, long countChunks){
        try {
            Path dirPath = Path.of(outPathDirectory);
            Files.createDirectories(dirPath); // 🔥 Создаём папку, если её нет

            try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
                long fileLength = raf.length();
                long chunkSize = fileLength / countChunks; // 🔥 Размер каждого куска

                for (long i = 0; i < countChunks; i++) {
                    long minIndex = i * chunkSize / (symbolsPerElement * 2L);
                    long maxIndex = (i == countChunks - 1) ? fileLength / (symbolsPerElement * 2L) : (i + 1) * chunkSize / (symbolsPerElement * 2L);

                    String chunkPath = outPathDirectory + "/chunk_" + i + ".txt";
                    split(chunkPath, minIndex, maxIndex); // 🔥 Используем существующий метод
                }
            }

            System.out.println("✅ Разделение завершено! Всего кусков: " + countChunks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void split(String outPath, long minIndex, long maxIndex) {
        int bytesPerChar = 2; // UTF-16BE → 2 байта на символ
        long byteOffset = minIndex * symbolsPerElement * bytesPerChar;
        long bytesToRead = (maxIndex - minIndex) * symbolsPerElement * bytesPerChar;

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             FileOutputStream fos = new FileOutputStream(outPath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {

            long fileLength = raf.length();

            if (byteOffset >= fileLength) {
                System.err.println("❌ Ошибка: Запрошенный диапазон выходит за границы файла.");
                return;
            }
            if (byteOffset + bytesToRead > fileLength) {
                bytesToRead = fileLength - byteOffset; // Корректируем размер, если превышает файл
            }

            byte[] buffer = new byte[(int) bytesToRead];
            raf.seek(byteOffset);
            raf.readFully(buffer); // Читаем весь диапазон одним блоком

            // 🔥 Конвертируем `byte[]` в `String` перед записью
            String content = new String(buffer, StandardCharsets.UTF_16BE);
            writer.write(content);

            System.out.println("✅ Файл успешно создан: " + outPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
