package Bytes;

public interface Hasher {
    String getName();

    String getHash(String input);
}