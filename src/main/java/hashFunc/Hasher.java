package hashFunc;

public interface Hasher {
    String getName();

    String getHash(String input);

    byte[] getBinHash(String input);

    byte[] hexToBytes(String hex);
}