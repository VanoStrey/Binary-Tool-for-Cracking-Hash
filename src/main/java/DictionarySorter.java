import java.nio.charset.StandardCharsets;
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

    public void sort(String outputFilePath, SymbolFileAccessor accessor, UnicodeConverter converter,
                     Hasher hasher) {
        long totalElements = accessor.getTotalElements();
        System.out.println("Общее количество элементов: " + totalElements);

        List<DictionaryEntry> entries = new ArrayList<>();
        for (long i = 0; i < 100; i++) {
            for (int j = 0; j < totalElements / 100; j++) {
                String element = accessor.getElement((totalElements / 100) * i + j);
                if (element != null) {
                    String hash = hasher.getHash(converter.unicodeToRangeString(element));
                    entries.add(new DictionaryEntry(element, hash));
                }
            }
            System.out.println((i + 1) + "%");
        }
        System.out.println("Начало сортировки");
        // Сортируем записи по MD5-хэшу в порядке возрастания
        entries.sort(Comparator.comparing(entry -> entry.hash));

        // Записываем отсортированные комбинации в новый файл, сохраняя ту же кодировку (UTF-16BE)
        try (FileOutputStream fos = new FileOutputStream(outputFilePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {

            for (DictionaryEntry entry : entries) {
                writer.write(entry.originalCombination);
            }
            System.out.println("Отсортированный словарь записан в файл: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
