package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hasher implements Hasher {
    public String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка: MD5 алгоритм не найден", e);
        }
    }
}
