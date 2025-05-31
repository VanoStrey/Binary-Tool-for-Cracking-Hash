import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static ArrayList<BinaryHashSearcher> binarySearch = new ArrayList<>();


    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789");
        SymbolFileAccessor accessor = new SymbolFileAccessor("sortSHA256_UC2_2^23.txt", 2);
        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator();
        DictionarySplitter dictionarySplitter = new DictionarySplitter(accessor);
        DictionarySorter dictionarySorter = new DictionarySorter();
        SHA256Hash sha256 = new SHA256Hash();
        SHA1Hash sha1 = new SHA1Hash();
        MD5Hash md5 = new MD5Hash();


        binarySearch.add(new BinaryHashSearcher(new SymbolFileAccessor("sortMD5_UC2_2^23.txt", 2), unicodeConverter, md5));
        binarySearch.add(new BinaryHashSearcher(new SymbolFileAccessor("sortSHA1_UC2_2^23.txt", 2), unicodeConverter, sha1));
        binarySearch.add(new BinaryHashSearcher(new SymbolFileAccessor("sortSHA256_UC2_2^23.txt", 2), unicodeConverter, sha256));

        //dictionarySplitter.split("UC2_2^23.txt", 0, accessor.getTotalElements() / 512);
        //dictionarySorter.sort("sortSHA1_UC2_2^23.txt", accessor, unicodeConverter, sha1);
        //dictionarySorter.sort("sortSHA256_UC2_2^23.txt", accessor, unicodeConverter, sha256);

        //menuApp(accessor, unicodeConverter, sha256);


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

    public static String universalCrackHash(String hash) {
        return switch (hash.length()) {
            case 32 -> binarySearch.get(0).search(hash);
            case 40 -> binarySearch.get(1).search(hash);
            case 64 -> binarySearch.get(2).search(hash);
            default -> "Неизвесный тип шифрования";
        };
    }

}