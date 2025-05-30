package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DictionarySorter {
    private static class DictionaryEntry {
        String originalCombination;
        String converted;
        String md5Hash;

        DictionaryEntry(String originalCombination, String converted, String md5Hash) {
            this.originalCombination = originalCombination;
            this.converted = converted;
            this.md5Hash = md5Hash;
        }
    }

    public void sort(String inputFilePath, String outputFilePath,
                     int symbolsPerElement, UnicodeConverter converter,
                     Hasher hasher) {
        SymbolFileAccessor accessor = new SymbolFileAccessor(inputFilePath, symbolsPerElement);
        long totalElements = accessor.getTotalElements();
        System.out.println("Общее количество элементов: " + totalElements);

        List<DictionaryEntry> entries = new ArrayList<>();
        for (long i = 0; i < totalElements; i++) {
            String element = accessor.getElement(i);
            if (element != null) {
                String converted = converter.unicodeToRangeString(element);
                String hash = hasher.getHash(converted);
                entries.add(new DictionaryEntry(element, converted, hash));
            }
        }

        // Сортируем записи по MD5-хэшу в порядке возрастания
        Collections.sort(entries, Comparator.comparing(entry -> entry.md5Hash));

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
