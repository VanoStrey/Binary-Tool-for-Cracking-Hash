package Bytes;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BinDictionarySorter {

    private static class DictionaryEntry {
        byte[] bytes;
        String hash;

        DictionaryEntry(byte[] bytes, String hash) {
            this.bytes = bytes;
            this.hash = hash;
        }
    }

    public void sort(String inputFilePath, String outputFilePath, int elementSize, BinBaseConverter converter, Hasher hasher) throws IOException, NoSuchAlgorithmException {


        byte[] fileData = Files.readAllBytes(Path.of(inputFilePath));
        List<DictionaryEntry> entries = new ArrayList<>();

        for (int i = 0; i < fileData.length; i += elementSize) {
            byte[] buffer = Arrays.copyOfRange(fileData, i, i + elementSize);
            String combination = converter.convertToBaseString(buffer);
            String hash = hasher.getHash(combination);
            entries.add(new DictionaryEntry(buffer, hash));
        }

        // 🔹 Сортировка по хешу
        entries.sort(Comparator.comparing(entry -> entry.hash));

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            for (DictionaryEntry entry : entries) {
                fos.write(entry.bytes); // 🔥 Записываем обратно `.bin`
            }
        }

        System.out.println("✅ Отсортированный `.bin` записан: " + outputFilePath);
    }

    private String getHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        return new BigInteger(1, hashBytes).toString(16); // 🔹 HEX представление
    }
}
