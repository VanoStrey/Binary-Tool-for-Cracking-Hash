package hashFunc;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hash implements Hasher {

    public String getName() {
        return "MD5";
    }

    public String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);

            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBinHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка: Алгоритм MD5 не найден!", e);
        }
    }

    public byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
}