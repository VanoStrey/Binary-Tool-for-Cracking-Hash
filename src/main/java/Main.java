import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        SymbolFileAccessor accessor = new SymbolFileAccessor("sortSHA1_UC2_2^23.txt", 2);
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();
        DictionarySplitter dictionarySplitter = new DictionarySplitter(accessor);
        DictionarySorter dictionarySorter = new DictionarySorter();
        Hasher sha256 = new SHA256Hash();
        Hasher sha1 = new SHA1Hash();
        Hasher md5 = new MD5Hash();

        //dictionarySpliter.split("UC2_2^23.txt", 0, accessor.getTotalElements() / 512);
        //dictionarySorter.sort("sortSHA1_UC2_2^23.txt", accessor, unicodeConverter, sha1);
        //dictionarySorter.sort("sortSHA256_UC2_2^23.txt", accessor, unicodeConverter, sha256);


        menuApp(accessor, unicodeConverter, sha1);
    }

    private static void menuApp(SymbolFileAccessor accessor, UnicodeConverter unicodeConverter, Hasher hasher) {
        BinaryHashSearcher searchHash = new BinaryHashSearcher(accessor, unicodeConverter, hasher);
        Scanner scanner = new Scanner(System.in);
        String hash, result;
        long startTime, endTime;
        while (true) {
            System.out.println(hasher.getName() + " hash для расшифровки: ");
            hash = scanner.next();

            startTime = System.currentTimeMillis();
            result = searchHash.search(hash);
            endTime = System.currentTimeMillis();


            System.out.println("Результат: " + result);
            System.out.println("Время выполнения: " + (endTime - startTime) + " милисекунд\n");
        }
    }
}