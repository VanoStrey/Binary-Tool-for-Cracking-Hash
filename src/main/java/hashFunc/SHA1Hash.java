package hashFunc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hash implements Hasher {

    public String getName() {
        return "SHA1";
    }

    public String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBinHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка: Алгоритм SHA-1 не найден!", e);
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
