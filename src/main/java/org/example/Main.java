package org.example;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        SymbolFileAccessor accessor = new SymbolFileAccessor("sortMD5_unicode_combinations1.txt", 1);
        DictionarySorter dictionarySorter = new DictionarySorter();
        MD5Hasher md5 = new MD5Hasher();
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();

        //generator.generateFile("unicode_combinations1.txt", 1);
        //generator.generateFile("unicode_combinations2.txt", 2);

        //dictionarySorter.sort("unicode_combinations1.txt", "sortMD5_unicode_combinations1.txt", 1, unicodeConverter, md5);

        ExternalDictionarySorter externalDictionarySorter = new ExternalDictionarySorter();
        externalDictionarySorter.sort("unicode_combinations2.txt", "sortMD5_unicode_combinations2.txt", unicodeConverter, md5);


        //menuApp(accessor, unicodeConverter, md5);
    }

    private static void menuApp(SymbolFileAccessor accessor, UnicodeConverter unicodeConverter, Hasher md5) {
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