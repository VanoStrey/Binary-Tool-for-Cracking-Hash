package org.example;

/**
 * Класс BinaryHashSearcher выполняет бинарный поиск по отсортированному словарю.
 * Словарь – это файл, в котором записи (комбинации символов фиксированной длины в UTF‑16BE)
 * отсортированы по возрастанию хэша, вычисленного с помощью переданного объекта Hasher.
 */
public class BinaryHashSearcher {
    private final Hasher hasher;
    private final SymbolFileAccessor accessor;
    private final UnicodeConverter unicodeConverter;

    /**
     * Конструктор инициализирует путь к словарю, число символов в комбинации и объект хеширования.
     *
     * @param dictionaryPath  путь к файлу-словарю (UTF‑16BE, фиксированная длина записей)
     * @param symbolsPerElement количество символов в одной комбинации
     * @param hasher          объект класса хеширования (должен иметь метод getHash(String))
     */
    public BinaryHashSearcher(String dictionaryPath, int symbolsPerElement, UnicodeConverter unicodeConverter,  Hasher hasher) {
        this.hasher = hasher;
        this.unicodeConverter = unicodeConverter;
        this.accessor = new SymbolFileAccessor(dictionaryPath, symbolsPerElement);
    }

    /**
     * Выполняет бинарный поиск в отсортированном словаре по заданному хэшу.
     *
     * @param targetHash искомый хэш (строка)
     * @return исходная комбинация (расшифрованное значение), хэш которой совпадает с targetHash;
     *         если соответствующая комбинация не найдена, возвращается null
     */
    public String search(String targetHash) {
        long totalElements = accessor.getTotalElements();
        long low = 0;
        long high = totalElements - 1;

        while (low <= high) {
            long mid = low + ((high - low) / 2);
            String element = unicodeConverter.unicodeToRangeString(accessor.getElement(mid));
            // Вычисляем хэш для элемента, используя переданный объект хеширования.
            // Предполагается, что хешер сам выполняет нужные преобразования (либо словарь отсортирован по вызову hasher.getHash())
            String computedHash = hasher.getHash(element);

            int cmp = computedHash.compareTo(targetHash);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return element;
            }
        }
        return null; // Если комбинация с таким хэшем не найдена
    }
}
