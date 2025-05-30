package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        /*
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();
        generator.generateFile("unicode_combinations1.txt", 1);
         */
        SymbolFileAccessor accessor = new SymbolFileAccessor("sortMD5_unicode_combinations1.txt", 1);
        MD5Hasher md5 = new MD5Hasher();


        /*DictionarySorter dictionarySorter = new DictionarySorter();
        dictionarySorter.sort("unicode_combinations1.txt", "sortMD5_unicode_combinations1.txt", 1, unicodeConverter, md5);
         */
        BinaryHashSearcher searchHash = new BinaryHashSearcher(accessor, unicodeConverter, md5);
        Scanner scanner = new Scanner(System.in);
        String hash, result = "";
        long startTime, endTime = 0;
        while (true) {
            System.out.println("MD5 hash для расшифровки: ");
            hash = scanner.next();

            startTime = System.currentTimeMillis();
            result = searchHash.search(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд");
        }
    }
}