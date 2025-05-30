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

    // Вспомогательный класс для хранения информации по одной комбинации
    private static class DictionaryEntry {
        String originalCombination; // исходная комбинация, как прочитана из файла
        String converted;           // преобразованное значение (через UnicodeConverter)
        String md5Hash;             // MD5-хэш от преобразованного значения

        DictionaryEntry(String originalCombination, String converted, String md5Hash) {
            this.originalCombination = originalCombination;
            this.converted = converted;
            this.md5Hash = md5Hash;
        }
    }

    /**
     * Сортирует словарь по возрастанию MD5-хэша (вычисленного от преобразованной комбинации)
     * и сохраняет результат в новый файл. Все параметры файла (кодировка, длина записи)
     * остаются неизменными, меняется только порядок записей.
     *
     * @param inputFilePath    путь к исходному файлу-словарю (UTF-16BE)
     * @param outputFilePath   путь к новому файлу, в котором будет записан отсортированный словарь
     * @param symbolsPerElement количество символов в одной комбинации
     * @param converter        объект UnicodeConverter для преобразования комбинаций
     * @param md5Hasher        объект MD5Hasher для вычисления MD5-хэша
     */
    public void sortDictionary(String inputFilePath, String outputFilePath,
                               int symbolsPerElement, UnicodeConverter converter,
                               MD5Hasher md5Hasher)
    {
        // Создаем объект для доступа к элементам файла
        SymbolFileAccessor accessor = new SymbolFileAccessor(inputFilePath, symbolsPerElement);
        long totalElements = accessor.getTotalElements();
        System.out.println("Общее количество элементов: " + totalElements);

        // Считываем все записи из файла и для каждой вычисляем преобразованное значение и MD5-хэш
        List<DictionaryEntry> entries = new ArrayList<>();
        for (long i = 0; i < totalElements; i++) {
            String element = accessor.getElement(i);
            if (element != null) {
                // Преобразование комбинации (например, "A" -> "65", где 65 – это десятичное значение кода символа)
                String converted = converter.unicodeToRangeString(element);
                // Вычисление MD5-хэша от преобразованного представления
                String hash = md5Hasher.getHash(converted);
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

    // Пример использования класса
    public static void main(String[] args) {
        String inputFile = "unicode_combinations1.txt";    // исходный словарь
        String outputFile = "sorted_unicode_combinations.txt"; // новый отсортированный словарь
        int symbolsPerElement = 1; // длина комбинации (для примера используется 1)

        // Для преобразования используется диапазон символов "0123456789" (десятичная система)
        UnicodeConverter converter = new UnicodeConverter("0123456789");
        MD5Hasher md5Hasher = new MD5Hasher();

        DictionarySorter sorter = new DictionarySorter();
        sorter.sortDictionary(inputFile, outputFile, symbolsPerElement, converter, md5Hasher);
    }
}
