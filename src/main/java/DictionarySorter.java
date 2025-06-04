
import Bytes.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.*;

public class DictionarySorter {
    private static class DictionaryEntry {
        String originalCombination;
        String hash;

        DictionaryEntry(String originalCombination, String hash) {
            this.originalCombination = originalCombination;
            this.hash = hash;
        }
    }

    public void sort(String inputFilePath, String outputFilePath, int lengthCombination, UnicodeConverter converter, Hasher hasher) {
        try {
            byte[] fileData = Files.readAllBytes(Path.of(inputFilePath));
            String fullContent = new String(fileData, StandardCharsets.UTF_16BE);

            List<String> segments = new ArrayList<>();
            for (int i = 0; i < fullContent.length(); i += Integer.MAX_VALUE) {
                segments.add(fullContent.substring(i, Math.min(i + Integer.MAX_VALUE, fullContent.length())));
            }


            List<String> elements = new ArrayList<>();
            String a = "";
            for (long i = 0; i < fullContent.length(); i++) {
                int segmentIndex = (int) (i / Integer.MAX_VALUE);
                int localIndex = (int) (i % Integer.MAX_VALUE);
                a += segments.get(segmentIndex).charAt(localIndex);
                if (a.length() % lengthCombination == 0) {
                    elements.add(a);
                    a = "";
                }
            }
            System.out.println("💡 Количество элементов после разбиения: " + elements.size());


            // 🔥 4️⃣ Создаём список для сортировки по MD5
            List<DictionaryEntry> entries = new ArrayList<>();
            for (String element : elements) {
                String hash = hasher.getHash(converter.unicodeToRangeString(element));
                entries.add(new DictionaryEntry(element, hash));
            }

            System.out.println("🚀 Начало сортировки...");
            entries.sort(Comparator.comparing(entry -> entry.hash));

            // 🔥 5️⃣ Записываем в файл (UTF-16BE)
            try (FileOutputStream fos = new FileOutputStream(outputFilePath);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {

                for (DictionaryEntry entry : entries) {
                    writer.write(entry.originalCombination); // Записываем строки
                }
                System.out.println("✅ Отсортированный словарь записан в файл: " + outputFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
